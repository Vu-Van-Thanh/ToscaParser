package com.example.etsi.vnfd.toscatype.data;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code tosca.datatypes.nfv.SelectedDeployableModules} - SOL001 V5.4.1 clause 9.2.13.
 *
 * <p>The SelectedDeployableModules data type indicates the selected deployable modules of a VNF, which is a constituent of an NS, as defined in ETSI GS NFV-IFA 011 [1].
 *
 * <p><b>Additional requirements</b> (clause 9.2.13): None.
 *
 * <p>Field names, types and cardinalities come from the ETSI type definitions;
 * the text above is quoted from the specification itself.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectedDeployableModules {

    /** Name(s) of the selected deployable module(s) for the VNF instances created from the applicable VNF or NS profile. */
    @JsonProperty("deployable_module")
    private List<String> deployableModule;

    /** Indicates whether it is allowed or not to override the selection of deployable modules indicated in a property of this data type by means of attributes in an NS LCM operation, or by attributes in the NsProfile if the VNF is part of a nested NS, during the lifecycle of a VNF instance. If not present, overriding the selection is allowed. required: true. */
    @JsonProperty("overriding_selection_allowed")
    private PropertyValue<Boolean> overridingSelectionAllowed;

}
