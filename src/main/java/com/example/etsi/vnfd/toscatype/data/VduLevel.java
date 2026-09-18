package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VduLevel} - SOL001 V5.4.1 clause 6.2.19.
 *
 * <p>The VduLevel data type indicates for a given Vdu.Compute in a given level the number of instances to deploy.
 *
 * <p><b>Additional requirements</b> (clause 6.2.19): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VduLevel {

    /** Number of instances of VNFC based on this VDU to deploy for this level. required: true. */
    @JsonProperty("number_of_instances")
    private PropertyValue<Integer> numberOfInstances;

}
