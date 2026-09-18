package com.example.etsi.vnfd.model;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code AffinityOrAntiAffinityGroup}, IFA011 V5.4.1 clause 7.1.8.12.
 *
 * <p>Built from a {@code tosca.policies.nfv.AffinityRule} or {@code AntiAffinityRule} policy, per
 * SOL001 V5.4.1 Table 6.1-1 NOTE 3. The members are not held here: each targeted element records
 * the group identifier on its own profile, which is how IFA011 expresses the relation.
 */
public final class AffinityOrAntiAffinityGroup {

    /** Whether members are kept together or apart. */
    public enum AffinityType {
        AFFINITY,
        ANTI_AFFINITY
    }

    private final String groupId;
    private final AffinityType type;
    private final String scope;

    private AffinityOrAntiAffinityGroup(String groupId, AffinityType type, String scope) {
        this.groupId = Objects.requireNonNull(groupId, "groupId");
        this.type = Objects.requireNonNull(type, "type");
        this.scope = scope;
    }

    public static AffinityOrAntiAffinityGroup of(String groupId, AffinityType type, String scope) {
        return new AffinityOrAntiAffinityGroup(groupId, type, scope);
    }

    /**
     * Mandatory. Identifier referenced from the profiles of the group members.
     *
     * <p>[ASSUMPTION] Taken from the policy name; the policy type declares no identifier property.
     */
    public String getGroupId() {
        return groupId;
    }

    /** Mandatory. Derived from which of the two policy types was used. */
    public AffinityType getType() {
        return type;
    }

    /**
     * Mandatory. One of nfvi_node, zone, zone_group, nfvi_pop, network_link_and_node,
     * container_namespace or cis_node.
     */
    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    @Override
    public String toString() {
        return groupId + " (" + type + ", " + scope + ")";
    }
}
