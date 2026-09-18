package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.UriComponents} - SOL001 V5.4.1 clause 6.2.50.
 *
 * <p>The UriComponents data type describes information used to build a URI that complies with IETF RFC 3986 [8].
 *
 * <p><b>Additional requirements</b> (clause 6.2.50): When this datatype is used to provide information for accessing APIs defined in ETSI GS NFV-SOL 002 [22], the path property may be included and the query and fragment properties shall be absent. The values of the scheme, authority and path properties form the {apiRoot} of the URI prefix.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UriComponents {

    /** scheme component of a URI. required: true. */
    @JsonProperty("scheme")
    private PropertyValue<String> scheme;

    /** Authority component of a URI */
    @JsonProperty("authority")
    private UriAuthority authority;

    /** path component of a URI. */
    @JsonProperty("path")
    private PropertyValue<String> path;

    /** query component of a URI. */
    @JsonProperty("query")
    private PropertyValue<String> query;

    /** fragment component of a URI. */
    @JsonProperty("fragment")
    private PropertyValue<String> fragment;

}
