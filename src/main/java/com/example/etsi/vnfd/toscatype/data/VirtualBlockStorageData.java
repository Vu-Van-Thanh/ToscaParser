package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualBlockStorageData} - SOL001 V5.4.1 clause 6.2.39.
 *
 * <p>The VirtualBlockStorageData data type describes block storage requirements associated with compute resources in a particular VDU, either as a local disk or as virtual attached storage.
 *
 * <p><b>Additional requirements</b> (clause 6.2.39): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualBlockStorageData {

    /** Size of virtualised storage resource required: true. */
    @JsonProperty("size_of_storage")
    private PropertyValue<Quantity> sizeOfStorage;

    /** Indicates valid values for the size of the virtualized storage resource. If this property is present the size of the virtualized storage resource can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the block storage resource is instantiated with the value indicated in the size_of_storage property. If this property is not present the size of the virtualized storage resource is not configurable via the VNF LCM interface and is always equal to the value indicated in the size_of_storage property. required: true. */
    @JsonProperty("size_of_storage_valid_values")
    private SizeRange sizeOfStorageValidValues;

    /** The hardware platform specific storage requirements. A map of strings that contains a set of key-value pairs that represents the hardware platform specific storage deployment requirements */
    @JsonProperty("vdu_storage_requirements")
    private Map<String, String> vduStorageRequirements;

    /** Indicates if the storage support RDMA required: true. */
    @JsonProperty("rdma_enabled")
    private PropertyValue<Boolean> rdmaEnabled;

}
