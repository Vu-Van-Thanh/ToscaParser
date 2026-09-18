package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.RequestedAdditionalCapability} - SOL001 V5.4.1 clause 6.2.6.
 *
 * <p>The RequestedAdditionalCapability data type describes requested additional capability for a particular VDU, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.6): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestedAdditionalCapability {

    /** Identifies a requested additional capability for the VDU. required: true. */
    @JsonProperty("requested_additional_capability_name")
    private PropertyValue<String> requestedAdditionalCapabilityName;

    /** Indicates whether the requested additional capability is mandatory for successful operation. required: true. */
    @JsonProperty("support_mandatory")
    private PropertyValue<Boolean> supportMandatory;

    /** Identifies the minimum version of the requested additional capability. */
    @JsonProperty("min_requested_additional_capability_version")
    private PropertyValue<String> minRequestedAdditionalCapabilityVersion;

    /** Identifies the preferred version of the requested additional capability. */
    @JsonProperty("preferred_requested_additional_capability_version")
    private PropertyValue<String> preferredRequestedAdditionalCapabilityVersion;

    /** Identifies specific attributes, dependent on the requested additional capability type. required: true. */
    @JsonProperty("target_performance_parameters")
    private Map<String, String> targetPerformanceParameters;

}
