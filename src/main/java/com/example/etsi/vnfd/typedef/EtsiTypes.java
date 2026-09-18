package com.example.etsi.vnfd.typedef;

/**
 * TOSCA type names defined by ETSI GS NFV-SOL 001 V5.4.1, each annotated with the clause that
 * defines it.
 *
 * <p>Collected in one place so a type name appears as a literal string exactly once. Clause numbers
 * come from the specification body, which does not always agree with informal sources: the
 * connection point type is defined in clause 6.8.8, not 6.8.5 - that number belongs to
 * Vdu.VirtualObjectStorage.
 *
 * <p>Only the CNF flow is mapped by this library. VM-flow types are listed where they are needed to
 * recognise and skip them, or to check a rule that mentions them.
 */
public final class EtsiTypes {

    private EtsiTypes() {
    }

    /** SOL001 V5.4.1 clause 6.8.1. Represents the VNFD, IFA011 clause 7.1.2. */
    public static final String VNF = "tosca.nodes.nfv.VNF";

    /** SOL001 V5.4.1 clause 6.8.2. */
    public static final String VNF_EXT_CP = "tosca.nodes.nfv.VnfExtCp";

    /** SOL001 V5.4.1 clause 6.8.3. VM flow: out of scope, recognised so it can be skipped. */
    public static final String VDU_COMPUTE = "tosca.nodes.nfv.Vdu.Compute";

    /** SOL001 V5.4.1 clause 6.8.4. */
    public static final String VDU_VIRTUAL_BLOCK_STORAGE = "tosca.nodes.nfv.Vdu.VirtualBlockStorage";

    /** SOL001 V5.4.1 clause 6.8.5. */
    public static final String VDU_VIRTUAL_OBJECT_STORAGE = "tosca.nodes.nfv.Vdu.VirtualObjectStorage";

    /** SOL001 V5.4.1 clause 6.8.6. */
    public static final String VDU_VIRTUAL_FILE_STORAGE = "tosca.nodes.nfv.Vdu.VirtualFileStorage";

    /** SOL001 V5.4.1 clause 9.8.1, in the common type definitions. Parent of every CP type. */
    public static final String CP = "tosca.nodes.nfv.Cp";

    /** SOL001 V5.4.1 clause 6.8.8. Represents VduCpd, IFA011 clause 7.1.6.4. */
    public static final String VDU_CP = "tosca.nodes.nfv.VduCp";

    /** SOL001 V5.4.1 clause 6.8.9. */
    public static final String VNF_VIRTUAL_LINK = "tosca.nodes.nfv.VnfVirtualLink";

    /** SOL001 V5.4.1 clause 6.8.10. */
    public static final String VIP_CP = "tosca.nodes.nfv.VipCp";

    /** SOL001 V5.4.1 clause 6.8.11. */
    public static final String VDU_SUB_CP = "tosca.nodes.nfv.VduSubCp";

    /** SOL001 V5.4.1 clause 6.8.12. Represents OsContainerDesc, IFA011 clause 7.1.6.13. */
    public static final String VDU_OS_CONTAINER = "tosca.nodes.nfv.Vdu.OsContainer";

    /** SOL001 V5.4.1 clause 6.8.13. Represents the Vdu information element, IFA011 clause 7.1.6.2. */
    public static final String VDU_OS_CONTAINER_DEPLOYABLE_UNIT =
            "tosca.nodes.nfv.Vdu.OsContainerDeployableUnit";

    /**
     * SOL001 V5.4.1 clause 6.8.14. Clause 6.8.14.1 states this node type "does not correspond to an
     * information element defined in ETSI GS NFV-IFA 011" but is "capable of being profiled by the
     * properties of the MciopProfile information element".
     */
    public static final String MCIOP = "tosca.nodes.nfv.Mciop";

    /** SOL001 V5.4.1 clause 6.8.15. */
    public static final String VIRTUAL_CP = "tosca.nodes.nfv.VirtualCp";

    /** SOL001 V5.4.1 clause 6.8.16. */
    public static final String DEPLOYABLE_MODULE = "tosca.nodes.nfv.DeployableModule";

    /** SOL001 V5.4.1 clause 6.8.17. */
    public static final String PAAS_SERVICE_REQUEST = "tosca.nodes.nfv.PaasServiceRequest";

    /** SOL001 V5.4.1 clause 6.8.18. */
    public static final String PAAS_SERVICE_PROFILE = "tosca.nodes.nfv.PaasServiceProfile";

