package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code VnfDf}, IFA011 V5.4.1 clause 7.1.8.2.
 *
 * <p>One deployment flavour. SOL001 V5.4.1 Table 6.1-1 NOTE 2 states that this element "is
 * represented as a TOSCA service template", so one flavour corresponds to one service template
 * file, whether that is the single template of clause 6.11.3 or one of the lower-level templates of
 * clause 6.11.2.
 *
 * <p>{@code instantiationLevel} is mandatory with cardinality 1..N, and
 * {@code defaultInstantiationLevelId} "shall be present if there are multiple instantiationLevel
 * entries". Both constraints are checked rather than assumed, because SOL001 expresses levels
 * through optional policies and a descriptor may declare none.
 *
 * <p>{@code mciopProfile} is conditional: it "shall be present if the DF references (via the
 * vduProfile) containerized workloads based on a MCIOP". The condition is on the flavour
 * referencing such workloads, not on every VDU doing so, which is what allows a flavour to mix VDUs
 * deployed by an MCIOP with VDUs realised directly from their container descriptions.
 */
public final class VnfDf {

    private final String flavourId;
    private final String description;
    private final List<VduProfile> vduProfile;
    private final List<MciopProfile> mciopProfile;
    private final List<VirtualLinkProfile> virtualLinkProfile;
    private final List<InstantiationLevel> instantiationLevel;
    private final String defaultInstantiationLevelId;
    private final List<AffinityOrAntiAffinityGroup> affinityOrAntiAffinityGroup;
    private final List<ScalingAspect> scalingAspect;
    private final List<DeployableModule> deployableModule;
    private final ScalingDelta initialDelta;
    private final VnfLcmOperationsConfiguration vnfLcmOperationsConfiguration;
    private final String sourceFile;

    private VnfDf(Builder builder) {
        this.flavourId = Objects.requireNonNull(builder.flavourId, "flavourId");
        this.description = builder.description;
        this.vduProfile = Collections.unmodifiableList(new ArrayList<>(builder.vduProfile));
        this.mciopProfile = Collections.unmodifiableList(new ArrayList<>(builder.mciopProfile));
        this.virtualLinkProfile =
                Collections.unmodifiableList(new ArrayList<>(builder.virtualLinkProfile));
        this.instantiationLevel =
                Collections.unmodifiableList(new ArrayList<>(builder.instantiationLevel));
        this.defaultInstantiationLevelId = builder.defaultInstantiationLevelId;
        this.affinityOrAntiAffinityGroup =
                Collections.unmodifiableList(new ArrayList<>(builder.affinityOrAntiAffinityGroup));
        this.scalingAspect = Collections.unmodifiableList(new ArrayList<>(builder.scalingAspect));
        this.deployableModule =
                Collections.unmodifiableList(new ArrayList<>(builder.deployableModule));
        this.initialDelta = builder.initialDelta;
        this.vnfLcmOperationsConfiguration = builder.vnfLcmOperationsConfiguration;
        this.sourceFile = builder.sourceFile;
    }

    public static Builder builder(String flavourId) {
        return new Builder(flavourId);
    }

    /** Mandatory. Identifier of this flavour within the VNFD. */
    public String getFlavourId() {
        return flavourId;
    }

    /** Mandatory. */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    /** Mandatory, 1..N. Instantiation data for the VDUs used in this flavour. */
    public List<VduProfile> getVduProfile() {
        return vduProfile;
    }

    /** Conditional, 0..N. Present when the flavour references MCIOP-based workloads. */
    public List<MciopProfile> getMciopProfile() {
        return mciopProfile;
    }

    public List<VirtualLinkProfile> getVirtualLinkProfile() {
        return virtualLinkProfile;
    }

    /** Mandatory, 1..N. */
    public List<InstantiationLevel> getInstantiationLevel() {
        return instantiationLevel;
    }

    /** Required when more than one instantiation level exists. */
    public Optional<String> getDefaultInstantiationLevelId() {
        return Optional.ofNullable(defaultInstantiationLevelId);
    }

    public List<AffinityOrAntiAffinityGroup> getAffinityOrAntiAffinityGroup() {
        return affinityOrAntiAffinityGroup;
    }

    public List<ScalingAspect> getScalingAspect() {
        return scalingAspect;
    }

