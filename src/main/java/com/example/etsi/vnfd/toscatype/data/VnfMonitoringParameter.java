package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfMonitoringParameter} - SOL001 V5.4.1 clause 9.2.9.
 *
 * <p>This data type provides information on virtualised resource related performance metrics applicable to VNF.
 *
 * <p><b>Additional requirements</b> (clause 9.2.9): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfMonitoringParameter {

    /** Human readable name of the monitoring parameter required: true. */
    @JsonProperty("name")
    private PropertyValue<String> name;

    /** Identifies a performance metric to be monitored, according to ETSI GS NFV-IFA 027. required: true. */
    @JsonProperty("performance_metric")
    private PropertyValue<String> performanceMetric;

    /** Describes the periodicity at which to collect the performance information. */
    @JsonProperty("collection_period")
    private PropertyValue<Quantity> collectionPeriod;

}
