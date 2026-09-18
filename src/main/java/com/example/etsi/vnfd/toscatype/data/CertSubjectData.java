package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.CertSubjectData} - SOL001 V5.4.1 clause 6.2.81.
 *
 * <p>The CertSubjectData data type describes subject data of the certificate.
 *
 * <p><b>Additional requirements</b> (clause 6.2.81): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertSubjectData {

    /** Information of certification target subject FQDN. */
    @JsonProperty("common_name")
    private PropertyValue<String> commonName;

    /** Information of certification target subject Organization. */
    @JsonProperty("organization")
    private PropertyValue<String> organization;

    /** Information of certification target subject Country. */
    @JsonProperty("country")
    private PropertyValue<String> country;

    /** Information of certification target subject State. */
    @JsonProperty("state")
    private PropertyValue<String> state;

    /** Information of certification target subject Locality. */
    @JsonProperty("locality")
    private PropertyValue<String> locality;

    /** Information of certification contact email address. */
    @JsonProperty("email_address")
    private PropertyValue<String> emailAddress;

}
