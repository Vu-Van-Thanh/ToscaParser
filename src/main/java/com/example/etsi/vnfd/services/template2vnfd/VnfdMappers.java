package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.AffinityOrAntiAffinityGroup;
import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.Cpd;
import com.example.etsi.vnfd.model.DeployableModule;
import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.ScaleInfo;
import com.example.etsi.vnfd.model.ScalingAspect;
import com.example.etsi.vnfd.model.ScalingDelta;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.Subport;
import com.example.etsi.vnfd.model.TrunkPortTopology;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VduLevel;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfLcmOperationsConfiguration;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VirtualLinkBitRateLevel;
import com.example.etsi.vnfd.model.VirtualLinkProfile;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.ImplementationDefinition;
import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.OperationAssignment;
import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.ParameterDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.value.FunctionCall;
import com.example.etsi.vnfd.template.value.FunctionName;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.artifact.HelmParamMappingScript;
import com.example.etsi.vnfd.toscatype.artifact.SwImage;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.data.TypeOfStorage;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduSubCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VduVirtualBlockStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualFileStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualObjectStorage;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.toscatype.policy.AffinityRule;
import com.example.etsi.vnfd.toscatype.policy.InstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.ScalingAspects;
import com.example.etsi.vnfd.toscatype.policy.VduInitialDelta;
import com.example.etsi.vnfd.toscatype.policy.VduScalingAspectDeltas;
import com.example.etsi.vnfd.toscatype.policy.VirtualLinkInstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.VnfPackageChange;
import com.example.etsi.vnfd.toscatype.policy.VduInstantiationLevels;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * SOL001 node types to IFA011 information elements, one class per element.
 *
 * <p>Every mapper below names the IFA011 clause it produces and cites the SOL001 clause it reads.
 * That pairing is the point of keeping them in one file: the mapping between the two
 * specifications is a table, and a table is easier to audit in one place than spread over nine.
 */
public final class VnfdMappers {

    /**
     * A mapper that writes a {@code PropertyValue} as what the descriptor wrote.
     *
     * <p>Several IFA011 attributes are typed as an opaque map - {@code VirtualStorageDesc.storageData},
     * {@code VirtualLinkProfile.maxBitrateRequirements} - and the bound SOL001 datatype behind them
     * holds {@code PropertyValue} fields. Converting through the binding mapper would serialise each
     * one as a bean, {@code {"resolved":true,"deferred":false}}, losing the value outright. Writing
     * {@code raw()} keeps both cases usable: a literal stays the number or string that was written,
     * an unresolved function stays the function.
     */
    private static final ObjectMapper PLAIN = plainMapper();

    private VnfdMappers() {
    }

    /** A bound SOL001 datatype as a plain map, property values intact. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> plainMap(Object value) {
        return value == null ? null : PLAIN.convertValue(value, Map.class);
    }

    private static ObjectMapper plainMapper() {
        SimpleModule module = new SimpleModule("plain-property-values");
        module.addSerializer(PropertyValue.class, new JsonSerializer<PropertyValue>() {
            @Override
            public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                provider.defaultSerializeValue(value.raw(), gen);
            }
        });
        return ToscaBindModule.mapper().copy().registerModule(module);
    }
}

/**
 * The VNF-level identification attributes, SOL001 V5.4.1 clause 6.8.1 to IFA011 clause 7.1.2.2.
 *
 * <p>Each is a plain String in the model rather than a {@code PropertyValue}: SOL001 Table 5.9-2
 * permits {@code get_input} only on {@code flavour_id}, {@code modifiable_attributes} and
 * {@code configurable_properties}, so an identifier that is not a literal is a descriptor defect,
 * and rendering the expression text as if it were an id would be worse than leaving it unset.
 *
 * <p>Values may be assigned on the node template or left to the {@code default} of the node type -
 * SOL001 Annex A.23 does the latter for everything but {@code flavour_description}. Both arrive here
 * the same way, because the binder lays the type defaults underneath before binding.
 */
final class VnfHeaderMapper {

    private VnfHeaderMapper() {
    }

    static void map(Vnf vnf, Vnfd.Builder builder) {
        Vnf.Properties p = vnf.getProperties();
        if (p == null) {
            return;
        }
        literal(p.getDescriptorId(), builder::vnfdId);
        literal(p.getProvider(), builder::vnfProvider);
        literal(p.getProductName(), builder::vnfProductName);
        literal(p.getSoftwareVersion(), builder::vnfSoftwareVersion);
        literal(p.getDescriptorVersion(), builder::vnfdVersion);
        literal(p.getProductInfoName(), builder::vnfProductInfoName);
        literal(p.getProductInfoDescription(), builder::vnfProductInfoDescription);
        literal(p.getExtInvariantId(), builder::vnfdExtInvariantId);
        literal(p.getDefaultLocalizationLanguage(), builder::defaultLocalizationLanguage);

        VnfdUtils.orEmpty(p.getVnfmInfo()).forEach(builder::addVnfmInfo);
        VnfdUtils.orEmpty(p.getLocalizationLanguages())
                .forEach(builder::addLocalizationLanguage);
    }

    private static void literal(PropertyValue<String> value, Consumer<String> sink) {
        if (value != null && value.isResolved()) {
            value.resolved().ifPresent(sink);
        }
    }
}

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
        Vdu.Builder builder = Vdu.builder(VnfdUtils.vduId(node));

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

        SpecRules.mciopCoverage(node, path, context.findings());
        SpecRules.mcioIdentificationData(node, context.findings());

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
                    .add(Subport.of(VnfdUtils.cpdId(sub),
                            p == null ? null : p.getSegmentationType(),
                            p == null ? null : p.getSegmentationId()));
        }

        List<TrunkPortTopology> out = new ArrayList<>();
        byParent.forEach((parent, subports) -> out.add(TrunkPortTopology.of(parent, subports)));
        return out;
    }
}

/**
 * SOL001 V5.4.1 clause 6.8.12 {@code Vdu.OsContainer} to IFA011 V5.4.1 clause 7.1.6.13
 * {@code OsContainerDesc}.
 *
 * <p>The CPU and the memory properties are typed differently on purpose: Table 6.8.12.2-1 declares
 * the CPU ones as {@code integer} in milli-CPU, while memory and ephemeral storage are
 * {@code scalar-unit.size}. Collapsing both to a number would lose the unit.
 */
final class OsContainerMapper {

    private OsContainerMapper() {
    }

    static OsContainerDesc map(VduOsContainer node, FlavourContext context) {
        OsContainerDesc.Builder builder = OsContainerDesc.builder(VnfdUtils.osContainerDescId(node));

        VduOsContainer.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName())
                   .description(p.getDescription())
                   .requestedCpuResources(p.getRequestedCpuResources())
                   .cpuResourceLimit(p.getCpuResourceLimit())
                   .requestedMemoryResources(p.getRequestedMemoryResources())
                   .memoryResourceLimit(p.getMemoryResourceLimit())
                   .requestedEphemeralStorageResources(p.getRequestedEphemeralStorageResources())
                   .ephemeralStorageResourceLimit(p.getEphemeralStorageResourceLimit());
        }

        // IFA011 clause 7.1.6.13.2 makes swImageDesc M,1; SOL001 clause 6.8.12.6 requires the
        // artifact and caps it at one. The id is the node template name, not the artifact name.
        if (context.artifactOfType(node, EtsiTypes.ARTIFACT_SW_IMAGE).isPresent()) {
            builder.swImageDesc(VnfdUtils.swImageDescId(node));
        }

        return builder.build();
    }
}

