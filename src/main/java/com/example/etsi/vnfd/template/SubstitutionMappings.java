package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@code substitution_mappings} of a topology template.
 *
 * <p>Two things a VNFD reader needs come from here. The deployment flavour identifier, via
 * {@link #substitutionFilter()} in the two-level design of SOL001 V5.4.1 clause 6.11.2. And which
 * internal connection points are exposed externally, via {@link #requirements()}.
 *
 * <p>{@link #propertyMappings()} holds the pre-3.3.1 {@code properties} grammar. SOL001 clause
 * 6.11.2 NOTE 1 records that the grammar changed to {@code substitution_filter} and that TOSCA
 * Simple Profile YAML 1.3 clause 3.8.8.3 specifies how to handle the previous form, so it is read
 * for backward compatibility and reported.
 */
public final class SubstitutionMappings {

    private String nodeType;
    private final List<PropertyFilter> substitutionFilter = new ArrayList<>();
    private final Map<String, Object> propertyMappings = new LinkedHashMap<>();
    private final Map<String, SubstitutionTarget> requirements = new LinkedHashMap<>();
    private final Map<String, SubstitutionTarget> capabilities = new LinkedHashMap<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final Map<String, InterfaceAssignment> interfaces = new LinkedHashMap<>();

    public SubstitutionMappings() {
    }

    /** The node type this service template can substitute for. */
    public String nodeType() {
        return nodeType;
    }

    /** Property filters, the current grammar for identifying the deployment flavour. */
    public List<PropertyFilter> substitutionFilter() {
        return substitutionFilter;
    }

    /** Legacy {@code properties} mappings, superseded by {@link #substitutionFilter()}. */
    public Map<String, Object> propertyMappings() {
        return propertyMappings;
    }

    /** Exposed requirements, keyed by the name the substituted node type declares. */
    public Map<String, SubstitutionTarget> requirements() {
        return requirements;
    }

    public Map<String, SubstitutionTarget> capabilities() {
        return capabilities;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    public Map<String, InterfaceAssignment> interfaces() {
        return interfaces;
    }

    /** The {@code equal} value of a named property filter, e.g. the flavour identifier. */
    public Optional<String> filterEqualValue(String propertyName) {
        return substitutionFilter.stream()
                .filter(f -> f.propertyName().equals(propertyName))
                .map(PropertyFilter::equalValue)
                .filter(java.util.Objects::nonNull)
                .map(String::valueOf)
                .findFirst();
    }

    /** Node template names exposed through any requirement mapping. */
    public List<String> exposedNodeTemplates() {
        List<String> names = new ArrayList<>();
        for (SubstitutionTarget target : requirements.values()) {
            if (target.nodeTemplateName() != null && !names.contains(target.nodeTemplateName())) {
                names.add(target.nodeTemplateName());
            }
        }
        return names;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String toString() {
        return "substitution_mappings(" + nodeType + ")";
    }
}
