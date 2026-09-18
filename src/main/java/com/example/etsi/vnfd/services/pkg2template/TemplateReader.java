package com.example.etsi.vnfd.services.pkg2template;

import com.example.etsi.vnfd.template.ActivityDefinition;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.CapabilityAssignment;
import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.ImplementationDefinition;
import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.NodeFilter;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.NotificationAssignment;
import com.example.etsi.vnfd.template.OperationAssignment;
import com.example.etsi.vnfd.template.ParameterDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.PropertyFilter;
import com.example.etsi.vnfd.template.RelationshipAssignment;
import com.example.etsi.vnfd.template.RelationshipTemplate;
import com.example.etsi.vnfd.template.RepositoryDefinition;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.SubstitutionMappings;
import com.example.etsi.vnfd.template.SubstitutionTarget;
import com.example.etsi.vnfd.template.TriggerDefinition;
import com.example.etsi.vnfd.template.value.FunctionCall;
import com.example.etsi.vnfd.template.value.FunctionName;
import com.example.etsi.vnfd.template.value.Literal;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.template.value.Resolution;
import com.example.etsi.vnfd.template.value.SizeUnit;
import com.example.etsi.vnfd.template.value.Kind;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a TOSCA service template document into the DOM.
 *
 * <p>Everything that turns loaded YAML into {@code template/} objects: the topology and its node
 * templates, interfaces, policies and substitution mappings, plus the property values inside them.
 *
 * <p>Kept out of {@code template/} itself so those DOM classes can keep package-private setters -
 * only this reader populates them, and nothing else can mutate a parsed template by accident.
 */
public final class TemplateReader {

    private TemplateReader() {
    }

    // ============================================================================================
    // THE topology_template AS A WHOLE
    // ============================================================================================

    // Reads the pieces of a {@code topology_template} that live in this package.
    //
    // <p>Kept here rather than beside {@link com.example.etsi.vnfd.template.TopologyTemplate} so the DOM
    // classes can keep package-private setters: only the readers populate them, and nothing outside
    // this package can mutate a parsed template by accident.


