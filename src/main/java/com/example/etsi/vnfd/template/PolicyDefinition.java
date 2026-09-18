package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@code topology_template.policies} entry.
 *
 * <p>Policies carry a large share of the deployment-flavour information in a VNFD: instantiation
 * levels (SOL001 V5.4.1 clauses 6.10.1 and 6.10.2), scaling (6.10.5 to 6.10.9) and affinity
 * (6.10.10). SOL001 Table 6.1-1 NOTE 3 is explicit that
 * {@code MciopProfile.affinityOrAntiAffinityGroupId} comes from an affinity policy rather than
 * from the {@code Mciop} node, so a VNFD cannot be mapped correctly without reading these.
 */
public final class PolicyDefinition {

    private final String name;
    private String type;
    private String description;
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final List<String> targets = new ArrayList<>();
    private final Map<String, TriggerDefinition> triggers = new LinkedHashMap<>();
    private SourceRef source;

    public PolicyDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** Policy type, e.g. {@code tosca.policies.nfv.VduInstantiationLevels}. */
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

    /** Node template or group names this policy applies to. */
    public List<String> targets() {
        return targets;
    }

    /**
     * Triggers, used by {@code tosca.policies.nfv.VnfPackageChange} (SOL001 V5.4.1 clause 6.10.15)
     * to bind a {@code Vnflcm} notification to an operation call.
     */
    public Map<String, TriggerDefinition> triggers() {
        return triggers;
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
        return name + "(" + type + ") -> " + targets;
    }
}
