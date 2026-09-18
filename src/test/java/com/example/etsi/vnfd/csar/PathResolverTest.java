package com.example.etsi.vnfd.csar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PathResolverTest {

    @ParameterizedTest
    @CsvSource({
            "Definitions/main.yaml,        Definitions/main.yaml",
            "./Definitions/main.yaml,      Definitions/main.yaml",
            "/Definitions/main.yaml,       Definitions/main.yaml",
            "Definitions//main.yaml,       Definitions/main.yaml",
            "Definitions/../Artifacts/x,   Artifacts/x",
    })
    @DisplayName("normalises package-internal paths to a canonical form")
    void normalises(String input, String expected) {
        assertThat(PathResolver.normalize(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("resolves an artifact path relative to the file that declares it")
    void resolvesArtifactRelativeToDeclaringFile() {
        // Exactly the reference used by the bundled SimpleWebCnf package: the Mciop node in
        // Definitions/ points one level up into Artifacts/.
        String resolved = PathResolver.resolve(
                "Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml",
                "../Artifacts/Charts/simple-web-cnf-1.0.0.tgz");

        assertThat(resolved).isEqualTo("Artifacts/Charts/simple-web-cnf-1.0.0.tgz");
    }

    @Test
    @DisplayName("a sibling import resolves inside the same directory")
    void resolvesSiblingImport() {
        assertThat(PathResolver.resolve(
                "Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml",
                "etsi_nfv_sol001_vnfd_types.yaml"))
                .isEqualTo("Definitions/etsi_nfv_sol001_vnfd_types.yaml");
    }

    @Test
    @DisplayName("a reference from a root-level file stays at the root")
    void resolvesFromRootLevelFile() {
        assertThat(PathResolver.resolve("main.yaml", "Artifacts/x.tgz")).isEqualTo("Artifacts/x.tgz");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../../etc/passwd",
            "../../etc/passwd",
            "../../outside.yaml",
            "Definitions/../../../escape.yaml",
    })
    @DisplayName("refuses paths that climb above the package root")
    void refusesZipSlip(String reference) {
        assertThatThrownBy(() -> PathResolver.resolve("Definitions/main.yaml", reference))
                .isInstanceOf(CsarSecurityException.class)
                .hasMessageContaining("escapes the VNF package root");
    }

    @Test
    @DisplayName("climbing exactly to the package root is legitimate, not an escape")
    void climbingToRootIsAllowed() {
        // Every bundled package does this: artifacts in Definitions/ reference ../Artifacts/...
        // Refusing one ".." from a first-level directory would break all of them.
        assertThat(PathResolver.resolve("Definitions/main.yaml", "../outside.yaml"))
                .isEqualTo("outside.yaml");
        assertThat(PathResolver.resolve("Definitions/main.yaml", "../Artifacts/Charts/c.tgz"))
                .isEqualTo("Artifacts/Charts/c.tgz");
    }

    @Test
    @DisplayName("a path that climbs and comes back is fine")
    void allowsClimbThatStaysInside() {
        assertThat(PathResolver.resolve("Definitions/a/b/main.yaml", "../../../Artifacts/x"))
                .isEqualTo("Artifacts/x");
    }

    @Test
    void extractsParentAndFileName() {
        assertThat(PathResolver.parentOf("Definitions/main.yaml")).isEqualTo("Definitions");
        assertThat(PathResolver.parentOf("main.yaml")).isEmpty();
        assertThat(PathResolver.fileNameOf("Artifacts/Charts/c.tgz")).isEqualTo("c.tgz");
    }
}
