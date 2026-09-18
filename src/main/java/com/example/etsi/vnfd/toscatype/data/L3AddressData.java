package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.L3AddressData} - SOL001 V5.4.1 clause 9.2.2.
 *
 * <p>The L3AddressData data type supports providing information about Layer 3 level addressing scheme and parameters applicable to a CP, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 9.2.2): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class L3AddressData {

    /** Specify which mode is used for the IP address assignment. If it is set to True and this property is not used in the context of the VirtualCp node type, IP configuration information shall be provided for the VNF by a management entity using the NFV MANO interfaces towards the VNFM. If it is set to True and this property is used in the context of the VirtualCp node type, IP configuration information should be provided for the VNF by a management entity using the NFV MANO interfaces towards the VNFM. If it is not provided, the CISM assigns an IP address. If it is set to False, the value of the ip_address_assignment_subtype property defines the method of IP address assignment. Shall be present if the fixed_ip_address property is not present and should be absent otherwise. */
    @JsonProperty("ip_address_assignment")
    private PropertyValue<Boolean> ipAddressAssignment;

    /** Method of IP address assignment in case the IP configuration is not provided using the NFV MANO interfaces towards the VNFM. Description of the valid values: (1) dynamic: the VNF gets an IP address that is dynamically assigned by the NFVI/VIM/CISM without receiving IP configuration information from the MANO interfaces, (2) vnf_pkg: an IP address defined by the VNF provider is assigned by means included as part of the VNF package (e.g., LCM script); (3) external: an IP address is provided by an external management entity (such as EM) directly towards the VNF. Shall be present in case the ip_address_assignment property is set to False and shall be absent otherwise. */
    @JsonProperty("ip_address_assignment_subtype")
    private PropertyValue<String> ipAddressAssignmentSubtype;

    /** Specifies if the floating IP scheme is activated on the Connection Point or not required: true. */
    @JsonProperty("floating_ip_activated")
    private PropertyValue<Boolean> floatingIpActivated;

    /** Defines address type. The address type should be aligned with the address type supported by the layer_protocols properties of the connetion point */
    @JsonProperty("ip_address_type")
    private PropertyValue<String> ipAddressType;

    /** Minimum number of IP addresses to be assigned */
    @JsonProperty("number_of_ip_address")
    private PropertyValue<Integer> numberOfIpAddress;

    /** Fixed IP addresses to be assigned to the internal CP instance. This property enables the VNF provider to define fixed IP addresses for internal CP instances to be assigned by the VNFM or the NFVO. This attribute property is only permitted for Cpds without external connectivity, i.e. connectivity outside the VNF. If present, it shall be compatible with the values of the L3ProtocolData of the VnfVirtualLink referred to by the Cp, if L3ProtocolData is included in the VnfVirtualLink */
    @JsonProperty("fixed_ip_address")
    private List<String> fixedIpAddress;

}