/**
 * SOL001 V5.4.1 clause 6.3.1 {@code tosca.artifacts.nfv.SwImage} to IFA011 V5.4.1 clause 7.1.6.5
 * {@code SwImageDesc}.
 *
 * <p>The identifier is the one rule SOL001 states outright rather than leaving to the data model:
 * clause 6.8.12.6 says the node template name of the owning {@code Vdu.OsContainer} "fulfils the
 * purpose of the 'id' attribute of the SwImageDesc information element". So the id comes from the
 * node, not from the artifact definition's own name.
 */
final class SwImageMapper {

    private final ObjectMapper mapper;

    SwImageMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    SwImageDesc map(ArtifactDefinition definition, NfvNode owner) {
        SwImage.Properties p = mapper.convertValue(
                java.util.Collections.singletonMap("properties", definition.properties()),
                SwImage.class).getProperties();

        SwImageDesc.Builder builder = SwImageDesc.builder(VnfdUtils.swImageDescId(owner));
        if (p != null) {
            builder.name(p.getName())
                   .version(p.getVersion())
                   .provider(p.getProvider())
                   .checksum(p.getChecksum())
                   .containerFormat(p.getContainerFormat())
                   .diskFormat(p.getDiskFormat())
                   .size(p.getSize())
                   .minDisk(p.getMinDisk())
                   .minRam(p.getMinRam())
                   .operatingSystem(p.getOperatingSystem());
        }
        return builder
                // IFA011 clause 7.1.6.5.2 swImage is "a reference to the actual software image";
                // the resolved form is what a consumer can act on.
                .swImage(VnfdUtils.pathOf(definition))
                .build();
    }
}

/**
 * The connection points, SOL001 V5.4.1 clauses 6.8.8 and 6.8.2 to IFA011 clauses 7.1.6.4 and 7.1.3.2.
 *
 * <p>One node template can become two information elements. SOL001 clause 6.8.2.8 lets a service
 * template omit a VnfExtCp node entirely and expose a VduCp through {@code substitution_mappings}
 * instead, and SOL001 Table 6.1-1 lists both VnfExtCp and VduCp as sources of a {@code VnfExtCpd}.
 * Such a connection point is emitted as a {@code VduCpd} - it still binds a VDU - and as a
 * {@code VnfExtCpd}, with {@code exposedThroughSubstitution} recording which grammar produced it.
 */
final class CpMapper {

    private CpMapper() {
    }

    /** SOL001 clause 6.8.8 VduCp (and clause 6.8.11 VduSubCp) to VduCpd. */
    static VduCpd mapVduCp(VduCp node) {
        VduCpd.Builder builder = VduCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);
        if (node.getRequirements() != null) {
            VnfdUtils.first(node.getRequirements().getVirtualBinding())
                    .ifPresent(builder::vduId);
            VnfdUtils.first(node.getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    }

    /** SOL001 clause 6.8.2 VnfExtCp, declared explicitly, to VnfExtCpd. */
    static VnfExtCpd mapVnfExtCp(VnfExtCp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);
        if (node.getRequirements() != null) {
            // SOL001 Table 6.8.2.4-1 names them internal_virtual_link and external_virtual_link;
            // the internal one is what IFA011 calls intVirtualLinkDesc.
            VnfdUtils.first(node.getRequirements().getInternalVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        builder.exposedThroughSubstitution(false);
        return builder.build();
    }

    /**
     * A connection point exposed through {@code substitution_mappings} rather than by a VnfExtCp
     * node - SOL001 clause 6.8.2.8. {@code intCpd} points back at the internal connection point the
     * external one stands for, which is what lets a consumer follow it down to its VDU.
     */
    static VnfExtCpd mapExposedCp(Cp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);
        builder.intCpd(VnfdUtils.cpdId(node));
        builder.exposedThroughSubstitution(true);
        if (node instanceof VduCp && ((VduCp) node).getRequirements() != null) {
            VnfdUtils.first(((VduCp) node).getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    }

    /** What every connection point descriptor shares - IFA011 clause 7.1.6.3 {@code Cpd}. */
    static void applyCommon(Cp node, Cpd.AbstractBuilder<?> builder) {
        Cp.Properties p = node.getProperties();
        if (p == null) {
            return;
        }
        VnfdUtils.orEmpty(p.getLayerProtocols()).forEach(builder::addLayerProtocol);
        builder.cpRole(p.getRole());
        builder.description(p.getDescription());
        builder.trunkMode(p.getTrunkMode());
        if (p.getProtocol() != null) {
            for (Object protocol : p.getProtocol()) {
                builder.addProtocol(java.util.Collections.singletonMap("protocol", protocol));
            }
        }
    }
}

/**
 * SOL001 V5.4.1 clause 6.8.9 {@code VnfVirtualLink} to IFA011 V5.4.1 clause 7.1.7.2
 * {@code VnfVirtualLinkDesc}.
 *
 * <p>{@code vl_profile} is deliberately not read here: IFA011 clause 7.1.8.2.2 puts the profile in
 * {@code VnfDf.virtualLinkProfile}, not in the descriptor, so the flavour mapper reads it - the same
 * split as {@code vdu_profile}.
 */
final class VirtualLinkMapper {

    private VirtualLinkMapper() {
    }

    static VnfVirtualLinkDesc map(VnfVirtualLink node) {
        VnfVirtualLinkDesc.Builder builder =
                VnfVirtualLinkDesc.builder(VnfdUtils.virtualLinkDescId(node));
        VnfVirtualLink.Properties p = node.getProperties();
        if (p != null) {
            builder.description(p.getDescription());
        }
        return builder.build();
    }
}

/**
 * SOL001 V5.4.1 clause 6.8.14 {@code Mciop} to IFA011 V5.4.1 clause 7.1.8.20 {@code MciopProfile},
 * plus the two things that profile cannot hold.
 *
 * <p>Clause 6.8.14.1 is explicit that the node type "does not correspond to an information element
 * defined in ETSI GS NFV-IFA 011" and is only "capable of being profiled by the properties of the
 * MciopProfile", so one node produces three outputs:
 *
 * <ul>
 *   <li>the {@code MciopProfile}, which belongs to a deployment flavour;
 *   <li>an {@code LcmOpParameterMappingScript} (IFA011 clause 7.1.20), which belongs to the VNFD;
 *   <li>{@code MciopArtifacts}, which belongs nowhere in IFA011 - Table 7.1.8.20.2-1 has exactly
 *       six attributes and none holds the path of the Helm chart, yet
 *       {@code helm install {RELEASE} {CHART}} needs it.
 * </ul>
 */
final class MciopMapper {

    /** SOL001 clause 6.3.4.1 defines three ordered input parameters. */
    private static final int HELM_SCRIPT_ARITY = 3;

    private final TypeReader.Hierarchy hierarchy;
    private final ObjectMapper mapper;

    MciopMapper(TypeReader.Hierarchy hierarchy, ObjectMapper mapper) {
        this.hierarchy = hierarchy;
        this.mapper = mapper;
    }

    /** The cardinality rules of clauses 6.8.14.6 and 6.8.14.7 - see {@link SpecRules#mciop}. */
    void check(Mciop node, FlavourContext context) {
        SpecRules.mciop(node, context);
    }

    MciopProfile mapProfile(Mciop node) {
        return mapProfile(node, Collections.emptyMap());
    }

    MciopProfile mapProfile(Mciop node, Map<String, Integer> deploymentOrder) {
        MciopProfile.Builder builder = MciopProfile.builder(VnfdUtils.mciopId(node));
        Integer order = deploymentOrder.get(node.getKey());
        if (order != null) {
            builder.deploymentOrder(order);
        }

        // Table 6.8.14.4-1 gives associatedVdu occurrences [1, UNBOUNDED]; Annex A.23 declares the
        // key twice on one Mciop, which is why the bound field is a list.
        if (node.getRequirements() != null) {
            VnfdUtils.orEmpty(node.getRequirements().getAssociatedVdu())
                    .forEach(builder::addAssociatedVdu);
        }

        VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE)
                .map(VnfdUtils::pathOf)
                .ifPresent(builder::mciopParameterMappingRule);
        VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT)
                .map(ArtifactDefinition::name)
                .ifPresent(builder::lcmOpParameterMappingScriptId);

        return builder.build();
    }


    /**
     * Deployment order of the MCIOPs of one flavour, from the {@code dependency} requirements
     * between them.
     *
     * <p>SOL001 V5.4.1 clause 6.8.14.7: "The dependency requirement as defined in
     * TOSCA-Simple-Profile-YAML-v1.3 may be used towards other Mciop nodes to express the order of
     * deployment." It says the requirement expresses an order; it does not say how that order
     * becomes the integer IFA011 clause 7.1.8.20.2 calls {@code deploymentOrder}.
     *
     * <p>[ASSUMPTION] A topological rank numbered from zero: an MCIOP depending on nothing is 0, and
     * one depending on others is one past the highest of them. MCIOPs at the same rank have no
     * ordering between them, which is what "may be deployed together" looks like as a number.
     *
     * <p>Only emitted when at least one dependency exists. A flavour whose MCIOPs declare no order
     * gets no deploymentOrder at all, rather than every profile claiming rank 0 - saying nothing is
     * more accurate than saying they are all first.
     */
    static Map<String, Integer> deploymentOrder(FlavourContext context, Findings findings) {
        Map<String, List<String>> dependencies = new LinkedHashMap<>();
        boolean any = false;
        for (Mciop mciop : context.mciops()) {
            List<String> targets = new ArrayList<>();
            for (String target : context.rawRequirementTargets(mciop.getKey(), "dependency")) {
                // Clause 6.8.14.7 scopes this to other Mciop nodes; a dependency on anything else
                // is a TOSCA ordering statement this library has no reading for.
                if (context.mciops().stream().anyMatch(m -> m.getKey().equals(target))) {
                    targets.add(target);
                    any = true;
                }
            }
            dependencies.put(mciop.getKey(), targets);
        }
        if (!any) {
            return Collections.emptyMap();
        }

        Map<String, Integer> ranks = new LinkedHashMap<>();
        for (String key : dependencies.keySet()) {
            rank(key, dependencies, ranks, new LinkedHashSet<>(), context, findings);
        }
        return ranks;
    }

    private static int rank(String key, Map<String, List<String>> dependencies,
            Map<String, Integer> ranks, Set<String> visiting, FlavourContext context,
            Findings findings) {
        Integer known = ranks.get(key);
        if (known != null) {
            return known;
        }
        if (!visiting.add(key)) {
            // A cycle has no deployment order at all - every member would have to precede itself.
            findings.error("C30", "SOL001 V5.4.1 cl. 6.8.14.7",
                    "Mciop " + key + " takes part in a cycle of dependency requirements, so no "
                            + "order of deployment can be derived",
                    SourceRef.ofFile(context.template().file()));
            ranks.put(key, 0);
            return 0;
        }
        int order = 0;
        for (String target : dependencies.getOrDefault(key, Collections.emptyList())) {
            order = Math.max(order, rank(target, dependencies, ranks, visiting, context, findings) + 1);
        }
        visiting.remove(key);
        ranks.put(key, order);
        return order;
    }

    /**
     * The parameter mapping script as the VNFD-level information element.
     *
     * <p>SOL001 clause 6.3.4 {@code HelmParamMappingScript} maps onto IFA011 clause 7.1.20
     * {@code LcmOpParameterMappingScript}, but with the three-parameter calling convention of
     * clause 6.3.4.1 rather than the four of IFA011 clause 7.1.20.1 - hence the recorded arity.
     */
    Optional<LcmOpParameterMappingScript> mapScript(Mciop node) {
        return VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT).map(definition -> {
            HelmParamMappingScript.Properties p = mapper.convertValue(
                    Collections.singletonMap("properties", definition.properties()),
                    HelmParamMappingScript.class).getProperties();
            String language = p == null || p.getLanguage() == null
                    ? null
                    : p.getLanguage().resolved().orElse(null);
            return LcmOpParameterMappingScript.of(
                    VnfdUtils.lcmOpParameterMappingScriptId(definition),
                    VnfdUtils.pathOf(definition),
                    language,
                    LcmOpParameterMappingScript.ScriptKind.HELM_PARAM_MAPPING,
                    HELM_SCRIPT_ARITY);
        });
    }

