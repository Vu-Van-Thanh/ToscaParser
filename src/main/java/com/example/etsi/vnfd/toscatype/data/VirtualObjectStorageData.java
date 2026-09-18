package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualObjectStorageData} - SOL001 V5.4.1 clause 6.2.40.
 *
 * <p>The VirtualObjectStorageData data type describes object storage requirements associated with compute resources in a particular VDU.
 *
 * <p><b>Additional requirements</b> (clause 6.2.40): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualObjectStorageData {

    /** Maximum size of virtualized storage resource */
    @JsonProperty("max_size_of_storage")
    private PropertyValue<Quantity> maxSizeOfStorage;

    /** Indicates valid values for the maximum size of the virtualized storage resource. If this property is present the maximum size of the virtualized storage resource can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the object storage resource is instantiated with the value indicated in the max_size_of_storage property. If this property is not present the maximum size of the virtualized storage resource is not configurable via the VNF LCM interface and it is set to the value indicated in the in the max_size_of_storage property, if this property is present. */
    @JsonProperty("max_size_of_storage_valid_values")
    private SizeRange maxSizeOfStorageValidValues;

}
