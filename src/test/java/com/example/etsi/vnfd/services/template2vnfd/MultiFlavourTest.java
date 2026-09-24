package com.example.etsi.vnfd.services.template2vnfd;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.ToscaParser;
import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.validation.Severity;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A package with two deployment flavours, which is the half of the loader nothing else covers.
 *
 * <p>SOL001 V5.4.1 clause 6.11.2 gives a two-level design one service template per flavour, and the
 * same VDU may be written in every one of them. IFA011 clause 7.1.2.2 keeps the VDU at VNFD level
 * and clause 7.1.8.2.2 keeps the profile at flavour level, so the two halves of a redeclared node
 * behave differently: the element is contributed once, the profile once per flavour. Every other
 * test package has exactly one flavour and cannot tell the two apart.
 *
 * <p>{@code ExampleCorp_MultiDfCnf_vnf_pkg} declares {@code FrontVdu} and {@code FrontCp} in both
 * flavours with deliberately different content, which is what makes "first declaration wins"
 * observable rather than merely asserted.
 */
class MultiFlavourTest {

    private Vnfd parse() {
        return ToscaParser.parse(Fixtures.packageDir(Fixtures.MULTI_DF_CNF)).getVnfd();
    }

    private static VnfDf flavour(Vnfd vnfd, String flavourId) {
        return vnfd.getDf().stream()
                .filter(df -> flavourId.equals(df.getFlavourId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No flavour " + flavourId));
    }

    private static Optional<Integer> minInstances(VnfDf df, String vduId) {
        return df.getVduProfile().stream()
                .filter(p -> vduId.equals(p.getVduId()))
                .findFirst()
                .flatMap(VduProfile::getMinNumberOfInstances)
                .flatMap(PropertyValue::resolved);
    }

    @Test
    @DisplayName("Flavours reach the VNFD in the order TOSCA.meta lists them")
    void flavoursAreMappedInOrder() {
        assertThat(parse().getDf()).extracting(VnfDf::getFlavourId)
                .containsExactly("simple", "complex");
    }

    @Test
    @DisplayName("A VDU declared by both flavours is contributed once, by the first")
    void aVduDeclaredTwiceIsContributedOnce() {
        Vnfd vnfd = parse();

        // FrontVdu is declared in both templates, WorkerVdu only in 'complex'.
        assertThat(vnfd.getVdu()).extracting(Vdu::getVduId)
                .containsExactly("FrontVdu", "WorkerVdu");

        // Which of the two declarations survived: 'simple' writes multidf-simple-front,
        // 'complex' writes multidf-complex-front. First wins.
        Vdu front = vnfd.getVdu().get(0);
        assertThat(front.getMcioIdentificationData()
                .flatMap(data -> data.getName().resolved()))
                .contains("multidf-simple-front");
    }

    @Test
    @DisplayName("A connection point declared by both flavours keeps the first declaration")
    void aCpDeclaredTwiceKeepsTheFirstDeclaration() {
        Vnfd vnfd = parse();

        assertThat(vnfd.getVduCpd()).extracting(VduCpd::getCpdId)
                .containsExactly("FrontCp", "WorkerCp");

        // Only the 'complex' copy of FrontCp carries a virtual_link requirement. Seeing it here
        // would mean the later flavour had overwritten the earlier one.
        VduCpd front = vnfd.getVduCpd().get(0);
        assertThat(front.getIntVirtualLinkDesc()).isEmpty();
    }

    @Test
    @DisplayName("Flavour-level data is not deduplicated: one profile per flavour, per VDU")
    void flavourLevelDataIsNotDeduplicated() {
        Vnfd vnfd = parse();

        // The same VDU, two profiles, different numbers - IFA011 clause 7.1.8.2.2 puts VduProfile
        // on the flavour, so this is exactly what must NOT be merged.
        assertThat(minInstances(flavour(vnfd, "simple"), "FrontVdu")).contains(1);
        assertThat(minInstances(flavour(vnfd, "complex"), "FrontVdu")).contains(2);
        assertThat(minInstances(flavour(vnfd, "complex"), "WorkerVdu")).contains(3);

        assertThat(flavour(vnfd, "simple").getVduProfile()).hasSize(1);
        assertThat(flavour(vnfd, "complex").getVduProfile()).hasSize(2);
    }

    @Test
    @DisplayName("VNFD-level elements accumulate across flavours in order of first appearance")
    void elementsAccumulateAcrossFlavours() {
        Vnfd vnfd = parse();

        assertThat(vnfd.getMciopId()).containsExactly("simple_mciop", "complex_mciop");
        // Declared only by 'complex', so the second flavour still contributes.
        assertThat(vnfd.getIntVirtualLinkDesc())
                .extracting(vl -> vl.getVirtualLinkDescId())
                .containsExactly("WorkerVl");
    }

    /**
     * The VNF header is read from one flavour, not from every flavour that carries a VNF node.
     *
     * <p>IFA011 clause 7.1.2.2 keeps {@code lifeCycleManagementScript} at VNFD level while SOL001
     * clause 6.11.2 gives every flavour template the same VNF node type, so a script declared in a
     * two-level design appears once per template and must still reach the VNFD once.
     *
     * <p>The bundled package cannot show this as it ships - neither flavour declares a
     * {@code Vnflcm} interface, so both paths produce nothing - which is why the interface is
     * spliced into a copy here rather than asserted against the package on disk.
     */
    @Test
    @DisplayName("The VNF header and its scripts are read once, not once per flavour")
    void theHeaderIsReadFromOneFlavourOnly(@TempDir Path temp) throws Exception {
        Path pkg = temp.resolve(Fixtures.MULTI_DF_CNF);
        copyTree(Fixtures.packageDir(Fixtures.MULTI_DF_CNF), pkg);
        for (String df : new String[] {"simple", "complex"}) {
            spliceVnflcm(pkg.resolve("Definitions/ExampleCorp_MultiDfCnf_df_" + df + ".yaml"));
        }

        Vnfd vnfd = ToscaParser.parse(pkg).getVnfd();

        // Two templates declare it; the VNFD holds one. Without the latch this is 2.
        assertThat(vnfd.getLifeCycleManagementScript())
                .extracting(LifeCycleManagementScript::getLcmScriptId)
                .containsExactly("Vnflcm.instantiate_start");
    }

    /** Gives the VNF node template an implemented Vnflcm operation. */
    private static void spliceVnflcm(Path descriptor) throws Exception {
        String text = new String(Files.readAllBytes(descriptor), StandardCharsets.UTF_8);
        int anchor = text.indexOf("\n", text.indexOf("        flavour_description:"));
        String block = String.join("\n",
                "",
                "      interfaces:",
                "        Vnflcm:",
                "          type: tosca.interfaces.nfv.Vnflcm",
                "          operations:",
                "            instantiate_start:",
                "              implementation:",
                "                primary: ../Artifacts/Scripts/param_mapping.sh");
        Files.write(descriptor,
                (text.substring(0, anchor) + block + text.substring(anchor))
                        .getBytes(StandardCharsets.UTF_8));
    }

    private static void copyTree(Path from, Path to) throws Exception {
        try (java.util.stream.Stream<Path> tree = Files.walk(from)) {
            for (Path source : tree.collect(java.util.stream.Collectors.toList())) {
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

    @Test
    @DisplayName("The two-level design itself is not reported as a fault")
    void aCleanTwoLevelPackageIsAccusedOfNothing() {
        ParseResult result = ToscaParser.parse(Fixtures.packageDir(Fixtures.MULTI_DF_CNF));

        assertThat(result.hasErrors())
                .as("errors: %s", result.getFindings(Severity.ERROR))
                .isFalse();
    }
}
