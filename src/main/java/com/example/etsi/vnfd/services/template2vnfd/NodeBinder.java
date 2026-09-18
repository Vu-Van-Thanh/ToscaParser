package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.services.pkg2template.TemplateReader;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.value.Literal;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import com.example.etsi.vnfd.toscatype.node.DeployableModule;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.VduSubCp;
import com.example.etsi.vnfd.toscatype.node.VduVirtualBlockStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualFileStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualObjectStorage;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.typedef.Constraint;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.example.etsi.vnfd.template.value.FunctionName;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns a node template into the SOL001 class that stands for its type.
 *
 * <p>Three steps: find the class by {@code derived_from}, lay the type defaults under the assigned
 * properties, bind. The middle step is not optional - SOL001 V5.4.1 Annex A.23 writes a VNF node
 * template that assigns only {@code flavour_description} and leaves {@code descriptor_id},
 * {@code provider} and the rest on the node type, and clause 6.11.2 makes that the normal
 * arrangement rather than an edge case.
 */
public final class NodeBinder {

    private final ObjectMapper mapper = ToscaBindModule.mapper();
    private final NodeTypeResolver resolver;
    private final TypeDefaults defaults;
    private final ConstraintChecker constraints;

    /** Binds without reporting: the caller does not want the conformance findings. */
    public NodeBinder(TypeReader.Hierarchy hierarchy, List<Class<? extends NfvNode>> nodeClasses) {
        this(hierarchy, nodeClasses, new Findings());
    }

    public NodeBinder(TypeReader.Hierarchy hierarchy, List<Class<? extends NfvNode>> nodeClasses,
            Findings findings) {
        this.resolver = new NodeTypeResolver(hierarchy, nodeClasses);
        this.defaults = new TypeDefaults(hierarchy);
        this.constraints = new ConstraintChecker(hierarchy, findings);
    }

    /** Binds a node template, or returns empty when it is not a type this library maps. */
    public Optional<NfvNode> bind(NodeTemplate template) {
        return bind(template, java.util.Collections.emptyMap());
    }

    /**
     * Binds a node template, resolving {@code get_property} against the rest of the topology.
     *
     * <p>SOL001 V5.4.1 clause 5.9 Table 5.9-1 admits {@code get_property}, and a chain that stays
     * inside the descriptor has one answer at parse time - unlike {@code get_input} and
     * {@code get_attribute}, which need a VNF instance that does not exist yet. Resolving it here
     * rather than leaving it deferred is the difference between a descriptor that is under-specified
     * and one that merely refers to itself.
     */
    public Optional<NfvNode> bind(NodeTemplate template, Map<String, NodeTemplate> topology) {
        Optional<Class<? extends NfvNode>> target = resolver.resolve(template.type());
        if (!target.isPresent()) {
            return Optional.empty();
        }

        Map<String, Object> merged = defaults.apply(template.type(), template.properties());
        merged = resolveGetProperty(merged, template, topology);
        constraints.check(template.type(), merged, template.source());
        checkArtifacts(template);

        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("type", template.type());
        template.description().ifPresent(d -> declaration.put("description", d));
        declaration.put("metadata", template.metadata());
        declaration.put("directives", template.directives());
        declaration.put("attributes", template.attributes());
        declaration.put("properties", merged);
        declaration.put("requirements", groupRequirements(template.requirements()));
        declaration.put("capabilities", template.capabilities());

        NfvNode node = mapper.convertValue(declaration, target.get());
        // Already-typed collections are attached rather than round-tripped: convertValue would have
        // to serialise them first, and they are DOM objects, not Jackson beans.
        node.setInterfaces(template.interfaces());
        node.setArtifacts(template.artifacts());
        node.setKey(template.name());
        node.setSource(template.source());
        node.setEtsiType(resolver.etsiTypeOf(template.type()).orElse(template.type()));
        return Optional.of(node);
    }

    /**
     * The same check over the properties of each attached artifact.
     *
     * <p>Artifact types declare properties like any other type - {@code tosca.artifacts.nfv.SwImage}
     * gives {@code container_format} a {@code valid_values} constraint and {@code size} the type
     * {@code scalar-unit.size} - so leaving them out would silently exempt them. The values are not
     * bound into the node here; the artifact keeps its raw map and the mappers read it.
     */
    private void checkArtifacts(NodeTemplate template) {
        if (template.artifacts() == null) {
            return;
        }
        for (ArtifactDefinition artifact : template.artifacts().values()) {
            constraints.check(artifact.type(),
                    defaults.apply(artifact.type(), artifact.properties()),
                    artifact.source());
        }
    }


