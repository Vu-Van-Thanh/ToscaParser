package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.Hugepages} - SOL001 V5.4.1 clause 6.2.71.
 *
 * <p>The Hugepages data type supports the specification of requirements on a particular hugepage size in terms of total memory needs.
 *
 * <p><b>Additional requirements</b> (clause 6.2.71): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hugepages {

    /** Specifies the size of the hugepage. required: true. */
    @JsonProperty("hugepage_size")
    private PropertyValue<Quantity> hugepageSize;

    /** Specifies the total size required for all the hugepages of the size indicated by hugepage_size. required: true. */
    @JsonProperty("requested_size")
    private PropertyValue<Quantity> requestedSize;

    /** If this property is present the the total size required for all the hugepages of the size indicated by hugepage_size can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the value indicated in the requested_size property is used. If this property is not present the total size required for all the hugepages of the size indicated by hugepage_size is not configurable via the VNF LCM interface and is always equal to the value indicated in the requested_size attribute. */
    @JsonProperty("requested_size_valid_values")
    private SizeRange requestedSizeValidValues;

}
