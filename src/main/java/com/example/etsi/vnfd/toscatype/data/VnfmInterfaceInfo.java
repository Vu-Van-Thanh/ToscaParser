package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfmInterfaceInfo} - SOL001 V5.4.1 clause 6.2.54.
 *
 * <p>The VnfmInterfaceInfo data type describes information enabling the VNF instance to access the NFV-MANO interfaces produced by the VNFM (e.g. URIs and credentials).
 *
 * <p><b>Additional requirements</b> (clause 6.2.54): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfmInterfaceInfo {

    /** Identifies an interface produced by the VNFM. required: true. */
    @JsonProperty("interface_name")
    private PropertyValue<String> interfaceName;

    /** Provide additional data to access the interface endpoint */
    @JsonProperty("details")
    private InterfaceDetails details;

    /** Provides credential enabling access to the interface */
    @JsonProperty("credentials")
    private Map<String, String> credentials;

}