    /** The package-relative paths of the MCIOP artifacts - see the class javadoc for why. */
    Optional<MciopArtifacts> mapArtifacts(Mciop node) {
        Optional<ArtifactDefinition> chart = VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_CHART);
        Optional<ArtifactDefinition> script =
                VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT);
        Optional<ArtifactDefinition> rule =
                VnfdUtils.artifactOfType(hierarchy, node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE);
        if (!chart.isPresent() && !script.isPresent() && !rule.isPresent()) {
            return Optional.empty();
        }
        MciopArtifacts.Builder builder = MciopArtifacts.builder(VnfdUtils.mciopId(node));
        chart.ifPresent(c -> builder.packagePath(VnfdUtils.pathOf(c))
                .packageArtifactName(c.name())
                .packageArtifactType(c.type()));
        script.ifPresent(s -> builder.paramMappingScriptPath(VnfdUtils.pathOf(s)));
        rule.ifPresent(r -> builder.paramMappingRulePath(VnfdUtils.pathOf(r)));
        return Optional.of(builder.build());
    }

    /** Every MCIOP id of the flavour, in declaration order. */
    static List<String> idsOf(List<Mciop> mciops) {
        List<String> ids = new java.util.ArrayList<>();
        mciops.forEach(m -> ids.add(VnfdUtils.mciopId(m)));
        return ids;
    }
}

/**
 * The policies of one flavour, SOL001 V5.4.1 clause 6.10 to the IFA011 clause 7.1.8 elements of a
 * {@code VnfDf}.
 *
 * <p>Policies are the part of the descriptor furthest from the information model. A TOSCA policy
 * names its targets and carries a map keyed by level or aspect id; IFA011 wants the transpose - a
 * level that lists its VDUs. So each method reads every policy of one kind at once rather than
 * mapping them one by one.
 */
final class PolicyMapper {

    /** SOL001 V5.4.1 clause 6.9.1. Expanded before a policy target is interpreted. */
    private static final String PLACEMENT_GROUP = "tosca.groups.nfv.PlacementGroup";

    private final TypeReader.Hierarchy hierarchy;
    private final ObjectMapper mapper;

    PolicyMapper(TypeReader.Hierarchy hierarchy, ObjectMapper mapper) {
        this.hierarchy = hierarchy;
        this.mapper = mapper;
    }

