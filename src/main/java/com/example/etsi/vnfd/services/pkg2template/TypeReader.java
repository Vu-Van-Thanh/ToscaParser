package com.example.etsi.vnfd.services.pkg2template;

import com.example.etsi.vnfd.typedef.AbstractTypeDef;
import com.example.etsi.vnfd.typedef.ArtifactTypeDef;
import com.example.etsi.vnfd.typedef.CapabilityDefinition;
import com.example.etsi.vnfd.typedef.CapabilityTypeDef;
import com.example.etsi.vnfd.typedef.Constraint;
import com.example.etsi.vnfd.typedef.ConstraintKind;
import com.example.etsi.vnfd.typedef.DataTypeDef;
import com.example.etsi.vnfd.typedef.GroupTypeDef;
import com.example.etsi.vnfd.typedef.InterfaceTypeDef;
import com.example.etsi.vnfd.typedef.NodeTypeDef;
import com.example.etsi.vnfd.typedef.PolicyTypeDef;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.RelationshipTypeDef;
import com.example.etsi.vnfd.typedef.RequirementDefinition;
import com.example.etsi.vnfd.typedef.TypeRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Reads TOSCA type definitions, and answers questions about them.
 *
 * <p>Two halves of one subject. Building the registry - from the package files and from the ETSI
 * catalogue shipped on the classpath - and then walking {@code derived_from} to work out what a
 * type effectively declares once inheritance is applied. Nothing downstream classifies a node by
 * comparing type names, because SOL001 V5.4.1 clause 6.11.2 requires a VNF node type to be
 * VNF-specific, so the walk is the only correct answer to "what kind of node is this".
 */
public final class TypeReader {

    // ============================================================================================
    // BUILDING THE TYPE REGISTRY
    // ============================================================================================

    // Populates a {@link TypeRegistry} from the type sections of TOSCA documents.
    //
    // <p>Documents are added in precedence order, lowest first: the built-in ETSI catalogue, then
    // anything the package imports, then the package's own definitions. A later definition of the same
    // type name replaces an earlier one, so a package that ships its own copy of the ETSI types is
    // parsed according to that copy.


    private final TypeRegistry registry = new TypeRegistry();

