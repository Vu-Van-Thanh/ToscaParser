package com.example.etsi.vnfd.toscatype.artifact;

import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.artifacts.nfv.HelmChart} - SOL001 V5.4.1 clause 6.3.3.
 *
 * <p>The HelmChart artifact is a file containing a HelmTM chart [23]. TOSCA-Simple-Profile-YAML-v1.3 [20]. Whether the Helm chart contains custom resource definitions is out of scope of the present document.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.ARTIFACT_HELM_CHART)
public class HelmChart extends NfvArtifact {

}
