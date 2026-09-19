package com.example.etsi.vnfd.services.template2vnfd;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.ToscaParser;
import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Severity;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    /**
     * [PROJECT-SPECIFIC] What omitting the ETSI type definitions actually costs.
     *
     * <p>Not the VNFD. A descriptor names the ETSI types literally, so every node template still
     * binds and the VNFD comes out identical - which is exactly what makes this worth a test. What
     * disappears is the validation those definitions carry: TOSCA03 is the {@code vnfm_info} pattern
     * constraint, declared on {@code tosca.nodes.nfv.VNF} and on nothing the package itself writes.
     * A consumer reading only the VNFD cannot tell the difference, so the finding has to say it.
     */
    @Test
    @DisplayName("[PROJECT-SPECIFIC] omitting the ETSI type files silently drops type-borne checks")
    void missingEtsiTypeFilesDropValidation(@TempDir Path temp) throws Exception {
        Path pkg = temp.resolve(Fixtures.SIMPLE_WEB_CNF);
        copyTree(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF), pkg);
        Files.delete(pkg.resolve("Definitions/etsi_nfv_sol001_vnfd_types.yaml"));
        Files.delete(pkg.resolve("Definitions/etsi_nfv_sol001_common_types.yaml"));

        ParseResult without = parse(pkg);
        ParseResult with = parseFixture(Fixtures.SIMPLE_WEB_CNF);

        // The import went unsatisfied, and SOL001 V5.4.1 Annex B.2 NOTE 2 would have allowed that.
        assertThat(ruleIds(without, Severity.INFO)).contains("YAML01");
        assertThat(ruleIds(with, Severity.INFO)).doesNotContain("YAML01");

        // The cost: a constraint that only the ETSI type definitions declare stops being applied.
        assertThat(ruleIds(with, Severity.WARN)).contains("TOSCA03");
        assertThat(ruleIds(without, Severity.WARN)).doesNotContain("TOSCA03");

        // And the VNFD looks the same either way, which is why YAML01 is the only warning a
        // consumer gets that the package was checked less thoroughly than it appears.
        assertThat(without.getVnfd().getVdu()).hasSameSizeAs(with.getVnfd().getVdu());
        assertThat(without.getVnfd().getVnfdId()).isEqualTo(with.getVnfd().getVnfdId());
    }

    @Test
    @DisplayName("TYPE01: a node template whose type resolves to nothing is reported, not skipped")
    void unresolvableNodeTypeIsReported(@TempDir Path temp) throws Exception {
        Path pkg = temp.resolve(Fixtures.SIMPLE_WEB_CNF);
        copyTree(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF), pkg);

        // A descriptor that names a type nothing declares - a typo, or a vendor type whose defining
        // file was left out of the package. The node type itself stays declared, so only the node
        // template breaks.
        Path descriptor = pkg.resolve("Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml");
        String text = new String(Files.readAllBytes(descriptor), StandardCharsets.UTF_8);
        Files.write(descriptor, text.replace("      type: ExampleCorp.SimpleWebCnf.1_0",
                "      type: ExampleCorp.Undeclared.1_0").getBytes(StandardCharsets.UTF_8));

        ParseResult result = parse(pkg);

        List<Finding> dropped = result.getFindings(Severity.ERROR).stream()
                .filter(f -> "TYPE01".equals(f.ruleId()))
                .collect(Collectors.toList());
        assertThat(dropped).hasSize(1);
        assertThat(dropped.get(0).message()).contains("ExampleCorp.Undeclared.1_0");

        // Without TYPE01 this package would parse "successfully" and simply have no VNF header.
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getVnfd().getVnfdId()).isEmpty();
    }

    private static void copyTree(Path from, Path to) throws Exception {
        try (java.util.stream.Stream<Path> tree = Files.walk(from)) {
            for (Path source : tree.collect(Collectors.toList())) {
                Path target = to.resolve(from.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(source, target);
                }
            }
        }
    }

    private static Path negativePackage(String name) {
        return Fixtures.negativePackageDir(name);
    }
}