    /**
     * Reads every type section of one loaded document.
     *
     * @param declaredIn where the document came from, recorded on each type for traceability
     */
    public TypeReader add(Map<String, Object> document, String declaredIn) {
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
            type.properties().putAll(properties(body.get("properties")));
            type.attributes().putAll(properties(body.get("attributes")));
            type.requirements().addAll(requirements(body.get("requirements")));
            type.capabilities().putAll(capabilities(body.get("capabilities")));
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
            type.properties().putAll(properties(body.get("properties")));
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
            type.properties().putAll(properties(body.get("properties")));
            registry.put(type);
        }
    }

    private void readCapabilityTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            CapabilityTypeDef type = new CapabilityTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(properties(body.get("properties")));
            type.attributes().putAll(properties(body.get("attributes")));
            type.validSourceTypes().addAll(Yamls.stringList(body.get("valid_source_types")));
            registry.put(type);
        }
    }

    private void readRelationshipTypes(Map<String, Object> section, String declaredIn) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            Map<String, Object> body = Yamls.map(e.getValue());
            RelationshipTypeDef type = new RelationshipTypeDef(e.getKey());
            common(type, body, declaredIn);
            type.properties().putAll(properties(body.get("properties")));
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
            type.properties().putAll(properties(body.get("properties")));
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
            type.properties().putAll(properties(body.get("properties")));
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
    // ============================================================================================
    // THE BLOCKS A TYPE DEFINITION IS MADE OF
    // ============================================================================================

    // Reads the blocks a TOSCA type definition is made of: {@code properties}, {@code attributes},
    // {@code capabilities}, {@code requirements}.
    //
    // <p>One class because the four share the shape that matters. Every block admits a shorthand where
    // a plain scalar stands for the whole declaration - {@code descriptor_id: 3b7d5e2a-...} assigns a
    // default, {@code virtual_binding: tosca.capabilities.nfv.VirtualBindable} names a type,
    // {@code - dependency: tosca.capabilities.Node} names a capability - and each reader has to decide
    // the same way whether it is looking at a mapping or at that shorthand. Split across three files
    // the decision drifted; here it is one idiom repeated four times.
    //
    // <p>Called only by {@link TypeReader}.


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
    // ============================================================================================
    // THE BUILT-IN ETSI TYPE CATALOGUE
    // ============================================================================================

    // The official SOL001 V5.4.1 type definitions, bundled on the classpath.
    //
    // <p>Needed because SOL001 V5.4.1 Annex B.2 NOTE 2 states that the type definitions file "may, but
    // need not, be included in the VNF Package". Packages routinely reference it without shipping it:
    // all three bundled examples import {@code etsi_nfv_sol001_vnfd_types.yaml} and none contains it.
    // Without a built-in copy, {@code tosca.nodes.nfv.VduCp} would have no resolvable parent and no
    // connection point could be recognised.
    //
    // <p>The common types file is loaded first because the VNFD types file imports it, and because
    // {@code tosca.nodes.nfv.Cp} - the parent of all five connection point types - lives there.


    private static final String BASE = "/etsi/sol001/v5.4.1/";
    private static final String COMMON_TYPES = "etsi_nfv_sol001_common_types.yaml";
    private static final String VNFD_TYPES = "etsi_nfv_sol001_vnfd_types.yaml";

    /** Loaded once: the files are a few hundred kilobytes and never change at runtime. */
    private static volatile Map<String, Map<String, Object>> cachedDocuments;

    /** File names of the bundled definitions, in the order they must be loaded. */
    public static List<String> fileNames() {
        return Arrays.asList(COMMON_TYPES, VNFD_TYPES);
    }

    /** The bundled documents, keyed by file name, in load order. */
    public static Map<String, Map<String, Object>> documents() {
        Map<String, Map<String, Object>> local = cachedDocuments;
        if (local == null) {
            synchronized (TypeReader.class) {
                local = cachedDocuments;
                if (local == null) {
                    local = loadAll();
                    cachedDocuments = local;
                }
            }
        }
        return local;
    }

    /** Registers the bundled definitions into a builder, lowest precedence first. */
    public static void addCatalogue(TypeReader builder) {
        for (Map.Entry<String, Map<String, Object>> e : documents().entrySet()) {
            builder.add(e.getValue(), "built-in:" + e.getKey());
        }
    }

    /** Whether a file name refers to one of the bundled ETSI definition files. */
    public static boolean isCatalogueFile(String reference) {
        if (reference == null) {
            return false;
        }
        String leaf = reference.substring(reference.lastIndexOf('/') + 1);
        return COMMON_TYPES.equals(leaf) || VNFD_TYPES.equals(leaf);
    }

    private static Map<String, Map<String, Object>> loadAll() {
        Map<String, Map<String, Object>> documents = new LinkedHashMap<>();
        for (String fileName : fileNames()) {
            documents.put(fileName, load(fileName));
        }
        return Collections.unmodifiableMap(documents);
    }

    /**
     * Loads one bundled file through the same path as a file from the package.
     *
     * <p>Shared deliberately. The catalogue used to build its own {@code Yaml} and omitted
     * {@code allowDuplicateKeys(false)}, so a duplicate key in the ETSI type definitions would have
     * been silently resolved to the last occurrence here while being rejected in a descriptor.
     * One load path means one set of rules.
     */
    private static Map<String, Object> load(String fileName) {
        String resource = BASE + fileName;
        try (InputStream in = TypeReader.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Bundled ETSI type definitions are missing from the classpath: " + resource);
            }
            return PackageReader.loadMapping(resource, readAll(in));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
    // ============================================================================================
    // WALKING derived_from
    // ============================================================================================

    // Walks {@code derived_from} chains.
    //
    // <p>Every decision about what a node template <em>is</em> goes through here rather than comparing
    // type names, because a VNFD names its own types. In all three bundled packages the VNF node is
    // declared as {@code ExampleCorp.SimpleWebCnf.1_0} or similar, and only a walk up to
    // {@code tosca.nodes.nfv.VNF} identifies it. Matching on the literal string would find nothing, and
    // a reader that then falls back on position - "the VNF node is the first node template" - breaks as
    // soon as a descriptor lists its nodes in another order.

    public static final class Hierarchy {

        /** Guards against a malformed descriptor whose types derive from each other in a cycle. */
        private static final int MAX_DEPTH = 64;

        private final TypeRegistry registry;

        public Hierarchy(TypeRegistry registry) {
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

    // ============================================================================================
    // CHECKING A VALUE AGAINST A CONSTRAINT
    // ============================================================================================

    // Checks assigned values against the constraints a type declares.
    //
    // <p>These are worth evaluating rather than merely recording. All three bundled example packages
    // violate a constraint that SOL001 states in the official type definitions file: every one writes
    // {@code vnfm_info: [ GenericVnfm ]}, which does not match the pattern declared on
    // {@code tosca.nodes.nfv.VNF.vnfm_info}.
    //
    // <p>A violation never stops the parse. The value is mapped as written and the mismatch is
    // reported, because the library describes what a descriptor says, it does not correct it.


    /**
     * Checks a value against this constraint.
     *
     * @return empty when the value satisfies the constraint, otherwise a description of the
     *     mismatch suitable for a finding message. An unknown operator never fails, since failing
     *     on an operator this library does not implement would reject valid descriptors.
     */
    public static Optional<String> validate(Constraint constraint, Object candidate) {
        ConstraintKind kind = constraint.kind();
        Object value = constraint.value();
        if (candidate == null) {
            return Optional.empty();
        }
        switch (kind) {
            case EQUAL:
                return equalsValue(candidate, value) ? Optional.empty()
                        : Optional.of("expected " + value + " but was " + candidate);
            case VALID_VALUES:
                return validateValidValues(value, candidate);
            case PATTERN:
                return validatePattern(value, candidate);
            case MIN_LENGTH:
                return validateLength(value, candidate, true);
            case MAX_LENGTH:
                return validateLength(value, candidate, false);
            case GREATER_THAN:
                return compareNumeric(value, candidate, c -> c > 0, "greater than");
            case GREATER_OR_EQUAL:
                return compareNumeric(value, candidate, c -> c >= 0, "greater than or equal to");
            case LESS_THAN:
                return compareNumeric(value, candidate, c -> c < 0, "less than");
            case LESS_OR_EQUAL:
                return compareNumeric(value, candidate, c -> c <= 0, "less than or equal to");
            default:
                return Optional.empty();
        }
    }

    private static Optional<String> validateValidValues(Object value, Object candidate) {
        if (!(value instanceof Collection)) {
            return Optional.empty();
        }
        Collection<?> allowed = (Collection<?>) value;
        if (candidate instanceof Collection) {
            for (Object element : (Collection<?>) candidate) {
                if (allowed.stream().noneMatch(a -> equalsValue(element, a))) {
                    return Optional.of("value " + element + " is not one of " + allowed);
                }
            }
            return Optional.empty();
        }
        return allowed.stream().anyMatch(a -> equalsValue(candidate, a)) ? Optional.empty()
                : Optional.of("value " + candidate + " is not one of " + allowed);
    }

    private static Optional<String> validatePattern(Object value, Object candidate) {
        if (value == null) {
            return Optional.empty();
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(String.valueOf(value));
        } catch (PatternSyntaxException e) {
            return Optional.empty();
        }
        if (candidate instanceof Collection) {
            for (Object element : (Collection<?>) candidate) {
                if (!pattern.matcher(String.valueOf(element)).matches()) {
                    return Optional.of("value " + element + " does not match pattern " + value);
                }
            }
            return Optional.empty();
        }
        return pattern.matcher(String.valueOf(candidate)).matches() ? Optional.empty()
                : Optional.of("value " + candidate + " does not match pattern " + value);
    }

    private static Optional<String> validateLength(Object value, Object candidate, boolean minimum) {
        Optional<BigDecimal> bound = toNumber(value);
        if (!bound.isPresent()) {
            return Optional.empty();
        }
        int actual;
        if (candidate instanceof Collection) {
            actual = ((Collection<?>) candidate).size();
        } else if (candidate instanceof CharSequence) {
            actual = ((CharSequence) candidate).length();
        } else {
            return Optional.empty();
        }
        int limit = bound.get().intValue();
        if (minimum && actual < limit) {
            return Optional.of("length " + actual + " is below the minimum " + limit);
        }
        if (!minimum && actual > limit) {
            return Optional.of("length " + actual + " exceeds the maximum " + limit);
        }
        return Optional.empty();
    }

    private static Optional<String> compareNumeric(Object value, Object candidate,
                                                   java.util.function.IntPredicate accept,
                                                   String description) {
        Optional<BigDecimal> left = toNumber(candidate);
        Optional<BigDecimal> right = toNumber(value);
        if (!left.isPresent() || !right.isPresent()) {
            return Optional.empty();
        }
        return accept.test(left.get().compareTo(right.get())) ? Optional.empty()
                : Optional.of("value " + candidate + " is not " + description + " " + value);
    }

    private static Optional<BigDecimal> toNumber(Object o) {
        if (o instanceof Number) {
            return Optional.of(new BigDecimal(o.toString()));
        }
        if (o instanceof CharSequence) {
            try {
                return Optional.of(new BigDecimal(o.toString().trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean equalsValue(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        Optional<BigDecimal> na = toNumber(a);
        Optional<BigDecimal> nb = toNumber(b);
        if (na.isPresent() && nb.isPresent()) {
            return na.get().compareTo(nb.get()) == 0;
        }
        return String.valueOf(a).equals(String.valueOf(b));
    }
}
