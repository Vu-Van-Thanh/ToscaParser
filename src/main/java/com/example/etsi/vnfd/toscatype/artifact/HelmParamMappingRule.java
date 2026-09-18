package com.example.etsi.vnfd.toscatype.artifact;

import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.artifacts.nfv.HelmParamMappingRule} - SOL001 V5.4.1 clause 6.3.5.
 *
 * <p>The HelmParamMappingRule artifact contains a file with rules used by the HelmParamMappingScript artifact defined in the same node template as this artifact, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE)
public class HelmParamMappingRule extends NfvArtifact {

}
