package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code relationship_types} entry, e.g. {@code tosca.relationships.nfv.MciopAssociates}.
 */
public final class RelationshipTypeDef extends AbstractTypeDef {

    private final List<String> validTargetTypes = new ArrayList<>();
    private final Map<String, Object> interfaces = new LinkedHashMap<>();

    RelationshipTypeDef(String name) {
        super(name);
    }

    /** Capability types this relationship may point at. */
    public List<String> validTargetTypes() {
        return validTargetTypes;
    }

    public Map<String, Object> interfaces() {
        return interfaces;
    }
}