    /**
     * The instantiation levels of the flavour.
     *
     * <p>Table 7.1.8.7.2-1 makes {@code levelId} M,1, {@code description} M,1 and {@code vduLevel}
     * M,1..N, so a level naming no VDU is not a valid element - which is why the per-VDU policies
     * are folded into the levels rather than kept beside them.
     */
    List<InstantiationLevel> instantiationLevels(FlavourContext context) {
        Map<String, InstantiationLevel.Builder> builders = new LinkedHashMap<>();

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_INSTANTIATION_LEVELS)) {
            InstantiationLevels policy = bind(definition, InstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, level) -> {
                InstantiationLevel.Builder b =
                        builders.computeIfAbsent(levelId, InstantiationLevel::builder);
                if (level == null) {
                    return;
                }
                if (level.getDescription() != null) {
                    b.description(level.getDescription().resolved().orElse(null));
                }
                // IFA011 clause 7.1.8.7.2 gives InstantiationLevel.scaleInfo 0..N: for each aspect,
                // the scale level this instantiation level corresponds to. SOL001 clause 6.10.1
                // writes it as a map keyed by aspectId.
                if (level.getScaleInfo() != null) {
                    level.getScaleInfo().forEach((aspectId, info) -> {
                        if (info == null || info.getScaleLevel() == null) {
                            return;
                        }
                        info.getScaleLevel().resolved()
                                .ifPresent(lvl -> b.addScaleInfo(ScaleInfo.of(aspectId, lvl)));
                    });
                }
            });
        }

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_INSTANTIATION_LEVELS)) {
            VduInstantiationLevels policy = bind(definition, VduInstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, vduLevel) -> {
                InstantiationLevel.Builder b = builders.get(levelId);
                if (b == null || vduLevel == null || vduLevel.getNumberOfInstances() == null) {
                    return;
                }
                Integer count = vduLevel.getNumberOfInstances().resolved().orElse(null);
                if (count != null) {
                    for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                        b.addVduLevel(VduLevel.of(vduId, count));
                    }
                }
            });
        }

        applyVirtualLinkLevels(context, builders);

        List<InstantiationLevel> levels = new ArrayList<>();
        builders.values().forEach(b -> levels.add(b.build()));
        return levels;
    }

    /** The default level id, when an InstantiationLevels policy names one. */
    Optional<String> defaultInstantiationLevelId(FlavourContext context) {
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_INSTANTIATION_LEVELS)) {
            InstantiationLevels policy = bind(definition, InstantiationLevels.class);
            if (policy.getProperties() != null && policy.getProperties().getDefaultLevel() != null) {
                Optional<String> value = policy.getProperties().getDefaultLevel().resolved();
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The single level a descriptor without any InstantiationLevels policy implies.
     *
     * <p>[MANO INTERPRETATION] IFA011 clause 7.1.8.2.2 requires at least one level and Table
     * 7.1.8.7.2-1 requires each to carry at least one VduLevel, so an empty list cannot be emitted.
     * The count comes from {@code VduProfile.minNumberOfInstances} - the smallest deployment that
     * still satisfies the flavour - and the result is flagged so a consumer can tell it apart from
     * a level the descriptor actually declared.
     */
    static InstantiationLevel synthesiseLevel(Map<String, Integer> minInstancesByVdu) {
        InstantiationLevel.Builder builder = InstantiationLevel.builder("default")
                .description("Synthesised - the descriptor declares no InstantiationLevels policy")
                .synthesised(true);
        minInstancesByVdu.forEach((vduId, count) -> builder.addVduLevel(VduLevel.of(vduId, count)));
        return builder.build();
    }

    /** The scaling aspects, SOL001 clause 6.10.5. */
    /**
     * SOL001 V5.4.1 clause 6.10.6 {@code VduScalingAspectDeltas} to the {@code ScalingDelta}
     * elements of IFA011 V5.4.1 clause 7.1.10.4, keyed by the aspect they belong to.
     *
     * <p>SOL001 writes {@code deltas} as a map whose key is the scalingDeltaId and whose value gives
     * the instance count, with the policy targets naming the VDUs. IFA011 turns that inside out:
     * one ScalingDelta carries a {@code vduDelta} entry per VDU. So two policies naming the same
     * delta id for different VDUs are one delta with two entries, which is why this accumulates
     * rather than builds each policy independently.
     */
    Map<String, List<ScalingDelta>> scalingDeltas(FlavourContext context) {
        Map<String, Map<String, ScalingDelta.Builder>> byAspect = new LinkedHashMap<>();

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_SCALING_ASPECT_DELTAS)) {
            VduScalingAspectDeltas policy = bind(definition, VduScalingAspectDeltas.class);
            if (policy.getProperties() == null || policy.getProperties().getAspect() == null
                    || policy.getProperties().getDeltas() == null) {
                continue;
            }
            String aspectId = policy.getProperties().getAspect().resolved().orElse(null);
            if (aspectId == null) {
                continue;
            }
            Map<String, ScalingDelta.Builder> deltas =
                    byAspect.computeIfAbsent(aspectId, k -> new LinkedHashMap<>());
            policy.getProperties().getDeltas().forEach((deltaId, level) -> {
                if (level == null || level.getNumberOfInstances() == null) {
                    return;
                }
                Integer count = level.getNumberOfInstances().resolved().orElse(null);
                if (count == null) {
                    return;
                }
                ScalingDelta.Builder b =
                        deltas.computeIfAbsent(deltaId, ScalingDelta::builder);
                for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                    b.addVduDelta(VduLevel.of(vduId, count));
                }
            });
        }

        Map<String, List<ScalingDelta>> out = new LinkedHashMap<>();
        byAspect.forEach((aspectId, deltas) -> {
            List<ScalingDelta> built = new ArrayList<>();
            deltas.values().forEach(b -> built.add(b.build()));
            out.put(aspectId, built);
        });
        return out;
    }

    /**
     * SOL001 V5.4.1 clause 6.10.8 {@code VduInitialDelta} to IFA011 {@code VnfDf.initialDelta}.
     *
     * <p>IFA011 clause 7.1.8.2.2 describes it as "the minimum size of the VNF (i.e. scale level zero
     * for all scaling aspects)", so the per-VDU policies of one flavour make up a single delta
     * rather than one each.
     *
     * <p>[ASSUMPTION] Its {@code scalingDeltaId}. IFA011 makes the identifier mandatory and SOL001
     * gives the policy no name for it, so a fixed one is used.
     */
    Optional<ScalingDelta> initialDelta(FlavourContext context) {
        ScalingDelta.Builder builder = ScalingDelta.builder("initial_delta");
        boolean any = false;
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_INITIAL_DELTA)) {
            VduInitialDelta policy = bind(definition, VduInitialDelta.class);
            if (policy.getProperties() == null || policy.getProperties().getInitialDelta() == null) {
                continue;
            }
            com.example.etsi.vnfd.toscatype.data.VduLevel level =
                    policy.getProperties().getInitialDelta();
            if (level.getNumberOfInstances() == null) {
                continue;
            }
            Integer count = level.getNumberOfInstances().resolved().orElse(null);
            if (count == null) {
                continue;
            }
            for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                builder.addVduDelta(VduLevel.of(vduId, count));
                any = true;
            }
        }
        return any ? Optional.of(builder.build()) : Optional.empty();
    }

    /**
     * SOL001 V5.4.1 clause 6.10.3 {@code VirtualLinkInstantiationLevels} to
     * {@code InstantiationLevel.virtualLinkBitRateLevel}, IFA011 clause 7.1.10.5.
     *
     * <p>Folded into the levels an {@code InstantiationLevels} policy already declared, for the same
     * reason as the VDU levels: a level id nothing declared is a reference to a level that does not
     * exist.
     */
    void applyVirtualLinkLevels(FlavourContext context,
            Map<String, InstantiationLevel.Builder> builders) {
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VL_INSTANTIATION_LEVELS)) {
            VirtualLinkInstantiationLevels policy =
                    bind(definition, VirtualLinkInstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, level) -> {
                InstantiationLevel.Builder b = builders.get(levelId);
                if (b == null || level == null) {
                    return;
                }
                // The SOL001 level wraps the requirements in a bitrate_requirements field; IFA011
                // clause 7.1.10.5.2 has VirtualLinkBitRateLevel carry them directly, so unwrap.
                Map<String, Object> bitrate = VnfdMappers.plainMap(level.getBitrateRequirements());
                for (String vlId : VnfdUtils.orEmpty(definition.targets())) {
                    b.addVirtualLinkBitRateLevel(VirtualLinkBitRateLevel.of(vlId, bitrate));
                }
            });
        }
    }

    /**
     * SOL001 V5.4.1 clause 6.10.13 {@code SecurityGroupRule} to IFA011 clause 7.1.6.9.
     *
     * <p>[ASSUMPTION] {@code securityGroupRuleId} is the policy name. IFA011 makes the identifier
     * mandatory and NOTE 3 of Table 7.1.6.9.2-1 relies on it - "Different VduCpd or VnfExtCpd with
     * the same value of securityGroupRuleId imply they belong to the same security group" - but
     * SOL001 states no derivation.
     */
    List<SecurityGroupRule> securityGroupRules(FlavourContext context) {
        List<SecurityGroupRule> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_SECURITY_GROUP_RULE)) {
            com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule policy =
                    bind(definition, com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule.class);
            SecurityGroupRule.Builder builder = SecurityGroupRule.builder(definition.name());
            if (policy.getProperties() != null) {
                com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule.Properties p =
                        policy.getProperties();
                builder.description(p.getDescription())
                       .direction(p.getDirection())
                       .etherType(p.getEtherType())
                       .protocol(p.getProtocol())
                       .portRangeMin(p.getPortRangeMin())
                       .portRangeMax(p.getPortRangeMax());
            }
            VnfdUtils.orEmpty(definition.targets()).forEach(builder::addTarget);
            out.add(builder.build());
        }
        return out;
    }

    /**
     * SOL001 V5.4.1 clause 6.10.15 {@code VnfPackageChange} to IFA011 clause 7.1.15.2
     * {@code VnfPackageChangeInfo}.
     */
    List<VnfPackageChangeInfo> packageChanges(FlavourContext context) {
        List<VnfPackageChangeInfo> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VNF_PACKAGE_CHANGE)) {
            VnfPackageChange policy = bind(definition, VnfPackageChange.class);
            VnfPackageChangeInfo.Builder builder = VnfPackageChangeInfo.builder(definition.name());
            if (policy.getProperties() != null) {
                VnfPackageChange.Properties p = policy.getProperties();
                builder.modificationQualifier(p.getModificationQualifier())
                       .additionalModificationDescription(p.getAdditionalModificationDescription())
                       .destinationFlavourId(p.getDestinationFlavourId());
                if (p.getSelector() != null) {
                    p.getSelector().forEach(sel -> builder.addSelector(VnfdMappers.plainMap(sel)));
                }
                if (p.getComponentMappings() != null) {
                    p.getComponentMappings()
                            .forEach(cm -> builder.addComponentMapping(VnfdMappers.plainMap(cm)));
                }
            }
            out.add(builder.build());
        }
        return out;
    }

    List<ScalingAspect> scalingAspects(FlavourContext context) {
        List<ScalingAspect> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_SCALING_ASPECTS)) {
            ScalingAspects policy = bind(definition, ScalingAspects.class);
            if (policy.getProperties() == null || policy.getProperties().getAspects() == null) {
                continue;
            }
            Map<String, List<ScalingDelta>> deltasByAspect = scalingDeltas(context);
            policy.getProperties().getAspects().forEach((id, aspect) -> {
                ScalingAspect.Builder b = ScalingAspect.builder(id);
                deltasByAspect.getOrDefault(id, Collections.emptyList()).forEach(b::addDelta);
                if (aspect != null) {
                    resolved(aspect.getName()).ifPresent(b::name);
                    resolved(aspect.getDescription()).ifPresent(b::description);
                    if (aspect.getMaxScaleLevel() != null) {
                        aspect.getMaxScaleLevel().resolved().ifPresent(b::maxScaleLevel);
                    }
                    // IFA011 clause 7.1.8.8.2 stepDeltas: the scaling deltas applied for the
                    // successive scaling steps of this aspect, in order.
                    VnfdUtils.orEmpty(aspect.getStepDeltas()).forEach(b::addStepDelta);
                }
                out.add(b.build());
            });
        }
        return out;
    }

    /**
     * The affinity groups, and which element each applies to.
     *
     * <p>SOL001 Table 6.1-1 NOTE 3: {@code affinityOrAntiAffinityGroupId} is not a property of the
     * profiled element - the policy names its targets, so the assignment has to be read backwards.
     * The returned map is keyed by node template name, which is also the id of the VduProfile,
     * MciopProfile or VirtualLinkProfile that target becomes.
     */
    AffinityAssignment affinity(FlavourContext context) {
        List<AffinityOrAntiAffinityGroup> groups = new ArrayList<>();
        Map<String, List<String>> byTarget = new LinkedHashMap<>();

        for (PolicyDefinition definition : context.policies()) {
            boolean anti = hierarchy.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_ANTI_AFFINITY_RULE);
            boolean plain = hierarchy.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_AFFINITY_RULE);
            if (!anti && !plain) {
                continue;
            }
            // Both types declare the same properties, so one class reads either.
            AffinityRule policy = bind(definition, AffinityRule.class);
            String groupId = VnfdUtils.affinityGroupId(definition);
            String scope = policy.getProperties() == null
                    ? null
                    : resolved(policy.getProperties().getScope()).orElse(null);

            groups.add(AffinityOrAntiAffinityGroup.of(groupId,
                    anti ? AffinityOrAntiAffinityGroup.AffinityType.ANTI_AFFINITY
                         : AffinityOrAntiAffinityGroup.AffinityType.AFFINITY,
                    scope));
            for (String target : expand(definition.targets(), context)) {
                byTarget.computeIfAbsent(target, key -> new ArrayList<>()).add(groupId);
            }
        }
        return new AffinityAssignment(groups, byTarget);
    }

    /** Replaces every PlacementGroup target by its members - SOL001 clause 6.9.1. */
    private List<String> expand(List<String> targets, FlavourContext context) {
        List<String> out = new ArrayList<>();
        for (String target : VnfdUtils.orEmpty(targets)) {
            GroupDefinition group = context.topology().groups().get(target);
            if (group != null && hierarchy.isDerivedFrom(group.type(), PLACEMENT_GROUP)) {
                out.addAll(group.members());
            } else {
                out.add(target);
            }
        }
        return out;
    }

    private List<PolicyDefinition> of(FlavourContext context, String etsiType) {
        List<PolicyDefinition> out = new ArrayList<>();
        for (PolicyDefinition definition : context.policies()) {
            if (hierarchy.isDerivedFrom(definition.type(), etsiType)) {
                out.add(definition);
            }
        }
        return out;
    }

    private <T> T bind(PolicyDefinition definition, Class<T> target) {
        return mapper.convertValue(
                Collections.singletonMap("properties", definition.properties()), target);
    }

    private static Optional<String> resolved(
            com.example.etsi.vnfd.template.value.PropertyValue<String> value) {
        return value == null ? Optional.empty() : value.resolved();
    }

    /** Affinity groups together with the elements each one applies to. */
    static final class AffinityAssignment {

        private final List<AffinityOrAntiAffinityGroup> groups;
        private final Map<String, List<String>> byTarget;

        AffinityAssignment(List<AffinityOrAntiAffinityGroup> groups,
                Map<String, List<String>> byTarget) {
            this.groups = groups;
            this.byTarget = byTarget;
        }

        List<AffinityOrAntiAffinityGroup> groups() {
            return groups;
        }

        List<String> groupsOf(String nodeTemplateName) {
            return byTarget.getOrDefault(nodeTemplateName, Collections.emptyList());
        }
    }
}

