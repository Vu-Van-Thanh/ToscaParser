package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ServiceData} - SOL001 V5.4.1 clause 6.2.77.
 *
 * <p>The ServiceData data type indicates the service matching information exposed by the VirtualCp.
 *
 * <p><b>Additional requirements</b> (clause 6.2.77): None. 6.2.78 tosca.datatypes.nfv. SelectVnfDeployableModulesOperationConfiguration 6.2.78.1 Description The SelectVnfDeployableModulesOperationConfiguration data type represents information that affect the invocation of the SelectVnfDeployableModules operation, as specified in ETSI GS NFV-IFA 011 [1]. This data type definition is reserved for future use in the present document.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceData {

    /** Corresponds to the fully qualified domain name of a network host */
    @JsonProperty("host")
    private PropertyValue<String> host;

    /** path component of a URI. */
    @JsonProperty("path")
    private List<String> path;

}
