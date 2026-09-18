package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.TopologyTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.DeployableModule;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.toscatype.policy.NfvPolicy;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Turns a parsed TOSCA package into a VNFD.
 *
 * <p>The entry point of the second half of the library. {@code YamlService} answers what the package
 * says; this answers what it means. The split follows the two specifications - everything up to
 * {@link ServiceToscaTemplate} is TOSCA and SOL001 vocabulary, everything produced here is IFA011.
 *
 * <p>Each service template is one deployment flavour (SOL001 V5.4.1 clauses 6.11.2 and 6.11.3), so
 * flavours are mapped one at a time and their contributions merged by identifier: IFA011 clause
 * 7.1.2.2 keeps the descriptors themselves - vdu, osContainerDesc, swImageDesc, the connection
 * points - at VNFD level, shared by every flavour.
 */
public final class VnfdLoader {

    private final ObjectMapper mapper = ToscaBindModule.mapper();

    /** Parses a package, reporting only what this stage notices. */
    public ParseResult load(ServiceToscaTemplate template) {
        return load(template, new Findings());
    }

    /**
     * Parses into a collector the caller already holds, so package-level findings survive.
     *
     * <p>Reading the package and interpreting it are two stages with their own findings - a missing
     * manifest is noticed by {@code YamlService}, long before any VNFD exists. Passing the same
     * collector to both is what puts them in one result.
     */
    public ParseResult load(ServiceToscaTemplate template, Findings findings) {
        List<ToscaDescriptorTemplate> flavours = template.flavourTemplates();
        if (flavours.isEmpty()) {
            throw new VnfdParseException("The package contains no service template with a "
                    + "topology_template; there is nothing to parse into a VNFD");
        }

        TypeReader.Hierarchy hierarchy = new TypeReader.Hierarchy(template.typeRegistry());
        ArtifactSelector artifacts = new ArtifactSelector(hierarchy);
        NodeBinder binder = new NodeBinder(hierarchy, NodeTypes.ALL, findings);
        SwImageMapper swImages = new SwImageMapper(mapper);
        StorageMapper storages = new StorageMapper(mapper);
        DeploymentFlavourMapper flavourMapper =
                new DeploymentFlavourMapper(hierarchy, artifacts, mapper);

        Vnfd.Builder builder = Vnfd.builder();
        Merged merged = new Merged();
        Set<String> mciopIds = new LinkedHashSet<>();
        Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
        Map<String, SecurityGroupRule> securityGroupRules = new LinkedHashMap<>();
        Map<String, VnfPackageChangeInfo> packageChanges = new LinkedHashMap<>();
        boolean headerRead = false;

        for (ToscaDescriptorTemplate flavour : flavours) {
            FlavourContext context = new FlavourContext(flavour, binder, findings);

            Optional<Vnf> vnf = context.vnf();
            if (vnf.isPresent() && !headerRead) {
                VnfHeaderMapper.map(vnf.get(), builder);
                // IFA011 clause 7.1.2.2 keeps lifeCycleManagementScript at VNFD level, and SOL001
                // clause 6.11.2 gives every flavour template the same VNF node type, so the scripts
                // are read once with the header rather than once per flavour.
                for (LifeCycleManagementScript script : LcmMapper.map(vnf.get())) {
                    SpecRules.lifecycleScriptHasEvent(script, flavour.file(), findings);
                    builder.addLifeCycleManagementScript(script);
                }
                headerRead = true;
            }

            List<Vdu> flavourVdus = collectNodes(context, artifacts, swImages, storages, merged);

            DeploymentFlavourMapper.Result result = flavourMapper.map(context);
            SpecRules.flavour(result.df, flavourVdus, findings);
            builder.addDf(result.df);
            result.scripts.forEach(builder::addLcmOpParameterMappingScript);
            mciopIds.addAll(result.mciopIds);
            result.mciopArtifacts.forEach(a -> mciopArtifacts.putIfAbsent(a.getMciopId(), a));
            result.securityGroupRules.forEach(r ->
                    securityGroupRules.putIfAbsent(r.getSecurityGroupRuleId(), r));
            result.packageChanges.forEach(c ->
                    packageChanges.putIfAbsent(c.getChangeId(), c));
        }

        merged.vdus.values().forEach(builder::addVdu);
        merged.containers.values().forEach(builder::addOsContainerDesc);
        merged.images.values().forEach(builder::addSwImageDesc);
        merged.vduCpds.values().forEach(builder::addVduCpd);
        merged.extCpds.values().forEach(builder::addVnfExtCpd);
        merged.links.values().forEach(builder::addIntVirtualLinkDesc);
        merged.storages.values().forEach(builder::addVirtualStorageDesc);
        merged.vipCpds.values().forEach(builder::addVipCpd);
        merged.virtualCpds.values().forEach(builder::addVirtualCpd);
        merged.certificates.values().forEach(builder::addCertificateDesc);
        securityGroupRules.values().forEach(builder::addSecurityGroupRule);
        packageChanges.values().forEach(builder::addVnfPackageChangeInfo);
        mciopIds.forEach(builder::addMciopId);

        if (!mciopArtifacts.isEmpty()) {
            VnfdExtensions.Builder extensions = VnfdExtensions.builder();
            mciopArtifacts.values().forEach(extensions::addMciopArtifacts);
            builder.extensions(extensions.build());
        }

        Vnfd vnfd = builder.build();
        SpecRules.note6(vnfd, findings);
        return new ParseResult(vnfd, template, findings);
    }

