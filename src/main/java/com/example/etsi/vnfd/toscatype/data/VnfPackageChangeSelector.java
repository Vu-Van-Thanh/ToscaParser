package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfPackageChangeSelector} - SOL001 V5.4.1 clause 6.2.60.
 *
 * <p>The VnfPackageChangeSelector data type describes the source and destination VNFDs as well as source deployment flavour for a change current VNF Package.
 *
 * <p><b>Additional requirements</b> (clause 6.2.60): Either the source_descriptor_id or the destination_descriptor_id shall be equal to the vnfdId of the VNFD containing this version VnfPackageChangeSelector.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangeSelector {

    /** Identifier of the source VNFD and the source VNF package. required: true. */
    @JsonProperty("source_descriptor_id")
    private PropertyValue<String> sourceDescriptorId;

    /** Identifier of the destination VNFD and the destination VNF package. required: true. */
    @JsonProperty("destination_descriptor_id")
    private PropertyValue<String> destinationDescriptorId;

    /** Identifier of the deployment flavour in the source VNF package for which this data type applies. required: true. */
    @JsonProperty("source_flavour_id")
    private PropertyValue<String> sourceFlavourId;

}
