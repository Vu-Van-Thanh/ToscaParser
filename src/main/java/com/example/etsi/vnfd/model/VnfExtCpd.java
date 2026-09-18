package com.example.etsi.vnfd.model;

import java.util.Optional;

/**
 * {@code VnfExtCpd}, IFA011 V5.4.1 clause 7.1.4.
 *
 * <p>An external connection point of the VNF. It need not come from a
 * {@code tosca.nodes.nfv.VnfExtCp} node: SOL001 V5.4.1 Table A.9.2-1 maps this element to a node
 * template of type {@code VnfExtCp}, {@code VduCp} or {@code VipCp}, so a {@code VduCp} exposed
 * through {@code substitution_mappings} becomes one too. {@link #getIntCpd()} records that case.
 */
public final class VnfExtCpd extends Cpd {

    private final String intCpd;
    private final String intVirtualLinkDesc;
    private final boolean exposedThroughSubstitution;

    private VnfExtCpd(Builder builder) {
        super(builder);
        this.intCpd = builder.intCpd;
        this.intVirtualLinkDesc = builder.intVirtualLinkDesc;
        this.exposedThroughSubstitution = builder.exposedThroughSubstitution;
    }

    public static Builder builder(String cpdId) {
        return new Builder(cpdId);
    }

    /** The internal connection point this external one exposes, when it re-exposes one. */
    public Optional<String> getIntCpd() {
        return Optional.ofNullable(intCpd);
    }

    public Optional<String> getIntVirtualLinkDesc() {
        return Optional.ofNullable(intVirtualLinkDesc);
    }

    /**
     * Whether this descriptor exists because {@code substitution_mappings} exposed an internal
     * connection point, rather than because the descriptor declared a {@code VnfExtCp} node.
     */
    public boolean isExposedThroughSubstitution() {
        return exposedThroughSubstitution;
    }

    /** Builder for {@link VnfExtCpd}. */
    public static final class Builder extends Cpd.AbstractBuilder<Builder> {
        private String intCpd;
        private String intVirtualLinkDesc;
        private boolean exposedThroughSubstitution;

        private Builder(String cpdId) {
            super(cpdId);
        }

        public Builder intCpd(String value) {
            this.intCpd = value;
            return this;
        }

        public Builder intVirtualLinkDesc(String value) {
            this.intVirtualLinkDesc = value;
            return this;
        }

        public Builder exposedThroughSubstitution(boolean value) {
            this.exposedThroughSubstitution = value;
            return this;
        }

        public VnfExtCpd build() {
            return new VnfExtCpd(this);
        }
    }
}
