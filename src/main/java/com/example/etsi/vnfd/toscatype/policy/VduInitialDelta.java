package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.toscatype.data.VduLevel;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.VduInitialDelta} - SOL001 V5.4.1 clause 6.10.8.
 *
 * <p>The VduInitialDelta type is a policy type representing the Vdu.Compute detail of an initial delta used for horizontal scaling, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_VDU_INITIAL_DELTA)
public class VduInitialDelta extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Represents the initial minimum size of the VNF. required: true. */
        @JsonProperty("initial_delta")
        private VduLevel initialDelta;

    }

}