/**
 * One deployment flavour, assembled from everything the flavour topology declares.
 *
 * <p>The only mapper that reads no single node type. IFA011 V5.4.1 clause 7.1.8.2.2 collects into
 * {@code VnfDf} the parts of the descriptor that say how the VNF is deployed rather than what it is
 * made of - the profiles, the levels, the scaling aspects - and each sits on a different element in
 * TOSCA. SOL001 clauses 6.11.2 and 6.11.3 both make one service template one flavour, so the scope
 * of this mapper is exactly one service template.
 *
 * <p>Order matters once: the affinity assignments are read before any profile is built, because
 * SOL001 Table 6.1-1 NOTE 3 puts {@code affinityOrAntiAffinityGroupId} on the profile while the
 * policy names the profiled element as its target. Reading the policies first keeps the profiles
 * immutable instead of building them and patching them afterwards.
 */
final class DeploymentFlavourMapper {

    private final PolicyMapper policies;
    private final MciopMapper mciops;

    DeploymentFlavourMapper(TypeReader.Hierarchy hierarchy, ObjectMapper mapper) {
        this.policies = new PolicyMapper(hierarchy, mapper);
        this.mciops = new MciopMapper(hierarchy, mapper);
    }

    /** A mapped flavour plus the elements IFA011 keeps at VNFD level. */
    static final class Result {
        final VnfDf df;
        final List<SecurityGroupRule> securityGroupRules = new ArrayList<>();
        final List<VnfPackageChangeInfo> packageChanges = new ArrayList<>();
        final List<LcmOpParameterMappingScript> scripts = new ArrayList<>();
        final List<MciopArtifacts> mciopArtifacts = new ArrayList<>();
        final List<String> mciopIds = new ArrayList<>();

