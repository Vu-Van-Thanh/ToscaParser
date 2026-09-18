package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.CSRRequirements} - SOL001 V5.4.1 clause 6.2.80.
 *
 * <p>The CSRRequirements data type describes requirements for certificate.
 *
 * <p><b>Additional requirements</b> (clause 6.2.80): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CSRRequirements {

    /** The identifier of this CSR requirements required: true. */
    @JsonProperty("id")
    private PropertyValue<String> id;

    /** The certificate version can be supported by this VNF or VDU. required: true. */
    @JsonProperty("supported_certificate_version")
    private PropertyValue<String> supportedCertificateVersion;

    /** The signature algorithm can be supported by this VNF or VDU. required: true. */
    @JsonProperty("supported_signature")
    private PropertyValue<String> supportedSignature;

    /** Max key length can be supported this VNF or VDU. Default values is 4096 bits for RSA based algorithm. Otherwise default value is 512 bits. */
    @JsonProperty("supported_max_keylength")
    private PropertyValue<Integer> supportedMaxKeylength;

    /** Min key length can be supported this VNF or VDU. Default value is 3072 bits for RSA based algorithm. Otherwise default value is 256 bits. */
    @JsonProperty("supported_min_keylength")
    private PropertyValue<Integer> supportedMinKeylength;

    /** Maximum validity period in days for this certificate. */
    @JsonProperty("maximum_validity_period")
    private PropertyValue<Integer> maximumValidityPeriod;

    /** Minimum validity period in days for this certificate. */
    @JsonProperty("minimum_validity_period")
    private PropertyValue<Integer> minimumValidityPeriod;

    /** Requirements for key usage of this certificate. */
    @JsonProperty("key_usages")
    private List<String> keyUsages;

}
