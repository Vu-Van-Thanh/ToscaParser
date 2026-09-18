package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VlProfile} - SOL001 V5.4.1 clause 6.2.13.
 *
 * <p>The VlProfile data type describes additional instantiation data for a given VL used in a specific VNF deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.13): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VlProfile {

    /** Specifies the maximum bitrate requirements for a VL instantiated according to this profile. required: true. */
    @JsonProperty("max_bitrate_requirements")
    private LinkBitrateRequirements maxBitrateRequirements;

    /** Specifies the minimum bitrate requirements for a VL instantiated according to this profile. required: true. */
    @JsonProperty("min_bitrate_requirements")
    private LinkBitrateRequirements minBitrateRequirements;

    /** Specifies the QoS requirements of a VL instantiated according to this profile. */
    @JsonProperty("qos")
    private Qos qos;

    /** Specifies the protocol data for a virtual link. */
    @JsonProperty("virtual_link_protocol_data")
    private List<VirtualLinkProtocolData> virtualLinkProtocolData;

}
