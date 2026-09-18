package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VipCp} - SOL001 V5.4.1 clause 6.8.10.
 *
 * <p>A VipCp node type represents the VipCpd information element as defined in ETSI GS NFV-IFA 011 [1], which describes a connection point to allocate one or a set of virtual IP addresses.
 *
 * <p>Represents the {@code VipCpd} information element of IFA011 V5.4.1 clause 7.1.17.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VIP_CP)
public class VipCp extends Cp {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties extends Cp.Properties {

        /** Indicates whether the VIP address shall be different from the addresses allocated to all associated VduCp instances or shall be the same as one of them. required: true. */
        @JsonProperty("dedicated_ip_address")
        private PropertyValue<Boolean> dedicatedIpAddress;

        /** Indicates the function the virtual IP address is used for: high availability or load balancing. When used for high availability, only one of the internal VDU CP instances or VNF external CP instances that share the virtual IP is bound to the VIP address at a time. When used for load balancing purposes all CP instances that share the virtual IP are bound to it. required: true. */
        @JsonProperty("vip_function")
        private PropertyValue<String> vipFunction;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.Node, occurrences [1, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("target")
        private List<String> target;

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [0, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_link")
        private List<String> virtualLink;

    }

}
