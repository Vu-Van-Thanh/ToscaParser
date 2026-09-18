package com.example.etsi.vnfd.template.value;

import java.util.Objects;
import java.util.Optional;

/** A property value given directly in the descriptor, with no TOSCA function involved. */
public final class Literal<T> implements PropertyValue<T> {

    private final T value;
    private final Object raw;

    private Literal(T value, Object raw) {
        this.value = value;
        this.raw = raw;
    }

    /** Wraps a value that was written literally in the YAML. */
    public static <T> Literal<T> of(T value) {
        return new Literal<>(value, value);
    }

    /**
     * Wraps a value that was converted from the YAML form, keeping the original for traceability.
     * Example: {@code "128 MiB"} in YAML becomes a {@link Quantity} here, with {@code raw} still
     * holding the string.
     */
    public static <T> Literal<T> of(T value, Object raw) {
        return new Literal<>(value, raw);
    }

    @Override
    public Kind kind() {
        return Kind.LITERAL;
    }

    @Override
    public Resolution resolution() {
        return Resolution.RESOLVED_STATIC;
    }

    @Override
    public Optional<T> resolved() {
        return Optional.ofNullable(value);
    }

    @Override
    public Object raw() {
        return raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Literal)) {
            return false;
        }
        Literal<?> other = (Literal<?>) o;
        return Objects.equals(value, other.value) && Objects.equals(raw, other.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, raw);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
