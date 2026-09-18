package com.example.etsi.vnfd.services.pkg2template;

import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.template.ToscaMeta;
import com.example.etsi.vnfd.template.TopologyTemplate;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Stage one: an extracted VNF package directory to the TOSCA it contains.
 *
 * <p>Everything between a directory on disk and a {@link ServiceToscaTemplate} - finding the files,
 * reading TOSCA.meta, resolving relative paths, loading YAML, following imports. This step knows
 * about files, YAML and TOSCA, but nothing about NFV information elements: what it produces is the
 * descriptor as written, and interpreting it as a VNFD happens in the next stage.
 *
 * <p>One class because every part of it is about one package: the entry list, the root path and the
 * canonical-path rules were previously threaded between four classes as arguments.
 */
public final class PackageReader {

    // ============================================================================================
    // READING THE PACKAGE DIRECTORY
    // ============================================================================================

    // A VNF package laid out as a directory tree, i.e. an already-extracted CSAR.
    //
    // <p>Paths are package-internal and canonical, as produced by {@link PathResolver#normalize}:
    // forward slashes, no leading slash, e.g. {@code Definitions/main.yaml}.
    //
    // <p>SOL004 V5.1.1 clause 4.1.1 also allows a zip archive and a root-YAML layout without
    // {@code TOSCA-Metadata}. Neither is supported: an archive is expected to be extracted before it
    // gets here, and {@link ToscaMeta#parse} refuses a package with no {@code TOSCA.meta}.
    // [VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.
    //
    // <p>The directory is walked once at construction and the result is fixed: a package is onboarded
    // material, not a working directory, so re-reading it per lookup would only buy the ability to
    // observe someone editing it mid-parse.


    private final Path root;
    private final String name;
    private final Set<String> entries;

