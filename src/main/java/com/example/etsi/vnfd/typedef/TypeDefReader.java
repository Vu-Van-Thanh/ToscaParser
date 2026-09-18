package com.example.etsi.vnfd.typedef;

import com.example.etsi.vnfd.utils.Yamls;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the blocks a TOSCA type definition is made of: {@code properties}, {@code attributes},
 * {@code capabilities}, {@code requirements}.
 *
 * <p>One class because the four share the shape that matters. Every block admits a shorthand where
 * a plain scalar stands for the whole declaration - {@code descriptor_id: 3b7d5e2a-...} assigns a
 * default, {@code virtual_binding: tosca.capabilities.nfv.VirtualBindable} names a type,
 * {@code - dependency: tosca.capabilities.Node} names a capability - and each reader has to decide
 * the same way whether it is looking at a mapping or at that shorthand. Split across three files
 * the decision drifted; here it is one idiom repeated four times.
 *
 * <p>Called only by {@link TypeRegistryBuilder}.
 */
final class TypeDefReader {

    private TypeDefReader() {
    }

    // ---------------------------------------------------------------- properties and attributes

    static Map<String, PropertyDef> properties(Object block) {
        Map<String, PropertyDef> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), property(e.getKey(), e.getValue()));
        }
        return out;
    }

    static PropertyDef property(String name, Object body) {
        PropertyDef def = new PropertyDef(name);
        Map<String, Object> map = Yamls.map(body);
        if (map.isEmpty() && body != null) {
            // Shorthand form: "descriptor_id: 3b7d5e2a-..." assigns a default rather than declaring
            // a schema. SOL001 Annex A.23 writes VNF node type properties this way.
            def.setDefaultValue(body);
            return def;
        }
        def.setType(Yamls.string(map.get("type")));
        def.setDescription(Yamls.string(map.get("description")));
        def.setRequired(Yamls.bool(map.get("required")));
        def.setStatus(Yamls.string(map.get("status")));
        if (map.containsKey("default")) {
            def.setDefaultValue(map.get("default"));
        }
        def.metadata().putAll(Yamls.map(map.get("metadata")));
        constraints(def, map.get("constraints"));
        if (map.containsKey("entry_schema")) {
            def.setEntrySchema(property(name + "[]", map.get("entry_schema")));
        }
        if (map.containsKey("key_schema")) {
            def.setKeySchema(property(name + "{}", map.get("key_schema")));
        }
        return def;
    }

    /**
     * Constraints are a sequence of single-entry maps:
     * {@code constraints: [ { valid_values: [ bash, python ] } ]}. The inline form
     * {@code constraints: [ equal: X ]} parses to the same shape.
     */
    static void constraints(PropertyDef def, Object block) {
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> c : Yamls.map(entry).entrySet()) {
                def.constraints().add(Constraint.of(c.getKey(), c.getValue()));
            }
        }
    }

    // ------------------------------------------------------------------------------ capabilities

    static Map<String, CapabilityDefinition> capabilities(Object block) {
        Map<String, CapabilityDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), capability(e.getKey(), e.getValue()));
        }
        return out;
    }

    private static CapabilityDefinition capability(String name, Object body) {
        CapabilityDefinition def = new CapabilityDefinition(name);
        Map<String, Object> map = Yamls.map(body);
        if (map.isEmpty()) {
            // Shorthand: "virtual_binding: tosca.capabilities.nfv.VirtualBindable".
            def.setType(Yamls.string(body));
            return def;
        }
        def.setType(Yamls.string(map.get("type")));
        def.setDescription(Yamls.string(map.get("description")));
        def.properties().putAll(properties(map.get("properties")));
        def.attributes().putAll(properties(map.get("attributes")));
        def.validSourceTypes().addAll(Yamls.stringList(map.get("valid_source_types")));
        if (map.containsKey("occurrences")) {
            def.setOccurrences(Yamls.list(map.get("occurrences")));
        }
        return def;
    }

    // ------------------------------------------------------------------------------ requirements

    /**
     * A sequence of single-entry maps, so the same requirement name may legitimately appear more
     * than once and order is preserved.
     */
    static List<RequirementDefinition> requirements(Object block) {
        List<RequirementDefinition> out = new ArrayList<>();
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                out.add(requirement(e.getKey(), e.getValue()));
            }
        }
        return out;
    }

    private static RequirementDefinition requirement(String name, Object body) {
        RequirementDefinition def = new RequirementDefinition(name);
        Map<String, Object> map = Yamls.map(body);
        if (map.isEmpty()) {
            // Shorthand: "- dependency: tosca.capabilities.Node" names the capability only.
            def.setCapability(Yamls.string(body));
            return def;
        }
        def.setCapability(Yamls.string(map.get("capability")));
        def.setNode(Yamls.string(map.get("node")));
        def.setRelationship(relationship(map.get("relationship")));
        if (map.containsKey("occurrences")) {
            def.setOccurrences(Yamls.list(map.get("occurrences")));
        }
        return def;
    }

    /** {@code relationship} may be a type name or a map carrying {@code type}. */
    private static String relationship(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = Yamls.map(value);
        return map.isEmpty() ? Yamls.string(value) : Yamls.string(map.get("type"));
    }
}
