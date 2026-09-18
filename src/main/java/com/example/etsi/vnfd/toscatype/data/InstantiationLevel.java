package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.InstantiationLevel} - SOL001 V5.4.1 clause 6.2.18.
 *
 * <p>The InstantiationLevel data type describes the scale level for each aspect that corresponds to a given level of resources to be instantiated within a deployment flavour in term of the number VNFC instances.
 *
 * <p><b>Additional requirements</b> (clause 6.2.18): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstantiationLevel {

    /** Human readable description of the level required: true. */
    @JsonProperty("description")
    private PropertyValue<String> description;

    /** Represents for each aspect the scale level that corresponds to this instantiation level. scale_info shall be present if the VNF supports scaling. */
    @JsonProperty("scale_info")
    private Map<String, ScaleInfo> scaleInfo;

}
