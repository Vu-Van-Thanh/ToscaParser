package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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
 * Maps the nodes of one deployment flavour into the pool the VNFD is built from.
 *
 * <p>Most node types are handled the same way - take every node of a type, map it, contribute it
 * under its identifier - so they are held as data in {@link #MAPPINGS} rather than written out as a
 * loop each. Adding an ETSI node type that behaves this way is one line there.
 *
 * <p>Four do not fit, and are written out below with the reason. Forcing them into the table would
 * cost more than it saved: what the table is worth is that a reader can tell at a glance which node
 * types are ordinary, and that is only true while the extraordinary ones stay visible.
 */
final class FlavourProcessor {

    /**
     * The VDUs, kept out of {@link #MAPPINGS} only because the flavour needs them back - C28 is
     * asked about the VNFD-level VDU, which for a redeclared one belongs to an earlier flavour.
     */
    private static final NodeMapping<VduOsContainerDeployableUnit, Vdu> VDUS =
            mapping(VduOsContainerDeployableUnit.class, VduMapper::map,
                    VnfdUtils::vduId, CrossFlavourElements::addVdu);

    /** One line per ETSI node type. Adding a node type that behaves normally is one line here. */
    private static final List<NodeMapping<?, ?>> MAPPINGS = Arrays.asList(
            mapping(VduOsContainer.class, OsContainerMapper::map,
                    VnfdUtils::osContainerDescId, CrossFlavourElements::addOsContainerDesc),
            mapping(VduCp.class, CpMapper::mapVduCp,
                    VnfdUtils::cpdId, CrossFlavourElements::addVduCpd),
            mapping(VipCp.class, SpecialCpMapper::mapVipCp,
                    VnfdUtils::cpdId, CrossFlavourElements::addVipCpd),
            mapping(VirtualCp.class, SpecialCpMapper::mapVirtualCp,
                    VnfdUtils::cpdId, CrossFlavourElements::addVirtualCpd),
            mapping(VnfVirtualLink.class, VirtualLinkMapper::map,
                    VnfdUtils::virtualLinkDescId, CrossFlavourElements::addIntVirtualLinkDesc),
            mapping(Certificate.class, ModuleAndCertificateMapper::mapCertificate,
                    VnfdUtils::certificateDescId, CrossFlavourElements::addCertificateDesc));

    private final SwImageMapper swImages;
    private final StorageMapper storages;

    FlavourProcessor(SwImageMapper swImages, StorageMapper storages) {
        this.swImages = swImages;
        this.storages = storages;
    }

    /** Maps every node of one flavour into the shared pool, and returns that flavour's VDUs. */
    List<Vdu> process(FlavourContext context, CrossFlavourElements pool) {
        List<Vdu> flavourVdus = VDUS.applyTo(context, pool);
        for (NodeMapping<?, ?> mapping : MAPPINGS) {
            mapping.applyTo(context, pool);
        }

        // [1] SwImageDesc is not mapped from a node but from an artifact OF a node, and clause
        // 6.8.12.6 caps it at one - which SpecRules has to count rather than the mapper.
        for (VduOsContainer container : context.containers().values()) {
            SpecRules.swImage(container, context);
            context.artifactOfType(container, EtsiTypes.ARTIFACT_SW_IMAGE).ifPresent(image ->
                    pool.addSwImageDesc(VnfdUtils.swImageDescId(container),
                            swImages.map(image, container)));
        }

        // [2] One node template, two information elements. SOL001 clause 6.8.2.8 lets a VduCp be
        // exposed through substitution_mappings instead of a VnfExtCp node, and both become a
        // VnfExtCpd - so the two sources share one pass, which is what keeps them in declaration
        // order rather than grouped by which grammar produced them.
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VnfExtCp) {
                pool.addVnfExtCpd(VnfdUtils.cpdId(cp), CpMapper.mapVnfExtCp((VnfExtCp) cp));
            } else if (context.isExternallyExposed(cp.getKey())) {
                pool.addVnfExtCpd(VnfdUtils.cpdId(cp), CpMapper.mapExposedCp(cp));
            }
        }

        // [3] SOL001 gives block, object and file storage three node types with no common
        // supertype, and IFA011 clause 7.1.9.4.2 has one information element carrying a
        // typeOfStorage. Asking for the three separately would group the result by type instead of
        // by declaration order, so every node is offered and the mapper answers for the three it
        // knows.
        for (NfvNode node : context.nodes()) {
            storages.map(node).ifPresent(desc ->
                    pool.addVirtualStorageDesc(desc.getId(), desc));
        }

        // [4] Vnf, Mciop and DeployableModule are absent on purpose: the first becomes the VNFD
        // header, the other two belong to the deployment flavour, so none of them is a VNFD-level
        // element this pool holds.

        return flavourVdus;
    }

    // ------------------------------------------------------------------ the table's machinery

    /** One node type, the mapper that reads it, the rule that names it, and where it lands. */
    private static final class NodeMapping<N extends NfvNode, E> {

        private final Class<N> nodeType;
        private final BiFunction<N, FlavourContext, E> mapper;
        private final Function<? super N, String> id;
        private final Sink<E> sink;

        NodeMapping(Class<N> nodeType, BiFunction<N, FlavourContext, E> mapper,
                Function<? super N, String> id, Sink<E> sink) {
            this.nodeType = nodeType;
            this.mapper = mapper;
            this.id = id;
            this.sink = sink;
        }

        /** @return what the VNFD will hold for each node of this type, in declaration order */
        List<E> applyTo(FlavourContext context, CrossFlavourElements pool) {
            List<E> contributed = new ArrayList<>();
            for (N node : context.nodesOf(nodeType)) {
                contributed.add(sink.add(pool, id.apply(node), mapper.apply(node, context)));
            }
            return contributed;
        }
    }

    /** An {@code add} on the pool: contributes the element and answers with the one kept. */
    @FunctionalInterface
    private interface Sink<E> {
        E add(CrossFlavourElements pool, String id, E element);
    }

    private static <N extends NfvNode, E> NodeMapping<N, E> mapping(Class<N> nodeType,
            BiFunction<N, FlavourContext, E> mapper, Function<? super N, String> id, Sink<E> sink) {
        return new NodeMapping<>(nodeType, mapper, id, sink);
    }

    /** For a mapper that reads the node alone, which most of them do. */
    private static <N extends NfvNode, E> NodeMapping<N, E> mapping(Class<N> nodeType,
            Function<N, E> mapper, Function<? super N, String> id, Sink<E> sink) {
        return new NodeMapping<>(nodeType, (node, context) -> mapper.apply(node), id, sink);
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
