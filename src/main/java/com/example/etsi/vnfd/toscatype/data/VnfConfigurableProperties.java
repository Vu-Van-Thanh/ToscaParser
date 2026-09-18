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
 * {@code tosca.datatypes.nfv.VnfConfigurableProperties} - SOL001 V5.4.1 clause 6.2.31.
 *
 * <p>The VnfConfigurableProperties data type describes configurable properties for a given VNF. Configurable properties can be standardized as listed below (e.g. related to auto scaling, auto healing and interface configuration) or can be VNF-specific as defined by the VNF provider. The value of all VNF configurable properties listed in table 6.2.31.2-1 shall be modifiable anytime (including after instantiation of the VNF) via the Modify VNF information operation, unless stated otherwise in the description of the specific VNF configurable property.
 *
 * <p>Represents the {@code VnfConfigurableProperties} information element of IFA011 V5.4.1 clause 7.1.12 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.2.31): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfConfigurableProperties {

    /** It permits to enable (TRUE)/disable (FALSE) the auto-scaling functionality. If the property is not present, then configuring this VNF property is not supported */
    @JsonProperty("is_autoscale_enabled")
    private PropertyValue<Boolean> isAutoscaleEnabled;

    /** It permits to enable (TRUE)/disable (FALSE) the auto-healing functionality. If the property is not present, then configuring this VNF property is not supported */
    @JsonProperty("is_autoheal_enabled")
    private PropertyValue<Boolean> isAutohealEnabled;

    /** Contains information enabling access to the NFV-MANO interfaces produced by the VNFM (e.g. URIs and credentials), If the property is not present, then configuring this VNF property is not supported. */
    @JsonProperty("vnfm_interface_info")
    private List<VnfmInterfaceInfo> vnfmInterfaceInfo;

    /** Contains information to enable discovery of the authorization server protecting access to VNFM interfaces. If the property is not present, then configuring this VNF property is not supported. */
    @JsonProperty("vnfm_oauth_server_info")
    private OauthServerInfo vnfmOauthServerInfo;

    /** Contains information to enable discovery of the authorization server to validate the access tokens provided by the VNFM when the VNFM accesses the VNF interfaces, if that functionality (token introspection) is supported by the authorization server. If the property is not present, then configuring this VNF property is not supported. */
    @JsonProperty("vnf_oauth_server_info")
    private OauthServerInfo vnfOauthServerInfo;

    /** It provides VNF specific configurable properties that can be modified using the ModifyVnfInfo operation */
    @JsonProperty("additional_configurable_properties")
    private VnfAdditionalConfigurableProperties additionalConfigurableProperties;

    /** Contains information enabling the peering (connectivity) to the external management entity (e.g., URIs, credentials, and address information) managing the VNF. If the property is not present, then configuring this VNF property is not supported. */
    @JsonProperty("mgmt_entity_interface_info")
    private Map<String, String> mgmtEntityInterfaceInfo;

}
