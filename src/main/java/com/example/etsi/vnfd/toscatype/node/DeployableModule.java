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
 * {@code tosca.nodes.nfv.DeployableModule} - SOL001 V5.4.1 clause 6.8.16.
 *
 * <p>A DeployableModule node type represents the DeployableModule information element as defined in ETSI GS NFV-IFA 011 [1], which describes a set of optional VDUs.
 *
 * <p>Represents the {@code DeployableModule} information element of IFA011 V5.4.1 clause 7.1.8.24 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.DEPLOYABLE_MODULE)
public class DeployableModule extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Name of the deployable module required: true. */
        @JsonProperty("name")
        private PropertyValue<String> name;

        /** Describes the DeployableModule, e.g. in terms of the function performed by the VNFCs deployed with their associated VDUs. */
        @JsonProperty("description")
        private PropertyValue<String> description;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.DeployableModuleMember, occurrences [1, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("member")
        private List<String> member;

    }

}
