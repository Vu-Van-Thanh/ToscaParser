package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code OsContainerDesc}, IFA011 V5.4.1 clause 7.1.6.13.
 *
 * <p>The CPU, memory and image of one container. Present only where the VNFD describes the
 * container itself; a deployment flavour that leaves that to a Helm chart produces none of these,
 * and the corresponding information is simply not in the descriptor.
 *
 * <p>{@code swImageDesc} is mandatory with cardinality 1, matching SOL001 clause 6.8.12.6, which
 * requires a {@code Vdu.OsContainer} node template to carry exactly one SwImage artifact.
 *
 * <p>Units differ by attribute: CPU figures are plain integers in milli-CPU, memory and storage are
 * sizes. Keeping the quantity rather than a bare number preserves whether the provider wrote MB or
 * MiB, which differ by about seven percent.
 */
public final class OsContainerDesc {

    private final String osContainerDescId;
    private final PropertyValue<String> name;
    private final PropertyValue<String> description;
    private final PropertyValue<Integer> requestedCpuResources;
    private final PropertyValue<Integer> cpuResourceLimit;
    private final PropertyValue<Quantity> requestedMemoryResources;
    private final PropertyValue<Quantity> memoryResourceLimit;
    private final PropertyValue<Quantity> requestedEphemeralStorageResources;
    private final PropertyValue<Quantity> ephemeralStorageResourceLimit;
    private final List<Map<String, Object>> extendedResourceRequests;
    private final List<Map<String, Object>> hugePageResources;
    private final Map<String, Object> cpuPinningRequirements;
    private final String swImageDesc;

    private OsContainerDesc(Builder builder) {
        this.osContainerDescId = Objects.requireNonNull(builder.osContainerDescId, "osContainerDescId");
        this.name = builder.name;
        this.description = builder.description;
        this.requestedCpuResources = builder.requestedCpuResources;
        this.cpuResourceLimit = builder.cpuResourceLimit;
        this.requestedMemoryResources = builder.requestedMemoryResources;
        this.memoryResourceLimit = builder.memoryResourceLimit;
        this.requestedEphemeralStorageResources = builder.requestedEphemeralStorageResources;
        this.ephemeralStorageResourceLimit = builder.ephemeralStorageResourceLimit;
        this.extendedResourceRequests =
                Collections.unmodifiableList(new ArrayList<>(builder.extendedResourceRequests));
        this.hugePageResources =
                Collections.unmodifiableList(new ArrayList<>(builder.hugePageResources));
        this.cpuPinningRequirements = builder.cpuPinningRequirements;
        this.swImageDesc = builder.swImageDesc;
    }

    public static Builder builder(String osContainerDescId) {
        return new Builder(osContainerDescId);
    }

    /**
     * Mandatory. Unique identifier of this OsContainerDesc in the VNFD.
     *
     * <p>[ASSUMPTION] Taken from the {@code Vdu.OsContainer} node template name. SOL001 states that
     * rule explicitly for {@code SwImageDesc.id} in clause 6.8.12.6 and for
     * {@code virtualComputeDescId} in clause 6.8.3.7, but not for this attribute; the node template
     * name is nonetheless the only stable identifier the descriptor offers.
     */
    public String getOsContainerDescId() {
        return osContainerDescId;
    }

    public Optional<PropertyValue<String>> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    /** Milli-CPU requested. */
    public Optional<PropertyValue<Integer>> getRequestedCpuResources() {
        return Optional.ofNullable(requestedCpuResources);
    }

    /** Milli-CPU ceiling. */
    public Optional<PropertyValue<Integer>> getCpuResourceLimit() {
        return Optional.ofNullable(cpuResourceLimit);
    }

    public Optional<PropertyValue<Quantity>> getRequestedMemoryResources() {
        return Optional.ofNullable(requestedMemoryResources);
    }

