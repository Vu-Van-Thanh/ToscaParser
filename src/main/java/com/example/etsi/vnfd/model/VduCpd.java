package com.example.etsi.vnfd.model;

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

    private VduCpd(Builder builder) {
        super(builder);
        this.vduId = builder.vduId;
        this.intVirtualLinkDesc = builder.intVirtualLinkDesc;
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

    /** Builder for {@link VduCpd}. */
    public static final class Builder extends Cpd.AbstractBuilder<Builder> {
        private String vduId;
        private String intVirtualLinkDesc;

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

        public VduCpd build() {
            return new VduCpd(this);
        }
    }
}
