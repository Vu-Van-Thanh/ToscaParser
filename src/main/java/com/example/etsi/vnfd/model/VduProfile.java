package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code VduProfile}, IFA011 V5.4.1 clause 7.1.8.3.
 *
 * <p>Additional instantiation data for one VDU within one deployment flavour. The same VDU may
 * appear in several flavours with different bounds, which is why these figures live on the flavour
 * rather than on the VDU.
 *
 * <p>Identical for VM and container VDUs. SOL001 Table A.9.2-1 mentions only {@code Vdu.Compute},
 * but the official type definitions declare {@code vdu_profile} on
 * {@code Vdu.OsContainerDeployableUnit} too.
 */
public final class VduProfile {

    private final String vduId;
    private final PropertyValue<Integer> minNumberOfInstances;
    private final PropertyValue<Integer> maxNumberOfInstances;
    private final List<String> affinityOrAntiAffinityGroupId;
    private final List<String> deployableModule;
    private final List<String> modifyCapacityAttributesOp;

    private VduProfile(Builder builder) {
        this.vduId = Objects.requireNonNull(builder.vduId, "vduId");
        this.minNumberOfInstances = builder.minNumberOfInstances;
        this.maxNumberOfInstances = builder.maxNumberOfInstances;
        this.affinityOrAntiAffinityGroupId =
                Collections.unmodifiableList(new ArrayList<>(builder.affinityOrAntiAffinityGroupId));
        this.deployableModule = Collections.unmodifiableList(new ArrayList<>(builder.deployableModule));
        this.modifyCapacityAttributesOp =
                Collections.unmodifiableList(new ArrayList<>(builder.modifyCapacityAttributesOp));
    }

    public static Builder builder(String vduId) {
        return new Builder(vduId);
    }

    /** Mandatory. References a VDU. */
    public String getVduId() {
        return vduId;
    }

    /** Mandatory. Shall be zero or greater. */
    public Optional<PropertyValue<Integer>> getMinNumberOfInstances() {
        return Optional.ofNullable(minNumberOfInstances);
    }

    /** Mandatory. Shall be greater than zero. */
    public Optional<PropertyValue<Integer>> getMaxNumberOfInstances() {
        return Optional.ofNullable(maxNumberOfInstances);
    }

    /**
     * Affinity groups this VDU belongs to.
     *
     * <p>Filled from affinity policies, not from the VDU node template: SOL001 Table 6.1-1 NOTE 3
     * places this information on {@code tosca.policies.nfv.AffinityRule} and
     * {@code AntiAffinityRule}.
     */
    public List<String> getAffinityOrAntiAffinityGroupId() {
        return affinityOrAntiAffinityGroupId;
    }

    /**
     * Deployable modules this VDU belongs to.
     *
     * <p>Clause 7.1.8.3.2: a VDU belonging to none is instantiated unconditionally, while one
     * belonging to a module is instantiated only if that module is selected.
     */
    public List<String> getDeployableModule() {
        return deployableModule;
    }

    /** LCM operations in which capacity attributes of this VDU may be changed. */
    public List<String> getModifyCapacityAttributesOp() {
        return modifyCapacityAttributesOp;
    }

    @Override
    public String toString() {
        return vduId + "[" + minNumberOfInstances + ", " + maxNumberOfInstances + "]";
    }

    /** Builder for {@link VduProfile}. */
    public static final class Builder {
        private final String vduId;
        private PropertyValue<Integer> minNumberOfInstances;
        private PropertyValue<Integer> maxNumberOfInstances;
        private final List<String> affinityOrAntiAffinityGroupId = new ArrayList<>();
        private final List<String> deployableModule = new ArrayList<>();
        private final List<String> modifyCapacityAttributesOp = new ArrayList<>();

        private Builder(String vduId) {
            this.vduId = vduId;
        }

        public Builder minNumberOfInstances(PropertyValue<Integer> value) {
            this.minNumberOfInstances = value;
            return this;
        }

        public Builder maxNumberOfInstances(PropertyValue<Integer> value) {
            this.maxNumberOfInstances = value;
            return this;
        }

        public Builder addAffinityGroup(String groupId) {
            if (!affinityOrAntiAffinityGroupId.contains(groupId)) {
                affinityOrAntiAffinityGroupId.add(groupId);
            }
            return this;
        }

        public Builder addDeployableModule(String moduleId) {
            if (!deployableModule.contains(moduleId)) {
                deployableModule.add(moduleId);
            }
            return this;
        }

        public Builder addModifyCapacityAttributesOp(String value) {
            modifyCapacityAttributesOp.add(value);
            return this;
        }

        public VduProfile build() {
            return new VduProfile(this);
        }
    }
}
