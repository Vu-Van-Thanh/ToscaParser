package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfPackageChangeComponentMapping} - SOL001 V5.4.1 clause 6.2.61.
 *
 * <p>The VnfPackageChangeComponentMapping data type describes a mapping between the identifier of a components or property in the source VNFD and the identifier of the corresponding component or property in the destination VNFD.
 *
 * <p><b>Additional requirements</b> (clause 6.2.61): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfPackageChangeComponentMapping {

    /** The type of component or property. Possible values differentiate whether changes concern to some VNF component (e.g. VDU, internal VLD, etc.) or property (e.g. a Scaling Aspect, etc.). required: true. */
    @JsonProperty("component_type")
    private PropertyValue<String> componentType;

    /** Identifier of the component or property in the source VNFD. required: true. */
    @JsonProperty("source_id")
    private PropertyValue<String> sourceId;

    /** Identifier of the component or property in the destination VNFD. required: true. */
    @JsonProperty("destination_id")
    private PropertyValue<String> destinationId;

    /** Human readable description of the component changes. */
    @JsonProperty("description")
    private PropertyValue<String> description;

}
