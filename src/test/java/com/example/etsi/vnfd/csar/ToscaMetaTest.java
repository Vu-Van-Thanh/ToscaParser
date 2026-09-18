package com.example.etsi.vnfd.csar;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToscaMetaTest {

    private static ToscaMeta parse(String packageName, Findings findings) {
        CsarReader csar = CsarReader.of(Fixtures.packageDir(packageName));
        return ToscaMeta.parse(csar, findings);
    }

    @Test
    @DisplayName("reads Entry-Definitions from the bundled SimpleWebCnf package")
    void readsEntryDefinitions() {
        ToscaMeta meta = parse(Fixtures.SIMPLE_WEB_CNF, new Findings());

        assertThat(meta.entryDefinitions())
                .isEqualTo("Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml");
        assertThat(meta.get(ToscaMeta.KEY_CSAR_VERSION)).contains("1.1");
        assertThat(meta.get(ToscaMeta.KEY_CREATED_BY)).contains("ExampleCorp NFV Team");
    }

    @Test
    @DisplayName("Other-Definitions is a comma-separated list, canonicalised in order")
    void readsOtherDefinitions() {
        ToscaMeta meta = parse(Fixtures.SIMPLE_WEB_CNF, new Findings());

        assertThat(meta.otherDefinitions())
                .containsExactly("Definitions/etsi_nfv_sol001_vnfd_types.yaml");
    }

    @Test
    @DisplayName("reports the missing manifest that SOL004 marks required, without failing the parse")
    void reportsMissingRequiredEtsiKeys() {
        Findings findings = new Findings();
        ToscaMeta meta = parse(Fixtures.SIMPLE_WEB_CNF, findings);

        // The package still parses; the gap is reported so the caller decides what to do with it.
        assertThat(meta.entryDefinitions()).isNotEmpty();
        assertThat(meta.manifestPath()).isEmpty();

        assertThat(findings.withSeverity(Severity.WARN))
                .extracting(Finding::message)
                .anySatisfy(m -> assertThat(m).contains("ETSI-Entry-Manifest"))
                .anySatisfy(m -> assertThat(m).contains("ETSI-Entry-Change-Log"));
        assertThat(findings.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("all three bundled packages declare a reachable Entry-Definitions file")
    void everyBundledPackageHasReachableEntryDefinitions() {
        for (String pkg : new String[]{Fixtures.SIMPLE_WEB_CNF, Fixtures.REGULAR_CNF,
                Fixtures.HYBRID_WEB_CNF}) {
            CsarReader csar = CsarReader.of(Fixtures.packageDir(pkg));
            ToscaMeta meta = ToscaMeta.parse(csar, new Findings());

            assertThat(csar.exists(meta.entryDefinitions()))
                    .as("%s declares Entry-Definitions %s which must exist in the package",
                            pkg, meta.entryDefinitions())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Other-Definitions may name a file the package does not ship")
    void otherDefinitionsMayBeAbsentFromPackage() {
        CsarReader csar = CsarReader.of(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));
        ToscaMeta meta = ToscaMeta.parse(csar, new Findings());

        // SOL001 V5.4.1 Annex B.2 NOTE 2: the type definitions file "may, but need not, be
        // included in the VNF Package". All three bundled packages reference it without shipping it.
        assertThat(meta.otherDefinitions()).isNotEmpty();
        assertThat(csar.exists(meta.otherDefinitions().get(0))).isFalse();
    }
}
