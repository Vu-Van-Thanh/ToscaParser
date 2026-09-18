package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A capability declared by a node type: what this node offers to others.
 *
 * <p>Separate from {@link com.example.etsi.vnfd.template.CapabilityAssignment}, which supplies
 * values for one. The definition carries the occurrence bounds, for example
 * {@code Vdu.OsContainerDeployableUnit} declaring {@code associable} with {@code occurrences: [1, 1]}.
 */
public final class CapabilityDefinition {

    private final String name;
    private String type;
    private String description;
    private final Map<String, PropertyDef> properties = new LinkedHashMap<>();
    private final Map<String, PropertyDef> attributes = new LinkedHashMap<>();
    private final List<String> validSourceTypes = new ArrayList<>();
    private List<Object> occurrences;

    public CapabilityDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** Capability type, e.g. {@code tosca.capabilities.nfv.ContainerDeployable}. */
    public String type() {
        return type;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Map<String, PropertyDef> properties() {
        return properties;
    }

    public Map<String, PropertyDef> attributes() {
        return attributes;
    }

    public List<String> validSourceTypes() {
        return validSourceTypes;
    }

    public Optional<List<Object>> occurrences() {
        return Optional.ofNullable(occurrences);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOccurrences(List<Object> occurrences) {
        this.occurrences = occurrences;
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