    /**
     * Sets of optional VDUs a consumer may choose to instantiate.
     *
     * <p>IFA011 clause 7.1.8.2.2: {@code deployableModule}, M,0..N.
     */
    public List<DeployableModule> getDeployableModule() {
        return deployableModule;
    }

    /**
     * The minimum size of the VNF - scale level zero for every scaling aspect.
     *
     * <p>IFA011 V5.4.1 clause 7.1.8.2.2: {@code initialDelta}, a {@code ScalingDelta} that
     * "shall be present if the aspectDeltaDetails attribute is present".
     */
    public Optional<ScalingDelta> getInitialDelta() {
        return Optional.ofNullable(initialDelta);
    }

    public Optional<VnfLcmOperationsConfiguration> getVnfLcmOperationsConfiguration() {
        return Optional.ofNullable(vnfLcmOperationsConfiguration);
    }

    /** The service template file this flavour was built from. */
    public Optional<String> getSourceFile() {
        return Optional.ofNullable(sourceFile);
    }

    /** VDU identifiers referenced by this flavour, in profile order. */
    public List<String> vduIds() {
        List<String> ids = new ArrayList<>();
        for (VduProfile profile : vduProfile) {
            ids.add(profile.getVduId());
        }
        return ids;
    }

    /** VDU identifiers deployed through an MCIOP, across every MciopProfile of this flavour. */
    public List<String> mciopAssociatedVduIds() {
        List<String> ids = new ArrayList<>();
        for (MciopProfile profile : mciopProfile) {
            for (String vduId : profile.getAssociatedVdu()) {
                if (!ids.contains(vduId)) {
                    ids.add(vduId);
                }
            }
        }
        return ids;
    }

    @Override
    public String toString() {
        return flavourId + " " + vduIds();
    }

    /** Builder for {@link VnfDf}. */
    public static final class Builder {
        private final String flavourId;
        private String description;
        private final List<VduProfile> vduProfile = new ArrayList<>();
        private final List<MciopProfile> mciopProfile = new ArrayList<>();
        private final List<VirtualLinkProfile> virtualLinkProfile = new ArrayList<>();
        private final List<InstantiationLevel> instantiationLevel = new ArrayList<>();
        private String defaultInstantiationLevelId;
        private final List<AffinityOrAntiAffinityGroup> affinityOrAntiAffinityGroup = new ArrayList<>();
        private final List<ScalingAspect> scalingAspect = new ArrayList<>();
        private final List<DeployableModule> deployableModule = new ArrayList<>();
        private ScalingDelta initialDelta;
        private VnfLcmOperationsConfiguration vnfLcmOperationsConfiguration;
        private String sourceFile;

        private Builder(String flavourId) {
            this.flavourId = flavourId;
        }

        public Builder description(String value) {
            this.description = value;
            return this;
        }

        public Builder addVduProfile(VduProfile value) {
            vduProfile.add(value);
            return this;
        }

        public Builder addMciopProfile(MciopProfile value) {
            mciopProfile.add(value);
            return this;
        }

        public Builder addVirtualLinkProfile(VirtualLinkProfile value) {
            virtualLinkProfile.add(value);
            return this;
        }

        public Builder addInstantiationLevel(InstantiationLevel value) {
            instantiationLevel.add(value);
            return this;
        }

        public Builder defaultInstantiationLevelId(String value) {
            this.defaultInstantiationLevelId = value;
            return this;
        }

        public Builder addAffinityGroup(AffinityOrAntiAffinityGroup value) {
            affinityOrAntiAffinityGroup.add(value);
            return this;
        }

        public Builder addScalingAspect(ScalingAspect value) {
            scalingAspect.add(value);
            return this;
        }

        public Builder initialDelta(ScalingDelta value) {
            this.initialDelta = value;
            return this;
        }

        public Builder addDeployableModule(DeployableModule value) {
            deployableModule.add(value);
            return this;
        }

        public Builder vnfLcmOperationsConfiguration(VnfLcmOperationsConfiguration value) {
            this.vnfLcmOperationsConfiguration = value;
            return this;
        }

        public Builder sourceFile(String value) {
            this.sourceFile = value;
            return this;
        }

        public VnfDf build() {
            return new VnfDf(this);
        }
    }
}
