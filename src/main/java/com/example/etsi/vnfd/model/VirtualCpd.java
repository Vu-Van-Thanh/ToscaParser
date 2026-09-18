package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code VirtualCpd}, IFA011 V5.4.1 clause 7.1.18.2.
 *
 * <p>A connection point giving access to a number of VNFC instances, through the VDUs that implement
 * the service. Comes from a {@code tosca.nodes.nfv.VirtualCp} node template, SOL001 V5.4.1 clause
 * 6.8.15, whose {@code target} requirement points at the deployable units themselves.
 *
 * <p>Clause 7.1.18.2.2 NOTE 1 is worth keeping in view for the CNF case: if a VirtualCp represents a
 * load balancing virtual IP of a VNFC realized by OS containers and the address is configurable in
 * the declarative descriptor of the MCIO, {@code ipAddressAssignment} shall be true in the
 * L3AddressData.
 */
public final class VirtualCpd extends Cpd {

    private final List<String> vdu;
    private final List<Map<String, Object>> additionalServiceData;

    private VirtualCpd(Builder builder) {
        super(builder);
        this.vdu = Collections.unmodifiableList(new ArrayList<>(builder.vdu));
        this.additionalServiceData =
                Collections.unmodifiableList(new ArrayList<>(builder.additionalServiceData));
    }

    public static Builder builder(String cpdId) {
        return new Builder(cpdId);
    }

    /** The VDUs implementing this service. Mandatory, 1..N. */
    public List<String> getVdu() {
        return vdu;
    }

    /**
     * Service identification data exposed to NFV-MANO. 0..N.
     *
     * <p>Clause 7.1.18.3.1: where the CP is exposed by a component realized by OS containers, these
     * properties are mirrored from the declarative descriptor of the corresponding MCIO. Carried as
     * written rather than interpreted, since nothing here reads an MCIO.
     */
    public List<Map<String, Object>> getAdditionalServiceData() {
        return additionalServiceData;
    }

    @Override
    public String toString() {
        return "VirtualCpd(" + getCpdId() + ", vdu=" + vdu + ")";
    }

    /** Builder for {@link VirtualCpd}. */
    public static final class Builder extends Cpd.AbstractBuilder<Builder> {
        private final List<String> vdu = new ArrayList<>();
        private final List<Map<String, Object>> additionalServiceData = new ArrayList<>();

        private Builder(String cpdId) {
            super(cpdId);
        }

        public Builder addVdu(String value) {
            vdu.add(value);
            return this;
        }

        public Builder addAdditionalServiceData(Map<String, Object> value) {
            additionalServiceData.add(value);
            return this;
        }

        public VirtualCpd build() {
            return new VirtualCpd(this);
        }
    }
}