    /** Maps every node of one flavour into the shared pool, and returns that flavour's VDUs. */
    private List<Vdu> collectNodes(FlavourContext context, ArtifactSelector artifacts,
            SwImageMapper swImages, StorageMapper storages, Merged merged) {

        List<Vdu> flavourVdus = new ArrayList<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            String id = IdRegistry.vduId(vdu);
            merged.vdus.putIfAbsent(id, VduMapper.map(vdu, context));
            flavourVdus.add(merged.vdus.get(id));
        }
        for (VduOsContainer container : context.containers().values()) {
            SpecRules.swImage(container, artifacts, context.findings());
            merged.containers.putIfAbsent(IdRegistry.osContainerDescId(container),
                    OsContainerMapper.map(container, artifacts));
            artifacts.ofType(container, EtsiTypes.ARTIFACT_SW_IMAGE).ifPresent(image ->
                    merged.images.putIfAbsent(IdRegistry.swImageDescId(container),
                            swImages.map(image, container)));
        }
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VduCp) {
                merged.vduCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapVduCp((VduCp) cp));
            }
            // SOL001 clause 6.8.2.8: a VduCp exposed through substitution_mappings is also an
            // external CP, so one node template becomes two information elements.
            if (cp instanceof VnfExtCp) {
                merged.extCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapVnfExtCp((VnfExtCp) cp));
            } else if (context.isExternallyExposed(cp.getKey())) {
                merged.extCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapExposedCp(cp));
            }
        }
        for (VnfVirtualLink link : context.virtualLinks().values()) {
            merged.links.putIfAbsent(IdRegistry.virtualLinkDescId(link), VirtualLinkMapper.map(link));
        }
        // SOL001 gives block, object and file storage three node types; IFA011 clause 7.1.9.4.2 has
        // one information element carrying a typeOfStorage, so the node type is what decides it.
        for (NfvNode storage : context.storages()) {
            storages.map(storage).ifPresent(desc ->
                    merged.storages.putIfAbsent(desc.getId(), desc));
        }
        // Neither a VipCp nor a VirtualCp is a VduCp, so neither can go through the loop above:
        // they carry a `target` requirement where a VduCp carries `virtual_binding`.
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VipCp) {
                merged.vipCpds.putIfAbsent(IdRegistry.cpdId(cp),
                        SpecialCpMapper.mapVipCp((VipCp) cp));
            } else if (cp instanceof VirtualCp) {
                merged.virtualCpds.putIfAbsent(IdRegistry.cpdId(cp),
                        SpecialCpMapper.mapVirtualCp((VirtualCp) cp));
            }
        }
        for (Certificate certificate : context.certificates()) {
            merged.certificates.putIfAbsent(IdRegistry.certificateDescId(certificate),
                    ModuleAndCertificateMapper.mapCertificate(certificate));
        }
        return flavourVdus;
    }

    /**
     * The VNFD-level elements, keyed by identifier so two flavours describing the same VDU
     * contribute it once. First declaration wins; order of first appearance is kept.
     */
    private static final class Merged {
        final Map<String, Vdu> vdus = new LinkedHashMap<>();
        final Map<String, OsContainerDesc> containers = new LinkedHashMap<>();
        final Map<String, SwImageDesc> images = new LinkedHashMap<>();
        final Map<String, VduCpd> vduCpds = new LinkedHashMap<>();
        final Map<String, VnfExtCpd> extCpds = new LinkedHashMap<>();
        final Map<String, VnfVirtualLinkDesc> links = new LinkedHashMap<>();
        final Map<String, VirtualStorageDesc> storages = new LinkedHashMap<>();
        final Map<String, VipCpd> vipCpds = new LinkedHashMap<>();
        final Map<String, VirtualCpd> virtualCpds = new LinkedHashMap<>();
        final Map<String, CertificateDesc> certificates = new LinkedHashMap<>();
    }
}