    /** SOL001 V5.4.1 clause 6.8.19. */
    public static final String CERTIFICATE = "tosca.nodes.nfv.Certificate";

    /**
     * SOL001 V5.4.1 clause 6.8.20. The VM-side counterpart of the Mciop node, targeting the VIM.
     *
     * <p>The clause heading and description name this type
     * tosca.nodes.nfv.VirtualisedResourceDescriptor, while the clause 6.8.20.6 definition body,
     * Table 6.1-1 and the official type definitions file all write
     * ...VirtualisedResourceDescriptorProfile. The type definitions file is what a package actually
     * imports, so that name is used here.
     */
    public static final String VIRTUALISED_RESOURCE_DESCRIPTOR_PROFILE =
            "tosca.nodes.nfv.VirtualisedResourceDescriptorProfile";

    /** SOL001 V5.4.1 clause 6.3.1. Represents SwImageDesc, IFA011 clause 7.1.6.5. */
    public static final String ARTIFACT_SW_IMAGE = "tosca.artifacts.nfv.SwImage";

    /** SOL001 V5.4.1 clause 6.3.3. The MCIOP itself, per SOL018 V5.4.1 clause 5.2. */
    public static final String ARTIFACT_HELM_CHART = "tosca.artifacts.nfv.HelmChart";

    /** SOL001 V5.4.1 clause 6.3.4. Carries a language property of bash or python. */
    public static final String ARTIFACT_HELM_PARAM_MAPPING_SCRIPT =
            "tosca.artifacts.nfv.HelmParamMappingScript";

    /** SOL001 V5.4.1 clause 6.3.5. Valid only alongside a HelmParamMappingScript. */
    public static final String ARTIFACT_HELM_PARAM_MAPPING_RULE =
            "tosca.artifacts.nfv.HelmParamMappingRule";

    /** SOL001 V5.4.1 clause 6.3.7. The generic counterpart, carrying script_dsl. */
    public static final String ARTIFACT_LCM_OP_PARAM_MAPPING_SCRIPT =
            "tosca.artifacts.nfv.LcmOpParameterMappingScript";

    /** SOL001 V5.4.1 clause 6.10.1. */
    public static final String POLICY_INSTANTIATION_LEVELS = "tosca.policies.nfv.InstantiationLevels";

    /** SOL001 V5.4.1 clause 6.10.2. Targets both Vdu.Compute and Vdu.OsContainerDeployableUnit. */
    public static final String POLICY_VDU_INSTANTIATION_LEVELS =
            "tosca.policies.nfv.VduInstantiationLevels";

    /** SOL001 V5.4.1 clause 6.10.3. */
    public static final String POLICY_VL_INSTANTIATION_LEVELS =
            "tosca.policies.nfv.VirtualLinkInstantiationLevels";

    /** SOL001 V5.4.1 clause 6.10.5. */
    public static final String POLICY_SCALING_ASPECTS = "tosca.policies.nfv.ScalingAspects";

    /** SOL001 V5.4.1 clause 6.10.6. */
    public static final String POLICY_VDU_SCALING_ASPECT_DELTAS =
            "tosca.policies.nfv.VduScalingAspectDeltas";

    /** SOL001 V5.4.1 clause 6.10.8. */
    public static final String POLICY_VDU_INITIAL_DELTA = "tosca.policies.nfv.VduInitialDelta";

    /** SOL001 V5.4.1 clause 6.10.10, which defines AffinityRule and AntiAffinityRule together. */
    public static final String POLICY_AFFINITY_RULE = "tosca.policies.nfv.AffinityRule";

    /** SOL001 V5.4.1 clause 6.10.10. */
    public static final String POLICY_ANTI_AFFINITY_RULE = "tosca.policies.nfv.AntiAffinityRule";

    /** SOL001 V5.4.1 clause 6.10.13. */
    public static final String POLICY_SECURITY_GROUP_RULE = "tosca.policies.nfv.SecurityGroupRule";

    /** SOL001 V5.4.1 clause 6.10.15. */
    public static final String POLICY_VNF_PACKAGE_CHANGE = "tosca.policies.nfv.VnfPackageChange";

    /** SOL001 V5.4.1 clause 6.9.1. Members may include Mciop and Vdu.OsContainerDeployableUnit. */
    public static final String GROUP_PLACEMENT = "tosca.groups.nfv.PlacementGroup";

    /** SOL001 V5.4.1 clause 6.7.1. */
    public static final String INTERFACE_VNFLCM = "tosca.interfaces.nfv.Vnflcm";

