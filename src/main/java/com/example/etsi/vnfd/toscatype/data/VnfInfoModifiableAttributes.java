package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VnfInfoModifiableAttributes} - SOL001 V5.4.1 clause 6.2.33.
 *
 * <p>The VnfInfoModifiableAttributes data type describes VNF-specific extension and metadata for a given VNF.
 *
 * <p>Represents the {@code VnfInfoModifiableAttributes} information element of IFA011 V5.4.1 clause 7.1.14 (SOL001 V5.4.1 Table 6.1-1).
 *
 * <p><b>Additional requirements</b> (clause 6.2.33): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnfInfoModifiableAttributes {

    /** Extension properties of VnfInfo that are writeable */
    @JsonProperty("extensions")
    private VnfInfoModifiableAttributesExtensions extensions;

    /** Metadata properties of VnfInfo that are writeable */
    @JsonProperty("metadata")
    private VnfInfoModifiableAttributesMetadata metadata;

}
