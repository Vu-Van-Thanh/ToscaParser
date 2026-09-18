package com.example.etsi.vnfd.map;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.csar.DirectoryCsarReader;
import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.template.converter.YamlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** End to end: a bundled VNF package to a VNFD. */
class VnfdLoaderTest {

    private Vnfd parse(String pkg) {
        return new VnfdLoader()
                .load(new YamlService().parse(new DirectoryCsarReader(Fixtures.packageDir(pkg))))
                .getVnfd();
    }

    @Test
    @DisplayName("MCIOP-only VDU: container resources live in the chart, not in the VNFD")
    void simpleWebCnf() {
        Vnfd vnfd = parse(Fixtures.SIMPLE_WEB_CNF);

        assertThat(vnfd.getVnfdId()).contains("8f6a2e2e-2c3a-4a7a-9a1a-6a3f2b6c9d10");
        assertThat(vnfd.getVnfProvider()).contains("ExampleCorp");
        assertThat(vnfd.getVnfmInfo()).containsExactly("GenericVnfm");

        assertThat(vnfd.getVdu()).hasSize(1);
        Vdu vdu = vnfd.getVdu().get(0);
        assertThat(vdu.getVduId()).isEqualTo("WebVdu");
        assertThat(vdu.getOsContainerDesc()).isEmpty();
        assertThat(vdu.getIntCpd()).containsExactly("WebCp");
        assertThat(vdu.getLcmRealizationPath()).isEqualTo(LcmRealizationPath.MCIOP_CISM);

        assertThat(vnfd.getOsContainerDesc()).isEmpty();
        assertThat(vnfd.getSwImageDesc()).isEmpty();
        assertThat(vnfd.getMciopId()).containsExactly("web_mciop");

        VnfDf df = vnfd.getDf().get(0);
        assertThat(df.getFlavourId()).isEqualTo("simple");
        assertThat(df.getVduProfile()).hasSize(1);
        MciopProfile mciop = df.getMciopProfile().get(0);
        assertThat(mciop.getAssociatedVdu()).containsExactly("WebVdu");
        assertThat(mciop.getMciopParameterMappingRule())
                .contains("Artifacts/Scripts/lcm_param_mapping_rules.txt");

        assertThat(vnfd.getExtensions().flatMap(e -> e.getMciopArtifacts("web_mciop"))
                .flatMap(a -> a.getPackagePath()))
                .contains("Artifacts/Charts/simple-web-cnf-1.0.0.tgz");
    }

    @Test
    @DisplayName("OsContainer VDUs: SwImageDesc id is the Vdu.OsContainer node template name")
    void regularCnf() {
        Vnfd vnfd = parse(Fixtures.REGULAR_CNF);

        assertThat(vnfd.getVdu()).extracting(Vdu::getVduId).containsExactly("Vdu1", "Vdu2");
        assertThat(vnfd.getVdu()).extracting(Vdu::getLcmRealizationPath)
                .containsOnly(LcmRealizationPath.DIRECT_MCIO_CISM);
        assertThat(vnfd.getVdu().get(0).getOsContainerDesc()).containsExactly("Vdu1Container");
        assertThat(vnfd.getVdu().get(0).getIntCpd()).containsExactly("Vdu1Cp", "Vdu1InternalCp");

        assertThat(vnfd.getOsContainerDesc()).extracting(d -> d.getOsContainerDescId())
                .containsExactly("Vdu1Container", "Vdu2Container");
        assertThat(vnfd.getSwImageDesc()).extracting(d -> d.getId())
                .containsExactly("Vdu1Container", "Vdu2Container");

        assertThat(vnfd.getMciopId()).isEmpty();
        assertThat(vnfd.getDf().get(0).getMciopProfile()).isEmpty();
        assertThat(vnfd.getIntVirtualLinkDesc()).extracting(v -> v.getVirtualLinkDescId())
                .containsExactly("InternalVl");
        assertThat(vnfd.getVnfExtCpd()).extracting(c -> c.getCpdId()).containsExactly("Vdu1Cp");
    }

    @Test
    @DisplayName("no InstantiationLevels policy: one level is synthesised, IFA011 cl. 7.1.8.2.2")
    void synthesisedLevel() {
        VnfDf df = parse(Fixtures.SIMPLE_WEB_CNF).getDf().get(0);

        assertThat(df.getInstantiationLevel()).hasSize(1);
        assertThat(df.getInstantiationLevel().get(0).getLevelId()).isEqualTo("default");
        assertThat(df.getInstantiationLevel().get(0).isSynthesised()).isTrue();
        assertThat(df.getInstantiationLevel().get(0).getVduLevel())
                .extracting(l -> l.getVduId()).containsExactly("WebVdu");
        assertThat(df.getDefaultInstantiationLevelId()).isEmpty();
    }

    @Test
    @DisplayName("both descriptions on one VDU: realisation still follows the MCIOP")
    void hybridWebCnf() {
        Vnfd vnfd = parse(Fixtures.HYBRID_WEB_CNF);

        assertThat(vnfd.getVdu()).hasSize(1);
        assertThat(vnfd.getVdu().get(0).getOsContainerDesc()).containsExactly("WebContainer");
        assertThat(vnfd.getVdu().get(0).getLcmRealizationPath())
                .isEqualTo(LcmRealizationPath.MCIOP_CISM);
        assertThat(vnfd.getOsContainerDesc()).hasSize(1);
        assertThat(vnfd.getDf().get(0).getMciopProfile()).hasSize(1);
    }
}
