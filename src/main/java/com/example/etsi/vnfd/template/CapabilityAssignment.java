package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A capability assignment inside a node template: values supplied for a capability the type
 * declares.
 *
 * <p>This is where the VM flow keeps {@code VirtualComputeDesc} - SOL001 V5.4.1 Table A.9.2-1 maps
 * that information element to "VirtualCompute capability of the Vdu.Compute node template" rather
 * than to a node. The CNF flow does not use it, but the shape is supported so the reader does not
 * lose data when it meets a mixed package.
 */
public final class CapabilityAssignment {

    private final String name;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public CapabilityAssignment(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return name + properties;
    }
}
