package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.VersionDependency} - SOL001 V5.4.1 clause 9.2.10.
 *
 * <p>The VersionDependency data type describes all dependencies that an NSD constituent has on the versions of other NSD constituents.
 *
 * <p><b>Additional requirements</b> (clause 9.2.10): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionDependency {

    /** Identifier of the NSD constituent which has version dependencies on other NSD constituents. required: true. */
    @JsonProperty("dependent_constituent_id")
    private PropertyValue<String> dependentConstituentId;

    /** Identifies one or multiple versions of an NSD constituent upon which the dependent constituent identified by dependent_constituent_id has a dependency. required: true. */
    @JsonProperty("version_dependency_statement")
    private List<VersionDependencyStatement> versionDependencyStatement;

}