    /** SOL001 V5.4.1 clause 6.7.2. */
    public static final String INTERFACE_VNF_INDICATOR = "tosca.interfaces.nfv.VnfIndicator";

    /** SOL001 V5.4.1 clause 6.7.3. */
    public static final String INTERFACE_CHANGE_CURRENT_VNF_PACKAGE =
            "tosca.interfaces.nfv.ChangeCurrentVnfPackage";

    /** SOL001 V5.4.1 clause 6.6.6. Models MciopProfile.associatedVdu. */
    public static final String RELATIONSHIP_MCIOP_ASSOCIATES =
            "tosca.relationships.nfv.MciopAssociates";

    /** SOL001 V5.4.1 Table 6.8.13.4-1. */
    public static final String CAPABILITY_CONTAINER_DEPLOYABLE =
            "tosca.capabilities.nfv.ContainerDeployable";

    /** SOL001 V5.4.1 Table 6.8.14.4-1. */
    public static final String CAPABILITY_ASSOCIABLE_VDU = "tosca.capabilities.nfv.AssociableVdu";

    /** SOL001 V5.4.1 clause 6.2.75. Name plus type of Deployment, StatefulSet or DaemonSet. */
    public static final String DATATYPE_MCIO_IDENTIFICATION_DATA =
            "tosca.datatypes.nfv.McioIdentificationData";

    /** Represents VduProfile, IFA011 clause 7.1.8.3. Declared on both VDU node types. */
    public static final String DATATYPE_VDU_PROFILE = "tosca.datatypes.nfv.VduProfile";

    /** Represents InstantiationLevel, IFA011 clause 7.1.8.7. */
    public static final String DATATYPE_INSTANTIATION_LEVEL = "tosca.datatypes.nfv.InstantiationLevel";

    /** Represents VduLevel, IFA011 clause 7.1.8.9. */
    public static final String DATATYPE_VDU_LEVEL = "tosca.datatypes.nfv.VduLevel";

    public static final String PROP_DESCRIPTOR_ID = "descriptor_id";
    public static final String PROP_DESCRIPTOR_VERSION = "descriptor_version";
    public static final String PROP_PROVIDER = "provider";
    public static final String PROP_PRODUCT_NAME = "product_name";
    public static final String PROP_SOFTWARE_VERSION = "software_version";
    public static final String PROP_PRODUCT_INFO_NAME = "product_info_name";
    public static final String PROP_PRODUCT_INFO_DESCRIPTION = "product_info_description";
    public static final String PROP_EXT_INVARIANT_ID = "ext_invariant_id";
    public static final String PROP_VNFM_INFO = "vnfm_info";
    public static final String PROP_LOCALIZATION_LANGUAGES = "localization_languages";
    public static final String PROP_DEFAULT_LOCALIZATION_LANGUAGE = "default_localization_language";
    public static final String PROP_CONFIGURABLE_PROPERTIES = "configurable_properties";
    public static final String PROP_MODIFIABLE_ATTRIBUTES = "modifiable_attributes";
    public static final String PROP_LCM_OPERATIONS_CONFIGURATION = "lcm_operations_configuration";
    public static final String PROP_FLAVOUR_ID = "flavour_id";
    public static final String PROP_FLAVOUR_DESCRIPTION = "flavour_description";
    public static final String PROP_VDU_PROFILE = "vdu_profile";
    public static final String PROP_MCIO_IDENTIFICATION_DATA = "mcio_identification_data";
    public static final String PROP_MCIO_CONSTRAINT_PARAMS = "mcio_constraint_params";
    public static final String PROP_IS_NUM_OF_INSTANCES_CLUSTER_BASED =
            "is_num_of_instances_cluster_based";
    public static final String PROP_NAME = "name";
    public static final String PROP_DESCRIPTION = "description";

    /** SOL001 V5.4.1 Table 6.8.13.4-1, occurrences [0, UNBOUNDED]. */
    public static final String REQ_CONTAINER = "container";

    /** SOL001 V5.4.1 Table 6.8.14.4-1, occurrences [1, UNBOUNDED]. */
    public static final String REQ_ASSOCIATED_VDU = "associatedVdu";

    public static final String REQ_VIRTUAL_BINDING = "virtual_binding";
    public static final String REQ_VIRTUAL_LINK = "virtual_link";
    public static final String REQ_VIRTUAL_STORAGE = "virtual_storage";
    public static final String REQ_DEPENDENCY = "dependency";
}
