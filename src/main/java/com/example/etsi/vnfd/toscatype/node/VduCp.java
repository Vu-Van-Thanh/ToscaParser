package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.VirtualNetworkInterfaceRequirements;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VduCp} - SOL001 V5.4.1 clause 6.8.8.
 *
 * <p>A VduCp node type represents the VduCpd information element as defined in ETSI GS NFV-IFA 011 [1], which describes network connectivity between a VNFC instance (based on VDU) and an internal VL.
 *
 * <p>Represents the {@code VduCpd} information element of IFA011 V5.4.1 clause 7.1.6.4 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.8): The occurrence 0 of the virtual_binding requirement is applicable for node templates of tosca.nodes.nfv.VduSubCp node type derived from tosca.nodes.nfv.VduCp. For node templates of tosca.nodes.nfv.VduCp node type occurrence 1 applies.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_CP)
public class VduCp extends Cp {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties extends Cp.Properties {

        /** Bitrate requirement in bit per second on this connection point */
        @JsonProperty("bitrate_requirement")
        private PropertyValue<Integer> bitrateRequirement;

        /** Specifies requirements on a virtual network interface realising the CPs instantiated from this CPD */
        @JsonProperty("virtual_network_interface_requirements")
        private List<VirtualNetworkInterfaceRequirements> virtualNetworkInterfaceRequirements;

        /** The order of the NIC on the compute instance (e.g.eth2) */
        @JsonProperty("order")
        private PropertyValue<Integer> order;

        /** Describes the type of the virtual network interface realizing the CPs instantiated from this CPD */
        @JsonProperty("vnic_type")
        private PropertyValue<String> vnicType;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [0, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_link")
        private List<String> virtualLink;

        /** capability tosca.capabilities.nfv.VirtualBindable, occurrences [0, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_binding")
        private List<String> virtualBinding;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.TrunkBindable, occurrences [0, UNBOUNDED]. */
        @JsonProperty("trunk_binding")
        private Map<String, Object> trunkBinding;

    }

}
