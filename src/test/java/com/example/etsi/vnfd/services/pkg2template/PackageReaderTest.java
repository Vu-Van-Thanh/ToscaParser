package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Reading an extracted VNF package directory. */
class PackageReaderTest {

    @Test
    @DisplayName("every path the reader hands on is canonical")
    void everyPathIsCanonical() {
        ServiceToscaTemplate tst = new PackageReader(
                Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)).parse();

        // Forward slashes, no leading slash, whatever the host filesystem uses. Downstream stages
        // compare these paths against TOSCA.meta keys and artifact references, so one spelling.
        assertThat(tst.descriptorTemplates()).extracting(t -> t.file())
                .allSatisfy(f -> assertThat(f).doesNotStartWith("/").doesNotContain("\\"));
        assertThat(tst.meta().entryDefinitions())
                .isEqualTo("Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml");
    }

    @Test
    @DisplayName("a path escaping the package root is refused, not read")
    void refusesEscapingPath() {
        assertThatThrownBy(() -> PackageReader.normalize("../../../etc/passwd"))
                .isInstanceOf(PackageReader.CsarSecurityException.class);
    }

    @Test
    @DisplayName("a package with no TOSCA.meta cannot be parsed at all")
    void refusesPackageWithoutToscaMeta(@TempDir Path tempDir) {
        assertThatThrownBy(() -> new PackageReader(tempDir).parse())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonPackagePath(@TempDir Path tempDir) {
        assertThatThrownBy(() -> new PackageReader(tempDir.resolve("nope.txt")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
