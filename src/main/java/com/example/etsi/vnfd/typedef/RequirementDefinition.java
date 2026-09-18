package com.example.etsi.vnfd.typedef;

import java.util.List;
import java.util.Optional;

/**
 * A requirement declared by a node type: the constraint, not the value.
 *
 * <p>Deliberately separate from
 * {@link com.example.etsi.vnfd.template.RequirementAssignment}. The two use the same
 * {@code requirements} keyname but carry different information, and conflating them loses the
 * cardinality and target-type rules that several conformance checks depend on:
 *
 * <pre>
 * tosca.nodes.nfv.Mciop:
 *   requirements:
 *     - associatedVdu:
 *         capability: tosca.capabilities.nfv.AssociableVdu
 *         relationship: tosca.relationships.nfv.MciopAssociates
 *         node: tosca.nodes.nfv.Vdu.OsContainerDeployableUnit
 *         occurrences: [1, UNBOUNDED]
 * </pre>
 *
 * <p>Here {@code occurrences} is what makes an {@code Mciop} without any {@code associatedVdu}
 * invalid, and {@code node} is what makes one pointing at something other than a deployable unit
 * invalid. Neither fact exists on the assignment side.
 */
public final class RequirementDefinition {

    /** The TOSCA keyword for an unbounded upper occurrence. */
    public static final String UNBOUNDED = "UNBOUNDED";

    private final String name;
    private String capability;
    private String node;
    private String relationship;
    private List<Object> occurrences;

    RequirementDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** Required capability type of the target. */
    public Optional<String> capability() {
        return Optional.ofNullable(capability);
    }

    /** Required node type of the target, when the definition constrains it. */
    public Optional<String> node() {
        return Optional.ofNullable(node);
    }

    public Optional<String> relationship() {
        return Optional.ofNullable(relationship);
    }

    /** Raw {@code occurrences}, e.g. {@code [1, UNBOUNDED]}. */
    public Optional<List<Object>> occurrences() {
        return Optional.ofNullable(occurrences);
    }

    /** Lower bound; TOSCA defaults it to 1 when {@code occurrences} is absent. */
    public int minOccurrences() {
        if (occurrences == null || occurrences.isEmpty()) {
            return 1;
        }
        return toInt(occurrences.get(0), 1);
    }

    /**
     * Upper bound, or {@link Integer#MAX_VALUE} for {@code UNBOUNDED}.
     * TOSCA defaults it to 1 when {@code occurrences} is absent.
     */
    public int maxOccurrences() {
        if (occurrences == null || occurrences.size() < 2) {
            return 1;
        }
        Object upper = occurrences.get(1);
        if (UNBOUNDED.equalsIgnoreCase(String.valueOf(upper))) {
            return Integer.MAX_VALUE;
        }
        return toInt(upper, 1);
    }

    /** True when more than one assignment of this requirement is permitted. */
    public boolean allowsMultiple() {
        return maxOccurrences() > 1;
    }

    private static int toInt(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    void setCapability(String capability) {
        this.capability = capability;
    }

    void setNode(String node) {
        this.node = node;
    }

    void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    void setOccurrences(List<Object> occurrences) {
        this.occurrences = occurrences;
    }

    @Override
    public String toString() {
        return name + " [" + minOccurrences() + ", "
                + (maxOccurrences() == Integer.MAX_VALUE ? UNBOUNDED : maxOccurrences()) + "]";
    }
}
