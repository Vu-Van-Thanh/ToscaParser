package com.example.etsi.vnfd.template.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns a raw YAML value into a {@link PropertyValue} tree, detecting TOSCA function calls.
 *
 * <p>Detection rule, per TOSCA Simple Profile YAML 1.3 clause 4: a value is a function call when it
 * is a map with <em>exactly one</em> key and that key is a function name. Anything else is a
 * literal, including a single-key map whose key merely happens to resemble one.
 *
 * <p>The parser does not evaluate anything. {@code get_input} and {@code get_attribute} come back
 * tagged as deferred; the rest come back {@link Resolution#UNRESOLVABLE} until an evaluator with
 * access to the topology runs over them.
 */
public final class PropertyValueParser {

    private PropertyValueParser() {
    }

    /**
     * Parses a raw YAML value.
     *
     * @param raw the value as produced by the YAML loader: scalar, {@code List}, or {@code Map}
     */
    public static PropertyValue<Object> parse(Object raw) {
        Optional<FunctionCall<Object>> asFunction = asFunctionCall(raw);
        if (asFunction.isPresent()) {
            return asFunction.get();
        }
        return Literal.of(parseLiteralRecursively(raw), raw);
    }

    /** Parses and immediately narrows to a size literal, when the value is one. */
    public static Optional<Quantity> parseAsQuantity(Object raw) {
        if (raw instanceof String) {
            return ScalarUnitParser.parse((String) raw);
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
                args.add(parse(element));
            }
        } else {
            args.add(parse(rawArgs));
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
                out.add(unwrapIfLiteral(parse(element)));
            }
            return Collections.unmodifiableList(out);
        }
        if (raw instanceof Map) {
            Map<Object, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) raw).entrySet()) {
                out.put(e.getKey(), unwrapIfLiteral(parse(e.getValue())));
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
}
