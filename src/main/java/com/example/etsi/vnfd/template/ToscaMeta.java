package com.example.etsi.vnfd.template;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What {@code TOSCA-Metadata/TOSCA.meta} block_0 says about a package.
 *
 * <p>Key names follow SOL004 V5.1.1 clause 4.1.2.1 and Table 4.1.2.3-1. The constants live with
 * the data they name, so a reader in any stage can address a key without re-spelling it.
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

    public ToscaMeta(Map<String, String> block0, String entryDefinitions, List<String> otherDefinitions) {
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

    @Override
    public String toString() {
        return "ToscaMeta" + block0;
    }
}
