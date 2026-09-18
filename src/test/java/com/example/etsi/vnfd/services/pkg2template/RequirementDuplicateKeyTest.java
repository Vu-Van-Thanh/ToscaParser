package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.RequirementAssignment;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the single most damaging way to model requirements wrongly.
 *
 * <p>TOSCA writes requirements as a sequence of single-entry maps, and the same name may appear
 * more than once. SOL001 V5.4.1 Annex A.23 depends on it: its {@code mciop1} node declares
 * {@code associatedVdu} twice, once for each VDU the Helm chart deploys. Keying requirements by
 * name would keep only the last, silently dropping a VDU from the MCIOP association - and with it
 * the structural signal that distinguishes a hybrid deployment flavour.
 *
 * <p>No bundled example package exercises this, so it is tested directly.
 */
class RequirementDuplicateKeyTest {

    private static Map<String, NodeTemplate> parseNodeTemplates(String yaml) {
        Map<String, Object> document = PackageReader.loadMapping(
                "test.yaml", yaml.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> topology = Yamls.map(document.get("topology_template"));
        return TemplateReader.readNodeTemplates(topology.get("node_templates"), "test.yaml");
    }

    @Test
    @DisplayName("an Mciop declaring associatedVdu twice keeps both targets, in order")
    void keepsRepeatedRequirementName() {
        // Shape taken from SOL001 V5.4.1 Annex A.23, where mciop1 associates Vdu1 and Vdu2.
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    mciop1:",
                "      type: tosca.nodes.nfv.Mciop",
                "      requirements:",
                "        - associatedVdu: Vdu1",
                "        - associatedVdu: Vdu2");

        NodeTemplate mciop = parseNodeTemplates(yaml).get("mciop1");

        assertThat(mciop.requirements()).hasSize(2);
        assertThat(mciop.requirementTargets("associatedVdu")).containsExactly("Vdu1", "Vdu2");
    }

    @Test
    @DisplayName("declaration order is preserved across mixed requirement names")
    void preservesDeclarationOrder() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    Vdu2:",
                "      type: tosca.nodes.nfv.Vdu.OsContainerDeployableUnit",
                "      requirements:",
                "        - container: FirstContainer",
                "        - virtual_storage: Storage",
                "        - container: SecondContainer");

        NodeTemplate vdu = parseNodeTemplates(yaml).get("Vdu2");

        assertThat(vdu.requirements()).extracting(RequirementAssignment::name)
                .containsExactly("container", "virtual_storage", "container");
        assertThat(vdu.requirements()).extracting(RequirementAssignment::declarationIndex)
                .containsExactly(0, 1, 2);
        // SOL001 clause 6.8.13.4 gives the container requirement occurrences [0, UNBOUNDED],
        // so more than one container on a VDU is legitimate and both must survive.
        assertThat(vdu.requirementTargets("container"))
                .containsExactly("FirstContainer", "SecondContainer");
    }

    @Test
    @DisplayName("the extended requirement form parses alongside the short form")
    void readsExtendedForm() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    WebCp:",
                "      type: tosca.nodes.nfv.VduCp",
                "      requirements:",
                "        - virtual_binding: WebVdu",
                "        - virtual_link:",
                "            node: InternalVl",
                "            capability: tosca.capabilities.nfv.VirtualLinkable",
                "            relationship: tosca.relationships.nfv.VirtualLinksTo");

        NodeTemplate cp = parseNodeTemplates(yaml).get("WebCp");

        assertThat(cp.requirementTargets("virtual_binding")).containsExactly("WebVdu");
        assertThat(cp.requirementTargets("virtual_link")).containsExactly("InternalVl");
        RequirementAssignment link = cp.requirements().get(1);
        assertThat(link.capability()).contains("tosca.capabilities.nfv.VirtualLinkable");
        assertThat(link.relationship().orElseThrow(AssertionError::new).type())
                .isEqualTo("tosca.relationships.nfv.VirtualLinksTo");
    }
}
