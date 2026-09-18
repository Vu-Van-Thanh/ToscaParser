package com.example.etsi.vnfd.toscatype.bind;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.csar.DirectoryCsarReader;
import com.example.etsi.vnfd.template.converter.YamlService;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Every property name a node class binds must be one its ETSI type actually declares.
 *
 * <p>Binding by annotation moves a whole class of mistake from compile time to run time: a
 * misspelled {@code @JsonProperty("vdu_profil")} does not fail to compile and does not throw - the
 * field simply stays null, and the VNFD comes out quietly incomplete. This is the check that brings
 * it back, and it is stricter than a reader would be, because it compares against the type
 * definitions rather than against whatever the reader happened to ask for.
 */
class DeclaredPropertyNamesTest {

    /** Structural keynames of a node template, not properties of its type. */
    private static final Set<String> STRUCTURAL =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "properties", "requirements", "capabilities", "artifacts", "interfaces"));

    @Test
    @DisplayName("every bound property name is declared by the ETSI type")
    void everyBoundPropertyNameIsDeclared() {
        ServiceToscaTemplate tst = new YamlService()
                .parse(new DirectoryCsarReader(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)));
        TypeHierarchy hierarchy = new TypeHierarchy(tst.typeRegistry());

        List<String> unknown = new ArrayList<>();
        for (Class<? extends NfvNode> nodeClass : NodeTypes.ALL) {
            String etsiType = nodeClass.getAnnotation(EtsiNodeType.class).value();
            // A class also holds the properties of the types deriving from it: VduSubCp adds three
            // to VduCp, and the binder fills the field as declared on the parent.
            Set<String> declared = new java.util.HashSet<>();
            for (Class<? extends NfvNode> candidate : NodeTypes.ALL) {
                String candidateType = candidate.getAnnotation(EtsiNodeType.class).value();
                if (hierarchy.isDerivedFrom(candidateType, etsiType)) {
                    declared.addAll(hierarchy.effectiveProperties(candidateType).keySet());
                }
            }
            for (Class<?> nested : nodeClass.getDeclaredClasses()) {
                if (!nested.getSimpleName().endsWith("Properties")) {
                    continue;
                }
                collectUnknown(nested, etsiType, declared, unknown);
            }
        }

        assertThat(unknown)
                .as("@JsonProperty names with no matching property in the type definitions")
                .isEmpty();
    }

    private void collectUnknown(Class<?> properties, String etsiType, Set<String> declared,
            List<String> unknown) {
        for (Class<?> type = properties; type != null && type != Object.class;
                type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                if (annotation == null || STRUCTURAL.contains(annotation.value())) {
                    continue;
                }
                if (!declared.contains(annotation.value())) {
                    unknown.add(etsiType + "." + annotation.value()
                            + " (" + type.getName() + "#" + field.getName() + ")");
                }
            }
        }
    }
}
