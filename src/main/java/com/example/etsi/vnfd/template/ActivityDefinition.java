package com.example.etsi.vnfd.template;

import java.util.Optional;

/**
 * One activity inside the {@code action} list of a policy trigger.
 *
 * <p>TOSCA 1.3 defines three forms: {@code call_operation}, {@code set_state} and {@code inline}.
 * SOL001 V5.4.1 uses {@code call_operation}.
 */
public final class ActivityDefinition {

    /** Which of the three TOSCA activity forms this is. */
    public enum Kind {
        CALL_OPERATION,
        SET_STATE,
        INLINE,
        /** A form this version of TOSCA does not define; retained verbatim. */
        UNKNOWN
    }

    private final Kind kind;
    private final String rawKey;
    private final Object value;

    public ActivityDefinition(Kind kind, String rawKey, Object value) {
        this.kind = kind;
        this.rawKey = rawKey;
        this.value = value;
    }

    public Kind kind() {
        return kind;
    }

    /** The activity keyname exactly as written. */
    public String rawKey() {
        return rawKey;
    }

    /**
     * The activity argument as written: for {@code call_operation} either an operation name or a
     * map carrying an operation plus inputs.
     */
    public Object value() {
        return value;
    }

    /** The operation name, when this is a simple call_operation with a plain string argument. */
    public Optional<String> operationName() {
        if (kind == Kind.CALL_OPERATION && value instanceof String) {
            return Optional.of((String) value);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return rawKey + ": " + value;
    }
}