    public static Map<String, GroupDefinition> readGroups(Object block, String file) {
        Map<String, GroupDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            GroupDefinition group = new GroupDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            group.setSource(SourceRef.of(file, e.getKey()));
            group.setType(Yamls.string(body.get("type")));
            group.setDescription(Yamls.string(body.get("description")));
            group.metadata().putAll(Yamls.map(body.get("metadata")));
            group.properties().putAll(Yamls.map(body.get("properties")));
            group.members().addAll(Yamls.stringList(body.get("members")));
            out.put(e.getKey(), group);
        }
        return out;
    }

    public static Map<String, RelationshipTemplate> readRelationshipTemplates(Object block, String file) {
        Map<String, RelationshipTemplate> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            RelationshipTemplate template = new RelationshipTemplate(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            template.setSource(SourceRef.of(file, e.getKey()));
            template.setType(Yamls.string(body.get("type")));
            template.setDescription(Yamls.string(body.get("description")));
            template.properties().putAll(Yamls.map(body.get("properties")));
            template.attributes().putAll(Yamls.map(body.get("attributes")));
            template.interfaces().putAll(readInterfaces(body.get("interfaces")));
            out.put(e.getKey(), template);
        }
        return out;
    }

    public static Map<String, ParameterDefinition> readParameters(Object block) {
        Map<String, ParameterDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            ParameterDefinition parameter = new ParameterDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                parameter.setValue(e.getValue());
            } else {
                parameter.setType(Yamls.string(body.get("type")));
                parameter.setDescription(Yamls.string(body.get("description")));
                parameter.setRequired(Yamls.bool(body.get("required")));
                parameter.setStatus(Yamls.string(body.get("status")));
                if (body.containsKey("default")) {
                    parameter.setDefaultValue(body.get("default"));
                }
                if (body.containsKey("value")) {
                    parameter.setValue(body.get("value"));
                }
                if (body.containsKey("entry_schema")) {
                    parameter.setEntrySchema(body.get("entry_schema"));
                }
                parameter.constraints().addAll(Yamls.list(body.get("constraints")));
            }
            out.put(e.getKey(), parameter);
        }
        return out;
    }

    public static Map<String, RepositoryDefinition> readRepositories(Object block) {
        Map<String, RepositoryDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            RepositoryDefinition repository = new RepositoryDefinition(e.getKey());
            if (Yamls.isMap(e.getValue())) {
                Map<String, Object> body = Yamls.map(e.getValue());
                repository.setUrl(Yamls.string(body.get("url")));
                repository.setDescription(Yamls.string(body.get("description")));
            } else {
                repository.setUrl(Yamls.string(e.getValue()));
            }
            out.put(e.getKey(), repository);
        }
        return out;
    }

    /** Resolves artifact file references against the package root. */
    public static void resolveArtifactPaths(NodeTemplate node, java.util.function.UnaryOperator<String> resolver) {
        for (ArtifactDefinition artifact : node.artifacts().values()) {
            if (artifact.file() != null) {
                artifact.setResolvedFile(resolver.apply(artifact.file()));
            }
        }
    }
    // ============================================================================================
    // node_templates
    // ============================================================================================

    //


    public static Map<String, NodeTemplate> readNodeTemplates(Object block, String file) {
        Map<String, NodeTemplate> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), readNodeTemplate(e.getKey(), e.getValue(), file));
        }
        return out;
    }

    private static NodeTemplate readNodeTemplate(String name, Object body, String file) {
        NodeTemplate node = new NodeTemplate(name);
        Map<String, Object> map = Yamls.map(body);
        node.setSource(SourceRef.of(file, name));
        node.setType(Yamls.string(map.get("type")));
        node.setDescription(Yamls.string(map.get("description")));
        node.metadata().putAll(Yamls.map(map.get("metadata")));
        node.properties().putAll(Yamls.map(map.get("properties")));
        node.attributes().putAll(Yamls.map(map.get("attributes")));
        node.setCopy(Yamls.string(map.get("copy")));
        if (map.containsKey("directives")) {
            node.setDirectives(Yamls.stringList(map.get("directives")));
        }
        if (map.containsKey("node_filter")) {
            node.setNodeFilter(readNodeFilter(map.get("node_filter")));
        }
        node.requirements().addAll(readRequirements(map.get("requirements")));
        node.capabilities().putAll(readCapabilities(map.get("capabilities")));
        node.artifacts().putAll(readArtifacts(map.get("artifacts"), file, name));
        node.interfaces().putAll(readInterfaces(map.get("interfaces")));
        return node;
    }

    /**
     * Requirements are a sequence of single-entry maps. Every entry is kept, including repeats of
     * the same name, which SOL001 Annex A.23 relies on for {@code associatedVdu}.
     */
    private static List<RequirementAssignment> readRequirements(Object block) {
        List<RequirementAssignment> out = new java.util.ArrayList<>();
        int index = 0;
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                out.add(readRequirement(e.getKey(), e.getValue(), index++));
            }
        }
        return out;
    }

    private static RequirementAssignment readRequirement(String name, Object body, int index) {
        RequirementAssignment requirement = new RequirementAssignment(name, index);
        if (!Yamls.isMap(body)) {
            // Short form: "- container: WebContainer".
            requirement.setNode(Yamls.string(body));
            return requirement;
        }
        Map<String, Object> map = Yamls.map(body);
        requirement.setNode(Yamls.string(map.get("node")));
        requirement.setCapability(Yamls.string(map.get("capability")));
        if (map.containsKey("relationship")) {
            requirement.setRelationship(readRelationship(map.get("relationship")));
        }
        if (map.containsKey("node_filter")) {
            requirement.setNodeFilter(readNodeFilter(map.get("node_filter")));
        }
        if (map.containsKey("occurrences")) {
            requirement.setOccurrences(Yamls.list(map.get("occurrences")));
        }
        return requirement;
    }

    private static RelationshipAssignment readRelationship(Object value) {
        if (!Yamls.isMap(value)) {
            return new RelationshipAssignment(Yamls.string(value));
        }
        Map<String, Object> map = Yamls.map(value);
        RelationshipAssignment relationship = new RelationshipAssignment(Yamls.string(map.get("type")));
        relationship.properties().putAll(Yamls.map(map.get("properties")));
        relationship.interfaces().putAll(readInterfaces(map.get("interfaces")));
        return relationship;
    }

    private static Map<String, CapabilityAssignment> readCapabilities(Object block) {
        Map<String, CapabilityAssignment> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            CapabilityAssignment capability = new CapabilityAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            capability.properties().putAll(Yamls.map(body.get("properties")));
            capability.attributes().putAll(Yamls.map(body.get("attributes")));
            out.put(e.getKey(), capability);
        }
        return out;
    }

    private static Map<String, ArtifactDefinition> readArtifacts(Object block, String file,
            String owningNode) {
        Map<String, ArtifactDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), readArtifact(e.getKey(), e.getValue(), file, owningNode));
        }
        return out;
    }

    /**
     * One artifact definition.
     *
     * <p>The source reference names the owning node, not just the file: SOL001 speaks of artifacts
     * per node template - "an artifact of type tosca.artifacts.nfv.SwImage" on a
     * {@code Vdu.OsContainer} (clause 6.8.12.6) - so a finding about one has to say which node.
     */
    private static ArtifactDefinition readArtifact(String name, Object body, String file,
            String owningNode) {
        ArtifactDefinition artifact = new ArtifactDefinition(name);
        artifact.setSource(SourceRef.of(file, owningNode + "." + name));
        if (!Yamls.isMap(body)) {
            // Short form: "my_artifact: path/to/file" gives a file with no declared type.
            artifact.setFile(Yamls.string(body));
            return artifact;
        }
        Map<String, Object> map = Yamls.map(body);
        artifact.setType(Yamls.string(map.get("type")));
        artifact.setFile(Yamls.string(map.get("file")));
        artifact.setRepository(Yamls.string(map.get("repository")));
        artifact.setDescription(Yamls.string(map.get("description")));
        artifact.setDeployPath(Yamls.string(map.get("deploy_path")));
        artifact.setArtifactVersion(Yamls.string(map.get("artifact_version")));
        artifact.setChecksum(Yamls.string(map.get("checksum")));
        artifact.setChecksumAlgorithm(Yamls.string(map.get("checksum_algorithm")));
        artifact.properties().putAll(Yamls.map(map.get("properties")));
        return artifact;
    }

    private static NodeFilter readNodeFilter(Object value) {
        NodeFilter filter = new NodeFilter();
        Map<String, Object> map = Yamls.map(value);
        for (Object entry : Yamls.list(map.get("properties"))) {
            filter.propertyConstraints().add(Yamls.map(entry));
        }
        filter.capabilityConstraints().putAll(Yamls.map(map.get("capabilities")));
        return filter;
    }
    // ============================================================================================
    // interfaces
    // ============================================================================================

    // Reads the {@code interfaces} block of a node template.
    //
    // <p>Handles both interface grammars. TOSCA 1.3 nests operations under an {@code operations}
    // keyname and notifications under {@code notifications}; TOSCA 1.2 and earlier place operations
    // directly under the interface. Both occur in real packages because SOL004 V5.1.1 clause 4.1.1
    // permits a CSAR to follow TOSCA Simple Profile YAML v1.1 or v1.3, and its clause 4.1.3.1 example
    // is written as {@code tosca_simple_yaml_1_2}.
    //
    // <p>Which grammar was used is recorded on the assignment rather than guessed at from the document
    // version, since a file may declare one version and be written in the other.


    /** Interface-level keynames that are never operation names. */
    private static final List<String> RESERVED =
            Arrays.asList("type", "inputs", "operations", "notifications", "description", "metadata");

    static Map<String, InterfaceAssignment> readInterfaces(Object block) {
        Map<String, InterfaceAssignment> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), readInterface(e.getKey(), e.getValue()));
        }
        return out;
    }

    private static InterfaceAssignment readInterface(String name, Object body) {
        InterfaceAssignment iface = new InterfaceAssignment(name);
        Map<String, Object> map = Yamls.map(body);
        iface.setType(Yamls.string(map.get("type")));
        iface.inputs().putAll(Yamls.map(map.get("inputs")));

        boolean modernGrammar = map.containsKey("operations") || map.containsKey("notifications");
        iface.setGrammar(modernGrammar ? InterfaceAssignment.Grammar.TOSCA_1_3
                : InterfaceAssignment.Grammar.LEGACY);

        if (modernGrammar) {
            readOperations(iface, Yamls.map(map.get("operations")));
            readNotifications(iface, Yamls.map(map.get("notifications")));
        } else {
            // Legacy form: anything that is not a reserved keyname is an operation.
            Map<String, Object> operations = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!RESERVED.contains(e.getKey())) {
                    operations.put(e.getKey(), e.getValue());
                }
            }
            readOperations(iface, operations);
        }
        return iface;
    }

    private static void readOperations(InterfaceAssignment iface, Map<String, Object> block) {
        for (Map.Entry<String, Object> e : block.entrySet()) {
            OperationAssignment operation = new OperationAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                // Shorthand: "instantiate: my_script.sh" is an implementation, not an input block.
                operation.setImplementation(readImplementation(e.getValue()));
            } else {
                operation.setDescription(Yamls.string(body.get("description")));
                operation.inputs().putAll(Yamls.map(body.get("inputs")));
                operation.outputs().putAll(Yamls.map(body.get("outputs")));
                if (body.containsKey("implementation")) {
                    operation.setImplementation(readImplementation(body.get("implementation")));
                }
            }
            iface.operations().put(e.getKey(), operation);
        }
    }

    private static void readNotifications(InterfaceAssignment iface, Map<String, Object> block) {
        for (Map.Entry<String, Object> e : block.entrySet()) {
            NotificationAssignment notification = new NotificationAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                notification.setImplementation(readImplementation(e.getValue()));
            } else {
                notification.setDescription(Yamls.string(body.get("description")));
                notification.outputs().putAll(Yamls.map(body.get("outputs")));
                if (body.containsKey("implementation")) {
                    notification.setImplementation(readImplementation(body.get("implementation")));
                }
            }
            iface.notifications().put(e.getKey(), notification);
        }
    }

    /** {@code implementation} may be a bare artifact or file name, or a map with {@code primary}. */
    static ImplementationDefinition readImplementation(Object value) {
        ImplementationDefinition impl = new ImplementationDefinition();
        if (!Yamls.isMap(value)) {
            impl.setPrimary(Yamls.string(value));
            return impl;
        }
        Map<String, Object> map = Yamls.map(value);
        impl.setPrimary(Yamls.string(map.get("primary")));
        impl.dependencies().addAll(Yamls.stringList(map.get("dependencies")));
        impl.setTimeout(Yamls.integer(map.get("timeout")));
        impl.setOperationHost(Yamls.string(map.get("operation_host")));
        return impl;
    }
    // ============================================================================================
    // policies
    // ============================================================================================

    // Reads {@code topology_template.policies}, a sequence of single-entry maps.


    public static List<PolicyDefinition> readPolicies(Object block, String file) {
        List<PolicyDefinition> out = new ArrayList<>();
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                out.add(readPolicy(e.getKey(), e.getValue(), file));
            }
        }
        return out;
    }

    private static PolicyDefinition readPolicy(String name, Object body, String file) {
        PolicyDefinition policy = new PolicyDefinition(name);
        Map<String, Object> map = Yamls.map(body);
        policy.setSource(SourceRef.of(file, name));
        policy.setType(Yamls.string(map.get("type")));
        policy.setDescription(Yamls.string(map.get("description")));
        policy.metadata().putAll(Yamls.map(map.get("metadata")));
        policy.properties().putAll(Yamls.map(map.get("properties")));
        policy.targets().addAll(Yamls.stringList(map.get("targets")));
        readTriggers(policy, map.get("triggers"));
        return policy;
    }

    private static void readTriggers(PolicyDefinition policy, Object block) {
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            TriggerDefinition trigger = new TriggerDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            trigger.setEvent(Yamls.string(body.get("event")));
            trigger.setDescription(Yamls.string(body.get("description")));
            trigger.condition().putAll(Yamls.map(body.get("condition")));
            for (Object activity : Yamls.list(body.get("action"))) {
                readActivities(trigger, activity);
            }
            policy.triggers().put(e.getKey(), trigger);
        }
    }

    private static void readActivities(TriggerDefinition trigger, Object activity) {
        for (Map.Entry<String, Object> a : Yamls.map(activity).entrySet()) {
            trigger.action().add(new ActivityDefinition(kindOf(a.getKey()), a.getKey(), a.getValue()));
        }
    }

    private static ActivityDefinition.Kind kindOf(String key) {
        switch (key) {
            case "call_operation":
                return ActivityDefinition.Kind.CALL_OPERATION;
            case "set_state":
                return ActivityDefinition.Kind.SET_STATE;
            case "inline":
                return ActivityDefinition.Kind.INLINE;
            default:
                return ActivityDefinition.Kind.UNKNOWN;
        }
    }
    // ============================================================================================
    // substitution_mappings
    // ============================================================================================

    // Reads {@code substitution_mappings}.
    //
    // <p>Two grammars for the flavour identifier are accepted. The current one is
    // {@code substitution_filter}, which SOL001 V5.4.1 clause 6.11.2 describes as carrying "a
    // flavour_id property and its value ... which identifies the DF corresponding to this low level
    // template". The older {@code properties} form is read as well, because clause 6.11.2 NOTE 1
    // records that the grammar changed at version 3.3.1 and that the previous form is still to be
    // handled; a caller can tell the two apart and report the deprecated one.


    public static SubstitutionMappings readSubstitutionMappings(Object block) {
        SubstitutionMappings mappings = new SubstitutionMappings();
        Map<String, Object> map = Yamls.map(block);
        mappings.setNodeType(Yamls.string(map.get("node_type")));

        readSubstitutionFilter(mappings, map.get("substitution_filter"));
        mappings.propertyMappings().putAll(Yamls.map(map.get("properties")));
        readTargets(mappings.requirements(), map.get("requirements"));
        readTargets(mappings.capabilities(), map.get("capabilities"));
        mappings.attributes().putAll(Yamls.map(map.get("attributes")));
        mappings.interfaces().putAll(readInterfaces(map.get("interfaces")));
        return mappings;
    }

    /**
     * The filter is a map with a {@code properties} sequence of single-entry maps, each naming a
     * property and its constraints: {@code properties: [ flavour_id: { equal: simple } ]}.
     */
    private static void readSubstitutionFilter(SubstitutionMappings mappings, Object block) {
        if (block == null) {
            return;
        }
        Map<String, Object> filter = Yamls.map(block);
        for (Object entry : Yamls.list(filter.get("properties"))) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                PropertyFilter propertyFilter = new PropertyFilter(e.getKey());
                if (Yamls.isMap(e.getValue())) {
                    propertyFilter.constraints().putAll(Yamls.map(e.getValue()));
                } else {
                    // Bare value is equivalent to an equal constraint.
                    propertyFilter.constraints().put("equal", e.getValue());
                }
                mappings.substitutionFilter().add(propertyFilter);
            }
        }
    }

    /**
     * Each mapping is a two-element sequence: the node template being exposed, then the requirement
     * or capability of that node, e.g. {@code virtual_link_mgmt: [ WebCp, virtual_link ]}.
     */
    private static void readTargets(Map<String, SubstitutionTarget> into, Object block) {
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            List<String> pair = Yamls.stringList(e.getValue());
            if (pair.isEmpty()) {
                continue;
            }
            String nodeTemplateName = pair.get(0);
            String targetName = pair.size() > 1 ? pair.get(1) : null;
            into.put(e.getKey(), new SubstitutionTarget(nodeTemplateName, targetName));
        }
    }
    // ============================================================================================
    // PROPERTY VALUES: A LITERAL OR A TOSCA FUNCTION
    // ============================================================================================

    // Turns a raw YAML value into a {@link PropertyValue} tree, detecting TOSCA function calls.
    //
    // <p>Detection rule, per TOSCA Simple Profile YAML 1.3 clause 4: a value is a function call when it
    // is a map with <em>exactly one</em> key and that key is a function name. Anything else is a
    // literal, including a single-key map whose key merely happens to resemble one.
    //
    // <p>The parser does not evaluate anything. {@code get_input} and {@code get_attribute} come back
    // tagged as deferred; the rest come back {@link Resolution#UNRESOLVABLE} until an evaluator with
    // access to the topology runs over them.


    /**
     * Parses a raw YAML value.
     *
     * @param raw the value as produced by the YAML loader: scalar, {@code List}, or {@code Map}
     */
    public static PropertyValue<Object> parsePropertyValue(Object raw) {
        Optional<FunctionCall<Object>> asFunction = asFunctionCall(raw);
        if (asFunction.isPresent()) {
            return asFunction.get();
        }
        return Literal.of(parseLiteralRecursively(raw), raw);
    }

    /** Parses and immediately narrows to a size literal, when the value is one. */
    public static Optional<Quantity> parseAsQuantity(Object raw) {
        if (raw instanceof String) {
            return parseScalarUnit((String) raw);
        }
        return Optional.empty();
    }

    private static Optional<FunctionCall<Object>> asFunctionCall(Object raw) {
        if (!(raw instanceof Map)) {
            return Optional.empty();
        }
        Map<?, ?> map = (Map<?, ?>) raw;
        if (map.size() != 1) {
            return Optional.empty();
        }
        Map.Entry<?, ?> only = map.entrySet().iterator().next();
        if (!(only.getKey() instanceof String)) {
            return Optional.empty();
        }
        String key = (String) only.getKey();

        Optional<FunctionName> known = FunctionName.fromKey(key);
        if (known.isPresent()) {
            return Optional.of(FunctionCall.unresolved(known.get(), key, parseArgs(only.getValue()), raw));
        }
        if (FunctionName.isKnownNonSol001Function(key)) {
            // Valid TOSCA, but absent from SOL001 Table 5.9-1. Parsed so the caller can report it.
            return Optional.of(FunctionCall.unresolved(FunctionName.UNKNOWN, key, parseArgs(only.getValue()), raw));
        }
        return Optional.empty();
    }

    /**
     * Function arguments are either a single value ({@code get_input: image_tag}) or a list
     * ({@code get_property: [ SELF, vdu_profile, min_number_of_instances ]}).
     */
    private static List<PropertyValue<?>> parseArgs(Object rawArgs) {
        List<PropertyValue<?>> args = new ArrayList<>();
        if (rawArgs instanceof List) {
            for (Object element : (List<?>) rawArgs) {
                args.add(parsePropertyValue(element));
            }
        } else {
            args.add(parsePropertyValue(rawArgs));
        }
        return args;
    }

    /**
     * Walks a literal container so a function nested inside a list or map is still detected,
     * e.g. {@code protocol: [ { associated_layer_protocol: { get_input: proto } } ]}.
     */
    private static Object parseLiteralRecursively(Object raw) {
        if (raw instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object element : (List<?>) raw) {
                out.add(unwrapIfLiteral(parsePropertyValue(element)));
            }
            return Collections.unmodifiableList(out);
        }
        if (raw instanceof Map) {
            Map<Object, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) raw).entrySet()) {
                out.put(e.getKey(), unwrapIfLiteral(parsePropertyValue(e.getValue())));
            }
            return Collections.unmodifiableMap(out);
        }
        return raw;
    }

    /**
     * Keeps nested {@link FunctionCall}s intact inside a literal container while flattening nested
     * literals back to plain values, so a caller reading a map of scalars is not forced to unwrap
     * every entry.
     */
    private static Object unwrapIfLiteral(PropertyValue<?> value) {
        if (value.kind() == Kind.FUNCTION) {
            return value;
        }
        return value.resolved().orElse(null);
    }
    // ============================================================================================
    // scalar-unit.size
    // ============================================================================================

    // Parses TOSCA {@code scalar-unit.size} literals such as {@code "128 MiB"}.
    //
    // <p>TOSCA Simple Profile YAML 1.3 writes the grammar as {@code <scalar> <unit>}, i.e. with
    // whitespace. Descriptors in the wild routinely omit it, so the parser accepts both and records
    // which form was used via {@link Quantity#hasCanonicalSpacing()}. Rejecting the compact form
    // outright would fail real packages over a cosmetic detail; accepting it silently would hide a
    // conformance problem, so the information is surfaced instead.


    /** Number, optional whitespace, unit. Group 1 = magnitude, group 2 = whitespace, group 3 = unit. */
    private static final Pattern SCALAR_UNIT =
            Pattern.compile("^\\s*([0-9]+(?:\\.[0-9]+)?)(\\s*)([A-Za-z]+)\\s*$");

    /**
     * Parses a size literal.
     *
     * @return the parsed quantity, or empty when the text is not a size literal at all (unknown
     *         unit, missing number, wrong shape). The caller decides whether that is an error.
     */
    public static Optional<Quantity> parseScalarUnit(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher m = SCALAR_UNIT.matcher(text);
        if (!m.matches()) {
            return Optional.empty();
        }
        Optional<SizeUnit> unit = SizeUnit.fromSymbol(m.group(3));
        if (!unit.isPresent()) {
            return Optional.empty();
        }
        BigDecimal magnitude = new BigDecimal(m.group(1));
        boolean canonicalSpacing = !m.group(2).isEmpty();
        return Optional.of(new Quantity(text.trim(), magnitude, unit.get(), canonicalSpacing));
    }

    /**
     * True when the text looks like it was meant to be a size literal, i.e. a number immediately
     * followed by letters. Used to tell "this property was left as a plain number" apart from
     * "this property has a malformed unit", which deserve different findings.
     */
    public static boolean looksLikeSizeLiteral(String text) {
        return text != null && SCALAR_UNIT.matcher(text).matches();
    }
}