/**
 * One deployment flavour, classified once, with the cross-references the mappers need.
 *
 * <p>Node templates are bound here rather than repeatedly by each mapper. The reverse indexes are
 * the point: several IFA011 attributes live on a different element from the one that declares the
 * relation. {@code Vdu.intCpd} is the clearest case - a {@code VduCp} states which VDU it binds to,
 * so the connection points of a VDU can only be found by reading every connection point first.
 */
final class FlavourContext {

    private final ToscaDescriptorTemplate template;
    private final TopologyTemplate topology;
    private final NodeBinder binder;
    private final Findings findings;

    private final List<VduOsContainerDeployableUnit> vdus = new ArrayList<>();
    private final Map<String, VduOsContainer> containers = new LinkedHashMap<>();
    private final Map<String, Cp> connectionPoints = new LinkedHashMap<>();
    private final Map<String, VnfVirtualLink> virtualLinks = new LinkedHashMap<>();
    private final List<Mciop> mciops = new ArrayList<>();
    private final List<NfvNode> storages = new ArrayList<>();
    private final List<Certificate> certificates = new ArrayList<>();
    private final List<DeployableModule> deployableModules = new ArrayList<>();
    private Vnf vnf;

    private final Map<String, List<String>> cpsByVdu = new LinkedHashMap<>();
    private final Map<String, List<String>> mciopsByVdu = new LinkedHashMap<>();
    private final Set<String> externallyExposedCps = new LinkedHashSet<>();

    FlavourContext(ToscaDescriptorTemplate template, NodeBinder binder, Findings findings) {
        this.template = template;
        this.binder = binder;
        this.findings = findings;
        this.topology = template.topologyTemplate().orElseThrow(() -> new VnfdParseException(
                "Service template has no topology_template: " + template.file()));
        classify();
        index();
    }

