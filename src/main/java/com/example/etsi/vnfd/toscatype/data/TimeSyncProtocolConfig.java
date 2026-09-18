package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.TimeSyncProtocolConfig} - SOL001 V5.4.1 clause 6.2.88.
 *
 * <p>The TimeSyncProtocolConfig data type specifies the time synchronization protocol configuration for a given virtual link.
 *
 * <p><b>Additional requirements</b> (clause 6.2.88): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeSyncProtocolConfig {

    /** Indicates whether time synchronization is enabled on this virtual link required: true. */
    @JsonProperty("time_sync_enabled")
    private PropertyValue<Boolean> timeSyncEnabled;

    /** Specifies PTP specific configuration for this virtual link. */
    @JsonProperty("ptp_config")
    private VlPtpConfig ptpConfig;

    /** Specifies NTP specific configuration for this virtual link. */
    @JsonProperty("ntp_config")
    private VlNtpConfig ntpConfig;

}