        Result(VnfDf df) {
            this.df = df;
        }
    }

    Result map(FlavourContext context) {
        String flavourId = flavourId(context).orElse("");
        VnfDf.Builder builder = VnfDf.builder(flavourId);
        builder.sourceFile(context.template().file());
        flavourDescription(context).ifPresent(builder::description);

        PolicyMapper.AffinityAssignment affinity = policies.affinity(context);
        affinity.groups().forEach(builder::addAffinityGroup);

        Map<String, Integer> minInstances = new LinkedHashMap<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            VduProfile profile = vduProfile(vdu, affinity);
            builder.addVduProfile(profile);
            profile.getMinNumberOfInstances().flatMap(v -> v.resolved())
                    .ifPresent(min -> minInstances.put(profile.getVduId(), min));
        }

        Map<String, Integer> deploymentOrder =
                MciopMapper.deploymentOrder(context, context.findings());
        for (Mciop mciop : context.mciops()) {
            mciops.check(mciop, context);
            builder.addMciopProfile(mciops.mapProfile(mciop, deploymentOrder));
        }

        // IFA011 clause 7.1.8.2.2 virtualLinkProfile: SOL001 clause 6.8.9 carries the same data as
        // the vl_profile property of the virtual link, so the profile is read from the node the
        // flavour references rather than from a separate element.
        for (VnfVirtualLink link : context.virtualLinks().values()) {
            builder.addVirtualLinkProfile(virtualLinkProfile(link, affinity));
        }

        policies.scalingAspects(context).forEach(builder::addScalingAspect);
        policies.initialDelta(context).ifPresent(builder::initialDelta);

        // IFA011 Table 7.1.8.2.2-1 puts deployableModule on the flavour, not on the VNFD: the set
        // of optional VDUs is what a consumer selects when instantiating this flavour.
        context.deployableModules().forEach(m ->
                builder.addDeployableModule(ModuleAndCertificateMapper.mapDeployableModule(m)));

        List<InstantiationLevel> levels = policies.instantiationLevels(context);
        if (levels.isEmpty()) {
            levels = Collections.singletonList(PolicyMapper.synthesiseLevel(minInstances));
        }
        levels.forEach(builder::addInstantiationLevel);
        policies.defaultInstantiationLevelId(context).ifPresent(builder::defaultInstantiationLevelId);

        // IFA011 clause 7.1.5.2.2 puts vnfLcmOperationsConfiguration on the flavour, while SOL001
        // declares lcm_operations_configuration as a property of the VNF node type. Consistent,
        // since one service template is one flavour (clauses 6.11.2 and 6.11.3).
        context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getLcmOperationsConfiguration())
                .ifPresent(cfg -> builder.vnfLcmOperationsConfiguration(lcmOperationsConfig(cfg)));

        Result result = new Result(builder.build());
        SpecRules.flavourIdentified(result.df, context.template().file(), context.findings());
        // IFA011 Table 7.1.2.2-1 keeps both of these at VNFD level, although SOL001 declares them as
        // policies inside a service template - one template being one flavour.
        result.securityGroupRules.addAll(policies.securityGroupRules(context));
        result.packageChanges.addAll(policies.packageChanges(context));
        for (Mciop mciop : context.mciops()) {
            result.mciopIds.add(VnfdUtils.mciopId(mciop));
            mciops.mapScript(mciop).ifPresent(result.scripts::add);
            mciops.mapArtifacts(mciop).ifPresent(result.mciopArtifacts::add);
        }
        return result;
    }

    /**
     * Which flavour this service template describes.
     *
     * <p>Three sources in order: {@code substitution_mappings.substitution_filter}, the form clause
     * 6.11.2 d) prescribes for a lower-level template; {@code properties.flavour_id} on the VNF node
     * template, which the single-flavour design of clause 6.11.3 uses; and the {@code default} on
     * the VNF node type, which {@code TypeDefaults} has already laid underneath by the time the
     * node was bound.
     */
    private Optional<String> flavourId(FlavourContext context) {
        Optional<String> fromFilter = context.topology().substitutionMappings()
                .flatMap(m -> m.filterEqualValue("flavour_id"));
        if (fromFilter.isPresent()) {
            return fromFilter;
        }

        PropertyValue<String> declared = context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourId())
                .orElse(null);
        if (declared == null) {
            return Optional.empty();
        }
        if (declared.isResolved()) {
            return declared.resolved();
        }
        // The property is a TOSCA function. SOL001 V5.4.1 Table 5.9-2 lists VNF.flavour_id as one of
        // the four places get_input is permitted, so a conformant descriptor can legitimately land
        // here - and IFA011 clause 7.1.8.2.2 still makes flavourId M,1, so an empty identifier is
        // not an acceptable answer.
        //
        // [MANO INTERPRETATION] The declared default of the input is used. It is part of the
        // descriptor rather than a runtime value, and TOSCA Simple Profile YAML 1.3 clause 3.6.11
        // defines it as the value to use when the consumer supplies none. No value is substituted
        // from anywhere outside the package.
        return inputDefault(context, declared);
    }

    /** The {@code default} of the input a {@code get_input} names, when the input declares one. */
    private Optional<String> inputDefault(FlavourContext context, PropertyValue<String> value) {
        if (!(value instanceof FunctionCall)) {
            return Optional.empty();
        }
        FunctionCall<String> call = (FunctionCall<String>) value;
        if (call.name() != FunctionName.GET_INPUT || call.args().isEmpty()) {
            return Optional.empty();
        }
        return call.args().get(0).resolved()
                .map(String::valueOf)
                .flatMap(inputName -> Optional
                        .ofNullable(context.topology().inputs().get(inputName))
                        .flatMap(ParameterDefinition::defaultValue)
                        .map(String::valueOf));
    }

    private Optional<String> flavourDescription(FlavourContext context) {
        return context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourDescription())
                .flatMap(v -> v == null ? Optional.<String>empty() : v.resolved());
    }

    /**
     * SOL001 {@code lcm_operations_configuration} to IFA011 clause 7.1.5.2
     * {@code VnfLcmOperationsConfiguration}.
     *
     * <p>Twelve sub-elements, each 0..1 and each a bag of operation-specific settings. Carried as
     * written: nothing here reads an individual setting, and re-modelling twelve tables to pass
     * them through would be twelve chances to lose one.
     */
    @SuppressWarnings("unchecked")
    private VnfLcmOperationsConfiguration lcmOperationsConfig(Object raw) {
        VnfLcmOperationsConfiguration.Builder builder = VnfLcmOperationsConfiguration.builder();
        Map<String, Object> asMap = ToscaBindModule.mapper().convertValue(raw, Map.class);
        if (asMap != null) {
            asMap.forEach((attribute, value) -> {
                if (value instanceof Map) {
                    builder.mergeOpConfig(attribute, (Map<String, Object>) value);
                }
            });
        }
        return builder.build();
    }

    /** SOL001 clause 6.8.9 {@code vl_profile} to IFA011 clause 7.1.8.13 {@code VirtualLinkProfile}. */
    private VirtualLinkProfile virtualLinkProfile(VnfVirtualLink link,
            PolicyMapper.AffinityAssignment affinity) {
        VirtualLinkProfile.Builder builder =
                VirtualLinkProfile.builder(VnfdUtils.virtualLinkDescId(link));
        if (link.getProperties() != null && link.getProperties().getVlProfile() != null) {
            com.example.etsi.vnfd.toscatype.data.VlProfile p = link.getProperties().getVlProfile();
            builder.maxBitrateRequirements(asMap(p.getMaxBitrateRequirements()))
                   .minBitrateRequirements(asMap(p.getMinBitrateRequirements()))
                   .qos(asMap(p.getQos()));
        }
        // SOL001 Table 6.1-1 NOTE 3: the affinity group lands on the profile, named by a policy
        // that targets the virtual link.
        affinity.groupsOf(link.getKey()).forEach(builder::addAffinityGroup);
        return builder.build();
    }

    /**
     * A bound datatype as a plain map, keeping what the descriptor wrote.
     *
     * <p>Converting through the binding mapper would serialise each {@code PropertyValue} field as a
     * bean - {@code {"resolved":true,"deferred":false}} - which loses the value entirely. Writing
     * {@code raw()} instead keeps both cases usable: a literal stays the number or string the
     * descriptor wrote, and an unresolved function stays the function, so nothing downstream has to
     * guess which it was looking at.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return VnfdMappers.plainMap(value);
    }



    /** SOL001 Table 6.8.13.2-1 {@code vdu_profile} to IFA011 clause 7.1.8.3 {@code VduProfile}. */
    private VduProfile vduProfile(VduOsContainerDeployableUnit vdu,
            PolicyMapper.AffinityAssignment affinity) {
        VduProfile.Builder builder = VduProfile.builder(VnfdUtils.vduId(vdu));
        if (vdu.getProperties() != null && vdu.getProperties().getVduProfile() != null) {
            com.example.etsi.vnfd.toscatype.data.VduProfile p = vdu.getProperties().getVduProfile();
            builder.minNumberOfInstances(p.getMinNumberOfInstances())
                   .maxNumberOfInstances(p.getMaxNumberOfInstances());
            VnfdUtils.orEmpty(p.getModifyCapacityAttributesOp())
                    .forEach(builder::addModifyCapacityAttributesOp);
        }
        affinity.groupsOf(vdu.getKey()).forEach(builder::addAffinityGroup);
        return builder.build();
    }
}

