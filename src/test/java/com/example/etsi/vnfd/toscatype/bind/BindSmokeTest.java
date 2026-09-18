package com.example.etsi.vnfd.toscatype.bind;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.csar.CsarReader;
import com.example.etsi.vnfd.template.converter.YamlService;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BindSmokeTest {

    private Map<String, NfvNode> bind(String pkg) {
        ServiceToscaTemplate tst = new YamlService().parse(CsarReader.of(Fixtures.packageDir(pkg)));
        NodeBinder binder = new NodeBinder(new TypeHierarchy(tst.typeRegistry()), NodeTypes.ALL);
        Map<String, NfvNode> out = new LinkedHashMap<>();
        for (Map.Entry<String, NodeTemplate> e :
                tst.flavourTemplates().get(0).topologyTemplate().get().nodeTemplates().entrySet()) {
            binder.bind(e.getValue()).ifPresent(n -> out.put(e.getKey(), n));
        }
        return out;
    }

    @Test
    void bindsVendorDerivedVnfAndReadsDefaultsFromTheNodeType() {
        Vnf vnf = (Vnf) bind(Fixtures.SIMPLE_WEB_CNF).get("VNF");

        assertThat(vnf.getType()).isEqualTo("ExampleCorp.SimpleWebCnf.1_0");
        assertThat(vnf.getEtsiType()).isEqualTo("tosca.nodes.nfv.VNF");
        assertThat(vnf.getKey()).isEqualTo("VNF");
        assertThat(vnf.getProperties().getDescriptorId().resolved())
                .contains("8f6a2e2e-2c3a-4a7a-9a1a-6a3f2b6c9d10");
        assertThat(vnf.getProperties().getVnfmInfo()).containsExactly("GenericVnfm");
    }

    @Test
    void bindsVduPropertiesAndRequirements() {
        VduOsContainerDeployableUnit vdu =
                (VduOsContainerDeployableUnit) bind(Fixtures.REGULAR_CNF).get("Vdu1");

        assertThat(vdu.getProperties().getName().resolved()).contains("frontend-vdu");
        assertThat(vdu.getProperties().getVduProfile().getMinNumberOfInstances().resolved())
                .contains(1);
        assertThat(vdu.getProperties().getMcioIdentificationData().getName().resolved())
                .contains("frontend");
        assertThat(vdu.getRequirements().getContainer()).containsExactly("Vdu1Container");
    }

    @Test
    void keepsRepeatedRequirementKeys() {
        Mciop mciop = (Mciop) bind(Fixtures.SIMPLE_WEB_CNF).get("web_mciop");
        assertThat(mciop.getRequirements().getAssociatedVdu()).containsExactly("WebVdu");
        assertThat(mciop.getArtifacts().keySet())
                .containsExactly("web_helm_chart", "web_param_mapping_script", "web_param_mapping_rule");
    }

    @Test
    void bindsInheritedCpProperties() {
        VduCp cp = (VduCp) bind(Fixtures.SIMPLE_WEB_CNF).get("WebCp");
        assertThat(cp.getProperties().getLayerProtocols()).containsExactly("ipv4");
        assertThat(cp.getRequirements().getVirtualBinding()).containsExactly("WebVdu");
    }
}
