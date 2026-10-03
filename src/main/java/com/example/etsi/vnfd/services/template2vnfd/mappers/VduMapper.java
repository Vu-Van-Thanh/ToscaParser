package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.Subport;
import com.example.etsi.vnfd.model.TrunkPortTopology;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.services.template2vnfd.validator.SpecRuleValidator;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VduSubCp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SOL001 V5.4.1 clause 6.8.13 {@code Vdu.OsContainerDeployableUnit} to IFA011 V5.4.1 clause 7.1.6.2
 * {@code Vdu}.
 *
 * <p>Two attributes are not on the node template at all and come from the flavour context:
 * {@code intCpd}, which each VduCp declares towards the VDU, and {@code lcmRealizationPath}, which
 * is derived once every MCIOP association is known.
 */
public final class VduMapper implements NodeMapper<VduOsContainerDeployableUnit, Vdu> {


    @Override
    public Class<VduOsContainerDeployableUnit> nodeType() {
        return VduOsContainerDeployableUnit.class;
    }

    @Override
    public String id(VduOsContainerDeployableUnit node) {
        return VnfdUtils.nodeId(node);
    }

    @Override
    public void contribute(VnfdElements pool, String id, Vdu element) {
        pool.addVdu(id, element);
    }

    @Override
    public Vdu map(VduOsContainerDeployableUnit node, FlavourContext context) {
        Vdu.Builder builder = Vdu.builder(VnfdUtils.nodeId(node));

        VduOsContainerDeployableUnit.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName())
                   .description(p.getDescription())
                   .mcioIdentificationData(p.getMcioIdentificationData())
                   .isNumOfInstancesClusterBased(p.getIsNumOfInstancesClusterBased());
            VnfdUtils.orEmpty(p.getMcioConstraintParams()).forEach(builder::addMcioConstraintParam);
        }

        VduOsContainerDeployableUnit.Requirements r = node.getRequirements();
        if (r != null) {
            // 'container' has occurrences [0, UNBOUNDED] (Table 6.8.13.4-1): a VDU may describe
            // several OS containers, as Annex A.18 vdu_2 does.
            VnfdUtils.orEmpty(r.getContainer()).forEach(builder::addOsContainerDesc);
            VnfdUtils.orEmpty(r.getVirtualStorage()).forEach(builder::addVirtualStorageDesc);
            VnfdUtils.orEmpty(r.getInstallableCertificate()).forEach(builder::addCertificateDesc);
        }

        // Declared on the connection points, collected here: IFA011 clause 7.1.6.2.2 intCpd.
        context.cpsBoundTo(node.getKey()).forEach(builder::addIntCpd);

        trunkPorts(node, context).forEach(builder::addTrunkPort);

        LcmRealizationPath path =
                VnfdUtils.lcmRealizationPath(node, context.mciopsAssociatedTo(node.getKey()));
        builder.lcmRealizationPath(path);

        SpecRuleValidator.mciopCoverage(node, path, context.findings());
        SpecRuleValidator.mcioIdentificationData(node, context.findings());

        return builder.build();
    }

    /**
     * The trunk topologies of one VDU, IFA011 V5.4.1 clause 7.1.6.11.
     *
     * <p>SOL001 states the relation on the subport rather than on the VDU or the parent: a
     * {@code VduSubCp} (clause 6.8.11) carries a {@code trunk_binding} requirement whose occurrences
     * are [1, 1] and which names the {@code VduCp} acting as the trunk port. So the topology is
     * assembled by reading every subport of the VDU and grouping them by the parent they name.
     *
     * <p>A subport bound to a parent that belongs to a different VDU is skipped rather than
     * reported: nothing in clause 6.8.11 forbids it, and this mapper is not the place to decide it
     * is wrong.
     */
    private static List<TrunkPortTopology> trunkPorts(VduOsContainerDeployableUnit vdu,
            FlavourContext context) {
        Map<String, List<Subport>> byParent = new LinkedHashMap<>();
        for (String cpKey : context.cpsBoundTo(vdu.getKey())) {
            Cp cp = context.connectionPoints().get(cpKey);
            if (!(cp instanceof VduSubCp)) {
                continue;
            }
            VduSubCp sub = (VduSubCp) cp;
            String parent = sub.getRequirements() == null
                    ? null
                    : VnfdUtils.first(sub.getRequirements().getTrunkBinding()).orElse(null);
            if (parent == null) {
                continue;
            }
            VduSubCp.Properties p = sub.getProperties();
            byParent.computeIfAbsent(parent, k -> new ArrayList<>())
                    .add(Subport.of(VnfdUtils.nodeId(sub),
                            p == null ? null : p.getSegmentationType(),
                            p == null ? null : p.getSegmentationId()));
        }

        List<TrunkPortTopology> out = new ArrayList<>();
        byParent.forEach((parent, subports) -> out.add(TrunkPortTopology.of(parent, subports)));
        return out;
    }
}
