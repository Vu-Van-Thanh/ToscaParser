package com.example.etsi.vnfd.typedef;

import java.util.List;

/**
 * One property constraint from a type definition, as written.
 *
 * <p>The operator and its operand only; evaluating a value against them is
 * {@link ConstraintEvaluator}. Kept apart because a constraint is read once per type definition
 * and evaluated once per assigned property, at two different stages.
 */
public final class Constraint {

    private final ConstraintKind kind;
    private final String rawKey;
    private final Object value;

    Constraint(ConstraintKind kind, String rawKey, Object value) {
        this.kind = kind;
        this.rawKey = rawKey;
        this.value = value;
    }

    public static Constraint of(String key, Object value) {
        ConstraintKind kind = ConstraintKind.fromKey(key).orElse(ConstraintKind.UNKNOWN);
        return new Constraint(kind, key, value);
    }

    public ConstraintKind kind() {
        return kind;
    }

    /** The operator exactly as written; meaningful when the kind is UNKNOWN. */
    public String rawKey() {
        return rawKey;
    }

    public Object value() {
        return value;
    }

    /** A list form of a single-value constraint, for callers building messages. */
    public List<Object> asList() {
        return value instanceof List ? (List<Object>) value : java.util.Collections.singletonList(value);
    }

    @Override
    public String toString() {
        return rawKey + ": " + value;
    }
}
