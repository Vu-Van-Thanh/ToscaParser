package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.NfviMaintenanceInfo;
import com.example.etsi.vnfd.toscatype.data.VirtualFileStorageData;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.Vdu.VirtualFileStorage} - SOL001 V5.4.1 clause 6.8.6.
 *
 * <p>The VirtualFileStorage node type describes the specifications of requirements related to virtual file storage resources, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p>Represents the {@code VirtualStorageDesc} information element of IFA011 V5.4.1 clause 7.1.9.4.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.8.6): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VDU_VIRTUAL_FILE_STORAGE)
public class VduVirtualFileStorage extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Describes the file storage characteristics. required: true. */
        @JsonProperty("virtual_file_storage_data")
        private VirtualFileStorageData virtualFileStorageData;

        /** Indicates whether the virtual storage descriptor shall be instantiated per VNFC instance. required: true. */
        @JsonProperty("per_vnfc_instance")
        private PropertyValue<Boolean> perVnfcInstance;

        /** Provides information on the rules to be observed when an instance based on this VirtualFileStorage is impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
        @JsonProperty("nfvi_maintenance_info")
        private NfviMaintenanceInfo nfviMaintenanceInfo;

    }

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirements {

        /** capability tosca.capabilities.nfv.VirtualLinkable, occurrences [1, 1]. A list: TOSCA allows the same requirement name more than once. */
        @JsonProperty("virtual_link")
        private List<String> virtualLink;

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
