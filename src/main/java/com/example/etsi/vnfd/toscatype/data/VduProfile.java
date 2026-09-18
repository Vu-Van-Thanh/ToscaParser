package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VduProfile} - SOL001 V5.4.1 clause 6.2.12.
 *
 * <p>The VduProfile data type describes additional instantiation data for a given Vdu.Compute (for VM based VDU) or Vdu.OsContainerDeployableUnit (for Oscontainer based VDU) used in a specific deployment flavour.
 *
 * <p><b>Additional requirements</b> (clause 6.2.12): The properties of the vdu_profile indicate the maximum and minimum number of Vdu.Compute instances that are permitted to exist, created from a given Vdu.Compute node template during its lifecycle, as well as: - If the 'per_vnfc_instance' property of the VirtualBlockStorage, VirtualObjectStorage or VirtualFileStorage nodes connected to the Vdu.Compute node is set to 'true' or absent: the maximum and minimum number of instances of each VirtualBlockStorage, VirtualObjectStorage and VirtualFileStorage nodes connected to the Vdu.Compute via one particular occurrence of the virtual_storage requirement. - If 'per_vnfc_instance' property is set to 'false' only one instance of the storage node shall exist. - The maximum and minimum number instances of each VduCp node connected to the Vdu.Compute via one particular occurrence of the virtual_binding capability.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VduProfile {

    /** Minimum number of instances of the VNFC based on this Vdu.Compute (for VM based VDU) or Vdu.OsContainerDeployableUnit node (for Oscontainer based VDU) that is permitted to exist for a particular VNF deployment flavour. required: true. */
    @JsonProperty("min_number_of_instances")
    private PropertyValue<Integer> minNumberOfInstances;

    /** Maximum number of instances of the VNFC based on this Vdu.Compute (for VM based VDU) or Vdu.OsContainerDeployableUnit node (for Oscontainer based VDU) that is permitted to exist for a particular VNF deployment flavour. required: true. */
    @JsonProperty("max_number_of_instances")
    private PropertyValue<Integer> maxNumberOfInstances;

    /** Provides information on the impact tolerance and rules to be observed when instance(s) of the Vdu.Compute (for VM based VDU) are impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
    @JsonProperty("nfvi_maintenance_info")
    private NfviMaintenanceInfo nfviMaintenanceInfo;

    /** Indicates in which VNF LCM operations the change of values in capacity related attributes is supported for VNFCs created from this VDU. When CHANGE_VNF_DF or CHANGE_CURRENT_VNF_PACKAGE is indicated, it refers to change of DF or VNF package, respectively, to the one where the attribute is indicated. */
    @JsonProperty("modify_capacity_attributes_op")
    private List<String> modifyCapacityAttributesOp;

}
