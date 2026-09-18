package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@code Vnfd}, IFA011 V5.4.1 clause 7.1.2.
 *
 * <p>Attribute names follow IFA011 exactly, with one deliberate exception: the deployment flavour
 * list is exposed as {@link #getDf()} rather than {@code deploymentFlavour}, because the published
 * JSON of this library uses {@code df}. That is the shorter name the SOL003 API uses and the one
 * consumers expect; the IFA011 name is recorded here so the difference is visible rather than
 * silent.
 *
 * <p>Descriptors, containers, images and connection points sit at this level while the deployment
 * flavours reference them by identifier, which is how IFA011 arranges it. A VDU used by two
 * flavours appears once here and twice in the flavours VDU profiles.
 *
 * <p>Clause 7.1.2.2 NOTE 6 requires that one of virtualComputeDesc, osContainerDesc or mciopId
 * contains at least one element. For the container flow that reduces to: either some container is
 * described, or some MCIOP is declared.
 */
public final class Vnfd {

    private final String vnfdId;
    private final String vnfProvider;
    private final String vnfProductName;
    private final String vnfSoftwareVersion;
    private final String vnfdVersion;
    private final String vnfProductInfoName;
    private final String vnfProductInfoDescription;
    private final String vnfdExtInvariantId;
    private final List<String> vnfmInfo;
    private final List<String> localizationLanguage;
    private final String defaultLocalizationLanguage;
    private final List<Vdu> vdu;
    private final List<OsContainerDesc> osContainerDesc;
    private final List<VirtualStorageDesc> virtualStorageDesc;
    private final List<SwImageDesc> swImageDesc;
    private final List<VnfVirtualLinkDesc> intVirtualLinkDesc;
    private final List<VduCpd> vduCpd;
    private final List<VnfExtCpd> vnfExtCpd;
    private final List<VipCpd> vipCpd;
    private final List<VirtualCpd> virtualCpd;
    private final List<CertificateDesc> certificateDesc;
    private final List<SecurityGroupRule> securityGroupRule;
    private final List<VnfPackageChangeInfo> vnfPackageChangeInfo;
    private final List<VnfDf> df;
    private final List<String> mciopId;
    private final List<LcmOpParameterMappingScript> lcmOpParameterMappingScript;
    private final List<LifeCycleManagementScript> lifeCycleManagementScript;
    private final VnfdExtensions extensions;

    private Vnfd(Builder b) {
        this.vnfdId = b.vnfdId;
        this.vnfProvider = b.vnfProvider;
        this.vnfProductName = b.vnfProductName;
        this.vnfSoftwareVersion = b.vnfSoftwareVersion;
        this.vnfdVersion = b.vnfdVersion;
        this.vnfProductInfoName = b.vnfProductInfoName;
        this.vnfProductInfoDescription = b.vnfProductInfoDescription;
        this.vnfdExtInvariantId = b.vnfdExtInvariantId;
        this.vnfmInfo = Collections.unmodifiableList(new ArrayList<>(b.vnfmInfo));
        this.localizationLanguage = Collections.unmodifiableList(new ArrayList<>(b.localizationLanguage));
        this.defaultLocalizationLanguage = b.defaultLocalizationLanguage;
        this.vdu = Collections.unmodifiableList(new ArrayList<>(b.vdu));
        this.osContainerDesc = Collections.unmodifiableList(new ArrayList<>(b.osContainerDesc));
        this.virtualStorageDesc = Collections.unmodifiableList(new ArrayList<>(b.virtualStorageDesc));
        this.swImageDesc = Collections.unmodifiableList(new ArrayList<>(b.swImageDesc));
        this.intVirtualLinkDesc = Collections.unmodifiableList(new ArrayList<>(b.intVirtualLinkDesc));
        this.vduCpd = Collections.unmodifiableList(new ArrayList<>(b.vduCpd));
        this.vnfExtCpd = Collections.unmodifiableList(new ArrayList<>(b.vnfExtCpd));
        this.vipCpd = Collections.unmodifiableList(new ArrayList<>(b.vipCpd));
        this.virtualCpd = Collections.unmodifiableList(new ArrayList<>(b.virtualCpd));
        this.certificateDesc = Collections.unmodifiableList(new ArrayList<>(b.certificateDesc));
        this.securityGroupRule =
                Collections.unmodifiableList(new ArrayList<>(b.securityGroupRule));
        this.vnfPackageChangeInfo =
                Collections.unmodifiableList(new ArrayList<>(b.vnfPackageChangeInfo));
        this.df = Collections.unmodifiableList(new ArrayList<>(b.df));
        this.mciopId = Collections.unmodifiableList(new ArrayList<>(b.mciopId));
        this.lcmOpParameterMappingScript =
                Collections.unmodifiableList(new ArrayList<>(b.lcmOpParameterMappingScript));
        this.lifeCycleManagementScript =
                Collections.unmodifiableList(new ArrayList<>(b.lifeCycleManagementScript));
        this.extensions = b.extensions;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Mandatory. Globally unique identifier of this VNFD, from descriptor_id. */
    public Optional<String> getVnfdId() {
        return Optional.ofNullable(vnfdId);
    }

    public Optional<String> getVnfProvider() {
        return Optional.ofNullable(vnfProvider);
    }

    public Optional<String> getVnfProductName() {
        return Optional.ofNullable(vnfProductName);
    }

    public Optional<String> getVnfSoftwareVersion() {
        return Optional.ofNullable(vnfSoftwareVersion);
    }

    public Optional<String> getVnfdVersion() {
        return Optional.ofNullable(vnfdVersion);
    }

    public Optional<String> getVnfProductInfoName() {
        return Optional.ofNullable(vnfProductInfoName);
    }

    public Optional<String> getVnfProductInfoDescription() {
        return Optional.ofNullable(vnfProductInfoDescription);
    }

    /** Identifies the VNFD in a version independent manner. */
    public Optional<String> getVnfdExtInvariantId() {
        return Optional.ofNullable(vnfdExtInvariantId);
    }

    /** Mandatory, 1..N. VNFMs compatible with this VNF. */
    public List<String> getVnfmInfo() {
        return vnfmInfo;
    }

    public List<String> getLocalizationLanguage() {
        return localizationLanguage;
    }

    public Optional<String> getDefaultLocalizationLanguage() {
        return Optional.ofNullable(defaultLocalizationLanguage);
    }

    /** Mandatory, 1..N. */
    public List<Vdu> getVdu() {
        return vdu;
    }

    public List<OsContainerDesc> getOsContainerDesc() {
        return osContainerDesc;
    }

    public List<VirtualStorageDesc> getVirtualStorageDesc() {
        return virtualStorageDesc;
    }

    public List<SwImageDesc> getSwImageDesc() {
        return swImageDesc;
    }

    public List<VnfVirtualLinkDesc> getIntVirtualLinkDesc() {
        return intVirtualLinkDesc;
    }

    /**
     * Internal connection point descriptors, clause 7.1.6.4.
     *
     * <p>[PROJECT-SPECIFIC] IFA011 V5.4.1 cl. 7.1.2.2 has no VNFD-level attribute for these - it
     * nests them inside {@code Vdu.intCpd}. They are hoisted here so each descriptor exists once;
     * see {@link Vdu#getIntCpd()} for the reasoning and for how to recover the IFA011 shape.
     */
    public List<VduCpd> getVduCpd() {
        return vduCpd;
    }

    /** Mandatory, 1..N in IFA011. */
    public List<VnfExtCpd> getVnfExtCpd() {
        return vnfExtCpd;
    }

    /** Virtual IP address requirements. IFA011 clause 7.1.2.2: {@code vipCpd}, M,0..N. */
    public List<VipCpd> getVipCpd() {
        return vipCpd;
    }

    /** Virtual connection points. IFA011 clause 7.1.2.2: {@code virtualCpd}, M,0..N. */
    public List<VirtualCpd> getVirtualCpd() {
        return virtualCpd;
    }

    /** Certificates the VNF uses. IFA011 clause 7.1.2.2: {@code certificateDesc}, M,0..N. */
    public List<CertificateDesc> getCertificateDesc() {
        return certificateDesc;
    }

    /** Security group rules. IFA011 clause 7.1.2.2: {@code securityGroupRule}, M,0..N. */
    public List<SecurityGroupRule> getSecurityGroupRule() {
        return securityGroupRule;
    }

    /**
     * Rules for changing a VNF instance to a different package.
     *
     * <p>IFA011 clause 7.1.2.2: {@code vnfPackageChangeInfo}, M,0..N.
     */
    public List<VnfPackageChangeInfo> getVnfPackageChangeInfo() {
        return vnfPackageChangeInfo;
    }

    /** Deployment flavours. IFA011 names this attribute deploymentFlavour. */
    public List<VnfDf> getDf() {
        return df;
    }

    /** MCIOPs declared in the VNF package, 0..N. */
    public List<String> getMciopId() {
        return mciopId;
    }

    public List<LcmOpParameterMappingScript> getLcmOpParameterMappingScript() {
        return lcmOpParameterMappingScript;
    }

    public List<LifeCycleManagementScript> getLifeCycleManagementScript() {
        return lifeCycleManagementScript;
    }

    /** [PROJECT-SPECIFIC] Data with no place in IFA011, kept apart from the standard attributes. */
    public Optional<VnfdExtensions> getExtensions() {
        return Optional.ofNullable(extensions);
    }

    /** A VDU by identifier. */
    public Optional<Vdu> findVdu(String vduId) {
        return vdu.stream().filter(v -> v.getVduId().equals(vduId)).findFirst();
    }

    /** A deployment flavour by identifier. */
    public Optional<VnfDf> findDf(String flavourId) {
        return df.stream().filter(f -> f.getFlavourId().equals(flavourId)).findFirst();
    }

    @Override
    public String toString() {
        return "Vnfd(" + vnfProductName + " " + vnfSoftwareVersion + ", " + df.size() + " flavour(s))";
    }

    /** Builder for {@link Vnfd}. */
    public static final class Builder {
        private String vnfdId;
        private String vnfProvider;
        private String vnfProductName;
        private String vnfSoftwareVersion;
        private String vnfdVersion;
        private String vnfProductInfoName;
        private String vnfProductInfoDescription;
        private String vnfdExtInvariantId;
        private String defaultLocalizationLanguage;
        private final List<String> vnfmInfo = new ArrayList<>();
        private final List<String> localizationLanguage = new ArrayList<>();
        private final List<Vdu> vdu = new ArrayList<>();
        private final List<OsContainerDesc> osContainerDesc = new ArrayList<>();
        private final List<VirtualStorageDesc> virtualStorageDesc = new ArrayList<>();
        private final List<SwImageDesc> swImageDesc = new ArrayList<>();
        private final List<VnfVirtualLinkDesc> intVirtualLinkDesc = new ArrayList<>();
        private final List<VduCpd> vduCpd = new ArrayList<>();
        private final List<VnfExtCpd> vnfExtCpd = new ArrayList<>();
        private final List<VipCpd> vipCpd = new ArrayList<>();
        private final List<VirtualCpd> virtualCpd = new ArrayList<>();
        private final List<CertificateDesc> certificateDesc = new ArrayList<>();
        private final List<SecurityGroupRule> securityGroupRule = new ArrayList<>();
        private final List<VnfPackageChangeInfo> vnfPackageChangeInfo = new ArrayList<>();
        private final List<VnfDf> df = new ArrayList<>();
        private final List<String> mciopId = new ArrayList<>();
        private final List<LcmOpParameterMappingScript> lcmOpParameterMappingScript = new ArrayList<>();
        private final List<LifeCycleManagementScript> lifeCycleManagementScript = new ArrayList<>();
        private VnfdExtensions extensions;

        private Builder() {
        }

        public Builder vnfdId(String value) {
            this.vnfdId = value;
            return this;
        }

        public Builder vnfProvider(String value) {
            this.vnfProvider = value;
            return this;
        }

        public Builder vnfProductName(String value) {
            this.vnfProductName = value;
            return this;
        }

        public Builder vnfSoftwareVersion(String value) {
            this.vnfSoftwareVersion = value;
            return this;
        }

        public Builder vnfdVersion(String value) {
            this.vnfdVersion = value;
            return this;
        }

        public Builder vnfProductInfoName(String value) {
            this.vnfProductInfoName = value;
            return this;
        }

        public Builder vnfProductInfoDescription(String value) {
            this.vnfProductInfoDescription = value;
            return this;
        }

        public Builder vnfdExtInvariantId(String value) {
            this.vnfdExtInvariantId = value;
            return this;
        }

        public Builder defaultLocalizationLanguage(String value) {
            this.defaultLocalizationLanguage = value;
            return this;
        }

        public Builder addVnfmInfo(String value) {
            if (value != null) {
                vnfmInfo.add(value);
            }
            return this;
        }

        public Builder addLocalizationLanguage(String value) {
            if (value != null) {
                localizationLanguage.add(value);
            }
            return this;
        }

        public Builder addVdu(Vdu value) {
            if (value != null) {
                vdu.add(value);
            }
            return this;
        }

        public Builder addOsContainerDesc(OsContainerDesc value) {
            if (value != null) {
                osContainerDesc.add(value);
            }
            return this;
        }

        public Builder addVirtualStorageDesc(VirtualStorageDesc value) {
            if (value != null) {
                virtualStorageDesc.add(value);
            }
            return this;
        }

        public Builder addSwImageDesc(SwImageDesc value) {
            if (value != null) {
                swImageDesc.add(value);
            }
            return this;
        }

        public Builder addIntVirtualLinkDesc(VnfVirtualLinkDesc value) {
            if (value != null) {
                intVirtualLinkDesc.add(value);
            }
            return this;
        }

        public Builder addVduCpd(VduCpd value) {
            if (value != null) {
                vduCpd.add(value);
            }
            return this;
        }

        public Builder addVnfExtCpd(VnfExtCpd value) {
            if (value != null) {
                vnfExtCpd.add(value);
            }
            return this;
        }

        public Builder addVipCpd(VipCpd value) {
            vipCpd.add(value);
            return this;
        }

        public Builder addVirtualCpd(VirtualCpd value) {
            virtualCpd.add(value);
            return this;
        }

        public Builder addSecurityGroupRule(SecurityGroupRule value) {
            securityGroupRule.add(value);
            return this;
        }

        public Builder addVnfPackageChangeInfo(VnfPackageChangeInfo value) {
            vnfPackageChangeInfo.add(value);
            return this;
        }

        public Builder addCertificateDesc(CertificateDesc value) {
            certificateDesc.add(value);
            return this;
        }

        public Builder addDf(VnfDf value) {
            if (value != null) {
                df.add(value);
            }
            return this;
        }

        public Builder addMciopId(String value) {
            if (value != null) {
                mciopId.add(value);
            }
            return this;
        }

        public Builder addLcmOpParameterMappingScript(LcmOpParameterMappingScript value) {
            if (value != null) {
                lcmOpParameterMappingScript.add(value);
            }
            return this;
        }

        public Builder addLifeCycleManagementScript(LifeCycleManagementScript value) {
            if (value != null) {
                lifeCycleManagementScript.add(value);
            }
            return this;
        }

        public Builder extensions(VnfdExtensions value) {
            this.extensions = value;
            return this;
        }

        public Vnfd build() {
            return new Vnfd(this);
        }
    }
}
