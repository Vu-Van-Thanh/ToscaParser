package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VduSubCp} - SOL001 V5.4.1 clause 6.8.11.
 *
 * <p>A VduSubCp node type represents the Subport information element as defined in ETSI GS NFV-IFA 011 [1], which describes network connectivity between a VNFC instance (based on VDU) and an internal VL through a trunk port.
 *
 * <p><b>Additional requirements</b> (clause 6.8.11): The trunk_mode property of the VduSubCp node shall be set as false.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_SUB_CP)
public class VduSubCp extends VduCp {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties extends VduCp.Properties {

        /** Specifies the encapsulation type for the traffics coming in and out of the trunk subport. */
        @JsonProperty("segmentation_type")
        private PropertyValue<String> segmentationType;

        /** Specifies the segmentation ID for the subport, which is used to differentiate the traffics on different networks coming in and out of the trunk port. */
        @JsonProperty("segmentation_id")
        private PropertyValue<Integer> segmentationId;

        /** Indicates if additional parameters are exchanged through the use of a routing protocol. */
        @JsonProperty("is_automatic_discovery")
        private PropertyValue<Boolean> isAutomaticDiscovery;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements extends VduCp.Requirements {

        /** capability tosca.capabilities.nfv.TrunkBindable, occurrences [1, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("trunk_binding")
        private List<String> trunkBinding;

    }

}
