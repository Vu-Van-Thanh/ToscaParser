package com.example.etsi.vnfd.template.converter;

import com.example.etsi.vnfd.csar.CsarReader;
import com.example.etsi.vnfd.csar.PathResolver;
import com.example.etsi.vnfd.csar.ToscaMeta;
import com.example.etsi.vnfd.utils.ToscaYamlLoader;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.TopologyTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.typedef.EtsiTypeCatalogue;
import com.example.etsi.vnfd.typedef.TypeRegistryBuilder;
import com.example.etsi.vnfd.validation.Findings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns a VNF package into a {@link ServiceToscaTemplate}.
 *
 * <p>This step knows about files, YAML and TOSCA, but nothing about NFV information elements. What
 * it produces is the descriptor as written; interpreting it as a VNFD happens separately.
 */
public final class YamlService {

    private static final String CLAUSE_TWO_LEVEL = "SOL001 V5.4.1 cl. 6.11.2";
    private static final String CLAUSE_TYPES_FILE = "SOL001 V5.4.1 Annex B.2 NOTE 2";

    /** Bounds import recursion in a package whose imports form a cycle. */
    private static final int MAX_IMPORT_DEPTH = 32;

    /**
     * Parses a package without keeping what was noticed along the way.
     *
     * <p>Convenient when the caller only wants the template. Note that findings raised here are
     * about the package rather than the descriptor - a missing manifest, a malformed TOSCA.meta key
     * - and this overload discards them. Genuine structural failures still throw.
     */
    public ServiceToscaTemplate parse(CsarReader csar) {
        return parse(csar, new Findings());
    }

    public ServiceToscaTemplate parse(CsarReader csar, Findings findings) {
        ToscaMeta meta = ToscaMeta.parse(csar, findings);

        List<String> candidateFiles = new ArrayList<>();
        candidateFiles.add(meta.entryDefinitions());
        for (String other : meta.otherDefinitions()) {
            if (!candidateFiles.contains(other)) {
                candidateFiles.add(other);
            }
        }

        TypeRegistryBuilder types = new TypeRegistryBuilder();
        EtsiTypeCatalogue.addTo(types);

        Map<String, ToscaDescriptorTemplate> templates = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        for (String file : candidateFiles) {
            readFileAndImports(csar, file, types, templates, visited, findings, 0);
        }

        List<ToscaDescriptorTemplate> withTopology = new ArrayList<>();
        for (ToscaDescriptorTemplate template : templates.values()) {
            if (template.topologyTemplate().isPresent()) {
                withTopology.add(template);
            }
        }
        markTopLevel(meta, withTopology, findings);

        return new ServiceToscaTemplate(csar, meta, new ArrayList<>(templates.values()), types.build());
    }

    /**
     * Reads one file, registers its type definitions, then follows its imports.
     *
     * <p>A missing import is not an error. SOL001 V5.4.1 Annex B.2 NOTE 2 says the ETSI type
     * definitions file "may, but need not, be included in the VNF Package", and packages routinely
     * reference it without shipping it; the bundled catalogue supplies those types instead.
     */
    private void readFileAndImports(CsarReader csar, String file, TypeRegistryBuilder types,
                                    Map<String, ToscaDescriptorTemplate> templates,
                                    Set<String> visited, Findings findings, int depth) {
        if (depth > MAX_IMPORT_DEPTH || !visited.add(file)) {
            return;
        }
        byte[] content = csar.read(file).orElse(null);
        if (content == null) {
            if (!EtsiTypeCatalogue.isCatalogueFile(file)) {
                findings.info("YAML01", CLAUSE_TYPES_FILE,
                        "Imported file is not present in the package: " + file,
                        com.example.etsi.vnfd.validation.SourceRef.ofFile(file));
            }
            return;
        }

        Map<String, Object> document = ToscaYamlLoader.loadMapping(file, content);
        types.add(document, file);
        ToscaDescriptorTemplate template = readTemplate(file, document);
        templates.put(file, template);

        for (String reference : template.imports()) {
            readFileAndImports(csar, PathResolver.resolve(file, reference), types, templates,
                    visited, findings, depth + 1);
        }
    }

