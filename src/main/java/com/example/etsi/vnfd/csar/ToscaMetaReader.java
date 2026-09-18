package com.example.etsi.vnfd.csar;

import com.example.etsi.vnfd.template.ToscaMeta;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads {@code TOSCA-Metadata/TOSCA.meta}.
 *
 * <p>The file is a sequence of {@code Name: value} lines. SOL004 V5.1.1 clause 4.1.2.1 describes
 * only block_0 for VNF packages, so blank-line-separated later blocks are not interpreted.
 *
 * <p>[VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.
 */
public final class ToscaMetaReader {

    private ToscaMetaReader() {
    }


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
    public static ToscaMeta parse(DirectoryCsarReader csar, Findings findings) {
        SourceRef ref = SourceRef.ofFile(ToscaMeta.TOSCA_META_PATH);
        String text = csar.readText(ToscaMeta.TOSCA_META_PATH)
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

        return new ToscaMeta(block0, PathResolver.normalize(entry),
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
                out.add(PathResolver.normalize(trimmed));
            }
        }
        return out;
    }
}
