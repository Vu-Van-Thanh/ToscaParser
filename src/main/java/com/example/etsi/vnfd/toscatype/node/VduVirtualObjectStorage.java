package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.NfviMaintenanceInfo;
import com.example.etsi.vnfd.toscatype.data.VirtualObjectStorageData;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Vdu.VirtualObjectStorage} - SOL001 V5.4.1 clause 6.8.5.
 *
 * <p>The VirtualObjectStorage node type describes the specifications of requirements related to virtual object storage resources, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code VirtualStorageDesc} information element of IFA011 V5.4.1 clause 7.1.9.4.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.5): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_VIRTUAL_OBJECT_STORAGE)
public class VduVirtualObjectStorage extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Describes the object storage characteristics. required: true. */
        @JsonProperty("virtual_object_storage_data")
        private VirtualObjectStorageData virtualObjectStorageData;

        /** Indicates whether the virtual storage descriptor shall be instantiated per VNFC instance. required: true. */
        @JsonProperty("per_vnfc_instance")
        private PropertyValue<Boolean> perVnfcInstance;

        /** Provides information on the rules to be observed when an instance based on this VirtualObjectStorage is impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
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
