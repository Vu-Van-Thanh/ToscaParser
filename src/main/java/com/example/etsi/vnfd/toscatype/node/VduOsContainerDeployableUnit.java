package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.LogicalNodeData;
import com.example.etsi.vnfd.toscatype.data.McioIdentificationData;
import com.example.etsi.vnfd.toscatype.data.RequestedAdditionalCapability;
import com.example.etsi.vnfd.toscatype.data.VduProfile;
import com.example.etsi.vnfd.toscatype.data.VnfcConfigurableProperties;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Vdu.OsContainerDeployableUnit} - SOL001 V5.4.1 clause 6.8.13.
 *
 * <p>The Vdu.OsContainerDeployableUnit node type describes the aggregate of OS containers of a VDU (when realized as OS containers) which is a construct supporting the description of the deployment and operational behaviour of a VNFC.
 *
 * <p>Represents the {@code Vdu} information element of IFA011 V5.4.1 clause 7.1.6.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Table 6.1-1 NOTE 1: the Vdu information element is represented as a collection of VduCp, Vdu.Compute (VM based), Vdu.OsContainerDeployableUnit and Vdu.OsContainer (OS container based), and the three Vdu.Virtual*Storage types - no single TOSCA type maps to it.
 *
 * <p><b>Additional requirements</b> (clause 6.8.13): In case a node template of type tosca.nodes.nfv.Vdu.OsContainerDeployableUnit is present in a VNFD service template, while no node template of type tosca.nodes.nfv.Vdu.OsContainer is present, at least one node template of type tosca.nodes.nfv.Mciop shall be present in the VNFD service template.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT)
public class VduOsContainerDeployableUnit extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Human readable name of the VDU required: true. */
        @JsonProperty("name")
        private PropertyValue<String> name;

        /** Human readable description of the VDU required: true. */
        @JsonProperty("description")
        private PropertyValue<String> description;

        /** Describes the Logical Node requirements */
        @JsonProperty("logical_node")
        private Map<String, LogicalNodeData> logicalNode;

        /** Describes additional capability for a particular OS container */
        @JsonProperty("requested_additional_capabilities")
        private Map<String, RequestedAdditionalCapability> requestedAdditionalCapabilities;

        /** Describes constraints on the NFVI for the VNFC instance(s) created from this VDU. This property is reserved for future use in the present document. */
        @JsonProperty("nfvi_constraints")
        private Map<String, String> nfviConstraints;

        @JsonProperty("configurable_properties")
        private VnfcConfigurableProperties configurableProperties;

        /** Defines additional instantiation data for the Vdu.OsContainerDeployableUnit node required: true. */
        @JsonProperty("vdu_profile")
        private VduProfile vduProfile;

        /** Defines the parameter names for constraints expected to be assigned to MCIOs realizing this Vdu.OsContainerDeployableUnit. The value specifies the standardized semantical context of the MCIO constraints. */
        @JsonProperty("mcio_constraint_params")
        private List<String> mcioConstraintParams;

        /** Name and type of the MCIO that realizes this Vdu.OsContainerDeployableUnit. It allows the VNFM to identify the MCIO e.g. when querying the CISM. required: true. */
        @JsonProperty("mcio_identification_data")
        private McioIdentificationData mcioIdentificationData;

        /** Indicates whether the VDU.OsContainerDeployableUnit is a template for a VNFC that is instantiated a number of times based on the instantiation level or scale level (FALSE) or it is a template describing a workload that is instantiated in every CIS-node, or in every CIS-node that fulfills certain characteristics (TRUE). required: true. */
        @JsonProperty("is_num_of_instances_cluster_based")
        private PropertyValue<Boolean> isNumOfInstancesClusterBased;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.VirtualStorage, occurrences [0, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_storage")
        private List<String> virtualStorage;

        /** capability tosca.capabilities.nfv.ContainerDeployable, occurrences [0, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("container")
        private List<String> container;

        /** capability tosca.capabilities.nfv.AssociablePaasService, occurrences [0, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("associated_paas_service")
        private List<String> associatedPaasService;

        /** capability tosca.capabilities.nfv.InstallableCertificate, occurrences [0, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("installable_certificate")
        private List<String> installableCertificate;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.VirtualBindable, occurrences [0, UNBOUNDED]. */
        @JsonProperty("virtual_binding")
        private Map<String, Object> virtualBinding;

        /** type tosca.capabilities.nfv.AssociableVdu, occurrences [1, 1]. */
        @JsonProperty("associable")
        private Map<String, Object> associable;

    }

}
