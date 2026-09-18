package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code VirtualLinkProfile}, IFA011 V5.4.1 clause 7.1.8.4.
 *
 * <p>Per-flavour data for one internal virtual link: bitrate requirements, QoS, and any affinity
 * groups the link belongs to. Comes from the {@code vl_profile} property of the corresponding
 * {@code VnfVirtualLink} node template plus any affinity policy targeting it.
 */
public final class VirtualLinkProfile {

    private final String virtualLinkDescId;
    private final Map<String, Object> maxBitrateRequirements;
    private final Map<String, Object> minBitrateRequirements;
    private final Map<String, Object> qos;
    private final List<String> affinityOrAntiAffinityGroupId;

    private VirtualLinkProfile(Builder builder) {
        this.virtualLinkDescId = Objects.requireNonNull(builder.virtualLinkDescId, "virtualLinkDescId");
        this.maxBitrateRequirements = builder.maxBitrateRequirements;
        this.minBitrateRequirements = builder.minBitrateRequirements;
        this.qos = builder.qos;
        this.affinityOrAntiAffinityGroupId =
                Collections.unmodifiableList(new ArrayList<>(builder.affinityOrAntiAffinityGroupId));
    }

    public static Builder builder(String virtualLinkDescId) {
        return new Builder(virtualLinkDescId);
    }

    /** Mandatory. References the virtual link descriptor. */
    public String getVirtualLinkDescId() {
        return virtualLinkDescId;
    }

    /** Required by the TOSCA data type. */
    public Map<String, Object> getMaxBitrateRequirements() {
        return maxBitrateRequirements == null ? Collections.emptyMap() : maxBitrateRequirements;
    }

    /** Required by the TOSCA data type. */
    public Map<String, Object> getMinBitrateRequirements() {
        return minBitrateRequirements == null ? Collections.emptyMap() : minBitrateRequirements;
    }

    public Map<String, Object> getQos() {
        return qos == null ? Collections.emptyMap() : qos;
    }

    /** Filled from affinity policies targeting the virtual link. */
    public List<String> getAffinityOrAntiAffinityGroupId() {
        return affinityOrAntiAffinityGroupId;
    }

    @Override
    public String toString() {
        return virtualLinkDescId;
    }

    /** Builder for {@link VirtualLinkProfile}. */
    public static final class Builder {
        private final String virtualLinkDescId;
        private Map<String, Object> maxBitrateRequirements;
        private Map<String, Object> minBitrateRequirements;
        private Map<String, Object> qos;
        private final List<String> affinityOrAntiAffinityGroupId = new ArrayList<>();

        private Builder(String virtualLinkDescId) {
            this.virtualLinkDescId = virtualLinkDescId;
        }

        public Builder maxBitrateRequirements(Map<String, Object> value) {
            this.maxBitrateRequirements = value;
            return this;
        }

        public Builder minBitrateRequirements(Map<String, Object> value) {
            this.minBitrateRequirements = value;
            return this;
        }

        public Builder qos(Map<String, Object> value) {
            this.qos = value;
            return this;
        }

        public Builder addAffinityGroup(String groupId) {
            if (!affinityOrAntiAffinityGroupId.contains(groupId)) {
                affinityOrAntiAffinityGroupId.add(groupId);
            }
            return this;
        }

        public VirtualLinkProfile build() {
            return new VirtualLinkProfile(this);
        }
    }
}
