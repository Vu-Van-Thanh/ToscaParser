package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ScalingAspect} - SOL001 V5.4.1 clause 6.2.28.
 *
 * <p>The ScalingAspect data type describes the details of an aspect used for horizontal scaling.
 *
 * <p><b>Additional requirements</b> (clause 6.2.28): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScalingAspect {

    /** Human readable name of the aspect required: true. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Human readable description of the aspect required: true. */
    @JsonProperty("description")
    private PropertyValue<String> description;

    /** Total number of scaling steps that can be applied w.r.t. this aspect. The value of this property corresponds to the number of scaling steps can be applied to this aspect when scaling it from the minimum scale level (i.e. 0) to the maximum scale level defined by this property required: true. */
    @JsonProperty("max_scale_level")
    private PropertyValue<Integer> maxScaleLevel;

    /** List of scaling deltas to be applied for the different subsequent scaling steps of this aspect. The first entry in the array shall correspond to the first scaling step (between scale levels 0 to 1) and the last entry in the array shall correspond to the last scaling step (between maxScaleLevel-1 and maxScaleLevel) */
    @JsonProperty("step_deltas")
    private List<String> stepDeltas;

}
