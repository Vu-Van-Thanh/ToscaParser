package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfOperateOperationConfiguration} - SOL001 V5.4.1 clause 6.2.26.
 *
 * <p>The VnfOperateOperationConfiguration data type represents information that affect the invocation of the OperateVnf operation, as specified in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.26): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfOperateOperationConfiguration {

    /** Minimum timeout value for graceful stop of a VNF instance required: true. */
    @JsonProperty("min_graceful_stop_timeout")
    private PropertyValue<Quantity> minGracefulStopTimeout;

    /** Maximum recommended timeout value that can be needed to gracefully stop a VNF instance of a particular type under certain conditions, such as maximum load condition. This is provided by VNF provider as information for the operator facilitating the selection of optimal timeout value. This value is not used as constraint */
    @JsonProperty("max_recommended_graceful_stop_timeout")
    private PropertyValue<Quantity> maxRecommendedGracefulStopTimeout;

}
