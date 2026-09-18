package com.example.etsi.vnfd.template.value;

/**
 * How far a {@link PropertyValue} could be resolved at descriptor-parse time.
 *
 * <p>The parser never resolves {@code get_input} / {@code get_attribute}: at onboarding time no VNF
 * instance and no instantiation request exist yet, so those values are only <em>tagged</em>.
 */
public enum Resolution {

    /** A final value is available; {@link PropertyValue#resolved()} is present. */
    RESOLVED_STATIC,

    /**
     * Depends on {@code get_input}, i.e. on parameters supplied with a VNF LCM request.
     * Never resolved by this library.
     */
    INPUT_BOUND,

    /**
     * Depends on {@code get_attribute}, i.e. on a running VNF instance.
     * SOL001 V5.4.1 Table 5.9-1 NOTE 2 restricts this to {@code scale_status} and VNF indicator
     * attributes. Never resolved by this library.
     */
    RUNTIME_BOUND,

    /** The function references something that does not exist in the descriptor. Raises a finding. */
    UNRESOLVABLE
}
