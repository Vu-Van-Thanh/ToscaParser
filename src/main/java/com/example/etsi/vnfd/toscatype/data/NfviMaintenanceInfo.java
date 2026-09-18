package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.NfviMaintenanceInfo} - SOL001 V5.4.1 clause 6.2.74.
 *
 * <p>The NfviMaintenanceInfo data type provides information related to the constraints and rules applicable to virtualised resources and their groups impacted due to NFVI maintenance operations, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 6.2.74): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NfviMaintenanceInfo {

    /** Specifies the minimum notification lead time requested for upcoming impact of the virtualised resource or their group (i.e. between the notification and the action causing the impact). required: true. */
    @JsonProperty("impact_notification_lead_time")
    private PropertyValue<Quantity> impactNotificationLeadTime;

    /** Indicates whether it is requested that at the time of the notification of an upcoming change that is expected to have an impact on the VNF, virtualised resource(s) of the same characteristics as the impacted ones is/are provided to compensate for the impact (TRUE) or not (FALSE). required: true. */
    @JsonProperty("is_impact_mitigation_requested")
    private PropertyValue<Boolean> isImpactMitigationRequested;

    /** Specifies the allowed migration types in the order of preference in case of an impact starting with the most preferred type. It is applicable to the Vdu.Compute node and to the VirtualBlockStorage, VirtualObjectStorage and VirtualFileStorage nodes. */
    @JsonProperty("supported_migration_type")
    private List<String> supportedMigrationType;

    /** Specifies the maximum interruption time that can go undetected at the VNF level and therefore which will not trigger VNF-internal recovery during live migration. It is applicable to the Vdu.Compute node and to the VirtualBlockStorage, VirtualObjectStorage and VirtualFileStorage nodes. */
    @JsonProperty("max_undetectable_interruption_time")
    private PropertyValue<Quantity> maxUndetectableInterruptionTime;

    /** Specifies the time required by the group to recover from an impact, thus, the minimum time requested between consecutive impacts of the group.. */
    @JsonProperty("min_recovery_time_between_impacts")
    private PropertyValue<Quantity> minRecoveryTimeBetweenImpacts;

    /** Specifies for different group sizes the maximum number of instances that can be impacted simultaneously within the group of virtualised resources without losing functionality. */
    @JsonProperty("max_number_of_impacted_instances")
    private List<MaxNumberOfImpactedInstances> maxNumberOfImpactedInstances;

    /** Specifies for different group sizes the minimum number of instances which need to be preserved simultaneously within the group of virtualised resources. */
    @JsonProperty("min_number_of_preserved_instances")
    private List<MinNumberOfPreservedInstances> minNumberOfPreservedInstances;

}
