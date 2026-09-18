package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@code VipCpd}, IFA011 V5.4.1 clause 7.1.17.2.
 *
 * <p>A requirement to allocate one or a set of virtual IP addresses. Clause 7.1.17.2.1: instances of
 * VduCps created from a VduCpd named by {@code intCpd} "are able to communicate via the addresses
 * associated to the VipCp instance created from the VipCpd".
 *
 * <p>Comes from a {@code tosca.nodes.nfv.VipCp} node template, SOL001 V5.4.1 clause 6.8.10, whose
 * {@code target} requirement points at VduCp nodes rather than at a VDU.
 */
public final class VipCpd extends Cpd {

    private final List<String> intCpd;
    private final String intVirtualLinkDesc;
    private final PropertyValue<Boolean> dedicatedIpAddress;
    private final PropertyValue<String> vipFunction;

    private VipCpd(Builder builder) {
        super(builder);
        this.intCpd = Collections.unmodifiableList(new ArrayList<>(builder.intCpd));
        this.intVirtualLinkDesc = builder.intVirtualLinkDesc;
        this.dedicatedIpAddress = builder.dedicatedIpAddress;
        this.vipFunction = builder.vipFunction;
    }

    public static Builder builder(String cpdId) {
        return new Builder(cpdId);
    }

    /**
     * The internal VDU CPDs sharing these addresses. Mandatory, 1..N.
     *
     * <p>Clause 7.1.17.2.2 NOTE 3: if more than one is named, {@code intVirtualLinkDesc} shall
     * either be present with the same value in all of them, or absent in all.
     */
    public List<String> getIntCpd() {
        return intCpd;
    }

    /** The internal virtual link this connects to. 0..1. */
    public Optional<String> getIntVirtualLinkDesc() {
        return Optional.ofNullable(intVirtualLinkDesc);
    }

    /**
     * Whether the VIP address differs from every address of the VduCp instances associated with it.
     * 0..1.
     */
    public Optional<PropertyValue<Boolean>> getDedicatedIpAddress() {
        return Optional.ofNullable(dedicatedIpAddress);
    }

    /**
     * What the virtual IP address is used for. Mandatory, 1.
     *
     * <p>IFA011 values are "high availability" and "load balancing"; SOL001 clause 6.8.10 spells the
     * same two {@code high_availability} and {@code load_balance}, and this carries what the
     * descriptor wrote.
     */
    public Optional<PropertyValue<String>> getVipFunction() {
        return Optional.ofNullable(vipFunction);
    }

    @Override
    public String toString() {
        return "VipCpd(" + getCpdId() + ", intCpd=" + intCpd + ")";
    }

    /** Builder for {@link VipCpd}. */
    public static final class Builder extends Cpd.AbstractBuilder<Builder> {
        private final List<String> intCpd = new ArrayList<>();
        private String intVirtualLinkDesc;
        private PropertyValue<Boolean> dedicatedIpAddress;
        private PropertyValue<String> vipFunction;

        private Builder(String cpdId) {
            super(cpdId);
        }

        public Builder addIntCpd(String value) {
            intCpd.add(value);
            return this;
        }

        public Builder intVirtualLinkDesc(String value) {
            this.intVirtualLinkDesc = value;
            return this;
        }

        public Builder dedicatedIpAddress(PropertyValue<Boolean> value) {
            this.dedicatedIpAddress = value;
            return this;
        }

        public Builder vipFunction(PropertyValue<String> value) {
            this.vipFunction = value;
            return this;
        }

        public VipCpd build() {
            return new VipCpd(this);
        }
    }
}
