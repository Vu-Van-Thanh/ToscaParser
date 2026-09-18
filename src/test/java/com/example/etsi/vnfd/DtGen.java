package com.example.etsi.vnfd;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.csar.CsarReader;
import com.example.etsi.vnfd.template.converter.YamlService;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.toscatype.bind.NodeTypes;
import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Generates one POJO per ETSI datatype reachable from the CNF scope. Not part of the build. */
class DtGen {

    private static final String OUT = System.getProperty("dtgen.out", "target/dtgen");
    private static final String PKG = "com.example.etsi.vnfd.toscatype.data";

    @Test
    void generate() throws IOException {
        ServiceToscaTemplate tst = new YamlService()
                .parse(CsarReader.of(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)));
        TypeHierarchy h = new TypeHierarchy(tst.typeRegistry());

        Set<String> seeds = new LinkedHashSet<>();
        NodeTypes.ALL.forEach(c -> seeds.add(c.getAnnotation(EtsiNodeType.class).value()));
        seeds.addAll(Arrays.asList(
            "tosca.policies.nfv.InstantiationLevels","tosca.policies.nfv.VduInstantiationLevels",
            "tosca.policies.nfv.VirtualLinkInstantiationLevels","tosca.policies.nfv.ScalingAspects",
            "tosca.policies.nfv.VduScalingAspectDeltas","tosca.policies.nfv.VduInitialDelta",
            "tosca.policies.nfv.AffinityRule","tosca.policies.nfv.AntiAffinityRule",
            "tosca.policies.nfv.SecurityGroupRule","tosca.policies.nfv.VnfPackageChange",
            "tosca.artifacts.nfv.SwImage","tosca.artifacts.nfv.HelmChart",
            "tosca.artifacts.nfv.HelmParamMappingScript","tosca.artifacts.nfv.HelmParamMappingRule"));

        Set<String> dts = new TreeSet<>();
        Deque<String> todo = new ArrayDeque<>(seeds);
        Set<String> seen = new HashSet<>();
        while (!todo.isEmpty()) {
            String t = todo.poll();
            if (!seen.add(t)) continue;
            for (PropertyDef d : h.effectivePropertiesOfAnyType(t).values()) collect(d, dts, todo);
        }

        Path dir = Paths.get(OUT);
        Files.createDirectories(dir);
        for (String dt : dts) {
            String simple = dt.substring(dt.lastIndexOf('.') + 1);
            Files.write(dir.resolve(simple + ".java"),
                    render(simple, dt, h).getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("GENERATED " + dts.size() + " -> " + dir.toAbsolutePath());
    }

    private void collect(PropertyDef d, Set<String> found, Deque<String> todo) {
        if (d.type() != null && d.type().startsWith("tosca.datatypes.nfv.")) {
            found.add(d.type()); todo.add(d.type());
        }
        d.entrySchema().ifPresent(e -> collect(e, found, todo));
        d.keySchema().ifPresent(e -> collect(e, found, todo));
    }

    private String render(String simple, String fqn, TypeHierarchy h) {
        Map<String, PropertyDef> props = h.effectivePropertiesOfAnyType(fqn);
        StringBuilder b = new StringBuilder();
        Set<String> imports = new TreeSet<>();
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, PropertyDef> e : props.entrySet()) {
            PropertyDef d = e.getValue();
            String java = javaType(d, imports);
            String field = camel(e.getKey());
            String doc = d.description().map(DtGen::oneLine).orElse("");
            if (d.isRequired()) {
                doc = doc.isEmpty() ? "required: true." : doc + " required: true.";
            }
            if (!doc.isEmpty()) {
                body.append("    /** ").append(doc).append(" */\n");
            }
            body.append("    @JsonProperty(\"").append(e.getKey()).append("\")\n");
            body.append("    private ").append(java).append(' ').append(field).append(";\n\n");
        }
        imports.add("com.fasterxml.jackson.annotation.JsonIgnoreProperties");
        imports.add("com.fasterxml.jackson.annotation.JsonProperty");
        imports.add("lombok.Getter");
        imports.add("lombok.NoArgsConstructor");
        imports.add("lombok.Setter");

        b.append("package ").append(PKG).append(";\n\n");
        imports.forEach(i -> b.append("import ").append(i).append(";\n"));
        b.append("\n").append(NodeGen.doc(fqn));
        b.append("@Getter\n@Setter\n@NoArgsConstructor\n@JsonIgnoreProperties(ignoreUnknown = true)\n");
        b.append("public class ").append(simple).append(" {\n\n");
        b.append(body);
        b.append("}\n");
        return b.toString();
    }

    private String javaType(PropertyDef d, Set<String> imports) {
        String t = d.type() == null ? "string" : d.type();
        if (t.startsWith("tosca.datatypes.nfv.")) return t.substring(t.lastIndexOf('.') + 1);
        switch (t) {
            case "string": case "timestamp": case "version":
                imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
                return "PropertyValue<String>";
            case "integer":
                imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
                return "PropertyValue<Integer>";
            case "float":
                imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
                return "PropertyValue<Double>";
            case "boolean":
                imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
                return "PropertyValue<Boolean>";
            case "scalar-unit.size": case "scalar-unit.time": case "scalar-unit.frequency":
            case "scalar-unit.bitrate":
                imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
                imports.add("com.example.etsi.vnfd.template.value.Quantity");
                return "PropertyValue<Quantity>";
            case "range":
                imports.add("java.util.List"); return "List<Object>";
            case "list":
                imports.add("java.util.List");
                return "List<" + boxed(d.entrySchema().orElse(null), imports) + ">";
            case "map":
                imports.add("java.util.Map");
                return "Map<String, " + boxed(d.entrySchema().orElse(null), imports) + ">";
            default:
                imports.add("java.util.Map"); return "Map<String, Object>";
        }
    }

    private String boxed(PropertyDef entry, Set<String> imports) {
        if (entry == null) return "Object";
        String t = javaType(entry, imports);
        return t.startsWith("PropertyValue<") ? t.substring(14, t.length() - 1) : t;
    }

    private static String camel(String snake) {
        StringBuilder b = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_' || c == '-') { up = true; continue; }
            b.append(up ? Character.toUpperCase(c) : c); up = false;
        }
        return b.toString();
    }

    static String oneLine(String s) {
        return s.replaceAll("\\s+", " ").trim().replace("*/", "* /");
    }
}
