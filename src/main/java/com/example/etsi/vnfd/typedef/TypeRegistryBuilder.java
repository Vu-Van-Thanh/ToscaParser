package com.example.etsi.vnfd.typedef;

import com.example.etsi.vnfd.utils.Yamls;
import java.util.List;
import java.util.Map;

/**
 * Populates a {@link TypeRegistry} from the type sections of TOSCA documents.
 *
 * <p>Documents are added in precedence order, lowest first: the built-in ETSI catalogue, then
 * anything the package imports, then the package's own definitions. A later definition of the same
 * type name replaces an earlier one, so a package that ships its own copy of the ETSI types is
 * parsed according to that copy.
 */
public final class TypeRegistryBuilder {

    private final TypeRegistry registry = new TypeRegistry();

    /**
     * Reads every type section of one loaded document.
     *
     * @param declaredIn where the document came from, recorded on each type for traceability
     */
    public TypeRegistryBuilder add(Map<String, Object> document, String declaredIn) {
        readNodeTypes(Yamls.map(document.get("node_types")), declaredIn);
        readDataTypes(Yamls.map(document.get("data_types")), declaredIn);
        readArtifactTypes(Yamls.map(document.get("artifact_types")), declaredIn);
        readCapabilityTypes(Yamls.map(document.get("capability_types")), declaredIn);
        readRelationshipTypes(Yamls.map(document.get("relationship_types")), declaredIn);
        readInterfaceTypes(Yamls.map(document.get("interface_types")), declaredIn);
        readPolicyTypes(Yamls.map(document.get("policy_types")), declaredIn);
        readGroupTypes(Yamls.map(document.get("group_types")), declaredIn);
        return this;
    }

    public TypeRegistry build() {
        return registry;
    }

    private void readNodeTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            NodeTypeDef type = new NodeTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            type.attributes().putAll(TypeDefReader.properties(body.get("attributes")));
            type.requirements().addAll(TypeDefReader.requirements(body.get("requirements")));
            type.capabilities().putAll(TypeDefReader.capabilities(body.get("capabilities")));
            type.interfaces().putAll(Yamls.map(body.get("interfaces")));
            type.artifacts().putAll(Yamls.map(body.get("artifacts")));
            registry.put(type);
        }
    }

    private void readDataTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            DataTypeDef type = new DataTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            for (Object entry : Yamls.list(body.get("constraints"))) {
                for (Map.Entry<String, Object> c : Yamls.map(entry).entrySet()) {
                    type.constraints().add(Constraint.of(c.getKey(), c.getValue()));
                }
            }
            registry.put(type);
        }
    }

    private void readArtifactTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            ArtifactTypeDef type = new ArtifactTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.setMimeType(Yamls.string(body.get("mime_type")));
            type.fileExt().addAll(Yamls.stringList(body.get("file_ext")));
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            registry.put(type);
        }
    }

    private void readCapabilityTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            CapabilityTypeDef type = new CapabilityTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            type.attributes().putAll(TypeDefReader.properties(body.get("attributes")));
            type.validSourceTypes().addAll(Yamls.stringList(body.get("valid_source_types")));
            registry.put(type);
        }
    }

    private void readRelationshipTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            RelationshipTypeDef type = new RelationshipTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            type.validTargetTypes().addAll(Yamls.stringList(body.get("valid_target_types")));
            type.interfaces().putAll(Yamls.map(body.get("interfaces")));
            registry.put(type);
        }
    }

    private void readInterfaceTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            InterfaceTypeDef type = new InterfaceTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.inputs().putAll(Yamls.map(body.get("inputs")));
            type.operations().putAll(Yamls.map(body.get("operations")));
            type.notifications().putAll(Yamls.map(body.get("notifications")));
            registry.put(type);
        }
    }

    private void readPolicyTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            PolicyTypeDef type = new PolicyTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            type.targets().addAll(Yamls.stringList(body.get("targets")));
            type.triggers().putAll(Yamls.map(body.get("triggers")));
            registry.put(type);
        }
    }

    private void readGroupTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            GroupTypeDef type = new GroupTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(TypeDefReader.properties(body.get("properties")));
            type.members().addAll(Yamls.stringList(body.get("members")));
            registry.put(type);
        }
    }

    private static void common(AbstractTypeDef type, Map<String, Object> body, String declaredIn) {
        type.setDerivedFrom(Yamls.string(body.get("derived_from")));
        type.setDescription(Yamls.string(body.get("description")));
        type.setVersion(Yamls.string(body.get("version")));
        type.metadata().putAll(Yamls.map(body.get("metadata")));
        type.setDeclaredIn(declaredIn);
    }

    /** Type sections recognised in a TOSCA document. */
    public static List<String> typeSectionNames() {
        return java.util.Arrays.asList("node_types", "data_types", "artifact_types",
                "capability_types", "relationship_types", "interface_types", "policy_types",
                "group_types");
    }
}
