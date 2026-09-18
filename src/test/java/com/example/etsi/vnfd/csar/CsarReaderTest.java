package com.example.etsi.vnfd.csar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.etsi.vnfd.fixture.Fixtures;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Reading an extracted VNF package directory. */
class CsarReaderTest {

    @Test
    @DisplayName("entries are addressed by canonical package-internal path")
    void readsByCanonicalPath() {
        DirectoryCsarReader csar = new DirectoryCsarReader(
                Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));

        // Forward slashes, no leading slash, whatever the host filesystem uses.
        assertThat(csar.read("TOSCA-Metadata/TOSCA.meta")).isPresent();
        assertThat(csar.read("Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml")).isPresent();
        assertThat(csar.read("Artifacts/Charts/simple-web-cnf-1.0.0.tgz")).isPresent();
    }

    @Test
    @DisplayName("reading an absent entry returns empty rather than throwing")
    void missingEntryIsEmpty() {
        DirectoryCsarReader csar = new DirectoryCsarReader(
                Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));

        // SOL001 V5.4.1 Annex B.2 NOTE 2: the ETSI types file need not be shipped in the package,
        // and all three bundled packages import it without shipping it.
        assertThat(csar.read("Definitions/etsi_nfv_sol001_vnfd_types.yaml")).isEmpty();
    }

    @Test
    @DisplayName("a path escaping the package root is refused, not read")
    void refusesEscapingPath() {
        DirectoryCsarReader csar = new DirectoryCsarReader(
                Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF));

        assertThatThrownBy(() -> csar.read("../../../etc/passwd"))
                .isInstanceOf(CsarSecurityException.class);
    }

    @Test
    void rejectsNonPackagePath(@TempDir Path tempDir) {
        assertThatThrownBy(() -> new DirectoryCsarReader(tempDir.resolve("nope.txt")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
