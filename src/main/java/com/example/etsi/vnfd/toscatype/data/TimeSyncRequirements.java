package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.TimeSyncRequirements} - SOL001 V5.4.1 clause 6.2.87.
 *
 * <p>The TimeSyncRequirements data type specifies the time synchronization requirements for a given virtual link.
 *
 * <p><b>Additional requirements</b> (clause 6.2.87): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeSyncRequirements {

    /** Identifies the time sync protocol supported by this virtual link required: true. */
    @JsonProperty("supported_protocol")
    private PropertyValue<String> supportedProtocol;

    /** Specifies time synchronization protocol configuration for this virtual link. */
    @JsonProperty("time_sync_protocol_config")
    private TimeSyncProtocolConfig timeSyncProtocolConfig;

}
