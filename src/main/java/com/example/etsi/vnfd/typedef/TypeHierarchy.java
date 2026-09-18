package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Walks {@code derived_from} chains.
 *
 * <p>Every decision about what a node template <em>is</em> goes through here rather than comparing
 * type names, because a VNFD names its own types. In all three bundled packages the VNF node is
 * declared as {@code ExampleCorp.SimpleWebCnf.1_0} or similar, and only a walk up to
 * {@code tosca.nodes.nfv.VNF} identifies it. Matching on the literal string would find nothing, and
 * a reader that then falls back on position - "the VNF node is the first node template" - breaks as
 * soon as a descriptor lists its nodes in another order.
 */
public final class TypeHierarchy {

    /** Guards against a malformed descriptor whose types derive from each other in a cycle. */
    private static final int MAX_DEPTH = 64;

    private final TypeRegistry registry;

    public TypeHierarchy(TypeRegistry registry) {
        this.registry = registry;
    }

    /**
     * Whether {@code typeName} is {@code ancestorName} or derives from it, directly or not.
     *
     * @return false when the chain cannot be followed because a type is unknown, which is the safe
     *     answer: an unresolvable type is not evidence of being anything in particular
     */
    public boolean isDerivedFrom(String typeName, String ancestorName) {
        if (typeName == null || ancestorName == null) {
            return false;
        }
        if (typeName.equals(ancestorName)) {
            return true;
        }
        return ancestry(typeName).contains(ancestorName);
    }

    /**
     * The chain from {@code typeName} up to the root, the type itself first.
     * Stops at the first unknown type, so a partially resolvable chain still yields what is known.
     */
    public List<String> ancestry(String typeName) {
        List<String> chain = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        String current = typeName;
        int depth = 0;
        while (current != null && depth++ < MAX_DEPTH) {
            if (!seen.add(current)) {
                break;
            }
            chain.add(current);
            Optional<AbstractTypeDef> type = registry.anyType(current);
            if (!type.isPresent()) {
                break;
            }
            current = type.get().derivedFrom().orElse(null);
        }
        return chain;
    }

    /** The nearest ancestor that is one of {@code candidates}, if any. */
    public Optional<String> nearestAncestorAmong(String typeName, java.util.Collection<String> candidates) {
        for (String ancestor : ancestry(typeName)) {
            if (candidates.contains(ancestor)) {
                return Optional.of(ancestor);
            }
        }
        return Optional.empty();
    }

    /**
     * All properties visible on a node type, inherited ones included.
     *
     * <p>Resolved from the root downwards so a derived type overrides its parent - which is how a
     * VNF-specific node type supplies defaults for {@code descriptor_id} and the rest.
     */
    public Map<String, PropertyDef> effectiveProperties(String nodeTypeName) {
        Map<String, PropertyDef> effective = new LinkedHashMap<>();
        List<String> chain = ancestry(nodeTypeName);
        for (int i = chain.size() - 1; i >= 0; i--) {
            registry.nodeType(chain.get(i)).ifPresent(type -> merge(effective, type.properties()));
        }
        return effective;
    }

    /** A single property declaration visible on a node type, inherited ones included. */
    public Optional<PropertyDef> propertyDef(String nodeTypeName, String propertyName) {
        return Optional.ofNullable(effectiveProperties(nodeTypeName).get(propertyName));
    }

    /** All requirement declarations visible on a node type, inherited ones included. */
    public List<RequirementDefinition> effectiveRequirements(String nodeTypeName) {
        Map<String, RequirementDefinition> effective = new LinkedHashMap<>();
        List<String> chain = ancestry(nodeTypeName);
        for (int i = chain.size() - 1; i >= 0; i--) {
            registry.nodeType(chain.get(i)).ifPresent(type -> {
                for (RequirementDefinition requirement : type.requirements()) {
                    effective.put(requirement.name(), requirement);
                }
            });
        }
        return new ArrayList<>(effective.values());
    }

    /** A single requirement declaration visible on a node type. */
    public Optional<RequirementDefinition> requirementDef(String nodeTypeName, String requirementName) {
        return effectiveRequirements(nodeTypeName).stream()
                .filter(r -> r.name().equals(requirementName))
                .findFirst();
    }

    /** All capability declarations visible on a node type, inherited ones included. */
    public Map<String, CapabilityDefinition> effectiveCapabilities(String nodeTypeName) {
        Map<String, CapabilityDefinition> effective = new LinkedHashMap<>();
        List<String> chain = ancestry(nodeTypeName);
        for (int i = chain.size() - 1; i >= 0; i--) {
            registry.nodeType(chain.get(i)).ifPresent(type -> effective.putAll(type.capabilities()));
        }
        return effective;
    }

    /**
     * All properties visible on a type of any category, inherited ones included.
     *
     * <p>Node types are not the only ones that declare properties: artifact types do too, which is
     * where {@code tosca.artifacts.nfv.SwImage} keeps {@code name}, {@code version} and
     * {@code checksum}, and where {@code HelmParamMappingScript} keeps {@code language}.
     */
    public Map<String, PropertyDef> effectivePropertiesOfAnyType(String typeName) {
        Map<String, PropertyDef> effective = new LinkedHashMap<>();
        List<String> chain = ancestry(typeName);
        for (int i = chain.size() - 1; i >= 0; i--) {
            registry.anyType(chain.get(i)).ifPresent(type -> merge(effective, type.properties()));
        }
        return effective;
    }

    /** A single property declaration visible on a type of any category. */
    public Optional<PropertyDef> anyPropertyDef(String typeName, String propertyName) {
        return Optional.ofNullable(effectivePropertiesOfAnyType(typeName).get(propertyName));
    }

    public TypeRegistry registry() {
        return registry;
    }

    /**
     * Lays a type's own property declarations over what it inherited.
     *
     * <p>Refinement rather than replacement - see {@link PropertyDef#refining}. Walking the chain
     * root-first and merging at each step is what lets a VNF-specific node type supply a default
     * without discarding the constraints the ETSI type declares for the same property.
     */
    private static void merge(Map<String, PropertyDef> effective, Map<String, PropertyDef> declared) {
        for (Map.Entry<String, PropertyDef> e : declared.entrySet()) {
            effective.put(e.getKey(), e.getValue().refining(effective.get(e.getKey())));
        }
    }
}
