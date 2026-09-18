package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.CertificateBaseProfile} - SOL001 V5.4.1 clause 6.2.79.
 *
 * <p>The CertificateBaseProfile data type describes base profile for certificate used in a specific deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.79): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificateBaseProfile {

    /** The identifier of this certificate profile. required: true. */
    @JsonProperty("id")
    private PropertyValue<String> id;

    /** Issuer of certificates. required: true. */
    @JsonProperty("issuer")
    private PropertyValue<String> issuer;

    /** Identifier of this issuer of certificates. required: true. */
    @JsonProperty("issuer_unique_identifier")
    private PropertyValue<String> issuerUniqueIdentifier;

    /** Subject of certificates. required: true. */
    @JsonProperty("subject")
    private CertSubjectData subject;

    /** Identifier of this subject of certificates. required: true. */
    @JsonProperty("subject_unique_identifier")
    private PropertyValue<String> subjectUniqueIdentifier;

    /** Basic constraints of certificates. required: true. */
    @JsonProperty("basic_constraints")
    private PropertyValue<String> basicConstraints;

    /** Alternative name of the issuer of certificates. */
    @JsonProperty("issuer_alt_name")
    private PropertyValue<String> issuerAltName;

    /** Alternative name of the subject of certificates. */
    @JsonProperty("subject_alt_name")
    private PropertyValue<String> subjectAltName;

    /** Name constraints of certificates. */
    @JsonProperty("name_constraints")
    private PropertyValue<String> nameConstraints;

}