    /**
     * Decides which template, if any, is the top level of a two-level VNFD.
     *
     * <p>SOL001 V5.4.1 clause 6.11.2 puts the lower-level templates in {@code Other-Definitions},
     * but that key is not a reliable signal on its own: packages also list type definition files
     * there, as all three bundled examples do. What distinguishes a deployment flavour template is
     * that it carries a topology, so the count of templates with a topology decides the design.
     */
    private void markTopLevel(ToscaMeta meta, List<ToscaDescriptorTemplate> withTopology,
                              Findings findings) {
        if (withTopology.size() < 2) {
            return;
        }
        for (ToscaDescriptorTemplate template : withTopology) {
            if (template.file().equals(meta.entryDefinitions())) {
                template.setTopLevel(true);
                findings.info("YAML02", CLAUSE_TWO_LEVEL,
                        "Package uses the two-level service template design; "
                                + (withTopology.size() - 1) + " deployment flavour template(s) found",
                        com.example.etsi.vnfd.validation.SourceRef.ofFile(template.file()));
                return;
            }
        }
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private ToscaDescriptorTemplate readTemplate(String file, Map<String, Object> document) {
        ToscaDescriptorTemplate template = new ToscaDescriptorTemplate(file);
        template.setToscaDefinitionsVersion(str(document.get("tosca_definitions_version")));
        template.setDescription(str(document.get("description")));
        template.setNamespace(str(document.get("namespace")));
        template.metadata().putAll(ToscaYamlLoader.asStringKeyedMap(document.get("metadata")));
        template.dslDefinitions().putAll(
                ToscaYamlLoader.asStringKeyedMap(document.get("dsl_definitions")));
        template.imports().addAll(readImports(document.get("imports")));
        template.repositories().putAll(TopologyConverter.readRepositories(document.get("repositories")));

        for (String section : TypeRegistryBuilder.typeSectionNames()) {
            if (document.containsKey(section)) {
                template.typeDefinitions().put(section,
                        ToscaYamlLoader.asStringKeyedMap(document.get(section)));
            }
        }

        if (document.containsKey("topology_template")) {
            template.setTopologyTemplate(readTopology(file, document.get("topology_template")));
        }
        return template;
    }

    private TopologyTemplate readTopology(String file, Object block) {
        Map<String, Object> map = ToscaYamlLoader.asStringKeyedMap(block);
        TopologyTemplate topology = new TopologyTemplate();
        topology.setDescription(str(map.get("description")));
        topology.inputs().putAll(TopologyConverter.readParameters(map.get("inputs")));
        topology.outputs().putAll(TopologyConverter.readParameters(map.get("outputs")));
        topology.nodeTemplates().putAll(
                TopologyConverter.readNodeTemplates(map.get("node_templates"), file));
        topology.relationshipTemplates().putAll(
                TopologyConverter.readRelationshipTemplates(map.get("relationship_templates"), file));
        topology.groups().putAll(TopologyConverter.readGroups(map.get("groups"), file));
        topology.policies().addAll(TopologyConverter.readPolicies(map.get("policies"), file));
        if (map.containsKey("substitution_mappings")) {
            topology.setSubstitutionMappings(
                    TopologyConverter.readSubstitutionMappings(map.get("substitution_mappings")));
        }

        // Artifact paths are written relative to the declaring file. Resolving them here means
        // nothing downstream has to know where that file lived in the package.
        for (NodeTemplate node : topology.nodeTemplates().values()) {
            TopologyConverter.resolveArtifactPaths(node,
                    reference -> PathResolver.resolve(file, reference));
        }
        return topology;
    }

    /**
     * Reads the {@code imports} sequence.
     *
     * <p>An entry is either a bare file name or a map carrying {@code file} alongside
     * {@code repository} and {@code namespace_prefix}. Only the file matters here: this library
     * never fetches from a repository, so an entry naming one still resolves by file name and is
     * simply not found if the package does not contain it.
     */
    private static List<String> readImports(Object block) {
        List<String> out = new ArrayList<>();
        if (block == null) {
            return out;
        }
        List<?> entries = block instanceof List ? (List<?>) block
                : java.util.Collections.singletonList(block);
        for (Object entry : entries) {
            if (entry instanceof Map) {
                Map<String, Object> map = ToscaYamlLoader.asStringKeyedMap(entry);
                Object file = map.get("file");
                if (file != null) {
                    out.add(String.valueOf(file));
                } else if (map.size() == 1) {
                    // Named form: "my_defs: { file: defs.yaml }" or "my_defs: defs.yaml".
                    Object only = map.values().iterator().next();
                    if (only instanceof Map) {
                        Object nested = ToscaYamlLoader.asStringKeyedMap(only).get("file");
                        if (nested != null) {
                            out.add(String.valueOf(nested));
                        }
                    } else if (only != null) {
                        out.add(String.valueOf(only));
                    }
                }
            } else if (entry != null) {
                out.add(String.valueOf(entry));
            }
        }
        return out;
    }
}
