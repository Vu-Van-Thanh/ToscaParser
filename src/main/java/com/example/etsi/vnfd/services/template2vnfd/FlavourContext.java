package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import java.util.ArrayList;
import java.util.List;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.template.TopologyTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.toscatype.node.DeployableModule;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.validation.Findings;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * One deployment flavour, classified once, with the cross-references the mappers need.
 *
 * <p>Node templates are bound here rather than repeatedly by each mapper. The reverse indexes are
 * the point: several IFA011 attributes live on a different element from the one that declares the
 * relation. {@code Vdu.intCpd} is the clearest case - a {@code VduCp} states which VDU it binds to,
 * so the connection points of a VDU can only be found by reading every connection point first.
 */
public final class FlavourContext {

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
    private final Map<String, List<String>> deployableModulesByVdu = new LinkedHashMap<>();
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
        collectVduRelations();
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
    public <T extends NfvNode> List<T> nodesOf(Class<T> type) {
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
    public <T extends NfvNode> Map<String, T> indexOf(Class<T> type) {
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
    public Collection<NfvNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /** The reverse indexes the mappers read: which CPs and MCIOPs name which VDU, and which CPs are exposed. */
    private void collectVduRelations() {
        collectCpsByVdu();
        collectMciopsByVdu();
        collectDeployableModulesByVdu();
        collectExposedCps();
    }

    /** A connection point names its VDU, never the other way round (SOL001 clause 6.8.8). */
    private void collectCpsByVdu() {
        for (VduCp cp : nodesOf(VduCp.class)) {
            VnfdUtils.first(cp.getRequirements() == null
                    ? null : cp.getRequirements().getVirtualBinding())
                    .ifPresent(vdu -> cpsByVdu
                            .computeIfAbsent(vdu, key -> new ArrayList<>()).add(cp.getKey()));
        }
    }

    /** An MCIOP names its VDUs, and may name several (occurrences [1, UNBOUNDED]). */
    private void collectMciopsByVdu() {
        for (Mciop mciop : nodesOf(Mciop.class)) {
            if (mciop.getRequirements() == null) {
                continue;
            }
            for (String vdu : VnfdUtils.orEmpty(mciop.getRequirements().getAssociatedVdu())) {
                mciopsByVdu.computeIfAbsent(vdu, key -> new ArrayList<>()).add(mciop.getKey());
            }
        }
    }

    /**
     * A DeployableModule names its members (SOL001 clause 6.8.16), and a member is either a VDU
     * directly or an Mciop - both declare the {@code DeployableModuleMember} capability, and nothing
     * in the type definitions picks one over the other. An Mciop member stands for the VDUs it
     * associates with, so the module applies to each of them.
     *
     * <p>IFA011 clause 7.1.8.24.1: a VDU named by no module is instantiated unconditionally; one
     * named by a module is only instantiated if that module is selected. This index is what lets
     * {@code VduProfile.deployableModule} (clause 7.1.8.3.2) make that distinction.
     */
    private void collectDeployableModulesByVdu() {
        for (DeployableModule module : nodesOf(DeployableModule.class)) {
            if (module.getRequirements() == null) {
                continue;
            }
            String moduleId = VnfdUtils.nodeId(module);
            for (String memberKey : VnfdUtils.orEmpty(module.getRequirements().getMember())) {
                NfvNode member = nodes.get(memberKey);
                if (member instanceof VduOsContainerDeployableUnit) {
                    addDeployableModuleFor(memberKey, moduleId);
                } else if (member instanceof Mciop) {
                    Mciop mciop = (Mciop) member;
                    if (mciop.getRequirements() != null) {
                        for (String vduKey
                                : VnfdUtils.orEmpty(mciop.getRequirements().getAssociatedVdu())) {
                            addDeployableModuleFor(vduKey, moduleId);
                        }
                    }
                }
            }
        }
    }

    private void addDeployableModuleFor(String vduKey, String moduleId) {
        deployableModulesByVdu.computeIfAbsent(vduKey, key -> new ArrayList<>()).add(moduleId);
    }

    /** SOL001 clause 6.8.2.8: substitution_mappings can expose a VduCp as an external CP. */
    private void collectExposedCps() {
        topology.substitutionMappings()
                .ifPresent(m -> externallyExposedCps.addAll(m.exposedNodeTemplates()));
    }

    /** Every artifact of that ETSI type on the node, matched through {@code derived_from}. */
    public List<ArtifactDefinition> artifactsOfType(NfvNode node, String etsiArtifactType) {
        return VnfdUtils.artifactsOfType(hierarchy, node, etsiArtifactType);
    }

    /** The single artifact of that type; more than one is a rule violation, not a parse failure. */
    public Optional<ArtifactDefinition> artifactOfType(NfvNode node, String etsiArtifactType) {
        return VnfdUtils.artifactOfType(hierarchy, node, etsiArtifactType);
    }

    /** Whether one type is, or derives from, another - the walk every classification goes through. */
    public boolean isDerivedFrom(String typeName, String ancestorTypeName) {
        return hierarchy.isDerivedFrom(typeName, ancestorTypeName);
    }

    public Findings findings() {
        return findings;
    }

    public ToscaDescriptorTemplate template() {
        return template;
    }

    public TopologyTemplate topology() {
        return topology;
    }

    /**
     * The VNF node of this flavour, last declaration winning as it always has.
     *
     * <p>SOL001 clause 6.11.2 gives a flavour template one VNF node, so a second one is already
     * malformed; this only says which of them is read.
     */
    public Optional<Vnf> vnf() {
        List<Vnf> all = nodesOf(Vnf.class);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(all.size() - 1));
    }

    public List<VduOsContainerDeployableUnit> vdus() {
            return nodesOf(VduOsContainerDeployableUnit.class);
    }

    public Map<String, VduOsContainer> containers() {
            return indexOf(VduOsContainer.class);
    }

    public Map<String, Cp> connectionPoints() {
            return indexOf(Cp.class);
    }

    public Map<String, VnfVirtualLink> virtualLinks() {
            return indexOf(VnfVirtualLink.class);
    }

    public List<Mciop> mciops() {
            return nodesOf(Mciop.class);
    }

    public List<Certificate> certificates() {
            return nodesOf(Certificate.class);
    }

    public List<DeployableModule> deployableModules() {
            return nodesOf(DeployableModule.class);
    }

    public List<PolicyDefinition> policies() {
        return topology.policies();
    }

    /** IFA011 clause 7.1.6.2.2 {@code Vdu.intCpd}, assembled from the connection points. */
    public List<String> cpsBoundTo(String vduKey) {
        return cpsByVdu.getOrDefault(vduKey, Collections.emptyList());
    }

    /** Drives {@code lcmRealizationPath} and the MCIOP coverage check. */
    public List<String> mciopsAssociatedTo(String vduKey) {
        return mciopsByVdu.getOrDefault(vduKey, Collections.emptyList());
    }

    /** {@code VduProfile.deployableModule} (IFA011 clause 7.1.8.3.2): the modules this VDU is a member of. */
    public List<String> deployableModulesOf(String vduKey) {
        return deployableModulesByVdu.getOrDefault(vduKey, Collections.emptyList());
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
    public List<String> rawRequirementTargets(String nodeKey, String requirementName) {
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

    public boolean isExternallyExposed(String cpKey) {
        return externallyExposedCps.contains(cpKey);
    }
}
