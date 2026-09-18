package com.example.etsi.vnfd.toscatype.node;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.ConnectivityType;
import com.example.etsi.vnfd.toscatype.data.NfviMaintenanceInfo;
import com.example.etsi.vnfd.toscatype.data.VirtualLinkMonitoringParameter;
import com.example.etsi.vnfd.toscatype.data.VlProfile;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.nodes.nfv.VnfVirtualLink} - SOL001 V5.4.1 clause 6.8.9.
 *
 * <p>The VnfVirtualLink node type represents the VnfVirtualLinkDesc information element as defined in ETSI GS NFV-IFA 011 [1], which describes the information about an internal VNF VL.
 *
 * <p>Represents the {@code VnfVirtualLinkDesc} information element of IFA011 V5.4.1 clause 7.1.7.2 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EtsiNodeType(EtsiTypes.VNF_VIRTUAL_LINK)
public class VnfVirtualLink extends NfvNode {

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {

        /** Specifies the protocol exposed by the VL and the flow pattern supported by the VL required: true. */
        @JsonProperty("connectivity_type")
        private ConnectivityType connectivityType;

        /** Provides human-readable information on the purpose of the VL */
        @JsonProperty("description")
        private PropertyValue<String> description;

        /** Test access facilities available on the VL */
        @JsonProperty("test_access")
        private List<String> testAccess;

        /** Defines additional data for the VL required: true. */
        @JsonProperty("vl_profile")
        private VlProfile vlProfile;

        /** Describes monitoring parameters applicable to the VL */
        @JsonProperty("monitoring_parameters")
        private Map<String, VirtualLinkMonitoringParameter> monitoringParameters;

        /** Provides information on the rules to be observed when an instance based on this VnfVirtualLink is impacted during NFVI operation and maintenance (e.g. NFVI resource upgrades). */
        @JsonProperty("nfvi_maintenance_info")
        private NfviMaintenanceInfo nfviMaintenanceInfo;

        /** Specifies the intent of the VNF designer w.r.t. the external management of the internal VL instances created from this descriptor, i.e. whether it is "allowed" or "required" that these are externally managed. If this property is absent, the value "allowed" is assumed. If the VNFD does not reference any LCM script and if the "vnfm_info" property in the VNF-specific node type derived from the tosca.nodes.nfv.VNF node type indicates that the VNF can be managed by any ETSI NFV compliant VNFM, this property shall not be present. */
        @JsonProperty("externally_managed")
        private PropertyValue<String> externallyManaged;

    }

    @JsonProperty("capabilities")
    private Capabilities capabilities;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capabilities {

        /** type tosca.capabilities.nfv.VirtualLinkable, occurrences [1, 1]. */
        @JsonProperty("virtual_linkable")
        private Map<String, Object> virtualLinkable;

    }

}
