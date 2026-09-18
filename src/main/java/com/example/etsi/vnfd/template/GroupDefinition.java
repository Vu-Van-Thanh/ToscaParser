package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@code topology_template.groups} entry.
 *
 * <p>SOL001 V5.4.1 defines {@code tosca.groups.nfv.PlacementGroup}, whose members may be
 * {@code Vdu.Compute}, {@code Vdu.OsContainerDeployableUnit}, {@code VnfVirtualLink} or
 * {@code Mciop}. An affinity policy may target the group instead of the nodes directly, so
 * resolving affinity means expanding group membership first.
 */
public final class GroupDefinition {

    private final String name;
    private String type;
    private String description;
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final List<String> members = new ArrayList<>();
    private SourceRef source;

    public GroupDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    /** Node template names belonging to this group. */
    public List<String> members() {
        return members;
    }

    public SourceRef source() {
        return source;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSource(SourceRef source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return name + "(" + type + ") " + members;
    }
}
