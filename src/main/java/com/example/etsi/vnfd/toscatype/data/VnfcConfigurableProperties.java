package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfcConfigurableProperties} - SOL001 V5.4.1 clause 6.2.10.
 *
 * <p>The VnfcConfigurableProperties data type defines the configurable properties of a VNFC, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.10): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfcConfigurableProperties {

    /** Describes additional configuration for VNFC that can be modified using the ModifyVnfInfo operation */
    @JsonProperty("additional_vnfc_configurable_properties")
    private VnfcAdditionalConfigurableProperties additionalVnfcConfigurableProperties;

}
