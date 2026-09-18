package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VlNtpConfig} - SOL001 V5.4.1 clause 6.2.90.
 *
 * <p>The VlNtpConfig data type specifies the NTP-specific configuration for a given virtual link.
 *
 * <p><b>Additional requirements</b> (clause 6.2.90): None. 6.3 Artifact Types.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VlNtpConfig {

    /** NTP operation mode (e.g. server, peer) to be used on this virtual link. required: true. */
    @JsonProperty("ntp_operation_mode")
    private PropertyValue<String> ntpOperationMode;

    /** List of NTP server addresses to be used. */
    @JsonProperty("ntp_servers")
    private List<String> ntpServers;

    /** NTP stratum level. */
    @JsonProperty("ntp_stratum")
    private PropertyValue<Integer> ntpStratum;

}
