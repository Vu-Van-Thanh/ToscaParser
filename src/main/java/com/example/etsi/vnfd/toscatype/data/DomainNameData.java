package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.DomainNameData} - SOL001 V5.4.1 clause 9.2.14.
 *
 * <p>The DomainNameData data type describes the information on the domain names to be applied to a connection point as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 9.2.14): None. 9.3 Artifact Types None. 9.4 Capability Types.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainNameData {

    /** Specifies which mode is used for the domain name assignment. required: true. */
    @JsonProperty("domain_name_assignment")
    private PropertyValue<String> domainNameAssignment;

    /** Specifies the fully qualified domain name (FQDN) to apply to the CP. */
    @JsonProperty("fully_qualified_domain_name")
    private PropertyValue<String> fullyQualifiedDomainName;

    /** Specifies a value of relative domain name to be considered when setting the fully qualified domain name. */
    @JsonProperty("relative_domain_name")
    private PropertyValue<String> relativeDomainName;

}
