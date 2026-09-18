package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;

/**
 * SOL001 V5.4.1 clause 6.8.13 {@code Vdu.OsContainerDeployableUnit} to IFA011 V5.4.1 clause 7.1.6.2
 * {@code Vdu}.
 *
 * <p>Two attributes are not on the node template at all and come from the flavour context:
 * {@code intCpd}, which each VduCp declares towards the VDU, and {@code lcmRealizationPath}, which
 * is derived once every MCIOP association is known.
 */
final class VduMapper {

    private VduMapper() {
    }

    static Vdu map(VduOsContainerDeployableUnit node, FlavourContext context) {
        Vdu.Builder builder = Vdu.builder(IdRegistry.vduId(node));

        VduOsContainerDeployableUnit.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName())
                   .description(p.getDescription())
                   .mcioIdentificationData(p.getMcioIdentificationData())
                   .isNumOfInstancesClusterBased(p.getIsNumOfInstancesClusterBased());
            FlavourContext.orEmpty(p.getMcioConstraintParams()).forEach(builder::addMcioConstraintParam);
        }

        VduOsContainerDeployableUnit.Requirements r = node.getRequirements();
        if (r != null) {
            // 'container' has occurrences [0, UNBOUNDED] (Table 6.8.13.4-1): a VDU may describe
            // several OS containers, as Annex A.18 vdu_2 does.
            FlavourContext.orEmpty(r.getContainer()).forEach(builder::addOsContainerDesc);
            FlavourContext.orEmpty(r.getVirtualStorage()).forEach(builder::addVirtualStorageDesc);
            FlavourContext.orEmpty(r.getInstallableCertificate()).forEach(builder::addCertificateDesc);
        }

        // Declared on the connection points, collected here: IFA011 clause 7.1.6.2.2 intCpd.
        context.cpsBoundTo(node.getKey()).forEach(builder::addIntCpd);

        LcmRealizationPath path =
                LcmRealizationResolver.resolve(node, context.mciopsAssociatedTo(node.getKey()));
        builder.lcmRealizationPath(path);

        SpecRules.mciopCoverage(node, path, context.findings());
        SpecRules.mcioIdentificationData(node, context.findings());

        return builder.build();
    }
}