    // ============================================================================================
    // RESOLVING get_property AGAINST THE TOPOLOGY
    // ============================================================================================

    /**
     * Replaces every statically resolvable {@code get_property} with the value it names.
     *
     * <p>Works on a copy: the DOM keeps what the descriptor wrote, because a reader asking what a
     * file says should not be handed what this library worked out. Anything that cannot be resolved
     * - a missing node, a property that is itself a function, a cycle - is left exactly as written,
     * so it still arrives downstream tagged rather than silently blank.
     */
    private Map<String, Object> resolveGetProperty(Map<String, Object> properties,
            NodeTemplate self, Map<String, NodeTemplate> topology) {
        if (properties.isEmpty() || topology.isEmpty()) {
            return properties;
        }
        Object resolved = resolveDeep(properties, self, topology, new LinkedHashSet<>());
        @SuppressWarnings("unchecked")
        Map<String, Object> out = (Map<String, Object>) resolved;
        return out;
    }

    private Object resolveDeep(Object value, NodeTemplate self, Map<String, NodeTemplate> topology,
            Set<String> visiting) {
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Object direct = resolveCall(map, self, topology, visiting);
            if (direct != null) {
                return direct;
            }
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                out.put(String.valueOf(e.getKey()), resolveDeep(e.getValue(), self, topology, visiting));
            }
            return out;
        }
        if (value instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object element : (List<?>) value) {
                out.add(resolveDeep(element, self, topology, visiting));
            }
            return out;
        }
        return value;
    }

    /** The value a {@code get_property} names, or null when this map is not one, or cannot resolve. */
    private Object resolveCall(Map<?, ?> map, NodeTemplate self, Map<String, NodeTemplate> topology,
            Set<String> visiting) {
        if (map.size() != 1) {
            return null;
        }
        Map.Entry<?, ?> only = map.entrySet().iterator().next();
        if (!"get_property".equals(only.getKey()) || !(only.getValue() instanceof List)) {
            return null;
        }
        List<?> args = (List<?>) only.getValue();
        if (args.size() < 2) {
            return null;
        }

        // TOSCA 1.3 clause 4.4.1: the first argument names the entity - SELF, or a node template.
        String entity = String.valueOf(args.get(0));
        NodeTemplate target = "SELF".equals(entity) || "SELF_NODE".equals(entity)
                ? self
                : topology.get(entity);
        if (target == null) {
            return null;
        }

        // A property reading a property of the same node that reads back is a cycle, not a value.
        String mark = target.name() + "." + args.get(1);
        if (!visiting.add(mark)) {
            return null;
        }
        try {
            Map<String, Object> source = defaults.apply(target.type(), target.properties());
            Object found = source;
            for (Object step : args.subList(1, args.size())) {
                if (!(found instanceof Map)) {
                    return null;
                }
                found = ((Map<?, ?>) found).get(String.valueOf(step));
                if (found == null) {
                    return null;
                }
            }
            Object value = resolveDeep(found, target, topology, visiting);
            if (!isFunctionCall(value)) {
                return value;
            }
            // The chain ends on another function. An intrinsic over values still has an answer -
            // get_property naming a property written as concat of literals, say - so ask the parser,
            // which evaluates those. Anything it cannot evaluate stays as written.
            PropertyValue<Object> evaluated = TemplateReader.parsePropertyValue(value);
            return evaluated.isResolved() ? evaluated.resolved().orElse(null) : null;
        } finally {
            visiting.remove(mark);
        }
    }

    private static boolean isFunctionCall(Object value) {
        if (!(value instanceof Map) || ((Map<?, ?>) value).size() != 1) {
            return false;
        }
        Object key = ((Map<?, ?>) value).keySet().iterator().next();
        return key instanceof String
                && FunctionName.fromKey((String) key).isPresent();
    }

    /** Binds and narrows in one step, for a caller that knows what it is looking for. */
    public <T extends NfvNode> Optional<T> bindAs(NodeTemplate template, Class<T> expected) {
        return bind(template).filter(expected::isInstance).map(expected::cast);
    }

    public NodeTypeResolver resolver() {
        return resolver;
    }

    /**
     * Requirements as a map of name to target list.
     *
     * <p>TOSCA writes them as a sequence of single-entry maps, and the same key may appear more than
     * once - SOL001 Annex A.23 declares {@code associatedVdu} twice on one {@code Mciop}. Grouping
     * into lists is what keeps the second one; reading the sequence into a map would drop it
     * silently, which is why every generated requirement field is a {@code List}.
     */
    private Map<String, List<String>> groupRequirements(List<RequirementAssignment> assignments) {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (RequirementAssignment assignment : assignments) {
            grouped.computeIfAbsent(assignment.name(), key -> new ArrayList<>())
                    .add(assignment.node());
        }
        return grouped;
    }
}

