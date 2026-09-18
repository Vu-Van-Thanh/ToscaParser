package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.ParameterDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.RelationshipTemplate;
import com.example.etsi.vnfd.template.RepositoryDefinition;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.SubstitutionMappings;
import com.example.etsi.vnfd.utils.Yamls;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads the pieces of a {@code topology_template} that live in this package.
 *
 * <p>Kept here rather than beside {@link com.example.etsi.vnfd.template.TopologyTemplate} so the DOM
 * classes can keep package-private setters: only the readers populate them, and nothing outside
 * this package can mutate a parsed template by accident.
 */
public final class TopologyConverter {

    private TopologyConverter() {
    }

    public static Map<String, NodeTemplate> readNodeTemplates(Object block, String file) {
        return NodeTemplateConverter.readAll(block, file);
    }

    public static java.util.List<PolicyDefinition> readPolicies(Object block, String file) {
        return PolicyConverter.readAll(block, file);
    }

    public static SubstitutionMappings readSubstitutionMappings(Object block) {
        return SubstitutionMappingsConverter.read(block);
    }

    public static Map<String, GroupDefinition> readGroups(Object block, String file) {
        Map<String, GroupDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            GroupDefinition group = new GroupDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            group.setSource(SourceRef.of(file, e.getKey()));
            group.setType(Yamls.string(body.get("type")));
            group.setDescription(Yamls.string(body.get("description")));
            group.metadata().putAll(Yamls.map(body.get("metadata")));
            group.properties().putAll(Yamls.map(body.get("properties")));
            group.members().addAll(Yamls.stringList(body.get("members")));
            out.put(e.getKey(), group);
        }
        return out;
    }

    public static Map<String, RelationshipTemplate> readRelationshipTemplates(Object block, String file) {
        Map<String, RelationshipTemplate> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            RelationshipTemplate template = new RelationshipTemplate(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            template.setSource(SourceRef.of(file, e.getKey()));
            template.setType(Yamls.string(body.get("type")));
            template.setDescription(Yamls.string(body.get("description")));
            template.properties().putAll(Yamls.map(body.get("properties")));
            template.attributes().putAll(Yamls.map(body.get("attributes")));
            template.interfaces().putAll(InterfaceConverter.readAll(body.get("interfaces")));
            out.put(e.getKey(), template);
        }
        return out;
    }

    public static Map<String, ParameterDefinition> readParameters(Object block) {
        Map<String, ParameterDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            ParameterDefinition parameter = new ParameterDefinition(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            if (body.isEmpty() && e.getValue() != null) {
                parameter.setValue(e.getValue());
            } else {
                parameter.setType(Yamls.string(body.get("type")));
                parameter.setDescription(Yamls.string(body.get("description")));
                parameter.setRequired(Yamls.bool(body.get("required")));
                parameter.setStatus(Yamls.string(body.get("status")));
                if (body.containsKey("default")) {
                    parameter.setDefaultValue(body.get("default"));
                }
                if (body.containsKey("value")) {
                    parameter.setValue(body.get("value"));
                }
                if (body.containsKey("entry_schema")) {
                    parameter.setEntrySchema(body.get("entry_schema"));
                }
                parameter.constraints().addAll(Yamls.list(body.get("constraints")));
            }
            out.put(e.getKey(), parameter);
        }
        return out;
    }

    public static Map<String, RepositoryDefinition> readRepositories(Object block) {
        Map<String, RepositoryDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            RepositoryDefinition repository = new RepositoryDefinition(e.getKey());
            if (Yamls.isMap(e.getValue())) {
                Map<String, Object> body = Yamls.map(e.getValue());
                repository.setUrl(Yamls.string(body.get("url")));
                repository.setDescription(Yamls.string(body.get("description")));
            } else {
                repository.setUrl(Yamls.string(e.getValue()));
            }
            out.put(e.getKey(), repository);
        }
        return out;
    }

    /** Resolves artifact file references against the package root. */
    public static void resolveArtifactPaths(NodeTemplate node, java.util.function.UnaryOperator<String> resolver) {
        for (ArtifactDefinition artifact : node.artifacts().values()) {
            if (artifact.file() != null) {
                artifact.setResolvedFile(resolver.apply(artifact.file()));
            }
        }
    }
}
