package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.MaxNumberOfImpactedInstances} - SOL001 V5.4.1 clause 6.2.72.
 *
 * <p>The MaxNumberOfImpactedInstances data type specifies the maximum number of instances of a given Vdu.Compute node or VnfVirtualLink node that may be impacted simultaneously without impacting the functionality of the group of a given size.
 *
 * <p><b>Additional requirements</b> (clause 6.2.72): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaxNumberOfImpactedInstances {

    /** Determines the size of the group for which the max_number_of_impacted_instances is specified. If not present the size is not limited. */
    @JsonProperty("group_size")
    private PropertyValue<Integer> groupSize;

    /** The maximum number of instances that can be impacted simultaneously within the group of the specified size. required: true. */
    @JsonProperty("max_number_of_impacted_instances")
    private PropertyValue<Integer> maxNumberOfImpactedInstances;

}
