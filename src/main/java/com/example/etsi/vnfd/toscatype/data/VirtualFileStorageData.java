package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualFileStorageData} - SOL001 V5.4.1 clause 6.2.41.
 *
 * <p>The VirtualFileStorageData data type describes file storage requirements associated with compute resources in a particular VDU.
 *
 * <p><b>Additional requirements</b> (clause 6.2.41): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualFileStorageData {

    /** Size of virtualized storage resource required: true. */
    @JsonProperty("size_of_storage")
    private PropertyValue<Quantity> sizeOfStorage;

    /** Indicates valid values for the size of the virtualized storage resource. If this property is present the size of the virtualized storage resource can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the file storage resource is instantiated with the value indicated in the size_of_storage property. If this property is not present the size of the virtualized storage resource is not configurable via the VNF LCM interface and is always equal to the value indicated in the size_of_storage property. */
    @JsonProperty("size_of_storage_valid_values")
    private SizeRange sizeOfStorageValidValues;

    /** The shared file system protocol required: true. */
    @JsonProperty("file_system_protocol")
    private PropertyValue<String> fileSystemProtocol;

}
