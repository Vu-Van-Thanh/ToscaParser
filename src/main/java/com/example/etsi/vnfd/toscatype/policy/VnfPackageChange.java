package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.VnfPackageChangeComponentMapping;
import com.example.etsi.vnfd.toscatype.data.VnfPackageChangeSelector;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.VnfPackageChange} - SOL001 V5.4.1 clause 6.10.15.
 *
 * <p>The VnfPackageChange type is a policy type specifying the processes and rules to be used for performing the resource related tasks, to change VNF instance to a different VNF Package (destination package) as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.10.15): The VnfPackageChange specific type policy shall have exactly one trigger with an event and an action. The event value shall be set to change_current_package_notification notification of Vnflcm interface. The target shall be set to the node template to which the policy applies, i.e. to the node template of the VNF specific type present in the topology template that represents a particular deployment flavour. The action value shall be set to either change_current_package operation on the Vnflcm (in case the same LCM script or no LCM script with the same set of "additionalParams" or no "additionalParams" is suitable for all change paths) or one of the VNF-specific operations on the ChangeCurrentVnfPackage interface (in case different change paths require different LCM scripts or no LCM script potentially with different sets of "additionalParams" or no "additionalParams"). The policy shall be applied when the actual values of the descriptor_id and flavour_id of the source VNF type and the descriptor_id of the destination VNF type match the source_descriptor_id, destination_descriptor_id and source_flavour_id properties, respectively, of the selector in the policy. VNF-specific coordination actions shall be declared with their parameters in data types derived from tosca.datatypes.nfv.VnfLcmOpCoord (see clause 6.2.67), and an interface_name property of the tosca.policies.nfv.SupportedVnfInterface set to "vnf_lcm_coordination" shall be specified in the related deployment flavour to signal that this interface is exposed by the VNF. NOTE: During and after the VNF Package change all information related to any other deployment flavours in the source VNFD than the source flavour is no longer applicable.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_VNF_PACKAGE_CHANGE)
public class VnfPackageChange extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Information to identify the source and destination VNFD for the change, and the related deployment flavours. required: true. */
        @JsonProperty("selector")
        private List<VnfPackageChangeSelector> selector;

        /** Specifies the type of modification resulting from transitioning from srcVnfdId to dstVnfdId. The possible values are UP indicating that the destination VNF version is newer than the source version, DOWN indicating that the destination VNF version is older than the source version. required: true. */
        @JsonProperty("modification_qualifier")
        private PropertyValue<String> modificationQualifier;

        /** Additional information to qualify further the change between the two versions. */
        @JsonProperty("additional_modification_description")
        private PropertyValue<String> additionalModificationDescription;

        /** Mapping information related to identifiers of components in source VNFD and destination VNFD that concern to the change process. */
        @JsonProperty("component_mappings")
        private List<VnfPackageChangeComponentMapping> componentMappings;

        /** Identifies the deployment flavour in the destination VNF package for which this change applies. The flavour ID is defined in the destination VNF package. required: true. */
        @JsonProperty("destination_flavour_id")
        private PropertyValue<String> destinationFlavourId;

        /** List of applicable supported LCM coordination action names (action_name) specified in this VNFD as a TOSCA policy of a type derived from tosca.policies.nfv.LcmCoordinationAction. */
        @JsonProperty("actions")
        private List<String> actions;

        /** List of names of coordination actions not specified within this VNFD as a TOSCA policy of a type derived from tosca.policies.nfv.LcmCoordinationAction. */
        @JsonProperty("referenced_coordination_actions")
        private List<String> referencedCoordinationActions;

        /** Supported upgrade type when change the current VNF Package on which a VNF instance is based. */
        @JsonProperty("upgrade_type")
        private PropertyValue<String> upgradeType;

    }

}
