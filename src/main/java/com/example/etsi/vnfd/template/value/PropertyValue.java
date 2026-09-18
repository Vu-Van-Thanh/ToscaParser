package com.example.etsi.vnfd.template.value;

import java.util.Optional;

/**
 * The value of a TOSCA property: either a literal or a TOSCA function call.
 *
 * <p>Every property read out of a descriptor is modelled this way rather than as a bare {@code T},
 * because SOL001 V5.4.1 clause 5.9 permits a function anywhere a value is expected. Callers that
 * need a concrete value must go through {@link #resolved()} and handle the empty case.
 *
 * @param <T> the Java type the value resolves to when it is a literal or a statically resolvable
 *            function
 */
public interface PropertyValue<T> {

    /** Literal or function. */
    Kind kind();

    /** How far this value could be resolved at parse time. */
    Resolution resolution();

    /**
     * The final value, present only when {@link #resolution()} is
     * {@link Resolution#RESOLVED_STATIC}.
     */
    Optional<T> resolved();

    /**
     * The original YAML structure this value was parsed from, always retained.
     * Needed for round-tripping, for debugging, and for re-evaluating a function later with an
     * instantiation request in hand.
     */
    Object raw();

    /** Convenience: this value carries a usable value right now. */
    default boolean isResolved() {
        return resolution() == Resolution.RESOLVED_STATIC && resolved().isPresent();
    }

    /** Convenience: the value resolves only once a VNF LCM request or instance exists. */
    default boolean isDeferred() {
        return resolution() == Resolution.INPUT_BOUND || resolution() == Resolution.RUNTIME_BOUND;
    }
}
