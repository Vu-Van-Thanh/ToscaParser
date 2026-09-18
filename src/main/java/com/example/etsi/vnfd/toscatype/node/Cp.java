package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.CpProtocolData;
import com.example.etsi.vnfd.toscatype.data.DomainNameData;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Cp} - SOL001 V5.4.1 clause 9.8.1.
 *
 * <p>A Cp node type represents the Cpd information element as defined in ETSI GS NFV-IFA 011 [1], which describes network connectivity to a compute resource or a VL. This is an abstract type used as parent for the various Cp node types.
 *
 * <p>Represents the {@code Cpd} information element of IFA011 V5.4.1 clause 7.1.6.3 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 9.8.1): The 'protocol' property shall not be included in a derived PnfExtCp node and shall be included in all other cases. 9.9 Group Types None. 9.10 Policy Types.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.CP)
public class Cp extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Identifies which protocol the connection point uses for connectivity purposes required: true. */
        @JsonProperty("layer_protocols")
        private List<String> layerProtocols;

        /** Specifies the capability of the CP to support IP dual stack or tunnelling */
        @JsonProperty("ip_stack_mode")
        private PropertyValue<String> ipStackMode;

        /** Identifies the role of the port in the context of the traffic flow patterns in the VNF or parent NS */
        @JsonProperty("role")
        private PropertyValue<String> role;

        /** Provides human-readable information on the purpose of the connection point */
        @JsonProperty("description")
        private PropertyValue<String> description;

        /** Provides information on the addresses to be assigned to the connection point(s) instantiated from this Connection Point Descriptor */
        @JsonProperty("protocol")
        private List<CpProtocolData> protocol;

        /** Provides information about whether the CP instantiated from this Cp is in Trunk mode (802.1Q or other), When operating in "trunk mode", the Cp is capable of carrying traffic for several VLANs. Absence of this property implies that trunkMode is not configured for the Cp i.e. It is equivalent to boolean value "false". */
        @JsonProperty("trunk_mode")
        private PropertyValue<Boolean> trunkMode;

        /** Identifies in a machine-processable form the purpose of the CP. */
        @JsonProperty("purpose")
        private List<String> purpose;

        /** Provides information on the domain names to be applied to the CP. */
        @JsonProperty("domain_name")
        private List<DomainNameData> domainName;

    }

}
