package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One notification of an interface assignment.
 *
 * <p>SOL001 V5.4.1 clause 6.7.1 states that {@code tosca.interfaces.nfv.Vnflcm} "also contains a
 * set of TOSCA notifications", and the official type definitions file declares three of them for
 * the Change current VNF package operation. A reader that models operations but not notifications
 * loses the half of {@code VnfPackageChange} that binds the policy trigger to the interface.
 */
public final class NotificationAssignment {

    private final String name;
    private String description;
    private ImplementationDefinition implementation;
    private final Map<String, Object> outputs = new LinkedHashMap<>();

    public NotificationAssignment(String name) {
        this.name = name;
    }

    /** Notification name, e.g. {@code change_current_package_notification}. */
    public String name() {
        return name;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ImplementationDefinition> implementation() {
        return Optional.ofNullable(implementation);
    }

    public Map<String, Object> outputs() {
        return outputs;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImplementation(ImplementationDefinition implementation) {
        this.implementation = implementation;
    }

    @Override
    public String toString() {
        return name;
    }
}
