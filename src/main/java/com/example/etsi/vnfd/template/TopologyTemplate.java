package com.example.etsi.vnfd.template;

import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.ParameterDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.RelationshipTemplate;
import com.example.etsi.vnfd.template.SubstitutionMappings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@code topology_template} of one service template file.
 *
 * <p>For a VNFD this is the deployment flavour: SOL001 V5.4.1 Table 6.1-1 NOTE 2 states that the
 * {@code VnfDf} information element "is represented as a TOSCA service template".
 *
 * <p>Every map preserves declaration order. The order VDUs appear in is the order they appear in
 * the parsed VNFD, which keeps output stable across runs and therefore diffable.
 */
public final class TopologyTemplate {

    private String description;
    private final Map<String, ParameterDefinition> inputs = new LinkedHashMap<>();
    private final Map<String, ParameterDefinition> outputs = new LinkedHashMap<>();
    private final Map<String, NodeTemplate> nodeTemplates = new LinkedHashMap<>();
    private final Map<String, RelationshipTemplate> relationshipTemplates = new LinkedHashMap<>();
    private final Map<String, GroupDefinition> groups = new LinkedHashMap<>();
    private final List<PolicyDefinition> policies = new ArrayList<>();
    private SubstitutionMappings substitutionMappings;

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** Declared inputs; the targets a {@code get_input} may legitimately name. */
    public Map<String, ParameterDefinition> inputs() {
        return inputs;
    }

    public Map<String, ParameterDefinition> outputs() {
        return outputs;
    }

    public Map<String, NodeTemplate> nodeTemplates() {
        return nodeTemplates;
    }

    /** TOSCA 1.3 keyname unused by SOL001; parsed and carried, never mapped. */
    public Map<String, RelationshipTemplate> relationshipTemplates() {
        return relationshipTemplates;
    }

    public Map<String, GroupDefinition> groups() {
        return groups;
    }

    /** A list, not a map: order is preserved and duplicate names are reported, not collapsed. */
    public List<PolicyDefinition> policies() {
        return policies;
    }

    public Optional<SubstitutionMappings> substitutionMappings() {
        return Optional.ofNullable(substitutionMappings);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSubstitutionMappings(SubstitutionMappings substitutionMappings) {
        this.substitutionMappings = substitutionMappings;
    }

    @Override
    public String toString() {
        return "topology_template" + nodeTemplates.keySet();
    }
}
