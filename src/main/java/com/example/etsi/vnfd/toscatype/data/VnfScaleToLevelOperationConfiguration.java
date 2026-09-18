package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfScaleToLevelOperationConfiguration} - SOL001 V5.4.1 clause 6.2.23.
 *
 * <p>The VnfScaleToLevelOperationConfiguration data type represents information that affect the invocation of the ScaleVnfToLevel operation, as specified in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.23): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfScaleToLevelOperationConfiguration {

    /** Signals whether scaling according to the parameter "scaleInfo" is supported by this VNF required: true. */
    @JsonProperty("arbitrary_target_levels_supported")
    private PropertyValue<Boolean> arbitraryTargetLevelsSupported;

}
