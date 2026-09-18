package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfProfile} - SOL001 V5.4.1 clause 9.2.8.
 *
 * <p>The VnfProfile data type describes a profile for instantiating VNFs of a particular NS DF according to a specific VNFD and VNF DF as defined in ETSI GS NFV-IFA 014 [2].
 *
 * <p><b>Additional requirements</b> (clause 9.2.8): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfProfile {

    /** Identifier of the instantiation level of the VNF DF to be used for instantiation. If not present, the default instantiation level as declared in the VNFD shall be used. */
    @JsonProperty("instantiation_level")
    private PropertyValue<String> instantiationLevel;

    /** For each scaling aspect of the current VNF deployment flavour, it specifies the scale level of VNF constituents (e.g., VDU level) to be instantiated. If the property is present it shall contain all scaling aspects */
    @JsonProperty("target_vnf_scale_level_info")
    private Map<String, ScaleInfo> targetVnfScaleLevelInfo;

    /** Minimum number of instances of the VNF based on this VNFD that is permitted to exist for this VnfProfile. required: true. */
    @JsonProperty("min_number_of_instances")
    private PropertyValue<Integer> minNumberOfInstances;

    /** Maximum number of instances of the VNF based on this VNFD that is permitted to exist for this VnfProfile. required: true. */
    @JsonProperty("max_number_of_instances")
    private PropertyValue<Integer> maxNumberOfInstances;

    /** Specifies the service availability level for the VNF instance created from this profile. */
    @JsonProperty("service_availability_level")
    private PropertyValue<Integer> serviceAvailabilityLevel;

    /** Identifies versions of descriptors of other constituents in the NSD upon which the VNF depends. The dependencies may be described for the VNFD referenced with descriptor_id in the VNF node where this profile is defined and for VNFDs with the same ext_invariant_id. */
    @JsonProperty("version_dependency")
    private List<VersionDependency> versionDependency;

    /** Indicates the selected deployable module(s) for the VNF instances created from this profile. */
    @JsonProperty("selected_deployable_modules")
    private SelectedDeployableModules selectedDeployableModules;

    /** Indicates whether the capacity related attributes specified in the VNFD as being configurable can be overridden by new values at run time. */
    @JsonProperty("vdu_capacity-overriding-allowed")
    private Map<String, Boolean> vduCapacityOverridingAllowed;

}
