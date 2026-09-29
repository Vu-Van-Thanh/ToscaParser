package com.example.etsi.vnfd.toscatype.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VersionDependencyStatement} - SOL001 V5.4.1 clause 9.2.11.
 *
 * <p>The VersionDependencyStatement data type lists one or more VNF, NS or PNF descriptor identifiers which describe one single dependency.
 *
 * <p><b>Additional requirements</b> (clause 9.2.11): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionDependencyStatement {

    /** List of identifiers of VNFDs, NSDs or PNFDs upon which the entity using this information element depends. When more than one descriptor is indicated, they shall correspond to versions of the same VNF, NS or PNF and they represent. alternatives, i.e. the presence of one of them fulfills the dependency. required: true. */
    @JsonProperty("descriptor_id")
    private List<String> descriptorId;

}
