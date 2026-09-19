package com.example.etsi.vnfd.typedef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * All TOSCA types visible to one VNF package: those it declares and those it imports.
 *
 * <p>[PROJECT-SPECIFIC] Nothing else. SOL001 V5.4.1 Annex B.2 NOTE 2 permits a package to reference
 * the ETSI type definitions file without shipping it, but this parser reads imports only from
 * inside the package, so the definitions have to be there. A package that references them and does
 * not ship them dead-ends the {@code derived_from} chain of every ETSI type and classifies nothing;
 * that outcome is reported rather than left silent - see {@code YAML01} and {@code TYPE01}.
 *
 * <p>A type declared by a file wins over one of the same name in a file it imports.
 */
public final class TypeRegistry {

    private final Map<String, NodeTypeDef> nodeTypes = new LinkedHashMap<>();
    private final Map<String, DataTypeDef> dataTypes = new LinkedHashMap<>();
    private final Map<String, ArtifactTypeDef> artifactTypes = new LinkedHashMap<>();
    private final Map<String, CapabilityTypeDef> capabilityTypes = new LinkedHashMap<>();
    private final Map<String, RelationshipTypeDef> relationshipTypes = new LinkedHashMap<>();
    private final Map<String, InterfaceTypeDef> interfaceTypes = new LinkedHashMap<>();
    private final Map<String, PolicyTypeDef> policyTypes = new LinkedHashMap<>();
    private final Map<String, GroupTypeDef> groupTypes = new LinkedHashMap<>();

    public Optional<NodeTypeDef> nodeType(String name) {
        return Optional.ofNullable(nodeTypes.get(name));
    }

    public Optional<DataTypeDef> dataType(String name) {
        return Optional.ofNullable(dataTypes.get(name));
    }

    public Optional<ArtifactTypeDef> artifactType(String name) {
        return Optional.ofNullable(artifactTypes.get(name));
    }

    public Optional<CapabilityTypeDef> capabilityType(String name) {
        return Optional.ofNullable(capabilityTypes.get(name));
    }

    public Optional<RelationshipTypeDef> relationshipType(String name) {
        return Optional.ofNullable(relationshipTypes.get(name));
    }

    public Optional<InterfaceTypeDef> interfaceType(String name) {
        return Optional.ofNullable(interfaceTypes.get(name));
    }

    public Optional<PolicyTypeDef> policyType(String name) {
        return Optional.ofNullable(policyTypes.get(name));
    }

    public Optional<GroupTypeDef> groupType(String name) {
        return Optional.ofNullable(groupTypes.get(name));
    }

    public Map<String, NodeTypeDef> nodeTypes() {
        return Collections.unmodifiableMap(nodeTypes);
    }

    public Map<String, DataTypeDef> dataTypes() {
        return Collections.unmodifiableMap(dataTypes);
    }

    public Map<String, ArtifactTypeDef> artifactTypes() {
        return Collections.unmodifiableMap(artifactTypes);
    }

    public Map<String, PolicyTypeDef> policyTypes() {
        return Collections.unmodifiableMap(policyTypes);
    }

    public Map<String, GroupTypeDef> groupTypes() {
        return Collections.unmodifiableMap(groupTypes);
    }

    public Map<String, CapabilityTypeDef> capabilityTypes() {
        return Collections.unmodifiableMap(capabilityTypes);
    }

    public Map<String, RelationshipTypeDef> relationshipTypes() {
        return Collections.unmodifiableMap(relationshipTypes);
    }

    public Map<String, InterfaceTypeDef> interfaceTypes() {
        return Collections.unmodifiableMap(interfaceTypes);
    }

    /** Any type definition with this name, whichever section declared it. */
    public Optional<AbstractTypeDef> anyType(String name) {
        AbstractTypeDef found = nodeTypes.get(name);
        if (found == null) {
            found = dataTypes.get(name);
        }
        if (found == null) {
            found = artifactTypes.get(name);
        }
        if (found == null) {
            found = capabilityTypes.get(name);
        }
        if (found == null) {
            found = relationshipTypes.get(name);
        }
        if (found == null) {
            found = interfaceTypes.get(name);
        }
        if (found == null) {
            found = policyTypes.get(name);
        }
        if (found == null) {
            found = groupTypes.get(name);
        }
        return Optional.ofNullable(found);
    }

    /** Total number of registered types, across all sections. */
    public int size() {
        return nodeTypes.size() + dataTypes.size() + artifactTypes.size() + capabilityTypes.size()
                + relationshipTypes.size() + interfaceTypes.size() + policyTypes.size()
                + groupTypes.size();
    }

    public void put(NodeTypeDef type) {
        nodeTypes.put(type.name(), type);
    }

    public void put(DataTypeDef type) {
        dataTypes.put(type.name(), type);
    }

    public void put(ArtifactTypeDef type) {
        artifactTypes.put(type.name(), type);
    }

    public void put(CapabilityTypeDef type) {
        capabilityTypes.put(type.name(), type);
    }

    public void put(RelationshipTypeDef type) {
        relationshipTypes.put(type.name(), type);
    }

    public void put(InterfaceTypeDef type) {
        interfaceTypes.put(type.name(), type);
    }

    public void put(PolicyTypeDef type) {
        policyTypes.put(type.name(), type);
    }

    public void put(GroupTypeDef type) {
        groupTypes.put(type.name(), type);
    }

    @Override
    public String toString() {
        return "TypeRegistry(" + size() + " types)";
    }
}
