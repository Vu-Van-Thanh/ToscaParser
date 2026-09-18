package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.L3ProtocolData} - SOL001 V5.4.1 clause 6.2.16.
 *
 * <p>The L3ProtocolData data type describes L3 protocol data for a given virtual link used in a specific VNF deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.16): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class L3ProtocolData {

    /** Identifies the network name associated with this L3 protocol. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Specifies IP version of this L3 protocol. The value of the ip_version property shall be consistent with the value of the layer_protocol in the connectivity_type property of the virtual link node. required: true. */
    @JsonProperty("ip_version")
    private PropertyValue<String> ipVersion;

    /** Specifies the CIDR (Classless Inter-Domain Routing) of this L3 protocol. The value may be overridden at run-time. required: true. */
    @JsonProperty("cidr")
    private PropertyValue<String> cidr;

    /** Specifies the allocation pools with start and end IP addresses for this L3 protocol. The value may be overridden at run-time. */
    @JsonProperty("ip_allocation_pools")
    private List<IpAllocationPool> ipAllocationPools;

    /** Specifies the gateway IP address for this L3 protocol. The value may be overridden at run-time. */
    @JsonProperty("gateway_ip")
    private PropertyValue<String> gatewayIp;

    /** Indicates whether DHCP (Dynamic Host Configuration Protocol) is enabled or disabled for this L3 protocol. The value may be overridden at run-time. */
    @JsonProperty("dhcp_enabled")
    private PropertyValue<Boolean> dhcpEnabled;

    /** Specifies IPv6 address mode. May be present when the value of the ipVersion attribute is "ipv6" and shall be absent otherwise. The value may be overridden at run-time. */
    @JsonProperty("ipv6_address_mode")
    private PropertyValue<String> ipv6AddressMode;

}
