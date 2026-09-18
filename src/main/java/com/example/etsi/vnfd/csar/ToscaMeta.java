package com.example.etsi.vnfd.csar;

import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code TOSCA-Metadata/TOSCA.meta} block_0: what it says, and how it is read.
 *
 * <p>The file is a sequence of {@code Name: value} lines. SOL004 V5.1.1 clause 4.1.2.1 describes
 * only block_0 for VNF packages, so blank-line-separated later blocks are not interpreted. Key
 * names follow clause 4.1.2.1 and Table 4.1.2.3-1.
 *
 * <p>Parsing lives here rather than in a separate class because the two are the same knowledge:
 * every key constant below is used by the reader immediately after it is declared, and a key added
 * to one half is meaningless without the other.
 *
 * <p>[VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.
 */
public final class ToscaMeta {

    /** Fixed location of the metadata file. SOL004 V5.1.1 clause 4.1.2. */
    public static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";

    public static final String KEY_META_FILE_VERSION = "TOSCA-Meta-File-Version";
    public static final String KEY_CSAR_VERSION = "CSAR-Version";
    public static final String KEY_CREATED_BY = "Created-By";
    public static final String KEY_ENTRY_DEFINITIONS = "Entry-Definitions";
    public static final String KEY_OTHER_DEFINITIONS = "Other-Definitions";

    /** SOL004 V5.1.1 Table 4.1.2.3-1, "required: yes". */
    public static final String KEY_ETSI_ENTRY_MANIFEST = "ETSI-Entry-Manifest";
    /** SOL004 V5.1.1 Table 4.1.2.3-1, "required: yes". */
    public static final String KEY_ETSI_ENTRY_CHANGE_LOG = "ETSI-Entry-Change-Log";
    public static final String KEY_ETSI_ENTRY_TESTS = "ETSI-Entry-Tests";
    public static final String KEY_ETSI_ENTRY_LICENSES = "ETSI-Entry-Licenses";
    public static final String KEY_ETSI_ENTRY_CERTIFICATE = "ETSI-Entry-Certificate";

    private final Map<String, String> block0;
    private final String entryDefinitions;
    private final List<String> otherDefinitions;

    ToscaMeta(Map<String, String> block0, String entryDefinitions, List<String> otherDefinitions) {
        this.block0 = Collections.unmodifiableMap(new LinkedHashMap<>(block0));
        this.entryDefinitions = entryDefinitions;
        this.otherDefinitions = Collections.unmodifiableList(otherDefinitions);
    }

    /** Every name-value pair as written, in file order. */
    public Map<String, String> block0() {
        return block0;
    }

    /** Raw value of a key, if present. */
    public Optional<String> get(String key) {
        return Optional.ofNullable(block0.get(key));
    }

    /**
     * The main service template, canonicalised. SOL001 V5.4.1 clause 6.11.2: for the two-level
     * design this is the top-level template; for the one-template design of clause 6.11.3 it is the
     * whole VNFD.
     */
    public String entryDefinitions() {
        return entryDefinitions;
    }

    /**
     * Files listed under {@code Other-Definitions}, canonicalised, in declaration order.
     *
     * <p>SOL001 V5.4.1 clause 6.11.2 requires the lower-level service templates to be declared
     * here. In practice packages also list type-definition files, so a caller must inspect each
     * file rather than assume every entry is a deployment flavour.
     */
    public List<String> otherDefinitions() {
        return otherDefinitions;
    }

    /** Location of the manifest file, when declared. */
    public Optional<String> manifestPath() {
        return get(KEY_ETSI_ENTRY_MANIFEST);
    }

    // ------------------------------------------------------------------------ reading

    private static final String CLAUSE_KEYNAMES = "SOL004 V5.1.1 cl. 4.1.2.3 Table 4.1.2.3-1";

    /**
     * Keys deprecated in favour of the {@code ETSI-} prefixed form.
     * SOL004 V5.1.1 clause 4.1.2.3: the pre-prefix names from SOL004 2.4.1 to 2.5.1 "is deprecated
     * ... provided for backward compatibility".
     */
    private static final Map<String, String> DEPRECATED_ALIASES;

    static {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("Entry-Manifest", KEY_ETSI_ENTRY_MANIFEST);
        aliases.put("Entry-Change-Log", KEY_ETSI_ENTRY_CHANGE_LOG);
        aliases.put("Entry-Tests", KEY_ETSI_ENTRY_TESTS);
        aliases.put("Entry-Licenses", KEY_ETSI_ENTRY_LICENSES);
        aliases.put("Entry-Certificate", KEY_ETSI_ENTRY_CERTIFICATE);
        DEPRECATED_ALIASES = Collections.unmodifiableMap(aliases);
    }

    /**
     * Parses the metadata file of a package.
     *
     * @throws IllegalStateException if the file is missing or declares no {@code Entry-Definitions};
     *     both make the package unparseable, as opposed to merely non-conformant
     */
    public static ToscaMeta parse(CsarReader csar, Findings findings) {
        SourceRef ref = SourceRef.ofFile(TOSCA_META_PATH);
        String text = csar.readText(TOSCA_META_PATH)
                .orElseThrow(() -> new IllegalStateException(
                        "VNF package has no " + TOSCA_META_PATH));

        Map<String, String> block0 = readBlock0(text);
        applyDeprecatedAliases(block0, findings, ref);
        checkRequiredEtsiKeys(block0, findings, ref);

        String entry = block0.get(KEY_ENTRY_DEFINITIONS);
        if (entry == null || entry.trim().isEmpty()) {
            throw new IllegalStateException(
                    TOSCA_META_PATH + " declares no " + KEY_ENTRY_DEFINITIONS);
        }

        return new ToscaMeta(block0, PathResolver.normalize(entry),
                splitOtherDefinitions(block0.get(KEY_OTHER_DEFINITIONS)));
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
        for (String required : Arrays.asList(KEY_ETSI_ENTRY_MANIFEST,
                KEY_ETSI_ENTRY_CHANGE_LOG)) {
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
                out.add(PathResolver.normalize(trimmed));
            }
        }
        return out;
    }

    @Override
    public String toString() {
        return "ToscaMeta" + block0;
    }
}
