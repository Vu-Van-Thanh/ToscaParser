package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code VnfVirtualLinkDesc}, IFA011 V5.4.1 clause 7.1.7.
 *
 * <p>An internal virtual link of the VNF. Its {@code vl_profile} contributes the
 * {@code VirtualLinkProfile} of the deployment flavour, clause 7.1.8.4, which is why that data is
 * kept separately on {@link VnfDf} rather than here.
 */
public final class VnfVirtualLinkDesc {

    private final String virtualLinkDescId;
    private final Map<String, Object> connectivityType;
    private final PropertyValue<String> description;
    private final Map<String, Object> nfviMaintenanceInfo;

    private VnfVirtualLinkDesc(Builder builder) {
        this.virtualLinkDescId = Objects.requireNonNull(builder.virtualLinkDescId, "virtualLinkDescId");
        this.connectivityType = builder.connectivityType;
        this.description = builder.description;
        this.nfviMaintenanceInfo = builder.nfviMaintenanceInfo;
    }

    public static Builder builder(String virtualLinkDescId) {
        return new Builder(virtualLinkDescId);
    }

    /** Mandatory. [ASSUMPTION] Taken from the node template name. */
    public String getVirtualLinkDescId() {
        return virtualLinkDescId;
    }

    /** Mandatory. Protocols exposed by the link and the flow pattern it supports. */
    public Map<String, Object> getConnectivityType() {
        return connectivityType == null ? Collections.emptyMap() : connectivityType;
    }

    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    public Map<String, Object> getNfviMaintenanceInfo() {
        return nfviMaintenanceInfo == null ? Collections.emptyMap() : nfviMaintenanceInfo;
    }

    @Override
    public String toString() {
        return virtualLinkDescId;
    }

    /** Builder for {@link VnfVirtualLinkDesc}. */
    public static final class Builder {
        private final String virtualLinkDescId;
        private Map<String, Object> connectivityType;
        private PropertyValue<String> description;
        private Map<String, Object> nfviMaintenanceInfo;

        private Builder(String virtualLinkDescId) {
            this.virtualLinkDescId = virtualLinkDescId;
        }

        public Builder connectivityType(Map<String, Object> value) {
            this.connectivityType = value;
            return this;
        }

        public Builder description(PropertyValue<String> value) {
            this.description = value;
            return this;
        }

        public Builder nfviMaintenanceInfo(Map<String, Object> value) {
            this.nfviMaintenanceInfo = value;
            return this;
        }

        public VnfVirtualLinkDesc build() {
            return new VnfVirtualLinkDesc(this);
        }
    }
}
