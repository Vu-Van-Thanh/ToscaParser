package com.example.etsi.vnfd.toscatype.node;

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
 * {@code tosca.nodes.nfv.VnfExtCp} - SOL001 V5.4.1 clause 6.8.2.
 *
 * <p>The VnfExtCp node type represents the VnfExtCpd information element as defined in ETSI GS NFV-IFA 011 [1], which describes a logical external connection point, exposed by this VNF enabling connecting with an external Virtual Link.
 *
 * <p>Represents the {@code VnfExtCpd} information element of IFA011 V5.4.1 clause 7.1.3.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.2): A node template of this type is used to represent a VNF external connection point only in the case the VnfExtCp is connected to an internal virtual link. The node template has the following requirements: - internal_virtual_link requirement to allow to connect it to an internal virtual link; - external_virtual_link requirement to allow to connect it to an external virtual link. In the case where a VNF external connection point is re-exposing a VduCp (internal connection point) or a VipCp or a VirtualCp or a VduSubCp, the VduCp or VipCp or VirtualCp or VduSubCp node type shall be used in the service template, instead of the VnfExtCp node type.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VNF_EXT_CP)
public class VnfExtCp extends Cp {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties extends Cp.Properties {

        /** The actual virtual NIC requirements that is been assigned when instantiating the connection point */
        @JsonProperty("virtual_network_interface_requirements")
        private List<VirtualNetworkInterfaceRequirements> virtualNetworkInterfaceRequirements;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [0, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("external_virtual_link")
        private List<String> externalVirtualLink;

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [1, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("internal_virtual_link")
        private List<String> internalVirtualLink;

    }

}
