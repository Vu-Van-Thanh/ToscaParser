package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns a node template into the SOL001 class that stands for its type.
 *
 * <p>Three steps: find the class by {@code derived_from}, lay the type defaults under the assigned
 * properties, bind. The middle step is not optional - SOL001 V5.4.1 Annex A.23 writes a VNF node
 * template that assigns only {@code flavour_description} and leaves {@code descriptor_id},
 * {@code provider} and the rest on the node type, and clause 6.11.2 makes that the normal
 * arrangement rather than an edge case.
 */
public final class NodeBinder {

    private final ObjectMapper mapper = ToscaBindModule.mapper();
    private final NodeTypeResolver resolver;
    private final TypeDefaults defaults;
    private final ConstraintChecker constraints;

    /** Binds without reporting: the caller does not want the conformance findings. */
    public NodeBinder(TypeReader.Hierarchy hierarchy, List<Class<? extends NfvNode>> nodeClasses) {
        this(hierarchy, nodeClasses, new Findings());
    }

    public NodeBinder(TypeReader.Hierarchy hierarchy, List<Class<? extends NfvNode>> nodeClasses,
            Findings findings) {
        this.resolver = new NodeTypeResolver(hierarchy, nodeClasses);
        this.defaults = new TypeDefaults(hierarchy);
        this.constraints = new ConstraintChecker(hierarchy, findings);
    }

    /** Binds a node template, or returns empty when it is not a type this library maps. */
    public Optional<NfvNode> bind(NodeTemplate template) {
        Optional<Class<? extends NfvNode>> target = resolver.resolve(template.type());
        if (!target.isPresent()) {
            return Optional.empty();
        }

        Map<String, Object> merged = defaults.apply(template.type(), template.properties());
        constraints.check(template.type(), merged, template.source());
        checkArtifacts(template);

        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("type", template.type());
        template.description().ifPresent(d -> declaration.put("description", d));
        declaration.put("metadata", template.metadata());
        declaration.put("directives", template.directives());
        declaration.put("attributes", template.attributes());
        declaration.put("properties", merged);
        declaration.put("requirements", groupRequirements(template.requirements()));
        declaration.put("capabilities", template.capabilities());

        NfvNode node = mapper.convertValue(declaration, target.get());
        // Already-typed collections are attached rather than round-tripped: convertValue would have
        // to serialise them first, and they are DOM objects, not Jackson beans.
        node.setInterfaces(template.interfaces());
        node.setArtifacts(template.artifacts());
        node.setKey(template.name());
        node.setSource(template.source());
        node.setEtsiType(resolver.etsiTypeOf(template.type()).orElse(template.type()));
        return Optional.of(node);
    }

    /**
     * The same check over the properties of each attached artifact.
     *
     * <p>Artifact types declare properties like any other type - {@code tosca.artifacts.nfv.SwImage}
     * gives {@code container_format} a {@code valid_values} constraint and {@code size} the type
     * {@code scalar-unit.size} - so leaving them out would silently exempt them. The values are not
     * bound into the node here; the artifact keeps its raw map and the mappers read it.
     */
    private void checkArtifacts(NodeTemplate template) {
        if (template.artifacts() == null) {
            return;
        }
        for (ArtifactDefinition artifact : template.artifacts().values()) {
            constraints.check(artifact.type(),
                    defaults.apply(artifact.type(), artifact.properties()),
                    artifact.source());
        }
    }

    /** Binds and narrows in one step, for a caller that knows what it is looking for. */
    public <T extends NfvNode> Optional<T> bindAs(NodeTemplate template, Class<T> expected) {
        return bind(template).filter(expected::isInstance).map(expected::cast);
    }

    public NodeTypeResolver resolver() {
        return resolver;
    }

    /**
     * Requirements as a map of name to target list.
     *
     * <p>TOSCA writes them as a sequence of single-entry maps, and the same key may appear more than
     * once - SOL001 Annex A.23 declares {@code associatedVdu} twice on one {@code Mciop}. Grouping
     * into lists is what keeps the second one; reading the sequence into a map would drop it
     * silently, which is why every generated requirement field is a {@code List}.
     */
    private Map<String, List<String>> groupRequirements(List<RequirementAssignment> assignments) {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (RequirementAssignment assignment : assignments) {
            grouped.computeIfAbsent(assignment.name(), key -> new ArrayList<>())
                    .add(assignment.node());
        }
        return grouped;
    }
}
