package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.CSRRequirements;
import com.example.etsi.vnfd.toscatype.data.CertificateBaseProfile;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Certificate} - SOL001 V5.4.1 clause 6.8.19.
 *
 * <p>The Certificate node type describes the certificate to be used by the VNF in delegation-mode. as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code CertificateDesc} information element of IFA011 V5.4.1 clause 7.1.19.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Table 6.1-1 NOTE 4: the use of certificate management in direct-mode does not require any information in VNFD.
 *
 * <p><b>Additional requirements</b> (clause 6.8.19): The VNF topology template shall only contain node templates of type tosca.nodes.nfv.Certificate when it supports the delegation-mode. The "installable_certificate" requirements in tosca.nodes.nfv.Vdu.Compute or tosca.nodes.nfv.Vdu.OsContainerDeployableUnit node template shall be present in each low level service template when the particular VDU in particular Deployment Flavour supports delegation-mode. If the VNF supports direct mode or does not require a certificate VNF topology template shall not contain any tosca.nodes.nfv.Certificate node template.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.CERTIFICATE)
public class Certificate extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Human readable name of the certificate required: true. */
        @JsonProperty("name")
        private PropertyValue<String> name;

        /** Type of the certificate. required: true. */
        @JsonProperty("certificate_type")
        private PropertyValue<String> certificateType;

        /** Basic information for this certificate when issuing a CSR. Shall be present in the delegation mode. Otherwise shall be absent. */
        @JsonProperty("certificate_base_profile")
        private CertificateBaseProfile certificateBaseProfile;

        /** Requirements for Certificate when issuing CSR. */
        @JsonProperty("csr_requirements")
        private List<CSRRequirements> csrRequirements;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.InstallableCertificate, occurrences [1, UNBOUNDED]. */
        @JsonProperty("installable_certificate")
        private Map<String, Object> installableCertificate;

    }

}
