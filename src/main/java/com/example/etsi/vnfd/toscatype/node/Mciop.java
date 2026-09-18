package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Mciop} - SOL001 V5.4.1 clause 6.8.14.
 *
 * <p>The Mciop node type does not correspond to an information element defined in ETSI GS NFV-IFA 011 [1]. It is a representation of the object described by the mciop artifact, capable of being profiled by the properties of the MciopProfile information element defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Table 6.1-1 NOTE 3: there is no direct mapping between MciopProfile and tosca.nodes.nfv.Mciop. The deploymentOrder and associatedVdu attributes of MciopProfile map to this node type; affinityOrAntiAffinityGroupId maps to tosca.policies.nfv.AffinityRule or AntiAffinityRule.
 *
 * <p><b>Additional requirements</b> (clause 6.8.14): The dependency requirement as defined in TOSCA-Simple-Profile-YAML-v1.3 [20] may be used towards other Mciop nodes to express the order of deployment. Node templates of type tosca.nodes.nfv.Mciop may contain an artifact definition of type tosca.artifacts.nfv.HelmChart. There shall be a maximum number of one such artifact definition in a tosca.nodes.nfv.Mciop node template. Node templates of type tosca.nodes.nfv.Mciop may contain an artifact definition of type tosca.artifacts.nfv HelmParamMappingScript and an artifact of type HelmParamMappingRule. There shall be a maximum number of one such artifact definition of each type in a tosca.nodes.nfv.Mciop node template. If there is no artifact definition of type HelmParamMappingScript there shall be no artifact definition of type HelmParamMappingRule.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.MCIOP)
public class Mciop extends NfvNode {

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.AssociableVdu, occurrences [1, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("associatedVdu")
        private List<String> associatedVdu;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.DeployableModuleMember, occurrences [1, UNBOUNDED]. */
        @JsonProperty("deployable_module_member")
        private Map<String, Object> deployableModuleMember;

    }

}
