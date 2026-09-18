package com.example.etsi.vnfd.template;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.etsi.vnfd.csar.DirectoryCsarReader;
import com.example.etsi.vnfd.template.converter.YamlService;
import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.example.etsi.vnfd.validation.Findings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class YamlServiceTest {

    private static ServiceToscaTemplate parse(String packageName) {
        return new YamlService().parse(new DirectoryCsarReader(Fixtures.packageDir(packageName)), new Findings());
    }

    private static TopologyTemplate topologyOf(ServiceToscaTemplate tst) {
        return tst.flavourTemplates().get(0).topologyTemplate().orElseThrow(AssertionError::new);
    }

    @Test
    @DisplayName("SimpleWebCnf is recognised as the single-template design, not two-level")
    void singleTemplateDesignHasNoTopLevel() {
        ServiceToscaTemplate tst = parse(Fixtures.SIMPLE_WEB_CNF);

        // Other-Definitions names a type definitions file, not a deployment flavour template, so
        // only one template carries a topology and the package is a clause 6.11.3 design.
        assertThat(tst.isTwoLevelDesign()).isFalse();
        assertThat(tst.topLevelTemplate()).isEmpty();
        assertThat(tst.flavourTemplates()).hasSize(1);
        assertThat(tst.flavourTemplates().get(0).file())
                .isEqualTo("Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml");
    }

    @Test
    @DisplayName("node templates are read in declaration order")
    void readsNodeTemplatesInOrder() {
        TopologyTemplate topology = topologyOf(parse(Fixtures.SIMPLE_WEB_CNF));

        assertThat(topology.nodeTemplates().keySet())
                .containsExactly("VNF", "WebVdu", "WebCp", "web_mciop");
    }

    @Test
    @DisplayName("a vendor VNF node type resolves to tosca.nodes.nfv.VNF through the bundled catalogue")
    void vendorVnfTypeResolves() {
        ServiceToscaTemplate tst = parse(Fixtures.SIMPLE_WEB_CNF);
        TypeHierarchy hierarchy = new TypeHierarchy(tst.typeRegistry());
        NodeTemplate vnf = topologyOf(tst).nodeTemplates().get("VNF");

        assertThat(vnf.type()).isEqualTo("ExampleCorp.SimpleWebCnf.1_0");
        assertThat(hierarchy.isDerivedFrom(vnf.type(), EtsiTypes.VNF)).isTrue();
    }

    @Test
    @DisplayName("artifact paths are resolved against the package root")
    void resolvesArtifactPaths() {
        NodeTemplate mciop = topologyOf(parse(Fixtures.SIMPLE_WEB_CNF)).nodeTemplates().get("web_mciop");

        ArtifactDefinition chart = mciop.artifacts().get("web_helm_chart");
        assertThat(chart.type()).isEqualTo(EtsiTypes.ARTIFACT_HELM_CHART);
        assertThat(chart.file()).isEqualTo("../Artifacts/Charts/simple-web-cnf-1.0.0.tgz");
        assertThat(chart.resolvedFile()).contains("Artifacts/Charts/simple-web-cnf-1.0.0.tgz");

        ArtifactDefinition script = mciop.artifacts().get("web_param_mapping_script");
        assertThat(script.properties()).containsEntry("language", "bash");
    }

    @Test
    @DisplayName("every resolved artifact path exists in the package")
    void resolvedArtifactsExist() {
        for (String pkg : new String[]{Fixtures.SIMPLE_WEB_CNF, Fixtures.REGULAR_CNF,
                Fixtures.HYBRID_WEB_CNF}) {
            ServiceToscaTemplate tst = parse(pkg);
            java.nio.file.Path root = Fixtures.packageDir(pkg);
            for (NodeTemplate node : topologyOf(tst).nodeTemplates().values()) {
                for (ArtifactDefinition artifact : node.artifacts().values()) {
                    String resolved = artifact.resolvedFile().orElseThrow(AssertionError::new);
                    assertThat(java.nio.file.Files.exists(root.resolve(resolved)))
                            .as("%s: artifact %s.%s resolves to %s", pkg, node.name(),
                                    artifact.name(), resolved)
                            .isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("requirements keep declaration order and expose their targets")
    void readsRequirements() {
        TopologyTemplate topology = topologyOf(parse(Fixtures.REGULAR_CNF));

        NodeTemplate vdu2 = topology.nodeTemplates().get("Vdu2");
        assertThat(vdu2.requirements()).extracting(r -> r.name())
                .containsExactly(EtsiTypes.REQ_CONTAINER, EtsiTypes.REQ_VIRTUAL_STORAGE);
        assertThat(vdu2.requirementTargets(EtsiTypes.REQ_CONTAINER)).containsExactly("Vdu2Container");
        assertThat(vdu2.requirementTargets(EtsiTypes.REQ_VIRTUAL_STORAGE)).containsExactly("Vdu2Storage");

        NodeTemplate vdu1Cp = topology.nodeTemplates().get("Vdu1InternalCp");
        assertThat(vdu1Cp.requirementTargets(EtsiTypes.REQ_VIRTUAL_BINDING)).containsExactly("Vdu1");
        assertThat(vdu1Cp.requirementTargets(EtsiTypes.REQ_VIRTUAL_LINK)).containsExactly("InternalVl");
    }

    @Test
    @DisplayName("substitution mappings expose the connection point that becomes external")
    void readsSubstitutionMappings() {
        TopologyTemplate topology = topologyOf(parse(Fixtures.SIMPLE_WEB_CNF));

        assertThat(topology.substitutionMappings()).isPresent();
        assertThat(topology.substitutionMappings().get().nodeType())
                .isEqualTo("ExampleCorp.SimpleWebCnf.1_0");
        assertThat(topology.substitutionMappings().get().requirements())
                .containsKey("virtual_link_mgmt");
        assertThat(topology.substitutionMappings().get().requirements()
                .get("virtual_link_mgmt").nodeTemplateName()).isEqualTo("WebCp");
        assertThat(topology.substitutionMappings().get().requirements()
                .get("virtual_link_mgmt").targetName()).isEqualTo("virtual_link");
        assertThat(topology.substitutionMappings().get().exposedNodeTemplates())
                .containsExactly("WebCp");
    }

    @Test
    @DisplayName("an operation declaring only inputs has no implementation, so yields no LCM script")
    void operationWithoutImplementation() {
        NodeTemplate vnf = topologyOf(parse(Fixtures.SIMPLE_WEB_CNF)).nodeTemplates().get("VNF");

        assertThat(vnf.interfaces()).containsKey("Vnflcm");
        assertThat(vnf.interfaces().get("Vnflcm").operations()).containsKey("instantiate");
        assertThat(vnf.interfaces().get("Vnflcm").operations().get("instantiate").implementation())
                .as("the bundled packages declare additional_parameters but no implementation")
                .isEmpty();
        assertThat(vnf.interfaces().get("Vnflcm").operations().get("instantiate")
                .additionalParameters()).isPresent();
    }

    @Test
    @DisplayName("the hybrid package declares both a container requirement and an Mciop on one VDU")
    void hybridPackageShape() {
        TopologyTemplate topology = topologyOf(parse(Fixtures.HYBRID_WEB_CNF));

        assertThat(topology.nodeTemplates().get("WebVdu")
                .requirementTargets(EtsiTypes.REQ_CONTAINER)).containsExactly("WebContainer");
        assertThat(topology.nodeTemplates().get("web_mciop")
                .requirementTargets(EtsiTypes.REQ_ASSOCIATED_VDU)).containsExactly("WebVdu");
    }
}
