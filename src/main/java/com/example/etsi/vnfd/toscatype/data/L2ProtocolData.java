package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.L2ProtocolData} - SOL001 V5.4.1 clause 6.2.15.
 *
 * <p>The L2ProtocolData data type escribes L2 protocol data for a given virtual link used in a specific VNF deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.15): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class L2ProtocolData {

    /** Identifies the network name associated with this L2 protocol. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Specifies the network type for this L2 protocol. The value may be overridden at run-time. */
    @JsonProperty("network_type")
    private PropertyValue<String> networkType;

    /** Specifies whether to support VLAN transparency for this L2 protocol or not. required: true. */
    @JsonProperty("vlan_transparent")
    private PropertyValue<Boolean> vlanTransparent;

    /** Specifies the maximum transmission unit (MTU) value for this L2 protocol. */
    @JsonProperty("mtu")
    private PropertyValue<Integer> mtu;

    /** Specifies a specific virtualised network segment, which depends on the network type. For e.g., VLAN ID for VLAN network type and tunnel ID for GRE/VXLAN network types */
    @JsonProperty("segmentation_id")
    private PropertyValue<String> segmentationId;

}
