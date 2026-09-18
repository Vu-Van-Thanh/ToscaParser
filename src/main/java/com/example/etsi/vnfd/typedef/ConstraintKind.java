package com.example.etsi.vnfd.typedef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The constraint operators of TOSCA Simple Profile YAML 1.3. */
public enum ConstraintKind {

    EQUAL("equal"),
    GREATER_THAN("greater_than"),
    GREATER_OR_EQUAL("greater_or_equal"),
    LESS_THAN("less_than"),
    LESS_OR_EQUAL("less_or_equal"),
    IN_RANGE("in_range"),
    VALID_VALUES("valid_values"),
    LENGTH("length"),
    MIN_LENGTH("min_length"),
    MAX_LENGTH("max_length"),
    PATTERN("pattern"),
    SCHEMA("schema"),
    /** An operator this version does not define; kept so the value is not lost. */
    UNKNOWN(null);

    private static final Map<String, ConstraintKind> BY_KEY;

    static {
        Map<String, ConstraintKind> byKey = new LinkedHashMap<>();
        for (ConstraintKind k : values()) {
            if (k.key != null) {
                byKey.put(k.key, k);
            }
        }
        BY_KEY = Collections.unmodifiableMap(byKey);
    }

    private final String key;

    ConstraintKind(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Optional<ConstraintKind> fromKey(String key) {
        return Optional.ofNullable(BY_KEY.get(key));
    }
}
