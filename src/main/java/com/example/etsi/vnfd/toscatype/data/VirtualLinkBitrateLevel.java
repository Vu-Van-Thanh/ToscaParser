package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VirtualLinkBitrateLevel} - SOL001 V5.4.1 clause 6.2.42.
 *
 * <p>The VirtualLinkBitrateLevel data type describes bitrate requirements applicable to the virtual link instantiated from a particular VnfVirtualLink.
 *
 * <p><b>Additional requirements</b> (clause 6.2.42): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualLinkBitrateLevel {

    /** Virtual link bitrate requirements for an instantiation level or bitrate delta for a scaling step required: true. */
    @JsonProperty("bitrate_requirements")
    private LinkBitrateRequirements bitrateRequirements;

}
