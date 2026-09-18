package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.template.TriggerDefinition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The keynames every TOSCA policy definition has, whatever its type.
 *
 * <p>TOSCA Simple Profile YAML 1.3 clause 3.7.11. {@code properties} differs per policy type and is
 * declared by each subclass; the rest have the same shape everywhere.
 *
 * <p>Policies carry what a deployment flavour cannot be built without. Instantiation levels, scaling
 * aspects and affinity groups live here rather than on node templates, and SOL001 V5.4.1 Table 6.1-1
 * NOTE 3 is explicit that {@code MciopProfile.affinityOrAntiAffinityGroupId} comes from an affinity
 * policy rather than from the {@code Mciop} node itself.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"key", "type", "targets"})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NfvPolicy {

    /** The policy name, i.e. its key under {@code policies}. Set by the binder. */
    @JsonIgnore
    private String key;

    /** The ETSI type recognised after walking {@code derived_from}. Set by the binder. */
    @JsonIgnore
    private String etsiType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Node template or group names this policy applies to.
     *
     * <p>A target may name a group rather than a node, so anything resolving affinity has to expand
     * group membership before matching against VDUs or MCIOPs.
     */
    @JsonProperty("targets")
    private List<String> targets;

    /** Used by {@code tosca.policies.nfv.VnfPackageChange} (SOL001 clause 6.10.15). */
    @JsonProperty("triggers")
    private Map<String, TriggerDefinition> triggers;
}
