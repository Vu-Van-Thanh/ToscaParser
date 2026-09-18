package com.example.etsi.vnfd.toscatype.artifact;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.artifacts.nfv.HelmParamMappingScript} - SOL001 V5.4.1 clause 6.3.4.
 *
 * <p>The HelmParamMappingScript artifact contains an executable file that generates in its standard output the contents of a HelmTM values.yaml file [23] to be passed to a Helm based CISM service interface for containerized workloads based on MCIOPs, when invoking a command triggered by a VNF LCM operation. TOSCA-Simple-Profile-YAML-v1.3 [20]. The executable file runs in the VNFM execution environment. It is invoked, after receiving the VNF LCM operation and prior to sending the request to the Helm based CISM service interface, with the following list of ordered parameters: 1) URL of a readable file containing the complete task resource (e.g. InstantiateVnfRequest) as received in the VNF LCM operation, in JSON format. 2) URL of a readable zip file with the contents of the VNFD, as defined in clause 10.4.4.3.2 of ETSI GS NFV-SOL 003 [25] using the option without security information. 3) URL of the file referenced in the HelmParamMappingRule artifact defined in the same node template as the HelmParamMappingScript artifact, if any. The executable file is only invoked for VNF LCM operations that trigger a request towards CISM, except the Query VNF operation, and if the artifact of type HelmParamMappingScript is defined for the applicable Mciop node template.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT)
public class HelmParamMappingScript extends NfvArtifact {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** required: true. */
        @JsonProperty("language")
        private PropertyValue<String> language;

    }

}
