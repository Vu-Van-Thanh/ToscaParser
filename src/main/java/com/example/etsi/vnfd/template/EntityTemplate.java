package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fields shared by every TOSCA template entity: node templates, relationship templates, policies
 * and groups.
 *
 * <p>The grouping follows TOSCA Simple Profile YAML 1.3 and matches the {@code EntityTemplate}
 * abstraction used by the OpenStack {@code tosca-parser} reference implementation, whose
 * {@code SECTIONS} tuple is {@code derived_from, properties, requirements, interfaces, capabilities,
 * type, description, directives, attributes, artifacts, node_filter, copy}. Keeping them in one
 * place is what stops keynames such as {@code directives} being forgotten on one entity and
 * supported on another.
 *
 * <p>This is the <em>syntax</em> layer: values stay as the YAML loader produced them
 * ({@code String}, {@code List}, {@code Map}). Interpreting them - resolving defaults from the type,
 * detecting TOSCA functions, parsing scalar units - happens one layer up, where type information is
 * available.
 */
public abstract class EntityTemplate {

    private final String name;
    private String type;
    private String description;
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final Map<String, InterfaceAssignment> interfaces = new LinkedHashMap<>();
    private List<String> directives;
    private String copy;
    private NodeFilter nodeFilter;
    private SourceRef source;

    protected EntityTemplate(String name) {
        this.name = name;
    }

    /** The key under which this entity appears in the template, e.g. the node template name. */
    public String name() {
        return name;
    }

    /** The declared type, verbatim. May be a vendor type derived from an ETSI one. */
    public String type() {
        return type;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    /** Property assignments as written, before any type-aware interpretation. */
    public Map<String, Object> properties() {
        return properties;
    }

    /**
     * Attribute assignments. Needed because {@code tosca.nodes.nfv.VNF} declares the
     * {@code scale_status} attribute, which SOL001 V5.4.1 Table 5.9-1 NOTE 2 allows
     * {@code get_attribute} to reference.
     */
    public Map<String, Object> attributes() {
        return attributes;
    }

    public Map<String, InterfaceAssignment> interfaces() {
        return interfaces;
    }

    /**
     * Processing directives. SOL001 V5.4.1 clause 6.11.2 allows the top-level VNF node template to
     * "include a substitute directive", and requires a consumer that does not support explicit
     * directives to ignore it silently.
     */
    public List<String> directives() {
        return directives;
    }

    /** {@code copy} keyname: another template this one is derived from. */
    public Optional<String> copy() {
        return Optional.ofNullable(copy);
    }

    public Optional<NodeFilter> nodeFilter() {
        return Optional.ofNullable(nodeFilter);
    }

    /** Where this entity was declared. */
    public SourceRef source() {
        return source;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDirectives(List<String> directives) {
        this.directives = directives;
    }

    public void setCopy(String copy) {
        this.copy = copy;
    }

    public void setNodeFilter(NodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }

    public void setSource(SourceRef source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + name + ": " + type + "}";
    }
}
