package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.NfviMaintenanceInfo;
import com.example.etsi.vnfd.toscatype.data.VirtualBlockStorageData;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Vdu.VirtualBlockStorage} - SOL001 V5.4.1 clause 6.8.4.
 *
 * <p>The VirtualBlockStorage node type describes the specifications of requirements related to virtual block storage resources, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code VirtualStorageDesc} information element of IFA011 V5.4.1 clause 7.1.9.4.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.4): Node templates of type tosca.nodes.nfv.Vdu.VirtualBlockStorage may contain an artifact definition of type tosca.artifacts.nfv.SwImage. There shall be a maximum number of one such artifact definition in a tosca.nodes.nfv.Vdu.VirtualBlockStorage node template when attached to the node with type tosca.nodes.nfv.Vdu.Compute, otherwise, such artifact definition shall not be present. The node template name of type tosca.nodes.nfv.Vdu.VirtualBlockStorage fulfils the purpose of the "id" attribute of the SwImageDesc information element in ETSI GS NFV-IFA 011 [1] and hence it will be used in APIs to identify the software image id from the VNFD descriptor. See example in clause 6.8.3.8.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_VIRTUAL_BLOCK_STORAGE)
public class VduVirtualBlockStorage extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Describes the block storage characteristics. required: true. */
        @JsonProperty("virtual_block_storage_data")
        private VirtualBlockStorageData virtualBlockStorageData;

        /** Indicates whether the virtual storage descriptor shall be instantiated per VNFC instance. required: true. */
        @JsonProperty("per_vnfc_instance")
        private PropertyValue<Boolean> perVnfcInstance;

        /** Provides information on the rules to be observed when an instance based on this VirtualBlockStorage is impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
        @JsonProperty("nfvi_maintenance_info")
        private NfviMaintenanceInfo nfviMaintenanceInfo;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.VirtualStorage, occurrences [1, 1]. */
        @JsonProperty("virtual_storage")
        private Map<String, Object> virtualStorage;

    }

}
