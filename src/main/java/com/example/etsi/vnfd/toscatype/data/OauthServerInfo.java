package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.OauthServerInfo} - SOL001 V5.4.1 clause 6.2.55.
 *
 * <p>The OauthServerInfo data type describes information to enable discovery of the authorization server. This data type definition is reserved for future use in the present document.
 *
 * <p><b>Additional requirements</b> (clause 6.2.55): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OauthServerInfo {

}