    private void classify() {
        for (NodeTemplate raw : topology.nodeTemplates().values()) {
            Optional<NfvNode> bound = binder.bind(raw, topology.nodeTemplates());
            if (!bound.isPresent()) {
                continue;
            }
            NfvNode node = bound.get();
            if (node instanceof Vnf) {
                vnf = (Vnf) node;
            } else if (node instanceof VduOsContainerDeployableUnit) {
                vdus.add((VduOsContainerDeployableUnit) node);
            } else if (node instanceof VduOsContainer) {
                containers.put(node.getKey(), (VduOsContainer) node);
            } else if (node instanceof Cp) {
                connectionPoints.put(node.getKey(), (Cp) node);
            } else if (node instanceof VnfVirtualLink) {
                virtualLinks.put(node.getKey(), (VnfVirtualLink) node);
            } else if (node instanceof Mciop) {
                mciops.add((Mciop) node);
            } else if (isStorage(node)) {
                storages.add(node);
            } else if (node instanceof Certificate) {
                certificates.add((Certificate) node);
            } else if (node instanceof DeployableModule) {
                deployableModules.add((DeployableModule) node);
            }
        }
    }

    private boolean isStorage(NfvNode node) {
        String etsi = node.getEtsiType();
        return etsi != null && etsi.startsWith("tosca.nodes.nfv.Vdu.Virtual")
                && etsi.endsWith("Storage");
    }

    private void index() {
        // A connection point names its VDU, never the other way round (SOL001 clause 6.8.8).
        for (Cp cp : connectionPoints.values()) {
            if (cp instanceof VduCp) {
                first(((VduCp) cp).getRequirements() == null
                        ? null : ((VduCp) cp).getRequirements().getVirtualBinding())
                        .ifPresent(vdu -> cpsByVdu
                                .computeIfAbsent(vdu, key -> new ArrayList<>()).add(cp.getKey()));
            }
        }
        // An MCIOP names its VDUs, and may name several (occurrences [1, UNBOUNDED]).
        for (Mciop mciop : mciops) {
            if (mciop.getRequirements() == null) {
                continue;
            }
            for (String vdu : orEmpty(mciop.getRequirements().getAssociatedVdu())) {
                mciopsByVdu.computeIfAbsent(vdu, key -> new ArrayList<>()).add(mciop.getKey());
            }
        }
        // SOL001 clause 6.8.2.8: substitution_mappings can expose a VduCp as an external CP.
        topology.substitutionMappings()
                .ifPresent(m -> externallyExposedCps.addAll(m.exposedNodeTemplates()));
    }

    /** Where the mappers report what the descriptor got wrong. */
    Findings findings() {
        return findings;
    }

    ToscaDescriptorTemplate template() {
        return template;
    }

    TopologyTemplate topology() {
        return topology;
    }

    Optional<Vnf> vnf() {
        return Optional.ofNullable(vnf);
    }

    List<VduOsContainerDeployableUnit> vdus() {
        return vdus;
    }

    Map<String, VduOsContainer> containers() {
        return containers;
    }

    Map<String, Cp> connectionPoints() {
        return connectionPoints;
    }

    Map<String, VnfVirtualLink> virtualLinks() {
        return virtualLinks;
    }

    List<Mciop> mciops() {
        return mciops;
    }

    List<NfvNode> storages() {
        return storages;
    }

    List<Certificate> certificates() {
        return certificates;
    }

    List<DeployableModule> deployableModules() {
        return deployableModules;
    }

    List<PolicyDefinition> policies() {
        return topology.policies();
    }

    /** IFA011 clause 7.1.6.2.2 {@code Vdu.intCpd}, assembled from the connection points. */
    List<String> cpsBoundTo(String vduKey) {
        return cpsByVdu.getOrDefault(vduKey, Collections.emptyList());
    }

    /** Drives {@code lcmRealizationPath} and the MCIOP coverage check. */
    List<String> mciopsAssociatedTo(String vduKey) {
        return mciopsByVdu.getOrDefault(vduKey, Collections.emptyList());
    }

