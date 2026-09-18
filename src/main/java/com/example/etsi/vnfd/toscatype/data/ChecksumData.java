package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ChecksumData} - SOL001 V5.4.1 clause 6.2.53.
 *
 * <p>The ChecksumData data type describes information about the result of performing a checksum operation over some arbitrary data.
 *
 * <p><b>Additional requirements</b> (clause 6.2.53): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChecksumData {

    /** Describes the algorithm used to obtain the checksum value - Note: it is not recommended to use sha-224 as it offers weak security. required: true. */
    @JsonProperty("algorithm")
    private PropertyValue<String> algorithm;

    /** Contains the result of applying the algorithm indicated by the algorithm property to the data to which this ChecksumData refers required: true. */
    @JsonProperty("hash")
    private PropertyValue<String> hash;

}
