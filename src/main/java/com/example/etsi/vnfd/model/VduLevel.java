package com.example.etsi.vnfd.model;

import java.util.Objects;

/**
 * {@code VduLevel}, IFA011 V5.4.1 clause 7.1.8.9.
 *
 * <p>How many instances of one VDU exist at a given instantiation level or scaling delta. Both
 * attributes are mandatory with cardinality 1.
 *
 * <p>The count reaches the infrastructure directly: SOL018 V5.4.1 Table 6.2.2.1-2 maps
 * {@code number_of_instances} onto {@code DeploymentSpec.replicas}.
 */
public final class VduLevel {

    private final String vduId;
    private final int numberOfInstances;

    private VduLevel(String vduId, int numberOfInstances) {
        this.vduId = Objects.requireNonNull(vduId, "vduId");
        this.numberOfInstances = numberOfInstances;
    }

    public static VduLevel of(String vduId, int numberOfInstances) {
        return new VduLevel(vduId, numberOfInstances);
    }

    /** Mandatory. References a VDU. */
    public String getVduId() {
        return vduId;
    }

    /** Mandatory. Shall be zero or greater. */
    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VduLevel)) {
            return false;
        }
        VduLevel other = (VduLevel) o;
        return numberOfInstances == other.numberOfInstances && vduId.equals(other.vduId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vduId, numberOfInstances);
    }

    @Override
    public String toString() {
        return vduId + " x" + numberOfInstances;
    }
}
