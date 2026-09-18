package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.CapabilityAssignment;
import com.example.etsi.vnfd.template.NodeFilter;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.RelationshipAssignment;
import com.example.etsi.vnfd.template.RequirementAssignment;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.utils.Yamls;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads {@code topology_template.node_templates}. */
final class NodeTemplateConverter {

    private NodeTemplateConverter() {
    }

    static Map<String, NodeTemplate> readAll(Object block, String file) {
        Map<String, NodeTemplate> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), read(e.getKey(), e.getValue(), file));
        }
        return out;
    }

    private static NodeTemplate read(String name, Object body, String file) {
        NodeTemplate node = new NodeTemplate(name);
        Map<String, Object> map = Yamls.map(body);
        node.setSource(SourceRef.of(file, name));
        node.setType(Yamls.string(map.get("type")));
        node.setDescription(Yamls.string(map.get("description")));
        node.metadata().putAll(Yamls.map(map.get("metadata")));
        node.properties().putAll(Yamls.map(map.get("properties")));
        node.attributes().putAll(Yamls.map(map.get("attributes")));
        node.setCopy(Yamls.string(map.get("copy")));
        if (map.containsKey("directives")) {
            node.setDirectives(Yamls.stringList(map.get("directives")));
        }
        if (map.containsKey("node_filter")) {
            node.setNodeFilter(readNodeFilter(map.get("node_filter")));
        }
        node.requirements().addAll(readRequirements(map.get("requirements")));
        node.capabilities().putAll(readCapabilities(map.get("capabilities")));
        node.artifacts().putAll(readArtifacts(map.get("artifacts"), file, name));
        node.interfaces().putAll(InterfaceConverter.readAll(map.get("interfaces")));
        return node;
    }

    /**
     * Requirements are a sequence of single-entry maps. Every entry is kept, including repeats of
     * the same name, which SOL001 Annex A.23 relies on for {@code associatedVdu}.
     */
    private static List<RequirementAssignment> readRequirements(Object block) {
        List<RequirementAssignment> out = new java.util.ArrayList<>();
        int index = 0;
        for (Object entry : Yamls.list(block)) {
            for (Map.Entry<String, Object> e : Yamls.map(entry).entrySet()) {
                out.add(readRequirement(e.getKey(), e.getValue(), index++));
            }
        }
        return out;
    }

    private static RequirementAssignment readRequirement(String name, Object body, int index) {
        RequirementAssignment requirement = new RequirementAssignment(name, index);
        if (!Yamls.isMap(body)) {
            // Short form: "- container: WebContainer".
            requirement.setNode(Yamls.string(body));
            return requirement;
        }
        Map<String, Object> map = Yamls.map(body);
        requirement.setNode(Yamls.string(map.get("node")));
        requirement.setCapability(Yamls.string(map.get("capability")));
        if (map.containsKey("relationship")) {
            requirement.setRelationship(readRelationship(map.get("relationship")));
        }
        if (map.containsKey("node_filter")) {
            requirement.setNodeFilter(readNodeFilter(map.get("node_filter")));
        }
        if (map.containsKey("occurrences")) {
            requirement.setOccurrences(Yamls.list(map.get("occurrences")));
        }
        return requirement;
    }

    private static RelationshipAssignment readRelationship(Object value) {
        if (!Yamls.isMap(value)) {
            return new RelationshipAssignment(Yamls.string(value));
        }
        Map<String, Object> map = Yamls.map(value);
        RelationshipAssignment relationship = new RelationshipAssignment(Yamls.string(map.get("type")));
        relationship.properties().putAll(Yamls.map(map.get("properties")));
        relationship.interfaces().putAll(InterfaceConverter.readAll(map.get("interfaces")));
        return relationship;
    }

    private static Map<String, CapabilityAssignment> readCapabilities(Object block) {
        Map<String, CapabilityAssignment> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            CapabilityAssignment capability = new CapabilityAssignment(e.getKey());
            Map<String, Object> body = Yamls.map(e.getValue());
            capability.properties().putAll(Yamls.map(body.get("properties")));
            capability.attributes().putAll(Yamls.map(body.get("attributes")));
            out.put(e.getKey(), capability);
        }
        return out;
    }

    private static Map<String, ArtifactDefinition> readArtifacts(Object block, String file,
            String owningNode) {
        Map<String, ArtifactDefinition> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : Yamls.map(block).entrySet()) {
            out.put(e.getKey(), readArtifact(e.getKey(), e.getValue(), file, owningNode));
        }
        return out;
    }

    /**
     * One artifact definition.
     *
     * <p>The source reference names the owning node, not just the file: SOL001 speaks of artifacts
     * per node template - "an artifact of type tosca.artifacts.nfv.SwImage" on a
     * {@code Vdu.OsContainer} (clause 6.8.12.6) - so a finding about one has to say which node.
     */
    private static ArtifactDefinition readArtifact(String name, Object body, String file,
            String owningNode) {
        ArtifactDefinition artifact = new ArtifactDefinition(name);
        artifact.setSource(SourceRef.of(file, owningNode + "." + name));
        if (!Yamls.isMap(body)) {
            // Short form: "my_artifact: path/to/file" gives a file with no declared type.
            artifact.setFile(Yamls.string(body));
            return artifact;
        }
        Map<String, Object> map = Yamls.map(body);
        artifact.setType(Yamls.string(map.get("type")));
        artifact.setFile(Yamls.string(map.get("file")));
        artifact.setRepository(Yamls.string(map.get("repository")));
        artifact.setDescription(Yamls.string(map.get("description")));
        artifact.setDeployPath(Yamls.string(map.get("deploy_path")));
        artifact.setArtifactVersion(Yamls.string(map.get("artifact_version")));
        artifact.setChecksum(Yamls.string(map.get("checksum")));
        artifact.setChecksumAlgorithm(Yamls.string(map.get("checksum_algorithm")));
        artifact.properties().putAll(Yamls.map(map.get("properties")));
        return artifact;
    }

    private static NodeFilter readNodeFilter(Object value) {
        NodeFilter filter = new NodeFilter();
        Map<String, Object> map = Yamls.map(value);
        for (Object entry : Yamls.list(map.get("properties"))) {
            filter.propertyConstraints().add(Yamls.map(entry));
        }
        filter.capabilityConstraints().putAll(Yamls.map(map.get("capabilities")));
        return filter;
    }
}