    /**
     * Targets of a requirement as written, straight off the node template.
     *
     * <p>Needed for requirements a node type does not declare, so the bound class has no field for
     * them. {@code dependency} is the case that matters: SOL001 V5.4.1 clause 6.8.14.7 says it "may
     * be used towards other Mciop nodes to express the order of deployment", but it comes from
     * {@code tosca.nodes.Root} rather than from the Mciop type, so nothing generated from the ETSI
     * type file can carry it.
     */
    List<String> rawRequirementTargets(String nodeKey, String requirementName) {
        NodeTemplate raw = topology.nodeTemplates().get(nodeKey);
        if (raw == null) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (RequirementAssignment requirement : raw.requirements()) {
            if (requirementName.equals(requirement.name()) && requirement.node() != null) {
                out.add(requirement.node());
            }
        }
        return out;
    }

    boolean isExternallyExposed(String cpKey) {
        return externallyExposedCps.contains(cpKey);
    }

    static Optional<String> first(List<String> values) {
        return values == null || values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    static List<String> orEmpty(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }
}

/**
 * Every rule this library uses to derive an IFA011 identifier from a TOSCA declaration.
 *
 * <p>Gathered in one class on purpose. The rules are nearly all the same - "the node template name
 * is the id" - but only one of them is stated outright by the specification, and scattering them
 * across the mappers would make it impossible to see which is which.
 */
final class IdRegistry {

    private IdRegistry() {
    }

    /**
     * [VERIFIED] SOL001 V5.4.1 clause 6.8.12.6: the node template name of a
     * {@code Vdu.OsContainer} "fulfils the purpose of the 'id' attribute of the SwImageDesc
     * information element and hence it will be used in APIs to identify the software image id from
     * the VNFD perspective".
     */
    static String swImageDescId(NfvNode owningNode) {
        return owningNode.getKey();
    }

    /** [ASSUMPTION] The node template name. SOL001 states the rule only for SwImageDesc. */
    static String vduId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String osContainerDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualStorageDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String cpdId(NfvNode node) {
        return node.getKey();
    }

    /**
     * {@code CertificateDesc.id}, IFA011 clause 7.1.19.2.2 - M,1.
     *
     * <p>[ASSUMPTION] The node template name. SOL001 clause 6.8.19 does not say how the identifier
     * is derived; the only place SOL001 states that rule outright is clause 6.8.12.6, for
     * SwImageDesc.
     */
    static String certificateDescId(NfvNode node) {
        return node.getKey();
    }

    /**
     * {@code DeployableModule.deployableModuleId}, IFA011 clause 7.1.8.24.2 - M,1.
     *
     * <p>[ASSUMPTION] The node template name, for the same reason as above. Note the identifier has
     * to agree with whatever {@code VduProfile.deployableModule} names, since that is the reference
     * IFA011 uses to attach a VDU to a module.
     */
    static String deployableModuleId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualLinkDescId(NfvNode node) {
        return node.getKey();
    }

    /**
     * [ASSUMPTION] The node template name.
     *
     * <p>IFA011 V5.4.1 clause 7.1.8.20.2 says {@code mciopId} "identifies the MCIOP in the VNF
     * package" without saying how. The node template name is the only stable handle a descriptor
     * offers, and SOL001 Table 6.1-1 NOTE 3 maps {@code associatedVdu} and {@code deploymentOrder}
     * onto this node type, so the profile is built around it either way.
     */
    static String mciopId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] The artifact definition name. */
    static String lcmOpParameterMappingScriptId(ArtifactDefinition artifact) {
        return artifact.name();
    }

    /**
     * [ASSUMPTION] The policy name.
     *
     * <p>SOL001 Table 6.1-1 NOTE 3 states that {@code affinityOrAntiAffinityGroupId} maps to an
     * {@code AffinityRule} or {@code AntiAffinityRule} policy, but not what the group id is.
     */
    static String affinityGroupId(NfvPolicy policy) {
        return policy.getKey();
    }

    /** [ASSUMPTION] Interface plus operation, e.g. {@code Vnflcm.instantiate_start}. */
    static String lcmScriptId(String interfaceName, String operationName) {
        return interfaceName + "." + operationName;
    }
}