/**
 * Decides which SOL001 class a node template belongs to.
 *
 * <p>By {@code derived_from}, never by comparing type names - see {@link EtsiNodeType} for why.
 * When a declared type derives from several of the registered ETSI types, the nearest ancestor
 * wins, which is what separates {@code VduSubCp} from {@code VduCp} from {@code Cp} without anyone
 * having to state a precedence.
 */
final class NodeTypeResolver {

    private final TypeReader.Hierarchy hierarchy;
    private final Map<String, Class<? extends NfvNode>> byEtsiType;

    public NodeTypeResolver(TypeReader.Hierarchy hierarchy, List<Class<? extends NfvNode>> classes) {
        this.hierarchy = hierarchy;
        Map<String, Class<? extends NfvNode>> map = new LinkedHashMap<>();
        for (Class<? extends NfvNode> type : classes) {
            EtsiNodeType annotation = type.getAnnotation(EtsiNodeType.class);
            if (annotation == null) {
                throw new IllegalStateException(type.getName() + " has no @EtsiNodeType");
            }
            map.put(annotation.value(), type);
        }
        this.byEtsiType = Collections.unmodifiableMap(map);
    }

    /** The class for a declared type, or empty when it derives from none of the ETSI node types. */
    public Optional<Class<? extends NfvNode>> resolve(String declaredType) {
        if (declaredType == null) {
            return Optional.empty();
        }
        return hierarchy.nearestAncestorAmong(declaredType, byEtsiType.keySet())
                .map(byEtsiType::get);
    }

    /** The ETSI type a declared type was recognised as. */
    public Optional<String> etsiTypeOf(String declaredType) {
        return declaredType == null
                ? Optional.empty()
                : hierarchy.nearestAncestorAmong(declaredType, byEtsiType.keySet());
    }

    /** The registered ETSI types, for diagnostics and for the property cross-check test. */
    public Map<String, Class<? extends NfvNode>> registered() {
        return byEtsiType;
    }
}

/**
 * The node classes this library binds.
 *
 * <p>An explicit list rather than a classpath scan: scanning needs a library this project does not
 * carry, and a list of class literals costs one line per type while still keeping the ETSI type
 * name in the class itself, where {@code @EtsiNodeType} declares it. Order does not matter - the
 * resolver picks the nearest ancestor, so {@code VduSubCp} wins over {@code VduCp} over {@code Cp}
 * without anyone stating a precedence.
 */
final class NodeTypes {

    /** Every SOL001 node type mapped in the CNF scope. */
    public static final List<Class<? extends NfvNode>> ALL = Collections.unmodifiableList(
            Arrays.<Class<? extends NfvNode>>asList(
                    Vnf.class,
                    VduOsContainerDeployableUnit.class,
                    VduOsContainer.class,
                    Mciop.class,
                    VduSubCp.class,
                    VduCp.class,
                    VnfExtCp.class,
                    VipCp.class,
                    VirtualCp.class,
                    VnfVirtualLink.class,
                    VduVirtualBlockStorage.class,
                    VduVirtualObjectStorage.class,
                    VduVirtualFileStorage.class,
                    DeployableModule.class,
                    Certificate.class));

    private NodeTypes() {
    }
}

/**
 * Lays the defaults a type declares under the values a template assigns.
 *
 * <p>Not a nicety. SOL001 V5.4.1 Annex A.23 writes a VNF node template that assigns only
 * {@code flavour_description}, leaving {@code descriptor_id}, {@code provider},
 * {@code software_version} and the rest on the VNF-specific node type. Binding the template alone
 * yields a VNFD with no identifier at all. Clause 6.11.2 makes that arrangement the normal one, not
 * an edge case, since it requires the VNF node type to be derived from {@code tosca.nodes.nfv.VNF}.
 *
 * <p>Applied before binding rather than after: merging two maps is exactly the semantics wanted -
 * the template wins where it speaks - and it needs no reflection over the bound object.
 */
