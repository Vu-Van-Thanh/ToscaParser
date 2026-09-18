package com.example.etsi.vnfd;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.services.pkg2template.PackageReader;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.typedef.CapabilityDefinition;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.RequirementDefinition;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Generates the SOL001 node classes from the ETSI type definitions. Not part of the build. */
public class NodeGen {

    private static final String OUT = "target/nodegen";
    private static final String PKG = "com.example.etsi.vnfd.toscatype.node";
    private static final String DT = "com.example.etsi.vnfd.toscatype.data";

    /** ETSI type, Java class name, EtsiTypes constant. */
    private static final String[][] NODES = {
        {"tosca.nodes.nfv.VNF", "Vnf", "VNF"},
        {"tosca.nodes.nfv.Vdu.OsContainerDeployableUnit", "VduOsContainerDeployableUnit", "VDU_OS_CONTAINER_DEPLOYABLE_UNIT"},
        {"tosca.nodes.nfv.Vdu.OsContainer", "VduOsContainer", "VDU_OS_CONTAINER"},
        {"tosca.nodes.nfv.Mciop", "Mciop", "MCIOP"},
        {"tosca.nodes.nfv.Cp", "Cp", "CP"},
        {"tosca.nodes.nfv.VduCp", "VduCp", "VDU_CP"},
        {"tosca.nodes.nfv.VduSubCp", "VduSubCp", "VDU_SUB_CP"},
        {"tosca.nodes.nfv.VnfExtCp", "VnfExtCp", "VNF_EXT_CP"},
        {"tosca.nodes.nfv.VipCp", "VipCp", "VIP_CP"},
        {"tosca.nodes.nfv.VirtualCp", "VirtualCp", "VIRTUAL_CP"},
        {"tosca.nodes.nfv.VnfVirtualLink", "VnfVirtualLink", "VNF_VIRTUAL_LINK"},
        {"tosca.nodes.nfv.Vdu.VirtualBlockStorage", "VduVirtualBlockStorage", "VDU_VIRTUAL_BLOCK_STORAGE"},
        {"tosca.nodes.nfv.Vdu.VirtualObjectStorage", "VduVirtualObjectStorage", "VDU_VIRTUAL_OBJECT_STORAGE"},
        {"tosca.nodes.nfv.Vdu.VirtualFileStorage", "VduVirtualFileStorage", "VDU_VIRTUAL_FILE_STORAGE"},
        {"tosca.nodes.nfv.DeployableModule", "DeployableModule", "DEPLOYABLE_MODULE"},
        {"tosca.nodes.nfv.Certificate", "Certificate", "CERTIFICATE"},
    };

    private TypeReader.Hierarchy h;
    private final Map<String, String> javaName = new LinkedHashMap<>();
    private final Map<String, String> constant = new LinkedHashMap<>();

