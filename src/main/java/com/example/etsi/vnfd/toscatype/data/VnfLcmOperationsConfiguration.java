package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfLcmOperationsConfiguration} - SOL001 V5.4.1 clause 6.2.20.
 *
 * <p>The VnfLcmOperationsConfiguration data type represents information to configure lifecycle management operations as specified in ETSI GS NFV-IFA 007 [i.1]. Each VNF LCM operations configuration property represents a container for all attributes that affect the invocation of the corresponding VNF Lifecycle Management operation.
 *
 * <p><b>Additional requirements</b> (clause 6.2.20): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfLcmOperationsConfiguration {

    /** Configuration parameters for the InstantiateVnf operation */
    @JsonProperty("instantiate")
    private VnfInstantiateOperationConfiguration instantiate;

    /** Configuration parameters for the ScaleVnf operation */
    @JsonProperty("scale")
    private VnfScaleOperationConfiguration scale;

    /** Configuration parameters for the ScaleVnfToLevel operation */
    @JsonProperty("scale_to_level")
    private VnfScaleToLevelOperationConfiguration scaleToLevel;

    /** Configuration parameters for the changeVnfFlavourOpConfig operation */
    @JsonProperty("change_flavour")
    private VnfChangeFlavourOperationConfiguration changeFlavour;

    /** Configuration parameters for the HealVnf operation */
    @JsonProperty("heal")
    private VnfHealOperationConfiguration heal;

    /** Configuration parameters for the TerminateVnf operation */
    @JsonProperty("terminate")
    private VnfTerminateOperationConfiguration terminate;

    /** Configuration parameters for the OperateVnf operation */
    @JsonProperty("operate")
    private VnfOperateOperationConfiguration operate;

    /** Configuration parameters for the changeExtVnfConnectivityOpConfig operation */
    @JsonProperty("change_ext_connectivity")
    private VnfChangeExtConnectivityOperationConfiguration changeExtConnectivity;

    /** Configuration parameters for the ChangeCurrentVnfPackage operation */
    @JsonProperty("change_current_package")
    private VnfChangeCurrentPackageOperationConfiguration changeCurrentPackage;

    /** Configuration parameters for the CreateVnfSnapshot operation */
    @JsonProperty("create_snapshot")
    private VnfCreateSnapshotOperationConfiguration createSnapshot;

    /** Configuration parameters for the RevertToVnfSnapshot operation */
    @JsonProperty("revert_to_snapshot")
    private VnfRevertToSnapshotOperationConfiguration revertToSnapshot;

    /** Configuration parameters for the SelectVnfDeployableModules operation */
    @JsonProperty("select_vnf_deployable_modules")
    private SelectVnfDeployableModulesOperationConfiguration selectVnfDeployableModules;

}