final class TypeDefaults {

    private final TypeReader.Hierarchy hierarchy;

    public TypeDefaults(TypeReader.Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * The assigned properties with type defaults filled in.
     *
     * @param declaredType the type the template declares, which may be a vendor type
     * @param assigned     the {@code properties} block of the template
     */
    public Map<String, Object> apply(String declaredType, Map<String, Object> assigned) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Map.Entry<String, PropertyDef> e
                : hierarchy.effectivePropertiesOfAnyType(declaredType).entrySet()) {
            e.getValue().defaultValue().ifPresent(value -> merged.put(e.getKey(), value));
        }
        if (assigned != null) {
            merged.putAll(assigned);
        }
        return merged;
    }
}

/**
 * Checks assigned properties against what their type declares.
 *
 * <p>Both what is checked and what counts as a violation come from the type definitions rather than
 * from annotations on the model. {@code required: true} and {@code constraints:} are written in
 * {@code etsi_nfv_sol001_vnfd_types.yaml}; stating them again in Java would create a second copy of
 * the same rule, and the two drift the moment ETSI publishes a new version.
 *
 * <p>A value still bound to an input or a runtime attribute is skipped: SOL001 V5.4.1 clause 5.9
 * allows the expression, and there is nothing to check until something evaluates it.
 */
final class ConstraintChecker {

    private static final String CLAUSE_CONSTRAINTS = "TOSCA Simple Profile YAML 1.3 cl. 3.6.3";
    private static final String CLAUSE_REQUIRED = "TOSCA Simple Profile YAML 1.3 cl. 3.6.2";
    private static final String CLAUSE_SCALAR_UNIT = "TOSCA Simple Profile YAML 1.3 cl. 3.3.6";
    private static final String SCALAR_UNIT_SIZE = "scalar-unit.size";

    private final TypeReader.Hierarchy hierarchy;
    private final Findings findings;

    public ConstraintChecker(TypeReader.Hierarchy hierarchy, Findings findings) {
        this.hierarchy = hierarchy;
        this.findings = findings;
    }

    /**
     * @param declaredType the type the declaration names, which may be a vendor type
     * @param merged       the properties after {@link TypeDefaults} has been applied
     */
    public void check(String declaredType, Map<String, Object> merged, SourceRef source) {
        Map<String, PropertyDef> declared = hierarchy.effectivePropertiesOfAnyType(declaredType);
        for (Map.Entry<String, PropertyDef> e : declared.entrySet()) {
            PropertyDef def = e.getValue();
            Object assigned = merged.get(e.getKey());

            if (assigned == null) {
                if (def.isRequired()) {
                    findings.error("TOSCA02", CLAUSE_REQUIRED,
                            "Required property " + e.getKey() + " is missing on a node of type "
                                    + declaredType,
                            ref(source));
                }
                continue;
            }

            PropertyValue<Object> parsed = TemplateReader.parsePropertyValue(assigned);
            if (!parsed.isResolved()) {
                continue;
            }
            Object candidate = parsed.resolved().orElse(null);
            checkScalarUnit(e.getKey(), def, candidate, source);
            checkAll(e.getKey(), def.constraints(), candidate, source);
            def.entrySchema().ifPresent(entry -> {
                if (candidate instanceof List) {
                    for (Object element : (List<?>) candidate) {
                        checkAll(e.getKey(), entry.constraints(), element, source);
                    }
                }
            });
        }
    }

    /**
     * TOSCA 1.3 clause 3.3.6 spells a scalar-unit as {@code <scalar> <unit>}, with the space.
     *
     * <p>A package writing {@code 128MB} is still readable, and rejecting it would be worse than
     * saying so - all three bundled packages write it that way - but it is not conformant, and a
     * consumer comparing descriptors from different vendors should know.
     */
    private void checkScalarUnit(String name, PropertyDef def, Object candidate, SourceRef source) {
        if (!SCALAR_UNIT_SIZE.equals(def.type()) || !(candidate instanceof String)) {
            return;
        }
        TemplateReader.parseScalarUnit((String) candidate)
                .filter(q -> !q.hasCanonicalSpacing())
                .ifPresent(q -> findings.warn("TOSCA01", CLAUSE_SCALAR_UNIT,
                        "Property " + name + " writes " + q.originalText()
                                + " without a space between the value and the unit",
                        ref(source)));
    }

