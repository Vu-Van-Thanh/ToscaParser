package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.SecurityGroupRule} - SOL001 V5.4.1 clause 6.10.13.
 *
 * <p>The SecurityGroupRule type is a policy type specifying the matching criteria for the ingress and/or egress traffic to and from visited connection points as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code SecurityGroupRule} information element of IFA011 V5.4.1 clause 7.1.6.9 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.10.13): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_SECURITY_GROUP_RULE)
public class SecurityGroupRule extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Human readable description of the security group rule. */
        @JsonProperty("description")
        private PropertyValue<String> description;

        /** The direction in which the security group rule is applied. The direction of 'ingress' or 'egress' is specified against the associated CP. I.e., 'ingress' means the packets entering a CP, while 'egress' means the packets sent out of a CP. required: true. */
        @JsonProperty("direction")
        private PropertyValue<String> direction;

        /** Indicates the protocol carried over the Ethernet layer. required: true. */
        @JsonProperty("ether_type")
        private PropertyValue<String> etherType;

        /** Indicates the protocol carried over the IP layer. Permitted values include any protocol defined in the IANA protocol registry, e.g. TCP, UDP, ICMP, etc. required: true. */
        @JsonProperty("protocol")
        private PropertyValue<String> protocol;

        /** Indicates minimum port number in the range that is matched by the security group rule. If a value is provided at design-time, this value may be overridden at run-time based on other deployment requirements or constraints. required: true. */
        @JsonProperty("port_range_min")
        private PropertyValue<Integer> portRangeMin;

        /** Indicates maximum port number in the range that is matched by the security group rule. If a value is provided at design-time, this value may be overridden at run-time based on other deployment requirements or constraints. required: true. */
        @JsonProperty("port_range_max")
        private PropertyValue<Integer> portRangeMax;

    }

}