    public PackageReader(Path root) {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not an extracted VNF package directory: " + root);
        }
        this.root = root.toAbsolutePath().normalize();
        this.name = this.root.getFileName() == null
                ? this.root.toString()
                : this.root.getFileName().toString();
        this.entries = Collections.unmodifiableSet(scan(this.root));
    }

    private static Set<String> scan(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> normalize(root.relativize(p).toString()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot scan VNF package directory: " + root, e);
        }
    }

    /** Human-readable identifier used in diagnostics and findings. */
    private String name() {
        return name;
    }

    /** File contents, or empty when the path does not exist. */
    private Optional<byte[]> read(String packageInternalPath) {
        String canonical = normalize(packageInternalPath);
        if (!entries.contains(canonical)) {
            return Optional.empty();
        }
        Path target = root.resolve(canonical).normalize();
        // normalize() already refuses to climb above the root; this is the belt-and-braces check
        // in case a symlink inside the directory points outside it.
        if (!target.startsWith(root)) {
            throw new CsarSecurityException("Entry resolves outside the package root: " + packageInternalPath);
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read package entry: " + canonical, e);
        }
    }

    /** Convenience for text files; VNF package descriptors and metadata are UTF-8. */
    private Optional<String> readText(String path) {
        return read(path).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "DirectoryCsarReader(" + name + ", " + entries.size() + " entries)";
    }
    // ============================================================================================
    // PATH RESOLUTION INSIDE THE PACKAGE
    // ============================================================================================

    // Normalises and resolves paths inside a VNF package.
    //
    // <p>All package-internal paths are expressed with {@code /} and are relative to the package root,
    // with no leading slash. {@code TOSCA.meta} keys and TOSCA {@code imports} are relative to the file
    // that contains them, not to the root, so resolution always needs the referring file.
    //
    // <p>SOL004 V5.1.1 clause 4.1.2.1 states that artifacts may be "pointed to by relative path names
    // through artifact definitions in one of the TOSCA definitions files"; it does not define the
    // resolution algorithm, so the rules below follow ordinary relative-path semantics.
    // [VERSION MISMATCH] SOL004 V5.1.1.


    /**
     * Canonical form of a package-internal path: forward slashes, no leading slash, no {@code .}
     * or {@code ..} segments left.
     *
     * @throws CsarSecurityException if the path climbs above the package root
     */
    static String normalize(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        String unified = path.replace('\\', '/').trim();
        Deque<String> stack = new ArrayDeque<>();
        for (String segment : unified.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                if (stack.isEmpty()) {
                    throw new CsarSecurityException(
                            "Path escapes the VNF package root: " + path);
                }
                stack.removeLast();
                continue;
            }
            stack.addLast(segment);
        }
        return String.join("/", stack);
    }

    /**
     * Resolves {@code reference} as written inside the file at {@code fromFile}.
     *
     * <p>Example: a {@code HelmChart} artifact in
     * {@code Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml} writing
     * {@code ../Artifacts/Charts/simple-web-cnf-1.0.0.tgz} resolves to
     * {@code Artifacts/Charts/simple-web-cnf-1.0.0.tgz}.
     *
     * <p>An absolute-looking reference ({@code /Artifacts/x}) is treated as root-relative rather
     * than as a filesystem path, since nothing outside the archive is addressable.
     *
     * @param fromFile package-internal path of the file containing the reference
     * @param reference the relative path as written in that file
     * @throws CsarSecurityException if the result escapes the package root
     */
    static String resolve(String fromFile, String reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        String unified = reference.replace('\\', '/').trim();
        if (unified.startsWith("/")) {
            return normalize(unified);
        }
        String parent = parentOf(fromFile);
        String joined = parent.isEmpty() ? unified : parent + "/" + unified;
        return normalize(joined);
    }

    /** Directory part of a package-internal file path, or empty for a root-level file. */
    static String parentOf(String filePath) {
        if (filePath == null) {
            return "";
        }
        String unified = filePath.replace('\\', '/').trim();
        int slash = unified.lastIndexOf('/');
        return slash < 0 ? "" : unified.substring(0, slash);
    }

    /** Last path segment, e.g. {@code chart.tgz}. */
    static String fileNameOf(String filePath) {
        String unified = filePath.replace('\\', '/').trim();
        int slash = unified.lastIndexOf('/');
        return slash < 0 ? unified : unified.substring(slash + 1);
    }
    // ============================================================================================
    // TOSCA-Metadata/TOSCA.meta
    // ============================================================================================

    // Reads {@code TOSCA-Metadata/TOSCA.meta}.
    //
    // <p>The file is a sequence of {@code Name: value} lines. SOL004 V5.1.1 clause 4.1.2.1 describes
    // only block_0 for VNF packages, so blank-line-separated later blocks are not interpreted.
    //
    // <p>[VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.



    private static final String CLAUSE_KEYNAMES = "SOL004 V5.1.1 cl. 4.1.2.3 Table 4.1.2.3-1";

    /**
     * Keys deprecated in favour of the {@code ETSI-} prefixed form.
     * SOL004 V5.1.1 clause 4.1.2.3: the pre-prefix names from SOL004 2.4.1 to 2.5.1 "is deprecated
     * ... provided for backward compatibility".
     */
    private static final Map<String, String> DEPRECATED_ALIASES;

    static {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("Entry-Manifest", ToscaMeta.KEY_ETSI_ENTRY_MANIFEST);
        aliases.put("Entry-Change-Log", ToscaMeta.KEY_ETSI_ENTRY_CHANGE_LOG);
        aliases.put("Entry-Tests", ToscaMeta.KEY_ETSI_ENTRY_TESTS);
        aliases.put("Entry-Licenses", ToscaMeta.KEY_ETSI_ENTRY_LICENSES);
        aliases.put("Entry-Certificate", ToscaMeta.KEY_ETSI_ENTRY_CERTIFICATE);
        DEPRECATED_ALIASES = Collections.unmodifiableMap(aliases);
    }

    /**
     * Parses the metadata file of a package.
     *
     * @throws IllegalStateException if the file is missing or declares no {@code Entry-Definitions};
     *     both make the package unparseable, as opposed to merely non-conformant
     */
    private ToscaMeta parseToscaMeta(Findings findings) {
        SourceRef ref = SourceRef.ofFile(ToscaMeta.TOSCA_META_PATH);
        String text = readText(ToscaMeta.TOSCA_META_PATH)
                .orElseThrow(() -> new IllegalStateException(
                        "VNF package has no " + ToscaMeta.TOSCA_META_PATH));

        Map<String, String> block0 = readBlock0(text);
        applyDeprecatedAliases(block0, findings, ref);
        checkRequiredEtsiKeys(block0, findings, ref);

        String entry = block0.get(ToscaMeta.KEY_ENTRY_DEFINITIONS);
        if (entry == null || entry.trim().isEmpty()) {
            throw new IllegalStateException(
                    ToscaMeta.TOSCA_META_PATH + " declares no " + ToscaMeta.KEY_ENTRY_DEFINITIONS);
        }

        return new ToscaMeta(block0, normalize(entry),
                splitOtherDefinitions(block0.get(ToscaMeta.KEY_OTHER_DEFINITIONS)));
    }

    /** Reads {@code Name: value} lines up to the first blank line, which ends block_0. */
    private static Map<String, String> readBlock0(String text) {
        Map<String, String> block0 = new LinkedHashMap<>();
        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            block0.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
        }
        return block0;
    }

    /**
     * Accepts the pre-prefix key names, reporting them. When both forms are present SOL004 V5.1.1
     * clause 4.1.2.3 requires them to "point to the same value"; a conflict is an error because
     * there is no basis for preferring one.
     */
    private static void applyDeprecatedAliases(Map<String, String> block0, Findings findings,
                                               SourceRef ref) {
        for (Map.Entry<String, String> alias : DEPRECATED_ALIASES.entrySet()) {
            String legacyKey = alias.getKey();
            String canonicalKey = alias.getValue();
            String legacyValue = block0.get(legacyKey);
            if (legacyValue == null) {
                continue;
            }
            String canonicalValue = block0.get(canonicalKey);
            if (canonicalValue == null) {
                block0.put(canonicalKey, legacyValue);
                findings.warn("C13a", CLAUSE_KEYNAMES,
                        "TOSCA.meta uses the deprecated key '" + legacyKey + "'; use '"
                                + canonicalKey + "'", ref);
            } else if (!canonicalValue.equals(legacyValue)) {
                findings.error("C13b", CLAUSE_KEYNAMES,
                        "TOSCA.meta declares both '" + legacyKey + "' and '" + canonicalKey
                                + "' with different values; they shall point to the same value", ref);
            }
        }
    }

    /** SOL004 V5.1.1 Table 4.1.2.3-1 marks these two "required: yes". */
    private static void checkRequiredEtsiKeys(Map<String, String> block0, Findings findings,
                                              SourceRef ref) {
        for (String required : Arrays.asList(ToscaMeta.KEY_ETSI_ENTRY_MANIFEST,
                ToscaMeta.KEY_ETSI_ENTRY_CHANGE_LOG)) {
            if (!block0.containsKey(required)) {
                findings.warn("C13", CLAUSE_KEYNAMES,
                        "TOSCA.meta is missing '" + required + "', which SOL004 marks as required",
                        ref);
            }
        }
    }

    /**
     * {@code Other-Definitions} holds a comma-separated list of file names, per TOSCA Simple
     * Profile YAML 1.3 as referenced by SOL001 V5.4.1 clause 6.11.2.
     */
    private static List<String> splitOtherDefinitions(String value) {
        List<String> out = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return out;
        }
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(normalize(trimmed));
            }
        }
        return out;
    }
    // ============================================================================================
    // LOADING YAML
    // ============================================================================================

    // Loads a TOSCA YAML file into plain Java collections.
    //
    // <p>Uses SnakeYAML's {@code SafeConstructor}, which will not instantiate arbitrary classes named
    // by the document. A VNF package is supplied by a third party, so a descriptor must never be able
    // to decide what objects the parser constructs.
    //
    // <p>Mappings come back as {@code LinkedHashMap}, preserving declaration order. Order is carried
    // all the way to the parsed VNFD so that output is stable between runs and therefore comparable.


    /** Bounds document nesting, so a hostile package cannot exhaust the stack. */
    private static final int MAX_ALIASES = 256;
    private static final int MAX_NESTING_DEPTH = 128;

    /**
     * Parses YAML into a map.
     *
     * @param file package-internal path, used only for the error message
     * @throws ToscaYamlException when the document is not valid YAML or is not a mapping; either
     *     makes the file unusable, as distinct from merely non-conformant
     */
    static Map<String, Object> loadMapping(String file, byte[] content) {
        Object loaded;
        try {
            loaded = newYaml().load(new String(content, StandardCharsets.UTF_8));
        } catch (YAMLException e) {
            throw new ToscaYamlException("Cannot parse YAML in " + file + ": " + e.getMessage(), e);
        }
        if (loaded == null) {
            return new LinkedHashMap<>();
        }
        if (!(loaded instanceof Map)) {
            throw new ToscaYamlException(
                    "Expected a YAML mapping at the top level of " + file + " but found "
                            + loaded.getClass().getSimpleName());
        }
        return Yamls.map(loaded);
    }

    private static Yaml newYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(MAX_ALIASES);
        options.setNestingDepthLimit(MAX_NESTING_DEPTH);
        return new Yaml(new SafeConstructor(options));
    }

    // ============================================================================================
    // WALKING THE SERVICE TEMPLATES
    // ============================================================================================

    // Turns a VNF package into a {@link ServiceToscaTemplate}.
    //
    // <p>This step knows about files, YAML and TOSCA, but nothing about NFV information elements. What
    // it produces is the descriptor as written; interpreting it as a VNFD happens separately.


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
    public ServiceToscaTemplate parse() {
        return parse(new Findings());
    }

    public ServiceToscaTemplate parse(Findings findings) {
        ToscaMeta meta = parseToscaMeta(findings);

        List<String> candidateFiles = new ArrayList<>();
        candidateFiles.add(meta.entryDefinitions());
        for (String other : meta.otherDefinitions()) {
            if (!candidateFiles.contains(other)) {
                candidateFiles.add(other);
            }
        }

        TypeReader types = new TypeReader();
        TypeReader.addCatalogue(types);

        Map<String, ToscaDescriptorTemplate> templates = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        for (String file : candidateFiles) {
            readFileAndImports(file, types, templates, visited, findings, 0);
        }

        List<ToscaDescriptorTemplate> withTopology = new ArrayList<>();
        for (ToscaDescriptorTemplate template : templates.values()) {
            if (template.topologyTemplate().isPresent()) {
                withTopology.add(template);
            }
        }
        markTopLevel(meta, withTopology, findings);

        return new ServiceToscaTemplate(name(), meta, new ArrayList<>(templates.values()), types.build());
    }

    /**
     * Reads one file, registers its type definitions, then follows its imports.
     *
     * <p>A missing import is not an error. SOL001 V5.4.1 Annex B.2 NOTE 2 says the ETSI type
     * definitions file "may, but need not, be included in the VNF Package", and packages routinely
     * reference it without shipping it; the bundled catalogue supplies those types instead.
     */
    private void readFileAndImports(String file, TypeReader types,
                                    Map<String, ToscaDescriptorTemplate> templates,
                                    Set<String> visited, Findings findings, int depth) {
        if (depth > MAX_IMPORT_DEPTH || !visited.add(file)) {
            return;
        }
        byte[] content = read(file).orElse(null);
        if (content == null) {
            if (!TypeReader.isCatalogueFile(file)) {
                findings.info("YAML01", CLAUSE_TYPES_FILE,
                        "Imported file is not present in the package: " + file,
                        com.example.etsi.vnfd.validation.SourceRef.ofFile(file));
            }
            return;
        }

        Map<String, Object> document = loadMapping(file, content);
        types.add(document, file);
        ToscaDescriptorTemplate template = readTemplate(file, document);
        templates.put(file, template);

        for (String reference : template.imports()) {
            readFileAndImports(resolve(file, reference), types, templates,
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
        template.metadata().putAll(Yamls.map(document.get("metadata")));
        template.dslDefinitions().putAll(
                Yamls.map(document.get("dsl_definitions")));
        template.imports().addAll(readImports(document.get("imports")));
        template.repositories().putAll(TemplateReader.readRepositories(document.get("repositories")));

        for (String section : TypeReader.typeSectionNames()) {
            if (document.containsKey(section)) {
                template.typeDefinitions().put(section,
                        Yamls.map(document.get(section)));
            }
        }

        if (document.containsKey("topology_template")) {
            template.setTopologyTemplate(readTopology(file, document.get("topology_template")));
        }
        return template;
    }

    private TopologyTemplate readTopology(String file, Object block) {
        Map<String, Object> map = Yamls.map(block);
        TopologyTemplate topology = new TopologyTemplate();
        topology.setDescription(str(map.get("description")));
        topology.inputs().putAll(TemplateReader.readParameters(map.get("inputs")));
        topology.outputs().putAll(TemplateReader.readParameters(map.get("outputs")));
        topology.nodeTemplates().putAll(
                TemplateReader.readNodeTemplates(map.get("node_templates"), file));
        topology.relationshipTemplates().putAll(
                TemplateReader.readRelationshipTemplates(map.get("relationship_templates"), file));
        topology.groups().putAll(TemplateReader.readGroups(map.get("groups"), file));
        topology.policies().addAll(TemplateReader.readPolicies(map.get("policies"), file));
        if (map.containsKey("substitution_mappings")) {
            topology.setSubstitutionMappings(
                    TemplateReader.readSubstitutionMappings(map.get("substitution_mappings")));
        }

        // Artifact paths are written relative to the declaring file. Resolving them here means
        // nothing downstream has to know where that file lived in the package.
        for (NodeTemplate node : topology.nodeTemplates().values()) {
            TemplateReader.resolveArtifactPaths(node,
                    reference -> resolve(file, reference));
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
                Map<String, Object> map = Yamls.map(entry);
                Object file = map.get("file");
                if (file != null) {
                    out.add(String.valueOf(file));
                } else if (map.size() == 1) {
                    // Named form: "my_defs: { file: defs.yaml }" or "my_defs: defs.yaml".
                    Object only = map.values().iterator().next();
                    if (only instanceof Map) {
                        Object nested = Yamls.map(only).get("file");
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

    // ============================================================================================
    // FAILURES THAT MAKE A PACKAGE UNREADABLE
    // ============================================================================================

    /**
     * Thrown when a path inside a VNF package tries to escape the package root.
     *
     * <p>A descriptor controls artifact paths, and those paths are relative
     * ({@code file: ../Artifacts/Charts/chart.tgz}). A malicious or broken package can write
     * {@code ../../../etc/passwd}. Resolution refuses rather than reading outside the archive.
     */
    public static class CsarSecurityException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CsarSecurityException(String message) {
            super(message);
        }
    }

    /** Thrown when a file in the package is not usable YAML at all. */
    public static class ToscaYamlException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ToscaYamlException(String message) {
            super(message);
        }

        public ToscaYamlException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
