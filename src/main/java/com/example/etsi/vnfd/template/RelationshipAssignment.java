package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code relationship} keyname of a requirement assignment.
 *
 * <p>May be written as a bare type name or as a map carrying properties and interfaces, so it needs
 * its own type rather than a {@code String}.
 */
public final class RelationshipAssignment {

    private final String type;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, InterfaceAssignment> interfaces = new LinkedHashMap<>();

    public RelationshipAssignment(String type) {
        this.type = type;
    }

    /** Relationship type, e.g. {@code tosca.relationships.nfv.MciopAssociates}. */
    public String type() {
        return type;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    public Map<String, InterfaceAssignment> interfaces() {
        return interfaces;
    }

    @Override
    public String toString() {
        return type;
    }
}
