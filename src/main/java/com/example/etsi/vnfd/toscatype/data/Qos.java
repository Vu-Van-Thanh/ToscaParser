package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.Qos} - SOL001 V5.4.1 clause 9.2.7.
 *
 * <p>The QoS describes QoS data type a given VL used in a VNF deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 9.2.7): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Qos {

    /** Specifies the maximum latency required: true. */
    @JsonProperty("latency")
    private PropertyValue<Quantity> latency;

    /** Specifies the maximum jitter required: true. */
    @JsonProperty("packet_delay_variation")
    private PropertyValue<Quantity> packetDelayVariation;

    /** Specifies the maximum packet loss ratio */
    @JsonProperty("packet_loss_ratio")
    private PropertyValue<Double> packetLossRatio;

}
