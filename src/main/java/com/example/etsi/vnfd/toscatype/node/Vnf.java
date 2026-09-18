package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.VnfConfigurableProperties;
import com.example.etsi.vnfd.toscatype.data.VnfInfoModifiableAttributes;
import com.example.etsi.vnfd.toscatype.data.VnfLcmOperationsConfiguration;
import com.example.etsi.vnfd.toscatype.data.VnfMonitoringParameter;
import com.example.etsi.vnfd.toscatype.data.VnfProfile;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VNF} - SOL001 V5.4.1 clause 6.8.1.
 *
 * <p>The VNF node type is the generic abstract type from which all VNF specific node types shall be derived to form, together with other node types, the TOSCA service template(s) representing the VNFD information element as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code Vnfd} information element of IFA011 V5.4.1 clause 7.1.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.1): For a given VNFD, a new VNF node type shall be defined following the below requirements: a) The node type shall be derived from: tosca.nodes.nfv.VNF. b) The following properties listed in tosca.nodes.nfv.VNF where the "required:" field is set to "true" shall be included with their values indicated as constraints and as default values or assigned as final fixed values if only one value is permitted (see clause 6.8.1.9 for an example): a. descriptor_id b. descriptor_version c. provider d. product_name e. software_version f. vnfm_info g. flavour_id NOTE 1: Indicating their values as default or assigning them a fixed value allows not to include them in property assignments in node templates, e.g. in the NSD. NOTE 2: Assignment of a fixed value to the flavour_id property is not applicable if multiple deployment flavours exist. c) An empty string shall be indicated as the default value of the flavour_description property, without providing constraints. d) The capabilities, requirements, interfaces of tosca.nodes.nfv.VNF shall be preserved. e) Depending on the number of external connection points of the VNF that need to connect to NS virtual links, additional requirements for VirtualLinkable capability shall be defined with the occurrences set to [ 0, 1 ]. In this case, it is the VNFD author's choice to use the requirement for VirtualLinkable capability defined in the tosca.nodes.nfv.VNF node type or use only the additional requirements defined in the derived VNF specific node type. In the latter case, the virtual_link requirement should be included in the node type definition with occurrences [ 0, 0 ]. If the external connection point exposes a VipCp, a new requirement for VirtualLinkableCapability using the VipVirtualLinksTo relationship shall be defined for this connection point. f) The rule for naming this node type in the service template should be: - provider.product_name.software_version.descriptor_version, by concatenating the values of the corresponding properties of the created VNF node type. NOTE 3: If the software_version value or descriptor_version value contains a dot (i.e. "."), this character should be replaced with an underscore (i.e. "_"). g) If the VNF supports VNF indicators, the VNF node type definition shall include an interface definition of a VNF specific interface type indicating the mapping of notification outputs to the VNF node attributes and, optionally, tosca.policies.nfv.VnfIndicator policies that may invoke auto-scale or auto-heal operations. For each of the VNF indicators, the name of the notification output shall be the same as the name of the corresponding VNF attribute. NOTE 4: The notifications keyname in TOSCA interface is defined in TOSCA-Simple-Profile-YAML-v1.3 [20]. h) If "additionalParams" are expected in the Change current VNF Package request on the API (ETSI GS NFV-SOL 003 [25] or ETSI GS NFV-SOL 002 [22]), then they shall be defined as "additional_parameters" inputs of the change_current_package operation on the Vnflcm interface (in case the same LCM script with the same set of "additionalParams" is suitable for all change paths) or the VNF-specific operations on the ChangeCurrentVnfPackage interface (in case different change paths require different LCM scripts potentially with different sets of "additionalParams"). i) If the VNFD supports external invariancy the VNF node type definition shall include the ext_invariant_id property with its value indicated as constraint. VNF Providers shall use the following types to derive the VNF specific modifiable attributes and additional configurable properties: - tosca.datatypes.nfv.VnfInfoModifiableAttributesExtensions. - tosca.datatypes.nfv.VnfInfoModifiableAttributesMetadata. - tosca.datatypes.nfv.VnfAdditionalConfigurableProperties. - tosca.datatypes.nfv.VnfInfoModifiableAttributes. - tosca.datatypes.nfv.VnfConfigurableProperties. See illustrative examples in clauses 6.8.1.9, 6.2.35.4 and 6.2.31.4. In the derived VNF node type, the modifiable_attributes and configurable_properties (VNF-specific extension of the tosca.datatypes.nfv.VnfInfoModifiableAttributes and tosca.datatypes.nfv.VnfConfigurableProperties, respectively, by extending the above listed types) describe the name and type of the modifiable attributes (extensions and metadata) and configurable properties (vnfConfigurableProperties). The modifiable_attributes and configurable_properties information provided in the node type is sufficient for the client of the VNF LCM API for providing values to these properties. A value provided via the VNF LCM API to such a property overrides the value (if any) assigned in the node template or defined as default value in the node type definition. Node templates of the VNF specific node type shall not include the vnf_profile property when they are part of a VNFD service template. For a given NSD, when describing a referenced VNFD as a node templates, the vnf_profile property shall be included with a valid value. For a given NSD, when describing a referenced VNFD as a node templates, the monitoring_parameters property shall not be included.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VNF)
