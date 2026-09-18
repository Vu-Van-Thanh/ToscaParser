package com.example.etsi.vnfd.toscatype.policy;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.NfviMaintenanceInfo;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.policies.nfv.AffinityRule} - SOL001 V5.4.1 clause 6.10.10.
 *
 * <p>The AffinityRule or AntiAffinityRule describes the affinity or anti-affinity rules applicable for the defined targets: - If there is only one node template with node type tosca.nodes.nfv.Vdu.Compute or tosca.nodes.nfv.Vdu.OsContainerDeployableUnit or tosca.nodes.nfv.VnfVirtualLink set as the targets, the AffinityRule or AntiAffinityRule applies between the virtualisation containers to be created based on a particular VDU, or between internal VLs to be created based on a particular VnfVirtualLinkDesc as described in ETSI GS NFV-IFA 011 [1]. - If there are more than one node templates with node type tosca.nodes.nfv.Vdu.Compute or tosca.nodes.nfv.Vdu.OsContainerDeployableUnit or tosca.nodes.nfv.VnfVirtualLink or tosca.nodes.nfv.Mciop set as the targets, or a group with type tosca.groups.nfv.PlacementGroup which contains more than one members set as targets, the AffinityRule or AntiAffinityRule applies between the virtualisation containers to be created based on different VDUs, or between internal VLs to be created based on different VnfVirtualLinkDesc(s) or between sets of virtualisation containers, realized by OS containers, to be created based on different MCIOPs as described in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.POLICY_AFFINITY_RULE)
public class AffinityRule extends NfvPolicy {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** scope of the rule is an NFVI_node, an NFVI_PoP, etc. required: true. */
        @JsonProperty("scope")
        private PropertyValue<String> scope;

        /** Provides information on the impact tolerance and rules to be observed when a group of instances based on the same Vdu.Compute (for VM based VDU) node is impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
        @JsonProperty("nfvi_maintenance_group_info")
        private NfviMaintenanceInfo nfviMaintenanceGroupInfo;

    }

}
