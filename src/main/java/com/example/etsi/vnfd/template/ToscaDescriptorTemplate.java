package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One TOSCA service template file from the VNF package.
 *
 * <p>A VNFD is one or more of these. SOL001 V5.4.1 clause 6.11.2 describes the two-level design,
 * where a top-level template holds an abstract VNF node and each lower-level template represents
 * one deployment flavour; clause 6.11.3 describes the single-flavour alternative that uses one
 * template for everything.
 *
 * <p>{@link #isTopLevel()} reports which role this file plays. The distinction cannot be taken from
 * {@code Other-Definitions} alone, because packages also list type-definition files there; it is
 * decided by whether the file carries a topology template with substitution mappings.
 */
public final class ToscaDescriptorTemplate {

    private final String file;
    private String toscaDefinitionsVersion;
    private String description;
    private String namespace;
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, Object> dslDefinitions = new LinkedHashMap<>();
    private final List<String> imports = new ArrayList<>();
    private final Map<String, RepositoryDefinition> repositories = new LinkedHashMap<>();
    private final Map<String, Map<String, Object>> typeDefinitions = new LinkedHashMap<>();
    private TopologyTemplate topologyTemplate;
    private boolean topLevel;

    public ToscaDescriptorTemplate(String file) {
        this.file = file;
    }

    /** Package-internal path of this file. */
    public String file() {
        return file;
    }

    /**
     * The declared TOSCA version, e.g. {@code tosca_simple_yaml_1_3}.
     *
     * <p>Read rather than assumed: SOL004 V5.1.1 clause 4.1.1 permits a CSAR to follow TOSCA Simple
     * Profile YAML v1.1 or v1.3, and interface grammar differs between them.
     */
    public Optional<String> toscaDefinitionsVersion() {
        return Optional.ofNullable(toscaDefinitionsVersion);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<String> namespace() {
        return Optional.ofNullable(namespace);
    }

    /** Service template metadata; also how SOL004 clause 4.1.3.1 identifies a root-YAML CSAR. */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /** SOL001 clauses 6.11.2 e) and 6.11.3 e) permit these; carried verbatim. */
    public Map<String, Object> dslDefinitions() {
        return dslDefinitions;
    }

    /** Import references exactly as written, relative to this file. */
    public List<String> imports() {
        return imports;
    }

    public Map<String, RepositoryDefinition> repositories() {
        return repositories;
    }

    /**
     * Type definitions declared in this file, keyed by section
     * ({@code node_types}, {@code data_types}, {@code artifact_types}, and so on) then by type name.
     * Kept raw here; the type registry interprets them.
     */
    public Map<String, Map<String, Object>> typeDefinitions() {
        return typeDefinitions;
    }

    public Optional<TopologyTemplate> topologyTemplate() {
        return Optional.ofNullable(topologyTemplate);
    }

    /**
     * Whether this file is the top-level template of a two-level VNFD.
     *
     * <p>True for the entry template of a SOL001 clause 6.11.2 package, whose topology holds only
     * the abstract VNF node. False for a lower-level template, which represents one deployment
     * flavour and carries the VDUs.
     */
    public boolean isTopLevel() {
        return topLevel;
    }

    public void setToscaDefinitionsVersion(String toscaDefinitionsVersion) {
        this.toscaDefinitionsVersion = toscaDefinitionsVersion;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setTopologyTemplate(TopologyTemplate topologyTemplate) {
        this.topologyTemplate = topologyTemplate;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    @Override
    public String toString() {
        return file + (topLevel ? " (top level)" : "");
    }
}
