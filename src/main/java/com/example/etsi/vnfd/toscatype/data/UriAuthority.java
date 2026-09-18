package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.UriAuthority} - SOL001 V5.4.1 clause 6.2.51.
 *
 * <p>The UriAuthority data type corresponds to the authority component of a URI as specified in IETF RFC 3986 [8].
 *
 * <p><b>Additional requirements</b> (clause 6.2.51): When this datatype is used to provide information for accessing APIs defined in ETSI GS NFV-SOL 002 [22], the host property and port properties may be included and the user_info property shall not be included. If the host property is included and the value is a registered name, it is assumed that means are in place to resolve the host name to the correct IP address. If the host property is not included, it is assumed that the VNFM will use the IP address associated to one of the connection point instances created from the VnfExpCp and VduCp node types declared as a target of the SupportedVnfInterface policy. NOTE: This means that if multiple CP instances exist that were created from a particular VnfExtCp or VduCp node template, the VNFM may use any of them to attempt accessing the interface. If no reply is received because the selected CP instance if out of service or is not reachable, the VNFM is expected to try reaching the interface through another CP instance.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UriAuthority {

    /** user_info field of the authority component of a URI */
    @JsonProperty("user_info")
    private PropertyValue<String> userInfo;

    /** host field of the authority component of a URI */
    @JsonProperty("host")
    private PropertyValue<String> host;

    /** port field of the authority component of a URI */
    @JsonProperty("port")
    private PropertyValue<String> port;

}
