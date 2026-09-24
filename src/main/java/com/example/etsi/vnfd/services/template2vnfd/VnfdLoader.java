package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
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
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
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
        NodeBinder binder = new NodeBinder(hierarchy, NodeTypes.ALL, findings);
        SwImageMapper swImages = new SwImageMapper(mapper);
        StorageMapper storages = new StorageMapper(mapper);
        DeploymentFlavourMapper flavourMapper =
                new DeploymentFlavourMapper(hierarchy, mapper);

        Vnfd.Builder builder = Vnfd.builder();
        CrossFlavourElements merged = new CrossFlavourElements();
        Set<String> mciopIds = new LinkedHashSet<>();
        Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
        Map<String, SecurityGroupRule> securityGroupRules = new LinkedHashMap<>();
        Map<String, VnfPackageChangeInfo> packageChanges = new LinkedHashMap<>();
        boolean headerRead = false;

        for (ToscaDescriptorTemplate flavour : flavours) {
            FlavourContext context = new FlavourContext(flavour, binder, hierarchy, findings);

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

            List<Vdu> flavourVdus = collectNodes(context, swImages, storages, merged);

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

        merged.drainInto(builder);
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
    private List<Vdu> collectNodes(FlavourContext context,
            SwImageMapper swImages, StorageMapper storages, CrossFlavourElements merged) {

        List<Vdu> flavourVdus = new ArrayList<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            flavourVdus.add(merged.addVdu(VnfdUtils.vduId(vdu), VduMapper.map(vdu, context)));
        }
        for (VduOsContainer container : context.containers().values()) {
            SpecRules.swImage(container, context);
            merged.addOsContainerDesc(VnfdUtils.osContainerDescId(container),
                    OsContainerMapper.map(container, context));
            context.artifactOfType(container, EtsiTypes.ARTIFACT_SW_IMAGE).ifPresent(image ->
                    merged.addSwImageDesc(VnfdUtils.swImageDescId(container),
                            swImages.map(image, container)));
        }
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VduCp) {
                merged.addVduCpd(VnfdUtils.cpdId(cp), CpMapper.mapVduCp((VduCp) cp));
            }
            // SOL001 clause 6.8.2.8: a VduCp exposed through substitution_mappings is also an
            // external CP, so one node template becomes two information elements.
            if (cp instanceof VnfExtCp) {
                merged.addVnfExtCpd(VnfdUtils.cpdId(cp), CpMapper.mapVnfExtCp((VnfExtCp) cp));
            } else if (context.isExternallyExposed(cp.getKey())) {
                merged.addVnfExtCpd(VnfdUtils.cpdId(cp), CpMapper.mapExposedCp(cp));
            }
        }
        for (VnfVirtualLink link : context.virtualLinks().values()) {
            merged.addIntVirtualLinkDesc(VnfdUtils.virtualLinkDescId(link), VirtualLinkMapper.map(link));
        }
        // SOL001 gives block, object and file storage three node types; IFA011 clause 7.1.9.4.2 has
        // one information element carrying a typeOfStorage, so the node type is what decides it.
        for (NfvNode node : context.nodes()) {
            storages.map(node).ifPresent(desc ->
                    merged.addVirtualStorageDesc(desc.getId(), desc));
        }
        // Neither a VipCp nor a VirtualCp is a VduCp, so neither can go through the loop above:
        // they carry a `target` requirement where a VduCp carries `virtual_binding`.
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VipCp) {
                merged.addVipCpd(VnfdUtils.cpdId(cp),
                        SpecialCpMapper.mapVipCp((VipCp) cp));
            } else if (cp instanceof VirtualCp) {
                merged.addVirtualCpd(VnfdUtils.cpdId(cp),
                        SpecialCpMapper.mapVirtualCp((VirtualCp) cp));
            }
        }
        for (Certificate certificate : context.certificates()) {
            merged.addCertificateDesc(VnfdUtils.certificateDescId(certificate),
                    ModuleAndCertificateMapper.mapCertificate(certificate));
        }
        return flavourVdus;
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
    private final TypeReader.Hierarchy hierarchy;
    private final Findings findings;

    /** Every node template this library could bind, in declaration order, keyed by node name. */
    private final Map<String, NfvNode> nodes = new LinkedHashMap<>();

    /** Typed views over {@link #nodes}, each computed the first time its type is asked for. */
    private final Map<Class<?>, List<? extends NfvNode>> listViews = new HashMap<>();
    private final Map<Class<?>, Map<String, ? extends NfvNode>> mapViews = new HashMap<>();

    private final Map<String, List<String>> cpsByVdu = new LinkedHashMap<>();
    private final Map<String, List<String>> mciopsByVdu = new LinkedHashMap<>();
    private final Set<String> externallyExposedCps = new LinkedHashSet<>();

    FlavourContext(ToscaDescriptorTemplate template, NodeBinder binder,
            TypeReader.Hierarchy hierarchy, Findings findings) {
        this.template = template;
        this.binder = binder;
        this.hierarchy = hierarchy;
        this.findings = findings;
        this.topology = template.topologyTemplate().orElseThrow(() -> new VnfdParseException(
                "Service template has no topology_template: " + template.file()));
        bindAll();
        index();
    }

    /**
     * Binds every node template once.
     *
     * <p>Once, because {@code NodeBinder.bind} is not a pure function: it reports TYPE01 for a type
     * it cannot resolve and runs the constraint checks the type declares. Binding a node a second
     * time would report it a second time, so the bound nodes are kept and the typed views below are
     * computed over them rather than by binding again.
     */
    private void bindAll() {
        for (NodeTemplate raw : topology.nodeTemplates().values()) {
            binder.bind(raw, topology.nodeTemplates())
                    .ifPresent(node -> nodes.put(node.getKey(), node));
        }
    }

    /**
     * Every bound node of the given type, in declaration order.
     *
     * <p>The type is an argument rather than a branch of a chain of {@code instanceof}, so a node
     * type added to {@code NodeTypes.ALL} is readable here without this class being edited.
     */
    <T extends NfvNode> List<T> nodesOf(Class<T> type) {
        @SuppressWarnings("unchecked")
        List<T> cached = (List<T>) listViews.get(type);
        if (cached != null) {
            return cached;
        }
        List<T> out = new ArrayList<>();
        for (NfvNode node : nodes.values()) {
            if (type.isInstance(node)) {
                out.add(type.cast(node));
            }
        }
        List<T> view = Collections.unmodifiableList(out);
        listViews.put(type, view);
        return view;
    }

    /** The same nodes keyed by node template name, so a lookup by name stays O(1). */
    <T extends NfvNode> Map<String, T> indexOf(Class<T> type) {
        @SuppressWarnings("unchecked")
        Map<String, T> cached = (Map<String, T>) mapViews.get(type);
        if (cached != null) {
            return cached;
        }
        Map<String, T> out = new LinkedHashMap<>();
        for (T node : nodesOf(type)) {
            out.put(node.getKey(), node);
        }
        Map<String, T> view = Collections.unmodifiableMap(out);
        mapViews.put(type, view);
        return view;
    }

    /**
     * Every bound node, in declaration order, for a mapper that decides by type itself.
     *
     * <p>SOL001 gives block, object and file storage three node types with no common supertype, so
     * no single {@link #nodesOf} call reproduces them - and asking three times would group the
     * result by type instead of by declaration order.
     */
    Collection<NfvNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    private void index() {
        // A connection point names its VDU, never the other way round (SOL001 clause 6.8.8).
        for (VduCp cp : nodesOf(VduCp.class)) {
            VnfdUtils.first(cp.getRequirements() == null
                    ? null : cp.getRequirements().getVirtualBinding())
                    .ifPresent(vdu -> cpsByVdu
                            .computeIfAbsent(vdu, key -> new ArrayList<>()).add(cp.getKey()));
        }
        // An MCIOP names its VDUs, and may name several (occurrences [1, UNBOUNDED]).
        for (Mciop mciop : nodesOf(Mciop.class)) {
            if (mciop.getRequirements() == null) {
                continue;
            }
            for (String vdu : VnfdUtils.orEmpty(mciop.getRequirements().getAssociatedVdu())) {
                mciopsByVdu.computeIfAbsent(vdu, key -> new ArrayList<>()).add(mciop.getKey());
            }
        }
        // SOL001 clause 6.8.2.8: substitution_mappings can expose a VduCp as an external CP.
        topology.substitutionMappings()
                .ifPresent(m -> externallyExposedCps.addAll(m.exposedNodeTemplates()));
    }

    /** Every artifact of that ETSI type on the node, matched through {@code derived_from}. */
    List<ArtifactDefinition> artifactsOfType(NfvNode node, String etsiArtifactType) {
        return VnfdUtils.artifactsOfType(hierarchy, node, etsiArtifactType);
    }

    /** The single artifact of that type; more than one is a rule violation, not a parse failure. */
    Optional<ArtifactDefinition> artifactOfType(NfvNode node, String etsiArtifactType) {
        return VnfdUtils.artifactOfType(hierarchy, node, etsiArtifactType);
    }

    Findings findings() {
        return findings;
    }

    ToscaDescriptorTemplate template() {
        return template;
    }

    TopologyTemplate topology() {
        return topology;
    }

    /**
     * The VNF node of this flavour, last declaration winning as it always has.
     *
     * <p>SOL001 clause 6.11.2 gives a flavour template one VNF node, so a second one is already
     * malformed; this only says which of them is read.
     */
    Optional<Vnf> vnf() {
        List<Vnf> all = nodesOf(Vnf.class);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(all.size() - 1));
    }

    List<VduOsContainerDeployableUnit> vdus() {
        return nodesOf(VduOsContainerDeployableUnit.class);
    }

    Map<String, VduOsContainer> containers() {
        return indexOf(VduOsContainer.class);
    }

    Map<String, Cp> connectionPoints() {
        return indexOf(Cp.class);
    }

    Map<String, VnfVirtualLink> virtualLinks() {
        return indexOf(VnfVirtualLink.class);
    }

    List<Mciop> mciops() {
        return nodesOf(Mciop.class);
    }

    List<Certificate> certificates() {
        return nodesOf(Certificate.class);
    }

    List<DeployableModule> deployableModules() {
        return nodesOf(DeployableModule.class);
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
}