    private void checkAll(String name, List<Constraint> constraints, Object candidate,
            SourceRef source) {
        for (Constraint constraint : constraints) {
            Optional<String> violation = TypeReader.validate(constraint, candidate);
            violation.ifPresent(message -> findings.warn("TOSCA03", CLAUSE_CONSTRAINTS,
                    "Property " + name + " violates constraint " + message, ref(source)));
        }
    }

    private static com.example.etsi.vnfd.validation.SourceRef ref(SourceRef source) {
        return source == null ? null : source.toFindingRef();
    }
}

/**
 * Binds a property value that may be a literal or a TOSCA function.
 *
 * <p>SOL001 V5.4.1 clause 5.9 permits {@code get_input}, {@code get_property},
 * {@code get_attribute}, {@code get_artifact} and the intrinsic functions anywhere a value is
 * expected. A field typed {@code String} would make Jackson fail on
 * {@code name: { get_input: vduName }}; a field typed {@code PropertyValue<String>} keeps the
 * expression and its resolution state instead.
 *
 * <p>Contextual because the target type of the value is the type argument of the field:
 * {@code PropertyValue<Integer>} must come back holding an {@code Integer}, not whatever YAML
 * produced. {@code Quantity} is the one target that is derived rather than cast - it is the parsed
 * form of a {@code scalar-unit.size}.
 */
final class PropertyValueDeserializer extends JsonDeserializer<PropertyValue<?>>
        implements ContextualDeserializer {

    private final JavaType valueType;

    public PropertyValueDeserializer() {
        this(null);
    }

    private PropertyValueDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType wrapper = property != null ? property.getType() : context.getContextualType();
        JavaType argument = wrapper != null && wrapper.containedTypeCount() > 0
                ? wrapper.containedType(0)
                : null;
        return new PropertyValueDeserializer(argument);
    }

    @Override
    public PropertyValue<?> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        Object raw = parser.readValueAs(Object.class);
        PropertyValue<Object> parsed = TemplateReader.parsePropertyValue(raw);
        if (!parsed.isResolved()) {
            // An expression still to be evaluated: nothing to convert, and converting would either
            // invent a value or throw away the expression the caller needs later.
            return parsed;
        }
        return Literal.of(convert(parsed.resolved().orElse(null), context), raw);
    }

    private Object convert(Object value, DeserializationContext context) {
        if (value == null || valueType == null) {
            return value;
        }
        Class<?> target = valueType.getRawClass();
        if (target == Quantity.class) {
            // TOSCA 1.3 clause 3.3.6: "<scalar> <unit>". A non-conformant spelling is still read,
            // with a finding, rather than failing the parse.
            return value instanceof String
                    ? TemplateReader.parseScalarUnit((String) value).orElse(null)
                    : null;
        }
        if (target.isInstance(value)) {
            return value;
        }
        if (target == String.class) {
            return String.valueOf(value);
        }
        if (target == Integer.class && value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (target == Long.class && value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (target == Boolean.class) {
            return Boolean.valueOf(String.valueOf(value));
        }
        if (target == BigDecimal.class && value instanceof Number) {
            return new BigDecimal(String.valueOf(value));
        }
        return context.getConfig().getTypeFactory() == null
                ? value
                : convertLoosely(value, target);
    }

    private Object convertLoosely(Object value, Class<?> target) {
        try {
            return target.cast(value);
        } catch (ClassCastException e) {
            return value;
        }
    }

    /** Whether a value survived as a literal - used by callers that need the plain value. */
    public static Optional<Object> literalOf(PropertyValue<?> value) {
        return value == null ? Optional.empty() : Optional.ofNullable(value.resolved().orElse(null));
    }
}

/**
 * The Jackson configuration used to bind TOSCA declarations onto the SOL001 classes.
 *
 * <p>Kept here rather than annotated onto the classes so the model stays free of binding concerns,
 * and so there is one place that says how a descriptor is read.
 */
final class ToscaBindModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public ToscaBindModule() {
        super("etsi-tosca-bind");
        addDeserializer(PropertyValue.class, new PropertyValueDeserializer());
    }


    /**
     * A mapper configured for descriptor binding.
     *
     * <p>Unknown properties are ignored on purpose: a descriptor may carry vendor keynames, and
     * TOSCA 1.3 has keynames SOL001 never uses. Failing on them would reject valid packages; what
     * matters instead is that every property the ETSI type declares is read, which
     * {@code ConstraintChecker} verifies from the type definitions.
     */
    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ToscaBindModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper;
    }
}
