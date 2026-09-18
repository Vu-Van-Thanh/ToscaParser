package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@code topology_template.inputs} or {@code outputs} entry.
 *
 * <p>Inputs are the declaration side of {@code get_input}. A {@code get_input} naming a parameter
 * that is not declared here is an error in the descriptor, which is the only check this library can
 * make about inputs: their values arrive with a VNF LCM request, long after onboarding.
 */
public final class ParameterDefinition {

    private final String name;
    private String type;
    private String description;
    private Object defaultValue;
    private Boolean required;
    private String status;
    private Object value;
    private final List<Object> constraints = new ArrayList<>();
    private Object entrySchema;

    public ParameterDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Optional<String> type() {
        return Optional.ofNullable(type);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** The declared default, used when a VNF LCM request omits the parameter. */
    public Optional<Object> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public Optional<Boolean> required() {
        return Optional.ofNullable(required);
    }

    public Optional<String> status() {
        return Optional.ofNullable(status);
    }

    /** Fixed value, used by outputs. */
    public Optional<Object> value() {
        return Optional.ofNullable(value);
    }

    public List<Object> constraints() {
        return constraints;
    }

    public Optional<Object> entrySchema() {
        return Optional.ofNullable(entrySchema);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setEntrySchema(Object entrySchema) {
        this.entrySchema = entrySchema;
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}
