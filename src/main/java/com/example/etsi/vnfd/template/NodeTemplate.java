package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@code topology_template.node_templates} entry.
 *
 * <p>Everything a VNFD describes - the VNF itself, VDUs, containers, connection points, MCIOPs -
 * is a node template. Which of those it <em>is</em> cannot be decided here: the declared type may
 * be a vendor type such as {@code ExampleCorp.SimpleWebCnf.1_0}, and only a walk up
 * {@code derived_from} settles it. That is why this layer stores the type as a plain string.
 */
public final class NodeTemplate extends EntityTemplate {

    private final List<RequirementAssignment> requirements = new ArrayList<>();
    private final Map<String, CapabilityAssignment> capabilities = new LinkedHashMap<>();
    private final Map<String, ArtifactDefinition> artifacts = new LinkedHashMap<>();

    public NodeTemplate(String name) {
        super(name);
    }

    /**
     * Requirement assignments in declaration order.
     *
     * <p>A {@code List}, not a {@code Map}: TOSCA writes requirements as a sequence of single-entry
     * maps and the <em>same name may appear more than once</em>. SOL001 V5.4.1 Annex A.23 relies on
     * this - its {@code mciop1} node declares {@code associatedVdu} twice, once per associated VDU.
     * Keying by name would silently discard all but the last.
     */
    public List<RequirementAssignment> requirements() {
        return requirements;
    }

    /** Every target declared under a given requirement name, in declaration order. */
    public List<String> requirementTargets(String requirementName) {
        return requirements.stream()
                .filter(r -> r.name().equals(requirementName))
                .map(RequirementAssignment::node)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<String, CapabilityAssignment> capabilities() {
        return capabilities;
    }

    /**
     * Artifacts attached to this node template.
     *
     * <p>SOL001 puts several VNFD-significant things here rather than in properties: the software
     * image of a {@code Vdu.OsContainer} (clause 6.8.12.6) and the Helm chart plus parameter
     * mapping files of an {@code Mciop} (clause 6.8.14.7).
     */
    public Map<String, ArtifactDefinition> artifacts() {
        return artifacts;
    }
}
