package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.toscatype.data.McioIdentificationData;
import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code Vdu}, IFA011 V5.4.1 clause 7.1.6.2.
 *
 * <p>One information element covers both VM and container realisations; the difference is which
 * attributes are present, not which element is used. Clause 7.1.6.2.2 NOTE 6 spells out the
 * exclusions, and NOTE 10 the obligations: a container-realised VDU should carry
 * {@code osContainerDesc}, and where it does not, "the MciopProfile associated with the VDU shall
 * be present in the VNFD".
 *
 * <p>Only the container branch is populated by this library. {@code virtualComputeDesc} and the
 * attributes NOTE 6 ties to it are deliberately absent rather than left empty.
 */
public final class Vdu {

    private final String vduId;
    private final PropertyValue<String> name;
    private final PropertyValue<String> description;
    private final List<String> intCpd;
    private final List<String> osContainerDesc;
    private final List<String> virtualStorageDesc;
    private final McioIdentificationData mcioIdentificationData;
    private final List<String> mcioConstraintParams;
    private final PropertyValue<Boolean> isNumOfInstancesClusterBased;
    private final List<String> certificateDesc;
    private final LcmRealizationPath lcmRealizationPath;

    private Vdu(Builder builder) {
        this.vduId = Objects.requireNonNull(builder.vduId, "vduId");
        this.name = builder.name;
        this.description = builder.description;
        this.intCpd = Collections.unmodifiableList(new ArrayList<>(builder.intCpd));
        this.osContainerDesc = Collections.unmodifiableList(new ArrayList<>(builder.osContainerDesc));
        this.virtualStorageDesc =
                Collections.unmodifiableList(new ArrayList<>(builder.virtualStorageDesc));
        this.mcioIdentificationData = builder.mcioIdentificationData;
        this.mcioConstraintParams =
                Collections.unmodifiableList(new ArrayList<>(builder.mcioConstraintParams));
        this.isNumOfInstancesClusterBased = builder.isNumOfInstancesClusterBased;
        this.certificateDesc = Collections.unmodifiableList(new ArrayList<>(builder.certificateDesc));
        this.lcmRealizationPath = builder.lcmRealizationPath;
    }

    public static Builder builder(String vduId) {
        return new Builder(vduId);
    }

    /** Mandatory. Unique identifier of this VDU in the VNFD, taken from the node template name. */
    public String getVduId() {
        return vduId;
    }

    /** Mandatory. */
    public Optional<PropertyValue<String>> getName() {
        return Optional.ofNullable(name);
    }

    /** Mandatory. */
    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * Internal connection points of this VDU, clause 7.1.6.4.
     *
     * <p>Obtained by inverting the relation: a {@code VduCp} declares which VDU it binds to, so the
     * list is built by reading every connection point rather than anything on the VDU itself.
     *
     * <p>[PROJECT-SPECIFIC] IFA011 V5.4.1 Table 7.1.6.2.2-1 gives this attribute the content type
     * {@code VduCpd}, that is the descriptors themselves rather than references to them. This model
     * holds identifiers here and keeps the descriptors once in {@code Vnfd.vduCpd}. The information
     * is the same, but two VDUs can then be compared without comparing their connection points, and
     * a connection point is serialized once instead of once per VDU that binds it. A consumer that
     * needs the IFA011 shape resolves the identifier against {@code Vnfd.vduCpd}.
     */
    public List<String> getIntCpd() {
        return intCpd;
    }

    /**
     * Container descriptions realising this VDU, cardinality 0..N.
     *
     * <p>A list because SOL001 Table 6.8.13.4-1 gives the {@code container} requirement occurrences
     * of {@code [0, UNBOUNDED]}. Empty is meaningful rather than missing: NOTE 10 then requires an
     * associated MciopProfile.
     */
    public List<String> getOsContainerDesc() {
        return osContainerDesc;
    }

    public List<String> getVirtualStorageDesc() {
        return virtualStorageDesc;
    }

    /**
     * Name and type of the MCIO realising this VDU.
     *
     * <p>Clause 7.1.6.2.2 requires it for a container-realised VDU and forbids it otherwise, which
     * makes it the information-model discriminator between the two realisations. SOL018 V5.4.1
     * Table 6.2.2.1-1 maps its {@code name} onto {@code Deployment.metadata.name}, so it is also
     * how a VNFM finds a workload that a Helm chart created.
     */
    public Optional<McioIdentificationData> getMcioIdentificationData() {
        return Optional.ofNullable(mcioIdentificationData);
    }

    public List<String> getMcioConstraintParams() {
        return mcioConstraintParams;
    }

    /** Whether instance count follows the instantiation level or one is placed per CIS node. */
    public Optional<PropertyValue<Boolean>> getIsNumOfInstancesClusterBased() {
        return Optional.ofNullable(isNumOfInstancesClusterBased);
    }

    public List<String> getCertificateDesc() {
        return certificateDesc;
    }

    /**
     * How the infrastructure is expected to realise this VDU.
     *
     * <p>[MANO INTERPRETATION] Not an IFA011 attribute. Derived from whether an MciopProfile
     * references this VDU and whether it carries its own container descriptions.
     */
    public LcmRealizationPath getLcmRealizationPath() {
        return lcmRealizationPath == null ? LcmRealizationPath.UNDETERMINED : lcmRealizationPath;
    }

    @Override
    public String toString() {
        return vduId + " (" + getLcmRealizationPath() + ")";
    }

    /** Builder for {@link Vdu}. */
    public static final class Builder {
        private final String vduId;
        private PropertyValue<String> name;
        private PropertyValue<String> description;
        private final List<String> intCpd = new ArrayList<>();
        private final List<String> osContainerDesc = new ArrayList<>();
        private final List<String> virtualStorageDesc = new ArrayList<>();
        private McioIdentificationData mcioIdentificationData;
        private final List<String> mcioConstraintParams = new ArrayList<>();
        private PropertyValue<Boolean> isNumOfInstancesClusterBased;
        private final List<String> certificateDesc = new ArrayList<>();
        private LcmRealizationPath lcmRealizationPath;

        private Builder(String vduId) {
            this.vduId = vduId;
        }

        public Builder name(PropertyValue<String> value) {
            this.name = value;
            return this;
        }

        public Builder description(PropertyValue<String> value) {
            this.description = value;
            return this;
        }

        public Builder addIntCpd(String cpdId) {
            if (!intCpd.contains(cpdId)) {
                intCpd.add(cpdId);
            }
            return this;
        }

        public Builder addOsContainerDesc(String descId) {
            osContainerDesc.add(descId);
            return this;
        }

        public Builder addVirtualStorageDesc(String descId) {
            virtualStorageDesc.add(descId);
            return this;
        }

        public Builder mcioIdentificationData(McioIdentificationData value) {
            this.mcioIdentificationData = value;
            return this;
        }

        public Builder addMcioConstraintParam(String value) {
            mcioConstraintParams.add(value);
            return this;
        }

        public Builder isNumOfInstancesClusterBased(PropertyValue<Boolean> value) {
            this.isNumOfInstancesClusterBased = value;
            return this;
        }

        public Builder addCertificateDesc(String value) {
            certificateDesc.add(value);
            return this;
        }

        public Builder lcmRealizationPath(LcmRealizationPath value) {
            this.lcmRealizationPath = value;
            return this;
        }

        public Vdu build() {
            return new Vdu(this);
        }
    }
}
