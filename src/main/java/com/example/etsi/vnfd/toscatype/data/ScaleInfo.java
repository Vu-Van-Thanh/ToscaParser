package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ScaleInfo} - SOL001 V5.4.1 clause 9.2.12.
 *
 * <p>The scaleInfo data type indicates for a given scaleAspect the corresponding scaleLevel.
 *
 * <p><b>Additional requirements</b> (clause 9.2.12): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleInfo {

    /** The scale level for a particular aspect required: true. */
    @JsonProperty("scale_level")
    private PropertyValue<Integer> scaleLevel;

}
