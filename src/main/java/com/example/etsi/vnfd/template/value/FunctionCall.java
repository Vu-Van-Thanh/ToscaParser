package com.example.etsi.vnfd.template.value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A property value expressed as a TOSCA function, e.g. {@code { get_input: image_tag }}.
 *
 * <p>Arguments are themselves {@link PropertyValue}s so nested functions round-trip, for example
 * {@code { concat: [ "v", { get_input: tag } ] }}.
 *
 * <p>A call starts out unresolved and is replaced by a resolved copy once an evaluator has run over
 * it. Instances are immutable; {@link #resolvedAs} and {@link #deferredAs} produce new ones.
 */
public final class FunctionCall<T> implements PropertyValue<T> {

    private final FunctionName name;
    /** The raw function key, kept verbatim; meaningful when {@code name} is {@link FunctionName#UNKNOWN}. */
    private final String rawKey;
    private final List<PropertyValue<?>> args;
    private final Resolution resolution;
    private final T value;
    private final Object raw;

    private FunctionCall(FunctionName name, String rawKey, List<PropertyValue<?>> args,
                         Resolution resolution, T value, Object raw) {
        this.name = name;
        this.rawKey = rawKey;
        this.args = Collections.unmodifiableList(args);
        this.resolution = resolution;
        this.value = value;
        this.raw = raw;
    }

    /**
     * Creates a not-yet-evaluated call.
     *
     * <p>{@code get_input} and {@code get_attribute} are tagged at construction: SOL001 V5.4.1
     * clause 5.9 NOTE 1 and NOTE 2 make clear these depend on a VNF LCM request or a running
     * instance, neither of which exists while parsing a descriptor. They are never evaluated here.
     */
    public static <T> FunctionCall<T> unresolved(FunctionName name, String rawKey,
                                                 List<PropertyValue<?>> args, Object raw) {
        Resolution initial;
        switch (name) {
            case GET_INPUT:
                initial = Resolution.INPUT_BOUND;
                break;
            case GET_ATTRIBUTE:
                initial = Resolution.RUNTIME_BOUND;
                break;
            default:
                initial = Resolution.UNRESOLVABLE;
                break;
        }
        return new FunctionCall<>(name, rawKey, args, initial, null, raw);
    }

    /** A copy of this call carrying a statically computed value. */
    public <R> FunctionCall<R> resolvedAs(R newValue) {
        return new FunctionCall<>(name, rawKey, args, Resolution.RESOLVED_STATIC, newValue, raw);
    }

    /**
     * A copy of this call carrying a deferred resolution propagated from an argument.
     * Used when e.g. a {@code concat} contains a {@code get_input}: the whole expression becomes
     * input-bound, but the argument tree is preserved so it can be rendered later.
     */
    public FunctionCall<T> deferredAs(Resolution newResolution) {
        return new FunctionCall<>(name, rawKey, args, newResolution, null, raw);
    }

    public FunctionName name() {
        return name;
    }

    /** The function key exactly as written in the descriptor. */
    public String rawKey() {
        return rawKey;
    }

    public List<PropertyValue<?>> args() {
        return args;
    }

    @Override
    public Kind kind() {
        return Kind.FUNCTION;
    }

    @Override
    public Resolution resolution() {
        return resolution;
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
    public String toString() {
        return "{" + rawKey + ": " + args + "}[" + resolution + "]";
    }
}
