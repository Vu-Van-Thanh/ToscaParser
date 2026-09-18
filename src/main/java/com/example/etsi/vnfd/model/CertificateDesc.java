package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code CertificateDesc}, IFA011 V5.4.1 clause 7.1.19.2.
 *
 * <p>Requirements related to a certificate the VNF uses. Comes from a
 * {@code tosca.nodes.nfv.Certificate} node template, SOL001 V5.4.1 clause 6.8.19.
 *
 * <p>Referenced from {@code Vdu.certificateDesc} and listed at VNFD level, per Table 7.1.2.2-1.
 */
public final class CertificateDesc {

    private final String id;
    private final PropertyValue<String> name;
    private final PropertyValue<String> certificateType;
    private final List<Map<String, Object>> csrRequirements;
    private final Map<String, Object> certificateBaseProfile;

    private CertificateDesc(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.certificateType = builder.certificateType;
        this.csrRequirements = Collections.unmodifiableList(new ArrayList<>(builder.csrRequirements));
        this.certificateBaseProfile = builder.certificateBaseProfile;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /**
     * Identifier of this certificate descriptor. Mandatory, 1.
     *
     * <p>[ASSUMPTION] The node template name. SOL001 states that rule only for SwImageDesc
     * (clause 6.8.12.6); nothing in clause 6.8.19 says how the identifier is derived.
     */
    public String getId() {
        return id;
    }

    /** Name of this certificate. Mandatory, 1. */
    public Optional<PropertyValue<String>> getName() {
        return Optional.ofNullable(name);
    }

    /** Type of this certificate. Mandatory, 1. IFA011 values: VNFCI_CERT, VNFOAM_CERT. */
    public Optional<PropertyValue<String>> getCertificateType() {
        return Optional.ofNullable(certificateType);
    }

    /** Requirements for the certificate when issuing a CSR. 0..N. */
    public List<Map<String, Object>> getCsrRequirements() {
        return csrRequirements;
    }

    /**
     * Basic information for this certificate when issuing a CSR. 0..1.
     *
     * <p>SOL001 clause 6.8.19 notes it "shall be present in the delegation mode, otherwise shall be
     * absent" - a condition this library records but does not check, because nothing in the
     * descriptor says which mode is in use.
     */
    public Map<String, Object> getCertificateBaseProfile() {
        return certificateBaseProfile;
    }

    @Override
    public String toString() {
        return "CertificateDesc(" + id + ")";
    }

    /** Builder for {@link CertificateDesc}. */
    public static final class Builder {
        private final String id;
        private PropertyValue<String> name;
        private PropertyValue<String> certificateType;
        private final List<Map<String, Object>> csrRequirements = new ArrayList<>();
        private Map<String, Object> certificateBaseProfile = Collections.emptyMap();

        private Builder(String id) {
            this.id = id;
        }

        public Builder name(PropertyValue<String> value) {
            this.name = value;
            return this;
        }

        public Builder certificateType(PropertyValue<String> value) {
            this.certificateType = value;
            return this;
        }

        public Builder addCsrRequirement(Map<String, Object> value) {
            if (value != null) {
                csrRequirements.add(value);
            }
            return this;
        }

        public Builder certificateBaseProfile(Map<String, Object> value) {
            this.certificateBaseProfile =
                    value == null ? Collections.emptyMap() : new java.util.LinkedHashMap<>(value);
            return this;
        }

        public CertificateDesc build() {
            return new CertificateDesc(this);
        }
    }
}
