package com.example.etsi.vnfd.services.template2vnfd;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.ToscaParser;
import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Severity;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a package is told about itself.
 *
 * <p>Both halves matter. A conformant package must not be accused of anything - a parser that cries
 * wolf is a parser whose findings get ignored - and a package that breaks a SHALL must be told so
 * without the parse failing, since a consumer decides for itself which findings are fatal.
 */
class FindingsTest {

    private ParseResult parse(Path packageDir) {
        return ToscaParser.parse(packageDir);
    }

    private ParseResult parseFixture(String name) {
        return parse(Fixtures.packageDir(name));
    }

    private static List<String> ruleIds(ParseResult result, Severity severity) {
        return result.getFindings(severity).stream()
                .map(Finding::ruleId)
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("The bundled packages break no SHALL, and are told only what they do break")
    void bundledPackagesRaiseNoErrors() {
        for (String pkg : new String[] {Fixtures.SIMPLE_WEB_CNF, Fixtures.REGULAR_CNF,
                Fixtures.HYBRID_WEB_CNF}) {
            ParseResult result = parseFixture(pkg);

            assertThat(result.hasErrors())
                    .as("%s should raise no ERROR: %s", pkg, result.getFindings(Severity.ERROR))
                    .isFalse();

            // F10: no ETSI-Entry-Manifest and no ETSI-Entry-Change-Log in TOSCA.meta.
            // F11: vnfm_info is 'GenericVnfm', which fails the pattern the type declares.
            assertThat(ruleIds(result, Severity.WARN)).as(pkg).contains("C13", "TOSCA03");
        }
    }

    @Test
    @DisplayName("F12: a scalar-unit written without the space is reported, not rejected")
    void scalarUnitSpacingOnArtifactProperties() {
        ParseResult result = parseFixture(Fixtures.REGULAR_CNF);

        // SwImage.size is scalar-unit.size and both images write '128MB' / '256MB'. The check
        // reaches them only because artifact properties are validated against the artifact type,
        // the same way node properties are validated against the node type.
        List<Finding> spacing = result.getFindings(Severity.WARN).stream()
                .filter(f -> "TOSCA01".equals(f.ruleId()))
                .collect(Collectors.toList());
        assertThat(spacing).hasSize(2);
        assertThat(spacing).allSatisfy(f -> assertThat(f.message()).contains("size"));

        // Reported, not rejected: the descriptor still parses.
        assertThat(result.getVnfd().getSwImageDesc()).hasSize(2);
    }

    @Test
    @DisplayName("C2 catches a VDU no MCIOP covers, where the template-level rule cannot")
    void vduWithNeitherContainerNorMciop() {
        ParseResult result = parse(negativePackage("HybridWebCnf2_missing_mciop"));

        List<Finding> errors = result.getFindings(Severity.ERROR);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).ruleId()).isEqualTo("C2");
        assertThat(errors.get(0).clause()).isEqualTo("IFA011 V5.4.1 cl. 7.1.6.2.2 Note 10");
        assertThat(errors.get(0).message()).contains("CacheVdu");

        // SOL001 clause 6.8.13.7 is satisfied - WebContainer is a Vdu.OsContainer - so a
        // template-level check would have passed this package. C2 is the rule that does not.
        assertThat(result.getVnfd().getOsContainerDesc()).hasSize(1);

        List<Vdu> vdus = result.getVnfd().getVdu();
        assertThat(vdus).hasSize(2);
        assertThat(vdus.stream().filter(v -> "CacheVdu".equals(v.getVduId())).findFirst())
                .hasValueSatisfying(v -> assertThat(v.getLcmRealizationPath())
                        .isEqualTo(LcmRealizationPath.UNDETERMINED));
        assertThat(vdus.stream().filter(v -> "WebVdu".equals(v.getVduId())).findFirst())
                .hasValueSatisfying(v -> assertThat(v.getLcmRealizationPath())
                        .isEqualTo(LcmRealizationPath.DIRECT_MCIO_CISM));
    }

    private static Path negativePackage(String name) {
        return Paths.get("src/test/resources/negative").resolve(name).toAbsolutePath();
    }
}
