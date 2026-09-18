package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.McioIdentificationData} - SOL001 V5.4.1 clause 6.2.75.
 *
 * <p>The McioIdentificationData data type contains data needed to identify an MCIO when interworking with the CISM, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.75): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class McioIdentificationData {

    /** The name of the mcio. required: true. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** The type of the mcio. required: true. */
    @JsonProperty("type")
    private PropertyValue<String> type;

}
