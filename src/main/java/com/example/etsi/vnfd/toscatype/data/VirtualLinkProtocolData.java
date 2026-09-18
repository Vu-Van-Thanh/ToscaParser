package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualLinkProtocolData} - SOL001 V5.4.1 clause 6.2.14.
 *
 * <p>The VirtualLinkProtocolData data type describes one protocol layer and associated protocol data for a given virtual link used in a specific VNF deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.14): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualLinkProtocolData {

    /** Identifies one of the protocols a virtualLink gives access to (ethernet, mpls, odu2, ipv4, ipv6, pseudo-wire) as specified by the connectivity_type property. required: true. */
    @JsonProperty("associated_layer_protocol")
    private PropertyValue<String> associatedLayerProtocol;

    /** Specifies the L2 protocol data for a virtual link. Shall be present when the associatedLayerProtocol attribute indicates a L2 protocol and shall be absent otherwise. */
    @JsonProperty("l2_protocol_data")
    private L2ProtocolData l2ProtocolData;

    /** Specifies the L3 protocol data for this virtual link. Shall be present when the associatedLayerProtocol attribute indicates a L3 protocol and shall be absent otherwise. */
    @JsonProperty("l3_protocol_data")
    private L3ProtocolData l3ProtocolData;

    /** Specifies the time synchronization requirements for this virtual link. */
    @JsonProperty("time_sync_requirements")
    private List<TimeSyncRequirements> timeSyncRequirements;

}
