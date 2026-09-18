package com.example.etsi.vnfd.template.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.template.InterfaceAssignment;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.utils.ToscaYamlLoader;
import com.example.etsi.vnfd.utils.Yamls;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Both interface grammars must parse.
 *
 * <p>SOL004 V5.1.1 clause 4.1.1 permits a CSAR to follow TOSCA Simple Profile YAML v1.1 or v1.3,
 * and its clause 4.1.3.1 example declares {@code tosca_simple_yaml_1_2}. TOSCA 1.3 nests operations
 * under an {@code operations} keyname; earlier versions put them directly under the interface.
 */
class InterfaceGrammarTest {

    private static Map<String, NodeTemplate> parse(String yaml) {
        Map<String, Object> document = ToscaYamlLoader.loadMapping(
                "test.yaml", yaml.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> topology = Yamls.map(document.get("topology_template"));
        return TopologyConverter.readNodeTemplates(topology.get("node_templates"), "test.yaml");
    }

    @Test
    @DisplayName("TOSCA 1.3 grammar: operations nested under an operations keyname")
    void readsModernGrammar() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    VNF:",
                "      type: MyCompany.ExampleVnf",
                "      interfaces:",
                "        Vnflcm:",
                "          type: tosca.interfaces.nfv.Vnflcm",
                "          operations:",
                "            instantiate:",
                "              implementation: instantiate.workbook.mistral.yaml",
                "          notifications:",
                "            change_current_package_notification:",
                "              description: on package change");

        InterfaceAssignment vnflcm = parse(yaml).get("VNF").interfaces().get("Vnflcm");

        assertThat(vnflcm.grammar()).isEqualTo(InterfaceAssignment.Grammar.TOSCA_1_3);
        assertThat(vnflcm.operations()).containsOnlyKeys("instantiate");
        assertThat(vnflcm.operations().get("instantiate").implementation()
                .orElseThrow(AssertionError::new).primary())
                .isEqualTo("instantiate.workbook.mistral.yaml");
        assertThat(vnflcm.notifications()).containsOnlyKeys("change_current_package_notification");
    }

    @Test
    @DisplayName("legacy grammar: operations directly under the interface")
    void readsLegacyGrammar() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    VNF:",
                "      type: MyCompany.ExampleVnf",
                "      interfaces:",
                "        Vnflcm:",
                "          type: tosca.interfaces.nfv.Vnflcm",
                "          instantiate:",
                "            implementation: instantiate.sh",
                "          terminate:",
                "            implementation: terminate.sh");

        InterfaceAssignment vnflcm = parse(yaml).get("VNF").interfaces().get("Vnflcm");

        assertThat(vnflcm.grammar()).isEqualTo(InterfaceAssignment.Grammar.LEGACY);
        assertThat(vnflcm.operations()).containsOnlyKeys("instantiate", "terminate");
        assertThat(vnflcm.operations().get("terminate").implementation()
                .orElseThrow(AssertionError::new).primary()).isEqualTo("terminate.sh");
    }

    @Test
    @DisplayName("legacy grammar does not mistake type or inputs for operation names")
    void legacyGrammarSkipsReservedKeynames() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    VNF:",
                "      type: MyCompany.ExampleVnf",
                "      interfaces:",
                "        Vnflcm:",
                "          type: tosca.interfaces.nfv.Vnflcm",
                "          inputs:",
                "            shared: value",
                "          instantiate:",
                "            implementation: instantiate.sh");

        InterfaceAssignment vnflcm = parse(yaml).get("VNF").interfaces().get("Vnflcm");

        assertThat(vnflcm.operations()).containsOnlyKeys("instantiate");
        assertThat(vnflcm.inputs()).containsEntry("shared", "value");
        assertThat(vnflcm.type()).contains("tosca.interfaces.nfv.Vnflcm");
    }

    @Test
    @DisplayName("the shorthand implementation form is read as an implementation, not as inputs")
    void readsShorthandImplementation() {
        String yaml = String.join("\n",
                "topology_template:",
                "  node_templates:",
                "    VNF:",
                "      type: MyCompany.ExampleVnf",
                "      interfaces:",
                "        Vnflcm:",
                "          operations:",
                "            terminate: terminate.sh");

        InterfaceAssignment vnflcm = parse(yaml).get("VNF").interfaces().get("Vnflcm");

        assertThat(vnflcm.operations().get("terminate").implementation()
                .orElseThrow(AssertionError::new).primary()).isEqualTo("terminate.sh");
    }
}
