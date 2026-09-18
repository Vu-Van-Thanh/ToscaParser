package com.example.etsi.vnfd.demo;

import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.ToscaParser;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.services.pkg2template.PackageReader;
import com.example.etsi.vnfd.services.template2vnfd.VnfdParseException;
import com.example.etsi.vnfd.template.NodeTemplate;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.validation.Finding;
import com.example.etsi.vnfd.validation.Severity;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The library, called the way a host application would call it.
 *
 * <p>One static entry point wrapping {@link ToscaParser#parse}, plus the rendering a caller wants
 * around it. Everything HTTP lives in {@link VnfdHttpServer}; this class has no idea a server
 * exists, so it can be used from a test, a CLI or a controller without change.
 *
 * <p>Failures are returned, not thrown. A package that cannot be read at all produces a report with
 * {@link Report#failure()} set; a package that reads but breaks a rule of SOL001 or IFA011 produces
 * a normal report whose findings say so. That distinction is the library's own and worth preserving
 * at the boundary.
 */
public final class ParseApi {

    /**
     * The ETSI node types the library can classify, for the diagnostic view.
     *
     * <p>Mirrors the binder's own list. The binder resolves by walking {@code derived_from} rather
     * than by comparing type names - SOL001 clause 6.11.2 requires a VNF node type to be
     * VNF-specific, so a descriptor writes {@code MyCompany.ExampleVNF} and string matching finds
     * nothing. The same walk is done here through the public {@code TypeReader.Hierarchy}.
     */
    private static final List<String> BINDABLE_NODE_TYPES = Collections.unmodifiableList(Arrays.asList(
            EtsiTypes.VNF,
            EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT,
            EtsiTypes.VDU_OS_CONTAINER,
            EtsiTypes.MCIOP,
            EtsiTypes.VDU_SUB_CP,
            EtsiTypes.VDU_CP,
            EtsiTypes.VNF_EXT_CP,
            EtsiTypes.VIP_CP,
            EtsiTypes.VIRTUAL_CP,
            EtsiTypes.VNF_VIRTUAL_LINK,
            EtsiTypes.VDU_VIRTUAL_BLOCK_STORAGE,
            EtsiTypes.VDU_VIRTUAL_OBJECT_STORAGE,
            EtsiTypes.VDU_VIRTUAL_FILE_STORAGE,
            EtsiTypes.DEPLOYABLE_MODULE,
            EtsiTypes.CERTIFICATE));

    /** Types a node template may legitimately declare that this library deliberately does not map. */
    private static final List<String> OUT_OF_SCOPE_NODE_TYPES = Collections.unmodifiableList(Arrays.asList(
            EtsiTypes.VDU_COMPUTE,
            EtsiTypes.PAAS_SERVICE_REQUEST,
            EtsiTypes.PAAS_SERVICE_PROFILE));

    private ParseApi() {
    }

    /** Parses one extracted VNF package directory. Never throws for a malformed package. */
    public static Report parse(Path packageDir) {
        try {
            return new Report(packageDir, ToscaParser.parse(packageDir), null);
        } catch (VnfdParseException | PackageReader.CsarSecurityException
                | PackageReader.ToscaYamlException | IllegalStateException
                | IllegalArgumentException e) {
            return new Report(packageDir, null, e);
        }
    }

    /** One parsed package, plus the views the demo offers over it. */
    public static final class Report {

        private final Path packageDir;
        private final ParseResult result;
        private final Exception failure;

        Report(Path packageDir, ParseResult result, Exception failure) {
            this.packageDir = packageDir;
            this.result = result;
            this.failure = failure;
        }

        public Path packageDir() {
            return packageDir;
        }

        /** Present when the package could not be read at all, as distinct from being non-conformant. */
        public Optional<Exception> failure() {
            return Optional.ofNullable(failure);
        }

        public boolean ok() {
            return failure == null;
        }

        public Vnfd vnfd() {
            require();
            return result.getVnfd();
        }

        public ServiceToscaTemplate template() {
            require();
            return result.getTemplate();
        }

        public List<Finding> findings() {
            return result == null ? Collections.emptyList() : result.getFindings();
        }

        public List<Finding> findings(Severity severity) {
            return result == null ? Collections.emptyList() : result.getFindings(severity);
        }

        public boolean hasErrors() {
            return result != null && result.hasErrors();
        }

        private void require() {
            if (result == null) {
                throw new IllegalStateException(
                        "Package could not be read: " + packageDir, failure);
            }
        }

        // -------------------------------------------------------------- JSON views

        /** {@code { vnfd, findings, hasErrors }} - the full answer. */
        public ObjectNode toJson() {
            ObjectNode n = VnfdJson.object();
            n.put("package", packageDir.getFileName().toString());
            if (!ok()) {
                return withFailure(n);
            }
            n.put("hasErrors", hasErrors());
            n.set("vnfd", VnfdJson.vnfd(vnfd()));
            n.set("findings", VnfdJson.findings(findings()));
            return n;
        }

        /** The VNFD alone, for diffing one run against another. */
        public ObjectNode vnfdJson() {
            ObjectNode n = VnfdJson.object();
            return ok() ? VnfdJson.vnfd(vnfd()) : withFailure(n);
        }

        /** Findings alone, each with the clause that justifies it. */
        public ObjectNode findingsJson() {
            ObjectNode n = VnfdJson.object();
            n.put("package", packageDir.getFileName().toString());
            if (!ok()) {
                return withFailure(n);
            }
            n.put("hasErrors", hasErrors());
            n.put("errors", findings(Severity.ERROR).size());
            n.put("warnings", findings(Severity.WARN).size());
            n.put("infos", findings(Severity.INFO).size());
            n.set("findings", VnfdJson.findings(findings()));
            return n;
        }

        /**
         * What the reading stage saw, before any of it became IFA011.
         *
         * <p>The useful half is {@code unboundNodeTemplates}: a node template whose type resolves to
         * no ETSI ancestor is dropped silently by the binder, which is the quietest way for a
         * descriptor to lose content. Listing them turns that into something visible.
         */
        public ObjectNode debugJson() {
            ObjectNode n = VnfdJson.object();
            n.put("package", packageDir.getFileName().toString());
            n.put("path", packageDir.toString());
            if (!ok()) {
                return withFailure(n);
            }

            ServiceToscaTemplate tst = template();
            n.put("packageName", tst.packageName());
            n.put("isTwoLevelDesign", tst.isTwoLevelDesign());
            n.put("entryDefinitions", tst.meta().entryDefinitions());
            VnfdJson.mapper();

            ArrayNode meta = VnfdJson.array();
            tst.meta().block0().forEach((k, v) -> {
                ObjectNode e = VnfdJson.object();
                e.put("key", k);
                e.put("value", v);
                meta.add(e);
            });
            n.set("toscaMeta", meta);

            n.put("typeCount", tst.typeRegistry().size());

            TypeReader.Hierarchy hierarchy = new TypeReader.Hierarchy(tst.typeRegistry());
            ArrayNode templates = VnfdJson.array();
            ArrayNode unbound = VnfdJson.array();
            for (ToscaDescriptorTemplate t : tst.descriptorTemplates()) {
                ObjectNode e = VnfdJson.object();
                e.put("file", t.file());
                e.put("isTopLevel", t.isTopLevel());
                t.toscaDefinitionsVersion().ifPresent(v -> e.put("toscaDefinitionsVersion", v));
                e.put("hasTopology", t.topologyTemplate().isPresent());

                if (t.topologyTemplate().isPresent()) {
                    ArrayNode nodes = VnfdJson.array();
                    for (NodeTemplate node : t.topologyTemplate().get().nodeTemplates().values()) {
                        ObjectNode ne = VnfdJson.object();
                        ne.put("name", node.name());
                        ne.put("type", node.type());
                        Optional<String> etsi = hierarchy
                                .nearestAncestorAmong(node.type(), BINDABLE_NODE_TYPES);
                        if (etsi.isPresent()) {
                            ne.put("etsiType", etsi.get());
                            ne.put("bound", true);
                        } else {
                            ne.put("bound", false);
                            Optional<String> outOfScope = hierarchy
                                    .nearestAncestorAmong(node.type(), OUT_OF_SCOPE_NODE_TYPES);
                            ne.put("reason", outOfScope
                                    .map(s -> "out of scope for this library: " + s)
                                    .orElse("type resolves to no ETSI node type this library binds"));
                            ObjectNode u = ne.deepCopy();
                            u.put("file", t.file());
                            unbound.add(u);
                        }
                        nodes.add(ne);
                    }
                    e.set("nodeTemplates", nodes);
                    e.put("policyCount", t.topologyTemplate().get().policies().size());
                }
                templates.add(e);
            }
            n.set("descriptorTemplates", templates);
            n.set("unboundNodeTemplates", unbound);
            return n;
        }

        /** One row of the cross-package comparison table. */
        public ObjectNode summaryJson() {
            ObjectNode n = VnfdJson.object();
            n.put("package", packageDir.getFileName().toString());
            if (!ok()) {
                n.put("ok", false);
                n.put("error", failure.getClass().getSimpleName() + ": " + failure.getMessage());
                return n;
            }
            Vnfd v = vnfd();
            n.put("ok", true);
            n.put("vnfdId", v.getVnfdId().orElse(""));
            n.put("vdu", v.getVdu().size());
            n.put("df", v.getDf().size());
            n.put("osContainerDesc", v.getOsContainerDesc().size());
            n.put("swImageDesc", v.getSwImageDesc().size());
            n.put("virtualStorageDesc", v.getVirtualStorageDesc().size());
            n.put("vduCpd", v.getVduCpd().size());
            n.put("vnfExtCpd", v.getVnfExtCpd().size());
            n.put("intVirtualLinkDesc", v.getIntVirtualLinkDesc().size());
            n.put("mciopId", v.getMciopId().size());

            ArrayNode paths = VnfdJson.array();
            for (Vdu vdu : v.getVdu()) {
                ObjectNode p = VnfdJson.object();
                p.put("vduId", vdu.getVduId());
                p.put("lcmRealizationPath", vdu.getLcmRealizationPath().name());
                paths.add(p);
            }
            n.set("lcmRealization", paths);

            n.put("errors", findings(Severity.ERROR).size());
            n.put("warnings", findings(Severity.WARN).size());
            ArrayNode ruleIds = VnfdJson.array();
            List<String> seen = new ArrayList<>();
            for (Finding f : findings()) {
                String key = f.severity().name() + " " + f.ruleId();
                if (!seen.contains(key)) {
                    seen.add(key);
                    ruleIds.add(key);
                }
            }
            n.set("rules", ruleIds);
            return n;
        }

        private ObjectNode withFailure(ObjectNode n) {
            n.put("ok", false);
            n.put("errorType", failure.getClass().getName());
            n.put("error", String.valueOf(failure.getMessage()));
            return n;
        }
    }
}