/**
 * Finds the artifacts of a node by ETSI type.
 *
 * <p>Artifacts cannot be looked up by name: the name of an artifact definition is the VNFD author's
 * choice - the bundled packages write {@code sw_image} and {@code web_helm_chart} - while SOL001
 * always speaks of the type. Clause 6.8.12.6 asks for "an artifact of type
 * tosca.artifacts.nfv.SwImage"; clause 6.8.14.7 caps each type on an {@code Mciop} at one.
 *
 * <p>Matching walks {@code derived_from} like everything else, so a vendor artifact type derived
 * from an ETSI one is still found.
 */
final class ArtifactSelector {

    private final TypeReader.Hierarchy hierarchy;

    ArtifactSelector(TypeReader.Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /** Every artifact of the given type, in declaration order. */
    List<ArtifactDefinition> allOfType(NfvNode node, String etsiArtifactType) {
        Map<String, ArtifactDefinition> declared = node.getArtifacts();
        if (declared == null || declared.isEmpty()) {
            return Collections.emptyList();
        }
        List<ArtifactDefinition> out = new ArrayList<>();
        for (ArtifactDefinition artifact : declared.values()) {
            if (hierarchy.isDerivedFrom(artifact.type(), etsiArtifactType)) {
                out.add(artifact);
            }
        }
        return out;
    }

    /**
     * The single artifact of that type.
     *
     * <p>More than one is a rule violation, not a parse failure, so the first is returned and the
     * caller checks {@link #allOfType} when it needs to report the cardinality.
     */
    Optional<ArtifactDefinition> ofType(NfvNode node, String etsiArtifactType) {
        List<ArtifactDefinition> all = allOfType(node, etsiArtifactType);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /** The package-root-relative path of an artifact, falling back to the reference as written. */
    static String pathOf(ArtifactDefinition artifact) {
        return artifact.resolvedFile().orElse(artifact.file());
    }
}

/**
 * Which CISM interface a VDU will be managed through.
 *
 * <p>[MANO INTERPRETATION] No attribute of IFA011 says "this VDU uses Helm". The conclusion follows
 * from two normative statements read together: IFA011 V5.4.1 clause 7.1.6.2.2 Note 10 - "In case
 * the VDU to be deployed is realized as OS containers and osContainerDesc is not present, the
 * MciopProfile associated with the VDU shall be present" - and SOL018 V5.4.1 clause 6.2.1.1, which
 * says the CISM exposes "management service interfaces on different abstraction levels. One
 * abstraction level are the MCIOPs, the other abstraction level are the MCIOs".
 *
 * <p>Worth deriving because the two paths differ in practice: through an MCIOP the VNFM runs the
 * parameter mapping script and then operates on a whole Helm release (SOL018 clause 7), while a VDU
 * described by an OsContainer is realized MCIO by MCIO through the Kubernetes API (clause 8), with
 * no equivalent of a rollback.
 *
 * <p>Note 10 is stated per VDU, and nothing requires the VDUs of one flavour to agree - so one
 * flavour may legitimately hold both kinds.
 */
final class LcmRealizationResolver {

    private LcmRealizationResolver() {
    }

    static LcmRealizationPath resolve(VduOsContainerDeployableUnit vdu,
            List<String> associatedMciops) {
        if (!associatedMciops.isEmpty()) {
            return LcmRealizationPath.MCIOP_CISM;
        }
        if (!containerTargets(vdu).isEmpty()) {
            return LcmRealizationPath.DIRECT_MCIO_CISM;
        }
        // Neither, which SpecRules reports as C2; the path itself is simply not derivable.
        return LcmRealizationPath.UNDETERMINED;
    }

    static List<String> containerTargets(VduOsContainerDeployableUnit vdu) {
        return vdu.getRequirements() == null
                ? java.util.Collections.emptyList()
                : FlavourContext.orEmpty(vdu.getRequirements().getContainer());
    }
}
