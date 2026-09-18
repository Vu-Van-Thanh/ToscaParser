package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.ExtendedResourceData} - SOL001 V5.4.1 clause 6.2.70.
 *
 * <p>The ExtendedResourceData data type supports the specification of requirements related to extended resources of a container, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.70): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedResourceData {

    /** The hardware platform specific extended resource. A map of string that contains one single key-value pair that describes one hardware platform specific container requirement. required: true. */
    @JsonProperty("extended_resource")
    private Map<String, String> extendedResource;

    /** Requested amount of the indicated extended resource. required: true. */
    @JsonProperty("amount")
    private PropertyValue<Integer> amount;

    /** If this property is present the amount of the extended resource requested for the container can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated with the value indicated in the amount property. If this property is not present the amount of the extended resource requested for the container is not configurable via the VNF LCM interface and is always equal to the value indicated in the amount attribute. required: true. */
    @JsonProperty("amount_valid_values")
    private IntegerRange amountValidValues;

}
