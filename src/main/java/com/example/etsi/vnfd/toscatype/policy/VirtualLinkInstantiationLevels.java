package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.toscatype.data.VirtualLinkBitrateLevel;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.VirtualLinkInstantiationLevels} - SOL001 V5.4.1 clause 6.10.3.
 *
 * <p>The VirtualLinkInstantiationLevels type is a policy type representing all the instantiation levels of virtual link resources to be instantiated within a deployment flavour as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.10.3): A VirtualLinkInstantiationLevels policy shall contain an entry for each instantiation level (and only for them) defined in the InstantiationLevels policy. 6.10.4 Void.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_VL_INSTANTIATION_LEVELS)
public class VirtualLinkInstantiationLevels extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Describes the virtual link levels of resources that can be used to instantiate the VNF using this flavour. required: true. */
        @JsonProperty("levels")
        private Map<String, VirtualLinkBitrateLevel> levels;

    }

}
