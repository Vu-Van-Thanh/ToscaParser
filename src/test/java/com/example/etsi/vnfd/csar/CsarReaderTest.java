package com.example.etsi.vnfd.csar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.etsi.vnfd.fixture.Fixtures;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Reading an extracted VNF package directory. */
class CsarReaderTest {
    @Test
    @DisplayName("entries use canonical package-internal paths")
    void entriesAreCanonical() {
        Set<String> entries = CsarReader.of(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)).entries();

        assertThat(entries).contains(
                "TOSCA-Metadata/TOSCA.meta",
                "Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml",
                "Artifacts/Charts/simple-web-cnf-1.0.0.tgz");
        assertThat(entries).allSatisfy(e -> assertThat(e).doesNotStartWith("/").doesNotContain("\\"));
    }

    @Test
    @DisplayName("reading an absent entry returns empty rather than throwing")
    void missingEntryIsEmpty() {
        CsarReader csar = CsarReader.of(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));

        assertThat(csar.exists("Definitions/etsi_nfv_sol001_vnfd_types.yaml")).isFalse();
        assertThat(csar.read("Definitions/etsi_nfv_sol001_vnfd_types.yaml")).isEmpty();
    }

    @Test
    @DisplayName("a path escaping the package root is refused, not read")
    void refusesEscapingPath() {
        CsarReader csar = CsarReader.of(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));

        assertThatThrownBy(() -> csar.read("../../../etc/passwd"))
                .isInstanceOf(CsarSecurityException.class);
    }

    @Test
    void rejectsNonPackagePath(@TempDir Path tempDir) {
        assertThatThrownBy(() -> CsarReader.of(tempDir.resolve("nope.txt")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
