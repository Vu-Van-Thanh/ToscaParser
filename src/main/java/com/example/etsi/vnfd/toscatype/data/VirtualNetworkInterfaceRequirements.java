package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualNetworkInterfaceRequirements} - SOL001 V5.4.1 clause 6.2.4.
 *
 * <p>The VirtualNetworkInterfaceRequirements data type describes requirements on a virtual network interface, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.4): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualNetworkInterfaceRequirements {

    /** Provides a human readable name for the requirement. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Provides a human readable description of the requirement. */
    @JsonProperty("description")
    private PropertyValue<String> description;

    /** The network interface requirements. A map of strings that contain a set of key-value pairs that describes the hardware platform specific network interface deployment requirements. required: true. */
    @JsonProperty("network_interface_requirements")
    private Map<String, String> networkInterfaceRequirements;

    /** references (couples) the CP with any logical node I/O requirements (for network devices) that may have been created. Linking these attributes is necessary so that so that I/O requirements that need to be articulated at the logical node level can be associated with the network interface requirements associated with the CP. */
    @JsonProperty("nic_io_requirements")
    private LogicalNodeData nicIoRequirements;

}
