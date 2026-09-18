package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.InterfaceDetails} - SOL001 V5.4.1 clause 6.2.49.
 *
 * <p>The InterfaceDetails data type describes information used to access an interface exposed by a VNF. It corresponds to the interfaceDetails attribute of the VnfInterfaceDetails information element defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.49): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfaceDetails {

    /** Provides components to build a Uniform Ressource Identifier (URI) where to access the interface end point. */
    @JsonProperty("uri_components")
    private UriComponents uriComponents;

    /** Provides additional details that are specific to the type of interface considered. */
    @JsonProperty("interface_specific_data")
    private Map<String, String> interfaceSpecificData;

}