    /**
     * Memory ceiling.
     *
     * <p>SOL018 V5.4.1 Table 6.2.2.1-5 maps this onto {@code Container.resources.limits.memory},
     * but only where the CISM realises the container directly. Where a Helm chart deploys it, the
     * chart carries its own limits and nothing reconciles the two.
     */
    public Optional<PropertyValue<Quantity>> getMemoryResourceLimit() {
        return Optional.ofNullable(memoryResourceLimit);
    }

    public Optional<PropertyValue<Quantity>> getRequestedEphemeralStorageResources() {
        return Optional.ofNullable(requestedEphemeralStorageResources);
    }

    public Optional<PropertyValue<Quantity>> getEphemeralStorageResourceLimit() {
        return Optional.ofNullable(ephemeralStorageResourceLimit);
    }

    public List<Map<String, Object>> getExtendedResourceRequests() {
        return extendedResourceRequests;
    }

    public List<Map<String, Object>> getHugePageResources() {
        return hugePageResources;
    }

    public Map<String, Object> getCpuPinningRequirements() {
        return cpuPinningRequirements == null ? Collections.emptyMap() : cpuPinningRequirements;
    }

    /** Mandatory, cardinality 1. References the software image realising this container. */
    public Optional<String> getSwImageDesc() {
        return Optional.ofNullable(swImageDesc);
    }

    @Override
    public String toString() {
        return osContainerDescId;
    }

    /** Builder for {@link OsContainerDesc}. */
    public static final class Builder {
        private final String osContainerDescId;
        private PropertyValue<String> name;
        private PropertyValue<String> description;
        private PropertyValue<Integer> requestedCpuResources;
        private PropertyValue<Integer> cpuResourceLimit;
        private PropertyValue<Quantity> requestedMemoryResources;
        private PropertyValue<Quantity> memoryResourceLimit;
        private PropertyValue<Quantity> requestedEphemeralStorageResources;
        private PropertyValue<Quantity> ephemeralStorageResourceLimit;
        private final List<Map<String, Object>> extendedResourceRequests = new ArrayList<>();
        private final List<Map<String, Object>> hugePageResources = new ArrayList<>();
        private Map<String, Object> cpuPinningRequirements;
        private String swImageDesc;

        private Builder(String osContainerDescId) {
            this.osContainerDescId = osContainerDescId;
        }

        public Builder name(PropertyValue<String> value) {
            this.name = value;
            return this;
        }

        public Builder description(PropertyValue<String> value) {
            this.description = value;
            return this;
        }

        public Builder requestedCpuResources(PropertyValue<Integer> value) {
            this.requestedCpuResources = value;
            return this;
        }

        public Builder cpuResourceLimit(PropertyValue<Integer> value) {
            this.cpuResourceLimit = value;
            return this;
        }

        public Builder requestedMemoryResources(PropertyValue<Quantity> value) {
            this.requestedMemoryResources = value;
            return this;
        }

        public Builder memoryResourceLimit(PropertyValue<Quantity> value) {
            this.memoryResourceLimit = value;
            return this;
        }

        public Builder requestedEphemeralStorageResources(PropertyValue<Quantity> value) {
            this.requestedEphemeralStorageResources = value;
            return this;
        }

        public Builder ephemeralStorageResourceLimit(PropertyValue<Quantity> value) {
            this.ephemeralStorageResourceLimit = value;
            return this;
        }

        public Builder addExtendedResourceRequest(Map<String, Object> value) {
            this.extendedResourceRequests.add(value);
            return this;
        }

        public Builder addHugePageResource(Map<String, Object> value) {
            this.hugePageResources.add(value);
            return this;
        }

        public Builder cpuPinningRequirements(Map<String, Object> value) {
            this.cpuPinningRequirements = value;
            return this;
        }

        public Builder swImageDesc(String value) {
            this.swImageDesc = value;
            return this;
        }

        public OsContainerDesc build() {
            return new OsContainerDesc(this);
        }
    }
}
