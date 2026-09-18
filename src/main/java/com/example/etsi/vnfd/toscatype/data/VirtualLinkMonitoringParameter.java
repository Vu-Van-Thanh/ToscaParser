package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualLinkMonitoringParameter} - SOL001 V5.4.1 clause 6.2.48.
 *
 * <p>This data type provides information on virtualised resource related performance metrics.
 *
 * <p><b>Additional requirements</b> (clause 6.2.48): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualLinkMonitoringParameter {

    /** Human readable name of the monitoring parameter required: true. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Identifies a performance metric to be monitored. required: true. */
    @JsonProperty("performance_metric")
    private PropertyValue<String> performanceMetric;

    /** Describes the periodicity at which to collect the performance information. */
    @JsonProperty("collection_period")
    private PropertyValue<Quantity> collectionPeriod;

}
