package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@code node_types} entry.
 *
 * <p>Requirements are a {@code List} for the same reason they are on a node template: TOSCA writes
 * them as a sequence and the order is part of the declaration.
 */
public final class NodeTypeDef extends AbstractTypeDef {

    private final List<RequirementDefinition> requirements = new ArrayList<>();
    private final Map<String, CapabilityDefinition> capabilities = new LinkedHashMap<>();
    private final Map<String, Object> interfaces = new LinkedHashMap<>();
    private final Map<String, Object> artifacts = new LinkedHashMap<>();

    NodeTypeDef(String name) {
        super(name);
    }

    public List<RequirementDefinition> requirements() {
        return requirements;
    }

    /** The requirement declaration of a given name, when this type declares one. */
    public Optional<RequirementDefinition> requirement(String name) {
        return requirements.stream().filter(r -> r.name().equals(name)).findFirst();
    }

    public Map<String, CapabilityDefinition> capabilities() {
        return capabilities;
    }

    /** Interface definitions, kept raw; the ETSI layer interprets {@code Vnflcm}. */
    public Map<String, Object> interfaces() {
        return interfaces;
    }

    public Map<String, Object> artifacts() {
        return artifacts;
    }
}
