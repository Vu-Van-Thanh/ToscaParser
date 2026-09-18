package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VlPtpConfig} - SOL001 V5.4.1 clause 6.2.89.
 *
 * <p>The VlPtPConfig data type specifies the PTP-specific configuration for a given virtual link.
 *
 * <p><b>Additional requirements</b> (clause 6.2.89): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VlPtpConfig {

    /** PTP profile (e.g. g2875.1, 802.1as) required: true. */
    @JsonProperty("ptp_profile")
    private PropertyValue<String> ptpProfile;

    /** Transport mechanism for PTP messages (e.g. ethernet, upd) required: true. */
    @JsonProperty("ptp_transport")
    private PropertyValue<String> ptpTransport;

    /** Time domain number for the 802.1as ptp profile. */
    @JsonProperty("tsn_time_domain")
    private PropertyValue<Integer> tsnTimeDomain;

}
