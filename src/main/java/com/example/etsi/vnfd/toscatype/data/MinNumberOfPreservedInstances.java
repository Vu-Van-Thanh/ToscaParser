package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.MinNumberOfPreservedInstances}.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinNumberOfPreservedInstances {

    /** Determines the size of the group for which the min_number_of_preserved_instances is specified. If not present the size is not limited. */
    @JsonProperty("group_size")
    private PropertyValue<Integer> groupSize;

    /** The minimum number of instances which need to be preserved simultaneously within the group of the specified size. required: true. */
    @JsonProperty("min_number_of_preserved_instances")
    private PropertyValue<Integer> minNumberOfPreservedInstances;

}
