package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.CpuPowerState} - SOL001 V5.4.1 clause 6.2.84.
 *
 * <p>The CpuPowerState data type supports the specification of requirements related to the CPU power state configuration of a virtual compute resource or OS container, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.84): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CpuPowerState {

    /** Indicates the policy for CPU power state management. The policy can take values of "static" or "dynamic". In case of "static", the virtual CPU cores are requested to be allocated to logical CPU cores according to the cpu_operational_power_state property. In case of "dynamic" the CPU power states of virtual CPU cores can be allocated to logical CPU cores whose power states can be adjusted dynamically depending on core utilization. required: true. */
    @JsonProperty("cpu_power_state_management_policy")
    private PropertyValue<String> cpuPowerStateManagementPolicy;

    /** Provides the list of operational power states (i.e., P and C states) defined for the virtual CPU. required: true. */
    @JsonProperty("cpu_operational_power_state")
    private List<String> cpuOperationalPowerState;

}
