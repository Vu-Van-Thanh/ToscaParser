package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.LinkBitrateRequirements} - SOL001 V5.4.1 clause 9.2.5.
 *
 * <p>The LinkBitrateRequirements data type describes the requirements in terms of bitrate for a virtual link.
 *
 * <p><b>Additional requirements</b> (clause 9.2.5): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkBitrateRequirements {

    /** Specifies the throughput requirement in bits per second of the link (e.g. bitrate of E-Line, root bitrate of E-Tree, aggregate capacity of E-LAN). required: true. */
    @JsonProperty("root")
    private PropertyValue<Integer> root;

    /** Specifies the throughput requirement in bits per second of leaf connections to the link when applicable to the connectivity type (e.g. for E-Tree and E LAN branches). */
    @JsonProperty("leaf")
    private PropertyValue<Integer> leaf;

}