/**
 * SOL001 V5.4.1 clauses 6.8.4 / 6.8.5 / 6.8.6 {@code Vdu.Virtual*Storage} to IFA011 V5.4.1 clause
 * 7.1.9.4.2 {@code VirtualStorageDesc}.
 *
 * <p>The kind of storage comes from the node TYPE, not from a property: SOL001 gives block, object
 * and file storage three separate node types, each with its own data property, while IFA011 has one
 * information element carrying a {@code typeOfStorage}. Walking {@code derived_from} is what turns
 * one into the other, so a vendor type derived from any of the three still classifies.
 */
final class StorageMapper {

    private final ObjectMapper mapper;

    StorageMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** Empty when the node is not one of the three storage types. */
    Optional<VirtualStorageDesc> map(NfvNode node) {
        if (node instanceof VduVirtualBlockStorage) {
            VduVirtualBlockStorage.Properties p = ((VduVirtualBlockStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.BLOCK,
                    p == null ? null : p.getVirtualBlockStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        if (node instanceof VduVirtualObjectStorage) {
            VduVirtualObjectStorage.Properties p = ((VduVirtualObjectStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.OBJECT,
                    p == null ? null : p.getVirtualObjectStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        if (node instanceof VduVirtualFileStorage) {
            VduVirtualFileStorage.Properties p = ((VduVirtualFileStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.FILE,
                    p == null ? null : p.getVirtualFileStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        return Optional.empty();
    }

    private VirtualStorageDesc build(NfvNode node, TypeOfStorage type,
            Object storageData, PropertyValue<Boolean> perVnfcInstance, Object maintenance) {
        VirtualStorageDesc.Builder builder =
                VirtualStorageDesc.builder(VnfdUtils.virtualStorageDescId(node), type);
        if (storageData != null) {
            builder.storageData(asMap(storageData));
        }
        if (perVnfcInstance != null) {
            builder.perVnfcInstance(perVnfcInstance);
        }
        if (maintenance != null) {
            builder.nfviMaintenanceInfo(asMap(maintenance));
        }
        return builder.build();
    }

    private Map<String, Object> asMap(Object value) {
        return VnfdMappers.plainMap(value);
    }
}

/**
 * SOL001 V5.4.1 clause 6.8.10 {@code VipCp} to IFA011 V5.4.1 clause 7.1.17.2 {@code VipCpd}, and
 * clause 6.8.15 {@code VirtualCp} to clause 7.1.18.2 {@code VirtualCpd}.
 *
 * <p>Both are connection points, so both inherit every attribute of the Cpd, but they differ in what
 * they point at: a VipCp targets the VduCps that will share the address (clause 6.8.10), while a
 * VirtualCp targets the deployable units implementing the service (clause 6.8.15). That is why
 * neither can go through the VduCp path, which reads a {@code virtual_binding} neither of them has.
 */
final class SpecialCpMapper {

    private SpecialCpMapper() {
    }

    /** IFA011 clause 7.1.17.2. */
    static VipCpd mapVipCp(VipCp node) {
        VipCpd.Builder builder = VipCpd.builder(VnfdUtils.cpdId(node));
        CpMapper.applyCommon(node, builder);

        VipCp.Properties p = node.getProperties();
        if (p != null) {
            builder.dedicatedIpAddress(p.getDedicatedIpAddress())
                   .vipFunction(p.getVipFunction());
        }
        VipCp.Requirements r = node.getRequirements();
        if (r != null) {
            // Table 6.8.10.4-1: target has occurrences [1, UNBOUNDED] and points at VduCp nodes,
            // which is exactly IFA011 intCpd (M,1..N, a reference to VduCpd).
            VnfdUtils.orEmpty(r.getTarget()).forEach(builder::addIntCpd);
            VnfdUtils.first(r.getVirtualLink()).ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    }

    /** IFA011 clause 7.1.18.2. */
    static VirtualCpd mapVirtualCp(VirtualCp node) {
        VirtualCpd.Builder builder = VirtualCpd.builder(VnfdUtils.cpdId(node));
        CpMapper.applyCommon(node, builder);

        VirtualCp.Properties p = node.getProperties();
        if (p != null && p.getAdditionalServiceData() != null) {
            p.getAdditionalServiceData().forEach(d -> builder.addAdditionalServiceData(
                    ToscaBindModule.mapper().convertValue(d, Map.class)));
        }
        VirtualCp.Requirements r = node.getRequirements();
        if (r != null) {
            VnfdUtils.orEmpty(r.getTarget()).forEach(builder::addVdu);
        }
        return builder.build();
    }
}

/**
 * SOL001 V5.4.1 clause 6.8.19 {@code Certificate} to IFA011 V5.4.1 clause 7.1.19.2
 * {@code CertificateDesc}, and clause 6.8.16 {@code DeployableModule} to clause 7.1.8.24.
 *
 * <p>The two are unrelated but share a shape: a node type whose whole content is a couple of
 * properties and a list of members, with nothing to derive or cross-reference.
 */
final class ModuleAndCertificateMapper {

    private ModuleAndCertificateMapper() {
    }

    /** IFA011 clause 7.1.19.2. */
    static CertificateDesc mapCertificate(Certificate node) {
        CertificateDesc.Builder builder =
                CertificateDesc.builder(VnfdUtils.certificateDescId(node));
        Certificate.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName()).certificateType(p.getCertificateType());
            if (p.getCertificateBaseProfile() != null) {
                builder.certificateBaseProfile(
                        ToscaBindModule.mapper().convertValue(p.getCertificateBaseProfile(), Map.class));
            }
            if (p.getCsrRequirements() != null) {
                p.getCsrRequirements().forEach(r -> builder.addCsrRequirement(
                        ToscaBindModule.mapper().convertValue(r, Map.class)));
            }
        }
        return builder.build();
    }

    /** IFA011 clause 7.1.8.24 - an attribute of the deployment flavour, not of the VNFD. */
    static DeployableModule mapDeployableModule(
            com.example.etsi.vnfd.toscatype.node.DeployableModule node) {
        DeployableModule.Builder builder =
                DeployableModule.builder(VnfdUtils.deployableModuleId(node));
        com.example.etsi.vnfd.toscatype.node.DeployableModule.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName()).description(p.getDescription());
        }
        com.example.etsi.vnfd.toscatype.node.DeployableModule.Requirements r = node.getRequirements();
        if (r != null) {
            // Table 6.8.16.4-1: member has occurrences [1, UNBOUNDED].
            VnfdUtils.orEmpty(r.getMember()).forEach(builder::addMember);
        }
        return builder.build();
    }
}

/**
 * SOL001 V5.4.1 clause 6.7.1.1 interface {@code tosca.interfaces.nfv.Vnflcm} to IFA011 V5.4.1
 * clause 7.1.13.2 {@code LifeCycleManagementScript}.
 *
 * <p>Only an operation carrying an {@code implementation} becomes a script. Declaring
 * {@code inputs} on an operation states the shape of its parameters, not that anything runs -
 * IFA011 clause 7.1.13.2 makes {@code script} M,1, so an operation with no implementation has
 * nothing to put there. Every bundled MCIOP package declares {@code instantiate.inputs} and no
 * implementation, and none of them should produce a script.
 *
 * <p>[ASSUMPTION] The operation-to-event table below. SOL001 clause 6.7.1.1 names the operations and
 * IFA011 clause 7.1.13.2 names the events, but neither prints a lookup between them; the names line
 * up closely enough to map by pattern, which is a judgement and is labelled as one.
 *
 * <p>SOL001 forms each pre- and post-amble as {@code <base>_start} and {@code <base>_end}. A base
 * operation with no suffix is not an internal VNFM lifecycle event at all: IFA011 describes those
 * values as "external stimulus detected on a VNFM reference point", i.e. the receipt of the request.
 */
final class LcmMapper {

    /** The eighteen internal events of IFA011 Table 7.1.13.2-1, keyed by SOL001 operation base. */
    private static final Map<String, String> EVENT_BASE;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("instantiate", "INSTANTIATION");
        m.put("scale", "SCALING");
        m.put("scale_to_level", "SCALING_TO_LEVEL");
        m.put("heal", "HEALING");
        m.put("terminate", "TERMINATION");
        m.put("change_flavour", "VNF_FLAVOR_CHANGE");
        m.put("operate", "VNF_OPERATION_CHANGE");
        m.put("change_external_connectivity", "VNF_EXT_CONN_CHANGE");
        m.put("modify_information", "VNFINFO_MODIFICATION");
        m.put("create_snapshot", "VNF_SNAPSHOT_CREATION");
        m.put("revert_to_snapshot", "VNF_SNAPSHOT_REVERTINGTO");
        m.put("change_current_package", "CHANGE_CURRENT_VNF_PACKAGE");
        EVENT_BASE = Collections.unmodifiableMap(m);
    }

    private LcmMapper() {
    }

    /** Every implemented operation of the VNF node template, as a script. */
    static List<LifeCycleManagementScript> map(Vnf vnf) {
        List<LifeCycleManagementScript> out = new ArrayList<>();
        if (vnf == null || vnf.getInterfaces() == null) {
            return out;
        }
        for (Map.Entry<String, InterfaceAssignment> iface : vnf.getInterfaces().entrySet()) {
            for (Map.Entry<String, OperationAssignment> op : iface.getValue().operations().entrySet()) {
                script(iface.getKey(), op.getKey(), op.getValue()).ifPresent(out::add);
            }
        }
        return out;
    }

    private static Optional<LifeCycleManagementScript> script(String interfaceName,
            String operationName, OperationAssignment operation) {
        Optional<ImplementationDefinition> implementation = operation.implementation();
        if (!implementation.isPresent()) {
            return Optional.empty();
        }
        String primary = implementation.get().primary();
        if (primary == null || primary.isEmpty()) {
            return Optional.empty();
        }

        LifeCycleManagementScript.Builder builder = LifeCycleManagementScript.builder(
                VnfdUtils.lcmScriptId(interfaceName, operationName));
        builder.script(primary);
        // [ASSUMPTION] scriptDsl is M,1 in IFA011 and SOL001 declares no language on a Vnflcm
        // operation, unlike HelmParamMappingScript (clause 6.3.4). The file extension is all the
        // descriptor offers.
        dslOf(primary).ifPresent(builder::scriptDsl);
        eventOf(operationName).ifPresent(builder::addEvent);
        builder.scriptInput(operation.inputs());
        return Optional.of(builder.build());
    }

    /**
     * The IFA011 event an operation name stands for.
     *
     * <p>Empty for a base operation: those correspond to the external stimuli IFA011 lists
     * separately, and inventing an EVENT_ value for them would state more than the specification
     * does. The script is still produced - IFA011 NOTE 1 wants at least one of event or
     * lcmTransitionEvent, and reporting that gap is rule C24's job, not this mapper's.
     */
    private static Optional<String> eventOf(String operationName) {
        if (operationName.endsWith("_start")) {
            String base = operationName.substring(0, operationName.length() - "_start".length());
            return Optional.ofNullable(EVENT_BASE.get(base)).map(e -> "EVENT_START_" + e);
        }
        if (operationName.endsWith("_end")) {
            String base = operationName.substring(0, operationName.length() - "_end".length());
            return Optional.ofNullable(EVENT_BASE.get(base)).map(e -> "EVENT_END_" + e);
        }
        return Optional.empty();
    }

    /** [ASSUMPTION] The interpreter implied by the file extension. */
    private static Optional<String> dslOf(String path) {
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".sh") || lower.endsWith(".bash")) {
            return Optional.of("bash");
        }
        if (lower.endsWith(".py")) {
            return Optional.of("python");
        }
        return Optional.empty();
    }
}
