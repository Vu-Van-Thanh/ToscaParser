package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.AdditionalServiceData;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VirtualCp} - SOL001 V5.4.1 clause 6.8.15.
 *
 * <p>A VirtualCp node type represents the VirtualCpd information element as defined in ETSI GS NFV-IFA 011 [1], which describes a requirement to create a virtual connection point allowing the access to a number of VNFC instances (based on their respective VDUs).
 *
 * <p>Represents the {@code VirtualCpd} information element of IFA011 V5.4.1 clause 7.1.18.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VIRTUAL_CP)
public class VirtualCp extends Cp {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties extends Cp.Properties {

        /** Additional service identification data of the VirtualCp exposed to NFV-MANO */
        @JsonProperty("additionalServiceData")
        private List<AdditionalServiceData> additionalServiceData;

        /** Indicates whether the corresponding MCIO supports configuration of an address pool name. required: true. */
        @JsonProperty("address_pool_name_configurable")
        private PropertyValue<Boolean> addressPoolNameConfigurable;

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
