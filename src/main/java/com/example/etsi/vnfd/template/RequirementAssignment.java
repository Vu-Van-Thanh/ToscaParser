package com.example.etsi.vnfd.template;

import java.util.List;
import java.util.Optional;

/**
 * A requirement assignment inside a node template, i.e. what this node actually points at.
 *
 * <p>Not to be confused with {@code RequirementDefinition} in the type layer, which declares the
 * constraint - which capability, which relationship, how many occurrences are permitted. The two
 * share a keyname but carry different information:
 *
 * <pre>
 * # definition, in a node type
 * requirements:
 *   - associatedVdu:
 *       capability: tosca.capabilities.nfv.AssociableVdu
 *       node: tosca.nodes.nfv.Vdu.OsContainerDeployableUnit
 *       occurrences: [1, UNBOUNDED]
 *
 * # assignment, in a node template
 * requirements:
 *   - associatedVdu: WebVdu
 * </pre>
 *
 * <p>Both the short form above and the extended form
 * ({@code - virtual_link: { node: X, capability: Y, relationship: Z }}) parse into this class.
 */
public final class RequirementAssignment {

    private final String name;
    private final int declarationIndex;
    private String node;
    private String capability;
    private RelationshipAssignment relationship;
    private NodeFilter nodeFilter;
    private List<Object> occurrences;

    public RequirementAssignment(String name, int declarationIndex) {
        this.name = name;
        this.declarationIndex = declarationIndex;
    }

    /** The requirement name, e.g. {@code container}, {@code associatedVdu}, {@code virtual_binding}. */
    public String name() {
        return name;
    }

    /**
     * Position within the node template's requirement list.
     *
     * <p>Order carries meaning in TOSCA - {@code Vdu.Compute.boot_order} makes the order of
     * {@code virtual_storage} requirements the boot index - so it is preserved even though the CNF
     * flow does not currently read it.
     */
    public int declarationIndex() {
        return declarationIndex;
    }

    /** Name of the target node template, or a type name in the extended form. */
    public String node() {
        return node;
    }

    public Optional<String> capability() {
        return Optional.ofNullable(capability);
    }

    public Optional<RelationshipAssignment> relationship() {
        return Optional.ofNullable(relationship);
    }

    public Optional<NodeFilter> nodeFilter() {
        return Optional.ofNullable(nodeFilter);
    }

    /** {@code occurrences} as written, e.g. {@code [1, UNBOUNDED]}. Rare on an assignment. */
    public Optional<List<Object>> occurrences() {
        return Optional.ofNullable(occurrences);
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public void setRelationship(RelationshipAssignment relationship) {
        this.relationship = relationship;
    }

    public void setNodeFilter(NodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public void setOccurrences(List<Object> occurrences) {
        this.occurrences = occurrences;
    }

    @Override
    public String toString() {
        return name + " -> " + node;
    }
}
