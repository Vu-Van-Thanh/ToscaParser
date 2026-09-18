package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.template.TopologyTemplate;
import com.example.etsi.vnfd.toscatype.bind.NodeBinder;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
            Optional<NfvNode> bound = binder.bind(raw);
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
