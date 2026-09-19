package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaMeta;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToscaMetaTest {

    private static ToscaMeta parse(String packageName, Findings findings) {
        return new PackageReader(Fixtures.packageDir(packageName)).parse(findings).meta();
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
            ServiceToscaTemplate tst = new PackageReader(Fixtures.packageDir(pkg)).parse();

            // The entry template was read, so the file Entry-Definitions names is really there.
            assertThat(tst.entryTemplate())
                    .as("%s declares Entry-Definitions %s which must exist in the package",
                            pkg, tst.meta().entryDefinitions())
                    .isPresent();
        }
    }

    @Test
    @DisplayName("the file Other-Definitions names is in the package and gets read")
    void otherDefinitionsIsShippedAndRead() {
        ServiceToscaTemplate tst = new PackageReader(
                Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)).parse();

        // [PROJECT-SPECIFIC] SOL001 V5.4.1 Annex B.2 NOTE 2 allows the type definitions file to be
        // referenced without being shipped, but this parser resolves imports only inside the
        // package, so a package that leaves it out resolves no ETSI type at all.
        assertThat(tst.meta().otherDefinitions()).isNotEmpty();
        assertThat(tst.descriptorTemplates()).extracting(t -> t.file())
                .contains(tst.meta().otherDefinitions().get(0));
    }
}