    @Test
    void generate() throws IOException {
        ServiceToscaTemplate tst = new PackageReader(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF)).parse();
        h = new TypeReader.Hierarchy(tst.typeRegistry());
        for (String[] n : NODES) {
            javaName.put(n[0], n[1]);
            constant.put(n[0], n[2]);
        }
        Path dir = Paths.get(OUT);
        Files.createDirectories(dir);
        for (String[] n : NODES) {
            Files.write(dir.resolve(n[1] + ".java"),
                    render(n[0], n[1]).getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("GENERATED " + NODES.length + " -> " + dir.toAbsolutePath());
    }

    private String nearestParent(String etsi) {
        String best = null;
        for (String candidate : javaName.keySet()) {
            if (candidate.equals(etsi) || !h.isDerivedFrom(etsi, candidate)) {
                continue;
            }
            if (best == null || h.isDerivedFrom(candidate, best)) {
                best = candidate;
            }
        }
        return best;
    }

    private String render(String etsi, String simple) {
        String parentEtsi = nearestParent(etsi);
        String parent = parentEtsi == null ? "NfvNode" : javaName.get(parentEtsi);

        Map<String, PropertyDef> props = minus(h.effectiveProperties(etsi),
                parentEtsi == null ? null : h.effectiveProperties(parentEtsi));
        Map<String, RequirementDefinition> reqs = reqs(etsi, parentEtsi);
        Map<String, CapabilityDefinition> caps = minus(h.effectiveCapabilities(etsi),
                parentEtsi == null ? null : h.effectiveCapabilities(parentEtsi));

        Set<String> imports = new TreeSet<>(Arrays.asList(
                "com.example.etsi.vnfd.typedef.EtsiTypes",
                "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
                "com.fasterxml.jackson.annotation.JsonProperty",
                "lombok.Getter", "lombok.NoArgsConstructor", "lombok.Setter"));

        StringBuilder body = new StringBuilder();
        nested(body, "Properties", "properties", propLines(props, imports), parent,
                !props.isEmpty(), parentEtsi != null && !h.effectiveProperties(parentEtsi).isEmpty());
        nested(body, "Requirements", "requirements", reqLines(reqs, imports), parent,
                !reqs.isEmpty(), parentEtsi != null && !reqs(parentEtsi, null).isEmpty());
        nested(body, "Capabilities", "capabilities", capLines(caps, imports), parent,
                !caps.isEmpty(), parentEtsi != null && !h.effectiveCapabilities(parentEtsi).isEmpty());

        StringBuilder b = new StringBuilder();
        b.append("package ").append(PKG).append(";\n\n");
        for (String i : imports) {
            b.append("import ").append(i).append(";\n");
        }
        b.append("\n").append(doc(etsi))
         .append("@Getter\n@Setter\n@NoArgsConstructor\n@JsonIgnoreProperties(ignoreUnknown = true)\n")
         .append("@EtsiNodeType(EtsiTypes.").append(constant.get(etsi)).append(")\n")
         .append("public class ").append(simple).append(" extends ").append(parent).append(" {\n\n")
         .append(body).append("}\n");
        return b.toString();
    }

    /** Javadoc built from the specification text; the type definitions carry no clause numbers. */
    public static String doc(String toscaType) {
        StringBuilder b = new StringBuilder("/**\n");
        boolean firstPara = true;
        for (String line : Sol001Doc.javadoc(toscaType)) {
            if (line.isEmpty()) {
                b.append(" *\n");
                continue;
            }
            b.append(" * ").append(firstPara ? "" : "<p>").append(line).append("\n");
            firstPara = false;
        }
        b.append(" *\n * <p>Field names, types and cardinalities come from the ETSI type definitions;\n")
         .append(" * the text above is quoted from the specification itself.\n */\n");
        return b.toString();
    }

    private <T> Map<String, T> minus(Map<String, T> self, Map<String, T> parent) {
        Map<String, T> out = new LinkedHashMap<>(self);
        if (parent != null) {
            parent.keySet().forEach(out::remove);
        }
        return out;
    }

    private Map<String, RequirementDefinition> reqs(String etsi, String parentEtsi) {
        Map<String, RequirementDefinition> out = new LinkedHashMap<>();
        for (RequirementDefinition r : h.effectiveRequirements(etsi)) {
            out.put(r.name(), r);
        }
        if (parentEtsi != null) {
            for (RequirementDefinition r : h.effectiveRequirements(parentEtsi)) {
                out.remove(r.name());
            }
        }
        return out;
    }

    private void nested(StringBuilder b, String cls, String field, String lines, String parent,
            boolean has, boolean parentHas) {
        if (!has) {
            return;
        }
        b.append("    @JsonProperty(\"").append(field).append("\")\n")
         .append("    private ").append(cls).append(' ').append(field).append(";\n\n")
         .append("    @Getter\n    @Setter\n    @NoArgsConstructor\n")
         .append("    @JsonIgnoreProperties(ignoreUnknown = true)\n")
         .append("    public static class ").append(cls);
        if (parentHas) {
            b.append(" extends ").append(parent).append('.').append(cls);
        }
        b.append(" {\n\n").append(lines).append("    }\n\n");
    }

    private String propLines(Map<String, PropertyDef> props, Set<String> imports) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, PropertyDef> e : props.entrySet()) {
            PropertyDef d = e.getValue();
            String doc = d.description().map(NodeGen::oneLine).orElse("");
            if (d.isRequired()) {
                doc = doc.isEmpty() ? "required: true." : doc + " required: true.";
            }
            if (!doc.isEmpty()) {
                b.append("        /** ").append(doc).append(" */\n");
            }
            b.append("        @JsonProperty(\"").append(e.getKey()).append("\")\n")
             .append("        private ").append(javaType(d, imports)).append(' ')
             .append(camel(e.getKey())).append(";\n\n");
        }
        return b.toString();
    }

    private String reqLines(Map<String, RequirementDefinition> reqs, Set<String> imports) {
        imports.add("java.util.List");
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, RequirementDefinition> e : reqs.entrySet()) {
            RequirementDefinition r = e.getValue();
            b.append("        /** capability ").append(r.capability().orElse("-"))
             .append(", occurrences ").append(r.occurrences().map(Object::toString).orElse("[1, 1]"))
             .append(". A list: TOSCA allows the same requirement name more than once. */\n")
             .append("        @JsonProperty(\"").append(e.getKey()).append("\")\n")
             .append("        private List<String> ").append(camel(e.getKey())).append(";\n\n");
        }
        return b.toString();
    }

    private String capLines(Map<String, CapabilityDefinition> caps, Set<String> imports) {
        imports.add("java.util.Map");
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, CapabilityDefinition> e : caps.entrySet()) {
            CapabilityDefinition c = e.getValue();
            b.append("        /** type ").append(c.type()).append(", occurrences ")
             .append(c.occurrences().map(Object::toString).orElse("[1, 1]")).append(". */\n")
             .append("        @JsonProperty(\"").append(e.getKey()).append("\")\n")
             .append("        private Map<String, Object> ").append(camel(e.getKey())).append(";\n\n");
        }
        return b.toString();
    }

    private String javaType(PropertyDef d, Set<String> imports) {
        String t = d.type() == null ? "string" : d.type();
        if (t.startsWith("tosca.datatypes.nfv.")) {
            String s = t.substring(t.lastIndexOf('.') + 1);
            imports.add(DT + "." + s);
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
        if ("float".equals(t)) {
            imports.add("com.example.etsi.vnfd.template.value.PropertyValue");
            return "PropertyValue<Double>";
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
        if ("range".equals(t)) {
            imports.add("java.util.List");
            return "List<Object>";
        }
        if ("list".equals(t)) {
            imports.add("java.util.List");
            return "List<" + boxed(d.entrySchema().orElse(null), imports) + ">";
        }
        if ("map".equals(t)) {
            imports.add("java.util.Map");
            return "Map<String, " + boxed(d.entrySchema().orElse(null), imports) + ">";
        }
        imports.add("java.util.Map");
        return "Map<String, Object>";
    }

    private String boxed(PropertyDef entry, Set<String> imports) {
        if (entry == null) {
            return "Object";
        }
        String t = javaType(entry, imports);
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

    static String oneLine(String s) {
        return s.trim().replace("*/", "* /").replaceAll("[ \t\r\n]+", " ");
    }
}
