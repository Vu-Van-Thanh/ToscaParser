package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.VduLevel;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.VduScalingAspectDeltas} - SOL001 V5.4.1 clause 6.10.6.
 *
 * <p>The VduScalingAspectDeltas type is a policy type representing the Vdu.Compute detail of an aspect deltas used for horizontal scaling, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.10.6): In the case of "uniform aspect", the deltas properties shall have only one entry. If a policy definition of this type is included in a service template, a policy definition of the type VduInitialDelta defined in clause 6.10.8 of the present document shall also be included with the same target.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_VDU_SCALING_ASPECT_DELTAS)
public class VduScalingAspectDeltas extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Represents the scaling aspect to which this policy applies. When two or more policies are related to the same scaling aspect, i.e. have the same value of the aspect property, all the respective targets of the policies (Vdu.Compute or Vdu.OsContainerDeployableUnit ), if they belong to a deployable module, shall belong exactly to the same deployable modules. required: true. */
        @JsonProperty("aspect")
        private PropertyValue<String> aspect;

        /** Describes the Vdu.Compute scaling deltas to be applied for every scaling steps of a particular aspect. required: true. */
        @JsonProperty("deltas")
        private Map<String, VduLevel> deltas;

    }

}
