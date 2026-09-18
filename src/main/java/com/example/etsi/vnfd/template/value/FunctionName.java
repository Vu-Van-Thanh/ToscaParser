package com.example.etsi.vnfd.template.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TOSCA functions recognised in a property value.
 *
 * <p>SOL001 V5.4.1 clause 5.9, Table 5.9-1 lists exactly the functions a conformant VNFD may use:
 * {@code get_property}, {@code get_artifact}, {@code get_input}, {@code get_attribute} and the
 * intrinsic functions {@code concat}, {@code join}, {@code token}.
 *
 * <p>{@code get_operation_output} and {@code get_nodes_of_type} exist in TOSCA Simple Profile YAML
 * 1.3 but are <em>absent</em> from Table 5.9-1; they parse to {@link #UNKNOWN} so the caller can
 * raise a finding rather than silently accepting them.
 */
public enum FunctionName {

    GET_INPUT("get_input"),
    GET_PROPERTY("get_property"),
    GET_ATTRIBUTE("get_attribute"),
    GET_ARTIFACT("get_artifact"),
    CONCAT("concat"),
    JOIN("join"),
    TOKEN("token"),

    /** Syntactically a function but not listed in SOL001 Table 5.9-1. */
    UNKNOWN(null);

    private static final Map<String, FunctionName> BY_KEY;

    /** TOSCA 1.3 function keys that are valid TOSCA but not permitted by SOL001 Table 5.9-1. */
    private static final Map<String, Boolean> NON_SOL001_KEYS;

    static {
        Map<String, FunctionName> byKey = new HashMap<>();
        for (FunctionName f : values()) {
            if (f.key != null) {
                byKey.put(f.key, f);
            }
        }
        BY_KEY = Collections.unmodifiableMap(byKey);

        Map<String, Boolean> other = new HashMap<>();
        other.put("get_operation_output", Boolean.TRUE);
        other.put("get_nodes_of_type", Boolean.TRUE);
        NON_SOL001_KEYS = Collections.unmodifiableMap(other);
    }

    private final String key;

    FunctionName(String key) {
        this.key = key;
    }

    /** The YAML key as it appears in a descriptor, e.g. {@code "get_input"}. */
    public String key() {
        return key;
    }

    /** Resolves a YAML map key to a SOL001-permitted function, if it is one. */
    public static Optional<FunctionName> fromKey(String key) {
        return Optional.ofNullable(BY_KEY.get(key));
    }

    /**
     * True when {@code key} is a TOSCA 1.3 function that SOL001 Table 5.9-1 does not list.
     * Such a value still parses (as {@link #UNKNOWN}) but warrants a finding.
     */
    public static boolean isKnownNonSol001Function(String key) {
        return NON_SOL001_KEYS.containsKey(key);
    }
}
