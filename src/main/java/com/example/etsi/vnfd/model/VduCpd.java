package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code VduCpd}, IFA011 V5.4.1 clause 7.1.6.4.
 *
 * <p>Connectivity between a VNFC instance and an internal virtual link. Comes from a
 * {@code tosca.nodes.nfv.VduCp} node template, SOL001 V5.4.1 clause 6.8.8.
 */
public final class VduCpd extends Cpd {

    private final String vduId;
    private final String intVirtualLinkDesc;
    private final PropertyValue<Integer> bitrateRequirement;
    private final PropertyValue<Integer> order;
    private final PropertyValue<String> vnicType;
    private final List<Map<String, Object>> virtualNetworkInterfaceRequirements;

    private VduCpd(Builder builder) {
        super(builder);
        this.vduId = builder.vduId;
        this.intVirtualLinkDesc = builder.intVirtualLinkDesc;
        this.bitrateRequirement = builder.bitrateRequirement;
        this.order = builder.order;
        this.vnicType = builder.vnicType;
        this.virtualNetworkInterfaceRequirements = Collections.unmodifiableList(
                new ArrayList<>(builder.virtualNetworkInterfaceRequirements));
    }

    public static Builder builder(String cpdId) {
        return new Builder(cpdId);
    }

    /** The VDU this connection point belongs to, from its {@code virtual_binding} requirement. */
    public Optional<String> getVduId() {
        return Optional.ofNullable(vduId);
    }

    /** The internal virtual link it attaches to, when it attaches to one. */
    public Optional<String> getIntVirtualLinkDesc() {
        return Optional.ofNullable(intVirtualLinkDesc);
    }

    /** SOL001 V5.4.1 clause 6.8.8: bitrate requirement in bit per second on this connection point. */
    public Optional<PropertyValue<Integer>> getBitrateRequirement() {
        return Optional.ofNullable(bitrateRequirement);
    }

    /** The order of the NIC on the compute instance, e.g. eth2. */
    public Optional<PropertyValue<Integer>> getOrder() {
        return Optional.ofNullable(order);
    }

    /** The type of the virtual network interface realizing the CPs instantiated from this CPD. */
    public Optional<PropertyValue<String>> getVnicType() {
        return Optional.ofNullable(vnicType);
    }

    /**
     * Requirements on a virtual network interface realising the CPs instantiated from this CPD -
     * carried as written, including nested {@code address_data} (e.g. {@code fixed_ip_address}).
     */
    public List<Map<String, Object>> getVirtualNetworkInterfaceRequirements() {
        return virtualNetworkInterfaceRequirements;
    }

    /** Builder for {@link VduCpd}. */
    public static final class Builder extends Cpd.AbstractBuilder<Builder> {
        private String vduId;
        private String intVirtualLinkDesc;
        private PropertyValue<Integer> bitrateRequirement;
        private PropertyValue<Integer> order;
        private PropertyValue<String> vnicType;
        private final List<Map<String, Object>> virtualNetworkInterfaceRequirements = new ArrayList<>();

        private Builder(String cpdId) {
            super(cpdId);
        }

        public Builder vduId(String value) {
            this.vduId = value;
            return this;
        }

        public Builder intVirtualLinkDesc(String value) {
            this.intVirtualLinkDesc = value;
            return this;
        }

        public Builder bitrateRequirement(PropertyValue<Integer> value) {
            this.bitrateRequirement = value;
            return this;
        }

        public Builder order(PropertyValue<Integer> value) {
            this.order = value;
            return this;
        }

        public Builder vnicType(PropertyValue<String> value) {
            this.vnicType = value;
            return this;
        }

        public Builder addVirtualNetworkInterfaceRequirement(Map<String, Object> value) {
            this.virtualNetworkInterfaceRequirements.add(value);
            return this;
        }

        public VduCpd build() {
            return new VduCpd(this);
        }
    }
}
