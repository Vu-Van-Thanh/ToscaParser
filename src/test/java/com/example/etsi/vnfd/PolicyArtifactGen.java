package com.example.etsi.vnfd;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.services.pkg2template.PackageReader;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/** Generates the SOL001 policy and artifact classes from the ETSI type definitions. */
class PolicyArtifactGen {

    /** ETSI type, Java class name, EtsiTypes constant. */
    private static final String[][] POLICIES = {
        {"tosca.policies.nfv.InstantiationLevels", "InstantiationLevels", "POLICY_INSTANTIATION_LEVELS"},
        {"tosca.policies.nfv.VduInstantiationLevels", "VduInstantiationLevels", "POLICY_VDU_INSTANTIATION_LEVELS"},
        {"tosca.policies.nfv.VirtualLinkInstantiationLevels", "VirtualLinkInstantiationLevels", "POLICY_VL_INSTANTIATION_LEVELS"},
        {"tosca.policies.nfv.ScalingAspects", "ScalingAspects", "POLICY_SCALING_ASPECTS"},
        {"tosca.policies.nfv.VduScalingAspectDeltas", "VduScalingAspectDeltas", "POLICY_VDU_SCALING_ASPECT_DELTAS"},
        {"tosca.policies.nfv.VduInitialDelta", "VduInitialDelta", "POLICY_VDU_INITIAL_DELTA"},
        {"tosca.policies.nfv.AffinityRule", "AffinityRule", "POLICY_AFFINITY_RULE"},
        {"tosca.policies.nfv.AntiAffinityRule", "AntiAffinityRule", "POLICY_ANTI_AFFINITY_RULE"},
        {"tosca.policies.nfv.SecurityGroupRule", "SecurityGroupRule", "POLICY_SECURITY_GROUP_RULE"},
        {"tosca.policies.nfv.VnfPackageChange", "VnfPackageChange", "POLICY_VNF_PACKAGE_CHANGE"},
    };

    private static final String[][] ARTIFACTS = {
        {"tosca.artifacts.nfv.SwImage", "SwImage", "ARTIFACT_SW_IMAGE"},
        {"tosca.artifacts.nfv.HelmChart", "HelmChart", "ARTIFACT_HELM_CHART"},
        {"tosca.artifacts.nfv.HelmParamMappingScript", "HelmParamMappingScript", "ARTIFACT_HELM_PARAM_MAPPING_SCRIPT"},
        {"tosca.artifacts.nfv.HelmParamMappingRule", "HelmParamMappingRule", "ARTIFACT_HELM_PARAM_MAPPING_RULE"},
    };

    @Test
    void generate() throws IOException {
        ServiceToscaTemplate tst = new PackageReader(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)).parse();
        TypeReader.Hierarchy h = new TypeReader.Hierarchy(tst.typeRegistry());

        Path pol = Paths.get("target/policygen");
        Path art = Paths.get("target/artifactgen");
        Files.createDirectories(pol);
        Files.createDirectories(art);

        for (String[] t : POLICIES) {
            Files.write(pol.resolve(t[1] + ".java"),
                    render(h, t, "com.example.etsi.vnfd.toscatype.policy", "NfvPolicy")
                            .getBytes(StandardCharsets.UTF_8));
        }
        for (String[] t : ARTIFACTS) {
            Files.write(art.resolve(t[1] + ".java"),
                    render(h, t, "com.example.etsi.vnfd.toscatype.artifact", "NfvArtifact")
                            .getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("GENERATED " + (POLICIES.length + ARTIFACTS.length));
    }

    private String render(TypeReader.Hierarchy h, String[] t, String pkg, String base) {
        String etsi = t[0];
        Map<String, PropertyDef> props = new LinkedHashMap<>(h.effectivePropertiesOfAnyType(etsi));

        Set<String> imports = new TreeSet<>(Arrays.asList(
                "com.example.etsi.vnfd.toscatype.node.EtsiNodeType",
                "com.example.etsi.vnfd.typedef.EtsiTypes",
                "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
                "com.fasterxml.jackson.annotation.JsonProperty",
                "lombok.Getter", "lombok.NoArgsConstructor", "lombok.Setter"));

        StringBuilder fields = new StringBuilder();
        for (Map.Entry<String, PropertyDef> e : props.entrySet()) {
            PropertyDef d = e.getValue();
            String doc = d.description().map(NodeGen::oneLine).orElse("");
            if (d.isRequired()) {
                doc = doc.isEmpty() ? "required: true." : doc + " required: true.";
            }
            if (!doc.isEmpty()) {
                fields.append("        /** ").append(doc).append(" */\n");
            }
            fields.append("        @JsonProperty(\"").append(e.getKey()).append("\")\n")
                  .append("        private ").append(type(d, imports)).append(' ')
                  .append(camel(e.getKey())).append(";\n\n");
        }

        StringBuilder b = new StringBuilder();
        b.append("package ").append(pkg).append(";\n\n");
        for (String i : imports) {
            b.append("import ").append(i).append(";\n");
        }
        b.append("\n").append(NodeGen.doc(etsi))
         .append("@Getter\n@Setter\n@NoArgsConstructor\n@JsonIgnoreProperties(ignoreUnknown = true)\n")
         .append("@EtsiNodeType(EtsiTypes.").append(t[2]).append(")\n")
         .append("public class ").append(t[1]).append(" extends ").append(base).append(" {\n\n");
        if (fields.length() > 0) {
            b.append("    @JsonProperty(\"properties\")\n    private Properties properties;\n\n")
             .append("    @Getter\n    @Setter\n    @NoArgsConstructor\n")
             .append("    @JsonIgnoreProperties(ignoreUnknown = true)\n")
             .append("    public static class Properties {\n\n").append(fields).append("    }\n\n");
        }
        b.append("}\n");
        return b.toString();
    }

    private String type(PropertyDef d, Set<String> imports) {
        String t = d.type() == null ? "string" : d.type();
        if (t.startsWith("tosca.datatypes.nfv.")) {
            String s = t.substring(t.lastIndexOf('.') + 1);
            imports.add("com.example.etsi.vnfd.toscatype.data." + s);
            return s;
        }
        if ("string".equals(t) || "timestamp".equals(t) || "version".equals(t)) {
            imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
            return "PropertyValue<String>";
        }
        if ("integer".equals(t)) {
            imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
            return "PropertyValue<Integer>";
        }
        if ("boolean".equals(t)) {
            imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
            return "PropertyValue<Boolean>";
        }
        if (t.startsWith("scalar-unit.")) {
            imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
            imports.add("com.example.etsi.vnfd.template.value.Quantity");
            return "PropertyValue<Quantity>";
        }
        if ("list".equals(t)) {
            imports.add("java.util.List");
            return "List<" + inner(d, imports) + ">";
        }
        if ("map".equals(t)) {
            imports.add("java.util.Map");
            return "Map<String, " + inner(d, imports) + ">";
        }
        imports.add("java.util.Map");
        return "Map<String, Object>";
    }

    private String inner(PropertyDef d, Set<String> imports) {
        PropertyDef entry = d.entrySchema().orElse(null);
        if (entry == null) {
            return "Object";
        }
        String t = type(entry, imports);
        return t.startsWith("PropertyValue<") ? t.substring(14, t.length() - 1) : t;
    }

    private static String camel(String snake) {
        StringBuilder b = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_' || c == '-') {
                up = true;
                continue;
            }
            b.append(up ? Character.toUpperCase(c) : c);
            up = false;
        }
        return b.toString();
    }
}
