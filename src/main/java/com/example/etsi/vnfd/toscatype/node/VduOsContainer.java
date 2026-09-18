package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.toscatype.data.CpuPowerState;
import com.example.etsi.vnfd.toscatype.data.ExtendedResourceData;
import com.example.etsi.vnfd.toscatype.data.Hugepages;
import com.example.etsi.vnfd.toscatype.data.IntegerRange;
import com.example.etsi.vnfd.toscatype.data.SizeRange;
import com.example.etsi.vnfd.toscatype.data.VirtualCpuPinning;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Vdu.OsContainer} - SOL001 V5.4.1 clause 6.8.12.
 *
 * <p>The Vdu.OsContainer node type represents the OsContainerDesc information element as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code OsContainerDesc} information element of IFA011 V5.4.1 clause 7.1.6.13 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.12): Node templates of type tosca.nodes.nfv.Vdu.OsContainer shall contain an artifact definition of type tosca.artifacts.nfv.SwImage. There shall be a maximum number of one such artifact definition in a tosca.nodes.nfv.Vdu.OsContainer node template. The node template name of type tosca.nodes.nfv.Vdu.OsContainer fulfils the purpose of the "id" attribute of the SwImageDesc information element in ETSI GS NFV-IFA 011 [1] and hence it will be used in APIs to identify the software image id from the VNFD perspective.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_OS_CONTAINER)
public class VduOsContainer extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Human readable name of the OS container required: true. */
        @JsonProperty("name")
        private PropertyValue<String> name;

        /** Human readable description of the OS container required: true. */
        @JsonProperty("description")
        private PropertyValue<String> description;

        /** Number of CPU resources requested for the OS container in milli-CPU. */
        @JsonProperty("requested_cpu_resources")
        private PropertyValue<Integer> requestedCpuResources;

        /** Indicates valid values for the number of CPU resources requested for the container in milli-CPU. If this property is present the number of CPU resources requested for the container can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated with the value indicated in the requested_cpu_resources property. If this property is not present the number of CPU resources requested for the container is not configurable via the VNF LCM interface and it is set to the value indicated in the requested_cpu_resources property, if this property is present. */
        @JsonProperty("requested_cpu_resources_valid_values")
        private IntegerRange requestedCpuResourcesValidValues;

        /** Number of CPU resources the OS container can maximally use in milli-CPU. */
        @JsonProperty("cpu_resource_limit")
        private PropertyValue<Integer> cpuResourceLimit;

        /** Indicates valid values for the number of CPU resources the container can maximally use in milli-CPU. If this property is present the number of CPU resources the container can maximally use can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated allowing the container to maximally use the value indicated in the cpu_resource_limit property. If this property is not present the number of CPU resources the container can maximally use is not configurable via the VNF LCM interface and it is set to the value indicated in the cpu_resource_limit property, if this property is present. */
        @JsonProperty("cpu_resource_limit_valid_values")
        private IntegerRange cpuResourceLimitValidValues;

        /** Amount of memory resources requested for the OS container (e.g. in MB). */
        @JsonProperty("requested_memory_resources")
        private PropertyValue<Quantity> requestedMemoryResources;

        /** Indicates valid values for the amount of memory resources requested for the container. If this property is present the amount of memory resources requested for the container can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated with the value indicated in the requested_memory_resources property. If this property is not present the amount of memory resources requested for the container is not configurable via the VNF LCM interface and it is set to the value indicated in the requested_memory_resources property, if this property is present. */
        @JsonProperty("requested_memory_resources_valid_values")
        private SizeRange requestedMemoryResourcesValidValues;

        /** Amount of memory resources the OS container can maximum use (e.g. in MB). */
        @JsonProperty("memory_resource_limit")
        private PropertyValue<Quantity> memoryResourceLimit;

        /** Indicates valid values for the amount of memory resources the container can maximally use (e.g. in MB). If this property is present the amount of memory resources the container can maximally use can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated allowing the container to maximally use the value indicated in the memory_resource_limit property. If this property is not present the amount of memory resources the container can maximally use is not configurable via the VNF LCM interface and it is set to the value indicated in the memory_resource_limit property, if this property is present. */
        @JsonProperty("memory_resource_limit_valid_values")
        private SizeRange memoryResourceLimitValidValues;

        /** Size of ephemeral storage resources requested for the OS container (e.g. in GB). */
        @JsonProperty("requested_ephemeral_storage_resources")
        private PropertyValue<Quantity> requestedEphemeralStorageResources;

        /** Indicates valid values for the size of ephemeral storage resources requested for the container (e.g. in GB). If this property is present the amount of ephemeral storage resources requested for the container can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated with the value indicated in the requested_ephemeral_storage_resources property. If this property is not present the amount of ephemeral storage resources requested for the container is not configurable via the VNF LCM interface and it is set to the value indicated in the requested_ephemeral_storage_resources property, if this property is present. */
        @JsonProperty("requested_ephemeral_storage_resources_valid_values")
        private SizeRange requestedEphemeralStorageResourcesValidValues;

        /** Size of ephemeral storage resources the OS container can maximum use (e.g. in GB). */
        @JsonProperty("ephemeral_storage_resource_limit")
        private PropertyValue<Quantity> ephemeralStorageResourceLimit;

        /** Indicates valid values for the size of ephemeral storage resources the container can maximally use (e.g. in GB). If this property is present the size of ephemeral storage resources the container can maximally use can be indicated in a VNF LCM operation. If no value is indicated in the VNF LCM operation, the set of co-located container compute resources is instantiated allowing the container to maximally use the value indicated in the ephemeral_storage_resource_limit property. If this attribute is not present the amount of ephemeral storage resources the container can maximally use is not configurable via the VNF LCM interface and it set to the value indicated in the ephemeral_storage_resource_limit, if this property is present. */
        @JsonProperty("ephemeral_storage_resource_limit_valid_values")
        private SizeRange ephemeralStorageResourceLimitValidValues;

        /** Extended resources and their respective amount required by the container. It may also optionally indicate a range of valid values of the amount required if this can be configurable via the VNF LCM interface. */
        @JsonProperty("extended_resource_requests")
        private List<ExtendedResourceData> extendedResourceRequests;

        /** The requirement for huge pages resources. Each element in the list indicates a hugepage size and the total memory requested for hugepages of that size. It may also optionally indicate a range of valid values for the total memory requested for hugepages of that size if the amount required can be configurable via the VNF LCM interface. */
        @JsonProperty("huge_pages_resources")
        private List<Hugepages> hugePagesResources;

        /** Requirements for CPU pinning configuration. */
        @JsonProperty("cpu_pinning_requirements")
        private VirtualCpuPinning cpuPinningRequirements;

        /** Requirements for CPU power state configuration for the OS container. */
        @JsonProperty("cpu_power_state_requirements")
        private CpuPowerState cpuPowerStateRequirements;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.ContainerDeployable, occurrences [1, UNBOUNDED]. */
        @JsonProperty("container_deployable")
        private Map<String, Object> containerDeployable;

    }

}