public class Vnf extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Identifier of this VNFD information element. This attribute shall be globally unique required: true. */
        @JsonProperty("descriptor_id")
        private PropertyValue<String> descriptorId;

        /** Identifies the VNFD in a version independent manner. This property is invariant across versions of the VNFD that fulfil certain conditions related to the external connectivity and management of the VNF. When used in a VNF node template in an NSD it allows for VNF instances during NS LCM the use of a VNFD different from the one referenced by the descriptor_id property, provided they have the same ext_invariant_id. This attribute shall be globally unique. */
        @JsonProperty("ext_invariant_id")
        private PropertyValue<String> extInvariantId;

        /** Identifies the version of the VNFD required: true. */
        @JsonProperty("descriptor_version")
        private PropertyValue<String> descriptorVersion;

        /** Provider of the VNF and of the VNFD required: true. */
        @JsonProperty("provider")
        private PropertyValue<String> provider;

        /** Human readable name for the VNF Product required: true. */
        @JsonProperty("product_name")
        private PropertyValue<String> productName;

        /** Software version of the VNF required: true. */
        @JsonProperty("software_version")
        private PropertyValue<String> softwareVersion;

        /** Human readable name for the VNF Product */
        @JsonProperty("product_info_name")
        private PropertyValue<String> productInfoName;

        /** Human readable description of the VNF Product */
        @JsonProperty("product_info_description")
        private PropertyValue<String> productInfoDescription;

        /** Identifies VNFM(s) compatible with the VNF required: true. */
        @JsonProperty("vnfm_info")
        private List<String> vnfmInfo;

        /** Information about localization languages of the VNF */
        @JsonProperty("localization_languages")
        private List<String> localizationLanguages;

        /** Default localization language that is instantiated if no information about selected localization language is available */
        @JsonProperty("default_localization_language")
        private PropertyValue<String> defaultLocalizationLanguage;

        /** Describes the configurable properties of the VNF */
        @JsonProperty("configurable_properties")
        private VnfConfigurableProperties configurableProperties;

        /** Describes the modifiable attributes of the VNF */
        @JsonProperty("modifiable_attributes")
        private VnfInfoModifiableAttributes modifiableAttributes;

        /** Describes the configuration parameters for the VNF LCM operations */
        @JsonProperty("lcm_operations_configuration")
        private VnfLcmOperationsConfiguration lcmOperationsConfiguration;

        /** Describes monitoring parameters applicable to the VNF. */
        @JsonProperty("monitoring_parameters")
        private Map<String, VnfMonitoringParameter> monitoringParameters;

        /** Identifier of the Deployment Flavour within the VNFD required: true. */
        @JsonProperty("flavour_id")
        private PropertyValue<String> flavourId;

        /** Human readable description of the DF required: true. */
        @JsonProperty("flavour_description")
        private PropertyValue<String> flavourDescription;

        /** Describes a profile for instantiating VNFs of a particular NS DF according to a specific VNFD and VNF DF */
        @JsonProperty("vnf_profile")
        private VnfProfile vnfProfile;

        /** Indicates in which VNF LCM operations in this DF the VNF supports the change of the selected deployable modules. When CHANGE_VNF_DF or CHANGE_CURRENT_VNF_PACKAGE is indicated, it refers to change of DF or VNF package, respectively, to the one where the attribute is indicated. */
        @JsonProperty("change_selected_deployable_modules_op")
        private List<String> changeSelectedDeployableModulesOp;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [0, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_link")
        private List<String> virtualLink;

        /** capability tosca.capabilities.nfv.AssociablePaasService, occurrences [0, UNBOUNDED]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("associated_paas_service")
        private List<String> associatedPaasService;

    }

}
