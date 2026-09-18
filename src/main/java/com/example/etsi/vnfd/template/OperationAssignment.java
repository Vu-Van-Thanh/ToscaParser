package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One operation of an interface assignment.
 *
 * <p>The presence of {@link #implementation()} is what separates the two things a VNFD expresses
 * through {@code Vnflcm}:
 * <ul>
 *   <li>an operation <em>with</em> an implementation yields a {@code LifeCycleManagementScript}
 *       (IFA011 V5.4.1 clause 7.1.13.2);</li>
 *   <li>an operation with only {@code inputs.additional_parameters} feeds
 *       {@code VnfLcmOperationsConfiguration} instead, per SOL001 V5.4.1 Table A.9.2-1, which maps
 *       that information element to a VNF node property "and/or inputs additional_parameters of the
 *       corresponding operation in the Vnflcm interface".</li>
 * </ul>
 */
public final class OperationAssignment {

    /** The input carrying VNF-specific operation parameters, per SOL001 Table A.9.2-1. */
    public static final String INPUT_ADDITIONAL_PARAMETERS = "additional_parameters";

    private final String name;
    private String description;
    private ImplementationDefinition implementation;
    private final Map<String, Object> inputs = new LinkedHashMap<>();
    private final Map<String, Object> outputs = new LinkedHashMap<>();

    public OperationAssignment(String name) {
        this.name = name;
    }

    /** Operation name, e.g. {@code instantiate}, {@code instantiate_start}, {@code scale_end}. */
    public String name() {
        return name;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ImplementationDefinition> implementation() {
        return Optional.ofNullable(implementation);
    }

    public Map<String, Object> inputs() {
        return inputs;
    }

    public Map<String, Object> outputs() {
        return outputs;
    }

    /** The {@code additional_parameters} input, when declared. */
    public Optional<Object> additionalParameters() {
        return Optional.ofNullable(inputs.get(INPUT_ADDITIONAL_PARAMETERS));
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImplementation(ImplementationDefinition implementation) {
        this.implementation = implementation;
    }

    @Override
    public String toString() {
        return name + (implementation == null ? "" : " -> " + implementation);
    }
}
