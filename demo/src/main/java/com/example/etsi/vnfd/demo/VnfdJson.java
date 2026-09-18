package com.example.etsi.vnfd.demo;

import com.example.etsi.vnfd.model.AffinityOrAntiAffinityGroup;
import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.Cpd;
import com.example.etsi.vnfd.model.DeployableModule;
import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.ScaleInfo;
import com.example.etsi.vnfd.model.ScalingAspect;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VduLevel;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VirtualLinkProfile;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import com.example.etsi.vnfd.template.value.FunctionCall;
import com.example.etsi.vnfd.template.value.Kind;
import com.example.etsi.vnfd.template.value.Literal;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.validation.Finding;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Renders a parsed VNFD as JSON.
 *
 * <p>Written by hand rather than delegating to Jackson databind, for two reasons found in the
 * library: the model classes carry no Jackson annotations at all, and almost every scalar getter
 * returns {@code Optional<PropertyValue<T>>} - a double wrapper that no default serializer renders
 * as anything a reader would want.
 *
 * <p>Conventions, chosen so the output is diffable and says what it knows:
 * <ul>
 *   <li>an empty {@code Optional} is omitted rather than written as null;</li>
 *   <li>a resolved {@link PropertyValue} is written as its plain value;</li>
 *   <li>an unresolved one becomes an object carrying the TOSCA function and its resolution, so a
 *       {@code get_input} is visibly deferred rather than silently missing;</li>
 *   <li>a {@link Quantity} keeps both what the descriptor wrote and the normalised byte count;</li>
 *   <li>lists and maps keep declaration order, so two runs of the same package diff cleanly.</li>
 * </ul>
 *
 * <p>Two naming conventions live in the library and are easy to mix up: {@code model/} uses
 * {@code getXxx()}, while {@code validation/} and {@code template/} use bare {@code xxx()}.
 * There are also two unrelated {@code SourceRef} classes; this file means the validation one.
 */
public final class VnfdJson {

    private static final ObjectMapper MAPPER = newMapper();

    private VnfdJson() {
    }

    /**
     * The mapper used for every value this class does not render by hand.
     *
     * <p>Two serializers are registered rather than handled at each call site. A
     * {@link PropertyValue} appears not only as a top-level property but nested inside the SOL001
     * datatype beans - {@code McioIdentificationData.name}, the entries of {@code protocol} - and
     * left to bean introspection it renders as {@code {"resolved":true,"deferred":false}}, which
     * says nothing about the value. Registering the rule once means a property reads the same
     * wherever it appears.
     */
    private static ObjectMapper newMapper() {
        SimpleModule module = new SimpleModule("vnfd-json");
        module.addSerializer(PropertyValue.class, new JsonSerializer<PropertyValue>() {
            @Override
            public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider sp)
                    throws IOException {
                gen.writeTree(propertyNode(value));
            }
        });
        // The interface registration above is not enough on its own: inside a Map<String,Object>
        // Jackson sees the runtime class and picks a bean serializer for it, which is how a
        // PropertyValue ends up rendered as {"resolved":true,"deferred":false}. Both concrete
        // implementations are registered so the rule holds wherever the value sits.
        module.addSerializer(Literal.class, new JsonSerializer<Literal>() {
            @Override
            public void serialize(Literal value, JsonGenerator gen, SerializerProvider sp)
                    throws IOException {
                gen.writeTree(propertyNode(value));
            }
        });
        module.addSerializer(FunctionCall.class, new JsonSerializer<FunctionCall>() {
            @Override
            public void serialize(FunctionCall value, JsonGenerator gen, SerializerProvider sp)
                    throws IOException {
                gen.writeTree(propertyNode(value));
            }
        });
        module.addSerializer(Quantity.class, new JsonSerializer<Quantity>() {
            @Override
            public void serialize(Quantity value, JsonGenerator gen, SerializerProvider sp)
                    throws IOException {
                gen.writeTree(scalar(value));
            }
        });
        return new ObjectMapper()
                .registerModule(module)
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static ObjectNode object() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode array() {
        return MAPPER.createArrayNode();
    }

    public static String write(JsonNode node, boolean pretty) {
        try {
            return pretty
                    ? MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node)
                    : MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialise JSON", e);
        }
    }

    // ------------------------------------------------------------------ the VNFD

    /** IFA011 V5.4.1 clause 7.1.2.2. */
    public static ObjectNode vnfd(Vnfd vnfd) {
        ObjectNode n = object();
        put(n, "vnfdId", vnfd.getVnfdId());
        put(n, "vnfProvider", vnfd.getVnfProvider());
        put(n, "vnfProductName", vnfd.getVnfProductName());
        put(n, "vnfSoftwareVersion", vnfd.getVnfSoftwareVersion());
        put(n, "vnfdVersion", vnfd.getVnfdVersion());
        put(n, "vnfProductInfoName", vnfd.getVnfProductInfoName());
        put(n, "vnfProductInfoDescription", vnfd.getVnfProductInfoDescription());
        put(n, "vnfdExtInvariantId", vnfd.getVnfdExtInvariantId());
        strings(n, "vnfmInfo", vnfd.getVnfmInfo());
        strings(n, "localizationLanguage", vnfd.getLocalizationLanguage());
        put(n, "defaultLocalizationLanguage", vnfd.getDefaultLocalizationLanguage());

        list(n, "vdu", vnfd.getVdu(), VnfdJson::vdu);
        list(n, "osContainerDesc", vnfd.getOsContainerDesc(), VnfdJson::osContainerDesc);
        list(n, "virtualStorageDesc", vnfd.getVirtualStorageDesc(), VnfdJson::virtualStorageDesc);
        list(n, "swImageDesc", vnfd.getSwImageDesc(), VnfdJson::swImageDesc);
        list(n, "intVirtualLinkDesc", vnfd.getIntVirtualLinkDesc(), VnfdJson::virtualLinkDesc);
        list(n, "vduCpd", vnfd.getVduCpd(), VnfdJson::vduCpd);
        list(n, "vnfExtCpd", vnfd.getVnfExtCpd(), VnfdJson::vnfExtCpd);
        list(n, "vipCpd", vnfd.getVipCpd(), VnfdJson::vipCpd);
        list(n, "virtualCpd", vnfd.getVirtualCpd(), VnfdJson::virtualCpd);
        list(n, "certificateDesc", vnfd.getCertificateDesc(), VnfdJson::certificateDesc);

        // IFA011 names this attribute deploymentFlavour; the JSON field is "df" by project decision.
        list(n, "df", vnfd.getDf(), VnfdJson::df);

        strings(n, "mciopId", vnfd.getMciopId());
        list(n, "lcmOpParameterMappingScript", vnfd.getLcmOpParameterMappingScript(),
                VnfdJson::lcmOpParameterMappingScript);
        list(n, "lifeCycleManagementScript", vnfd.getLifeCycleManagementScript(),
                VnfdJson::lifeCycleManagementScript);

        vnfd.getExtensions().filter(e -> !e.isEmpty())
                .ifPresent(e -> n.set("_extensions", extensions(e)));
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.6.2.2. */
    public static ObjectNode vdu(Vdu vdu) {
        ObjectNode n = object();
        n.put("vduId", vdu.getVduId());
        property(n, "name", vdu.getName());
        property(n, "description", vdu.getDescription());
        strings(n, "intCpd", vdu.getIntCpd());
        strings(n, "osContainerDesc", vdu.getOsContainerDesc());
        strings(n, "virtualStorageDesc", vdu.getVirtualStorageDesc());
        strings(n, "certificateDesc", vdu.getCertificateDesc());
        strings(n, "mcioConstraintParams", vdu.getMcioConstraintParams());
        vdu.getMcioIdentificationData()
                .ifPresent(d -> n.set("mcioIdentificationData", MAPPER.valueToTree(d)));
        property(n, "isNumOfInstancesClusterBased", vdu.getIsNumOfInstancesClusterBased());
        // [MANO INTERPRETATION] derived, not an IFA011 attribute - see the library javadoc.
        n.put("lcmRealizationPath", vdu.getLcmRealizationPath().name());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.6.13.2. */
    public static ObjectNode osContainerDesc(OsContainerDesc d) {
        ObjectNode n = object();
        n.put("osContainerDescId", d.getOsContainerDescId());
        property(n, "name", d.getName());
        property(n, "description", d.getDescription());
        property(n, "requestedCpuResources", d.getRequestedCpuResources());
        property(n, "cpuResourceLimit", d.getCpuResourceLimit());
        property(n, "requestedMemoryResources", d.getRequestedMemoryResources());
        property(n, "memoryResourceLimit", d.getMemoryResourceLimit());
        property(n, "requestedEphemeralStorageResources", d.getRequestedEphemeralStorageResources());
        property(n, "ephemeralStorageResourceLimit", d.getEphemeralStorageResourceLimit());
        maps(n, "extendedResourceRequests", d.getExtendedResourceRequests());
        maps(n, "hugePageResources", d.getHugePageResources());
        map(n, "cpuPinningRequirements", d.getCpuPinningRequirements());
        put(n, "swImageDesc", d.getSwImageDesc());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.6.5.2. The id is the Vdu.OsContainer node template name. */
    public static ObjectNode swImageDesc(SwImageDesc d) {
        ObjectNode n = object();
        n.put("id", d.getId());
        property(n, "name", d.getName());
        property(n, "version", d.getVersion());
        property(n, "provider", d.getProvider());
        d.getChecksum().ifPresent(c -> n.set("checksum", MAPPER.valueToTree(c)));
        property(n, "containerFormat", d.getContainerFormat());
        property(n, "diskFormat", d.getDiskFormat());
        property(n, "size", d.getSize());
        property(n, "minDisk", d.getMinDisk());
        property(n, "minRam", d.getMinRam());
        property(n, "operatingSystem", d.getOperatingSystem());
        put(n, "swImage", d.getSwImage());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.9.4.2.2. */
    public static ObjectNode virtualStorageDesc(VirtualStorageDesc d) {
        ObjectNode n = object();
        n.put("id", d.getId());
        n.put("typeOfStorage", d.getTypeOfStorage().name());
        map(n, "storageData", d.getStorageData());
        property(n, "perVnfcInstance", d.getPerVnfcInstance());
        map(n, "nfviMaintenanceInfo", d.getNfviMaintenanceInfo());
        put(n, "swImageDesc", d.getSwImageDesc());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.7.2. */
    public static ObjectNode virtualLinkDesc(VnfVirtualLinkDesc d) {
        ObjectNode n = object();
        n.put("virtualLinkDescId", d.getVirtualLinkDescId());
        property(n, "description", d.getDescription());
        map(n, "connectivityType", d.getConnectivityType());
        map(n, "nfviMaintenanceInfo", d.getNfviMaintenanceInfo());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.6.4. */
    public static ObjectNode vduCpd(VduCpd c) {
        ObjectNode n = cpd(c);
        put(n, "vduId", c.getVduId());
        put(n, "intVirtualLinkDesc", c.getIntVirtualLinkDesc());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.3.2. */
    public static ObjectNode vnfExtCpd(VnfExtCpd c) {
        ObjectNode n = cpd(c);
        put(n, "intCpd", c.getIntCpd());
        put(n, "intVirtualLinkDesc", c.getIntVirtualLinkDesc());
        // SOL001 clause 6.8.2.8: a VduCp exposed through substitution_mappings is also an external
        // CP, so one node template becomes two information elements. This flag says which route.
        n.put("exposedThroughSubstitution", c.isExposedThroughSubstitution());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.17.2. */
    public static ObjectNode vipCpd(VipCpd c) {
        ObjectNode n = cpd(c);
        strings(n, "intCpd", c.getIntCpd());
        put(n, "intVirtualLinkDesc", c.getIntVirtualLinkDesc());
        property(n, "dedicatedIpAddress", c.getDedicatedIpAddress());
        property(n, "vipFunction", c.getVipFunction());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.18.2. */
    public static ObjectNode virtualCpd(VirtualCpd c) {
        ObjectNode n = cpd(c);
        strings(n, "vdu", c.getVdu());
        maps(n, "additionalServiceData", c.getAdditionalServiceData());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.19.2. */
    public static ObjectNode certificateDesc(CertificateDesc c) {
        ObjectNode n = object();
        n.put("id", c.getId());
        property(n, "name", c.getName());
        property(n, "certificateType", c.getCertificateType());
        maps(n, "csrRequirements", c.getCsrRequirements());
        map(n, "certificateBaseProfile", c.getCertificateBaseProfile());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.24. */
    public static ObjectNode deployableModule(DeployableModule m) {
        ObjectNode n = object();
        n.put("deployableModuleId", m.getDeployableModuleId());
        property(n, "name", m.getName());
        property(n, "description", m.getDescription());
        strings(n, "member", m.getMember());
        return n;
    }

    private static ObjectNode cpd(Cpd c) {
        ObjectNode n = object();
        n.put("cpdId", c.getCpdId());
        strings(n, "layerProtocol", c.getLayerProtocol());
        property(n, "cpRole", c.getCpRole());
        property(n, "description", c.getDescription());
        maps(n, "protocol", c.getProtocol());
        property(n, "trunkMode", c.getTrunkMode());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.2.2. */
    public static ObjectNode df(VnfDf df) {
        ObjectNode n = object();
        n.put("flavourId", df.getFlavourId());
        put(n, "description", df.getDescription());
        list(n, "vduProfile", df.getVduProfile(), VnfdJson::vduProfile);
        list(n, "mciopProfile", df.getMciopProfile(), VnfdJson::mciopProfile);
        list(n, "virtualLinkProfile", df.getVirtualLinkProfile(), VnfdJson::virtualLinkProfile);
        list(n, "instantiationLevel", df.getInstantiationLevel(), VnfdJson::instantiationLevel);
        put(n, "defaultInstantiationLevelId", df.getDefaultInstantiationLevelId());
        list(n, "affinityOrAntiAffinityGroup", df.getAffinityOrAntiAffinityGroup(),
                VnfdJson::affinityGroup);
        list(n, "scalingAspect", df.getScalingAspect(), VnfdJson::scalingAspect);
        list(n, "deployableModule", df.getDeployableModule(), VnfdJson::deployableModule);
        df.getVnfLcmOperationsConfiguration().filter(c -> !c.isEmpty()).ifPresent(c -> {
            ObjectNode cfg = object();
            c.getOpConfigs().forEach((k, v) -> cfg.set(k, MAPPER.valueToTree(v)));
            n.set("vnfLcmOperationsConfiguration", cfg);
        });
        put(n, "_sourceFile", df.getSourceFile());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.3.2. */
    public static ObjectNode vduProfile(VduProfile p) {
        ObjectNode n = object();
        n.put("vduId", p.getVduId());
        property(n, "minNumberOfInstances", p.getMinNumberOfInstances());
        property(n, "maxNumberOfInstances", p.getMaxNumberOfInstances());
        strings(n, "affinityOrAntiAffinityGroupId", p.getAffinityOrAntiAffinityGroupId());
        strings(n, "deployableModule", p.getDeployableModule());
        strings(n, "modifyCapacityAttributesOp", p.getModifyCapacityAttributesOp());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.20.2 - exactly six attributes, none of them a chart path. */
    public static ObjectNode mciopProfile(MciopProfile p) {
        ObjectNode n = object();
        n.put("mciopId", p.getMciopId());
        p.getDeploymentOrder().ifPresent(v -> n.put("deploymentOrder", v));
        strings(n, "affinityOrAntiAffinityGroupId", p.getAffinityOrAntiAffinityGroupId());
        strings(n, "associatedVdu", p.getAssociatedVdu());
        put(n, "mciopParameterMappingRule", p.getMciopParameterMappingRule());
        put(n, "lcmOpParameterMappingScriptId", p.getLcmOpParameterMappingScriptId());
        return n;
    }

    public static ObjectNode virtualLinkProfile(VirtualLinkProfile p) {
        ObjectNode n = object();
        n.put("virtualLinkDescId", p.getVirtualLinkDescId());
        map(n, "maxBitrateRequirements", p.getMaxBitrateRequirements());
        map(n, "minBitrateRequirements", p.getMinBitrateRequirements());
        map(n, "qos", p.getQos());
        strings(n, "affinityOrAntiAffinityGroupId", p.getAffinityOrAntiAffinityGroupId());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.7.2. */
    public static ObjectNode instantiationLevel(InstantiationLevel l) {
        ObjectNode n = object();
        n.put("levelId", l.getLevelId());
        n.put("description", l.getDescription());
        list(n, "vduLevel", l.getVduLevel(), VnfdJson::vduLevel);
        list(n, "scaleInfo", l.getScaleInfo(), VnfdJson::scaleInfo);
        if (l.isSynthesised()) {
            // [MANO INTERPRETATION] IFA011 makes instantiationLevel M,1..N, so a descriptor with no
            // InstantiationLevels policy still needs one. Flagged rather than passed off as read.
            n.put("_synthesised", true);
        }
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.9.2. */
    public static ObjectNode vduLevel(VduLevel l) {
        ObjectNode n = object();
        n.put("vduId", l.getVduId());
        n.put("numberOfInstances", l.getNumberOfInstances());
        return n;
    }

    public static ObjectNode scaleInfo(ScaleInfo s) {
        ObjectNode n = object();
        n.put("aspectId", s.getAspectId());
        n.put("scaleLevel", s.getScaleLevel());
        return n;
    }

    public static ObjectNode scalingAspect(ScalingAspect a) {
        ObjectNode n = object();
        n.put("id", a.getId());
        put(n, "name", a.getName());
        put(n, "description", a.getDescription());
        a.getMaxScaleLevel().ifPresent(v -> n.put("maxScaleLevel", v));
        strings(n, "stepDeltas", a.getStepDeltas());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.8.12. */
    public static ObjectNode affinityGroup(AffinityOrAntiAffinityGroup g) {
        ObjectNode n = object();
        n.put("groupId", g.getGroupId());
        n.put("type", g.getType().name());
        put(n, "scope", g.getScope());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.20.2. */
    public static ObjectNode lcmOpParameterMappingScript(LcmOpParameterMappingScript s) {
        ObjectNode n = object();
        n.put("lcmOpParameterMappingScriptId", s.getLcmOpParameterMappingScriptId());
        put(n, "script", s.getScript());
        put(n, "scriptDsl", s.getScriptDsl());
        // SOL001 clause 6.3.4.1 gives the Helm variant three parameters where IFA011 clause 7.1.20.1
        // gives four, so the arity has to travel with the script for a caller to invoke it right.
        n.put("_kind", s.getKind().name());
        n.put("_invocationArity", s.getInvocationArity());
        return n;
    }

    /** IFA011 V5.4.1 clause 7.1.13.2. */
    public static ObjectNode lifeCycleManagementScript(LifeCycleManagementScript s) {
        ObjectNode n = object();
        n.put("lcmScriptId", s.getLcmScriptId());
        strings(n, "event", s.getEvent());
        strings(n, "lcmTransitionEvent", s.getLcmTransitionEvent());
        put(n, "script", s.getScript());
        put(n, "scriptDsl", s.getScriptDsl());
        map(n, "scriptInput", s.getScriptInput());
        return n;
    }

    /** Not IFA011: MciopProfile has no attribute able to hold the path of the Helm chart. */
    public static ObjectNode extensions(VnfdExtensions e) {
        ObjectNode n = object();
        if (!e.getMciopArtifacts().isEmpty()) {
            ObjectNode byId = object();
            e.getMciopArtifacts().forEach((id, a) -> byId.set(id, mciopArtifacts(a)));
            n.set("mciopArtifacts", byId);
        }
        map(n, "vendorExtensions", e.getVendorExtensions());
        return n;
    }

    public static ObjectNode mciopArtifacts(MciopArtifacts a) {
        ObjectNode n = object();
        n.put("mciopId", a.getMciopId());
        put(n, "packagePath", a.getPackagePath());
        put(n, "packageArtifactName", a.getPackageArtifactName());
        put(n, "packageArtifactType", a.getPackageArtifactType());
        put(n, "paramMappingScriptPath", a.getParamMappingScriptPath());
        put(n, "paramMappingRulePath", a.getParamMappingRulePath());
        return n;
    }

    // ------------------------------------------------------------------ findings

    public static ObjectNode finding(Finding f) {
        ObjectNode n = object();
        n.put("severity", f.severity().name());
        n.put("ruleId", f.ruleId());
        n.put("clause", f.clause());
        n.put("message", f.message());
        f.source().ifPresent(s -> {
            ObjectNode src = object();
            src.put("file", s.file());
            s.element().ifPresent(el -> src.put("element", el));
            n.set("source", src);
        });
        return n;
    }

    public static ArrayNode findings(List<Finding> list) {
        ArrayNode a = array();
        list.forEach(f -> a.add(finding(f)));
        return a;
    }

    // ------------------------------------------------------------------ helpers

    /**
     * A property as the descriptor wrote it.
     *
     * <p>A resolved value is written plainly. An unresolved one keeps the function and the reason it
     * could not be resolved, because SOL001 clause 5.9 allows {@code get_input} and
     * {@code get_attribute} in places where no value exists until a VNF instance does - dropping
     * them would make an incomplete descriptor look complete.
     */
    private static void property(ObjectNode n, String field, Optional<? extends PropertyValue<?>> value) {
        value.ifPresent(pv -> n.set(field, propertyNode(pv)));
    }

    public static JsonNode propertyNode(PropertyValue<?> pv) {
        if (pv.isResolved()) {
            return scalar(pv.resolved().orElse(null));
        }
        ObjectNode n = object();
        if (pv.kind() == Kind.FUNCTION && pv instanceof FunctionCall) {
            FunctionCall<?> fn = (FunctionCall<?>) pv;
            n.put("_fn", fn.rawKey());
            ArrayNode args = array();
            for (PropertyValue<?> arg : fn.args()) {
                args.add(propertyNode(arg));
            }
            if (args.size() > 0) {
                n.set("_args", args);
            }
        }
        n.put("_resolution", pv.resolution().name());
        n.set("_raw", MAPPER.valueToTree(pv.raw()));
        return n;
    }

    static JsonNode scalar(Object value) {
        if (value instanceof Quantity) {
            Quantity q = (Quantity) value;
            ObjectNode n = object();
            n.put("text", q.originalText());
            n.put("bytes", q.normalizedBytes());
            n.put("unit", q.unit().name());
            if (!q.hasCanonicalSpacing()) {
                // TOSCA 1.3 clause 3.3.6 spells a scalar-unit "<scalar> <unit>", with the space.
                n.put("_nonCanonicalSpacing", true);
            }
            return n;
        }
        return MAPPER.valueToTree(value);
    }

    private static void put(ObjectNode n, String field, Optional<String> value) {
        value.ifPresent(v -> n.put(field, v));
    }

    private static void strings(ObjectNode n, String field, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        ArrayNode a = array();
        values.forEach(a::add);
        n.set(field, a);
    }

    private static void map(ObjectNode n, String field, Map<String, Object> value) {
        if (value != null && !value.isEmpty()) {
            n.set(field, MAPPER.valueToTree(value));
        }
    }

    private static void maps(ObjectNode n, String field, List<Map<String, Object>> values) {
        if (values != null && !values.isEmpty()) {
            n.set(field, MAPPER.valueToTree(values));
        }
    }

    private static <T> void list(ObjectNode n, String field, List<T> values,
            Function<T, ObjectNode> render) {
        if (values == null || values.isEmpty()) {
            return;
        }
        ArrayNode a = array();
        values.forEach(v -> a.add(render.apply(v)));
        n.set(field, a);
    }

    /** Kept for callers that want to add their own section to a node. */
    public static <T> void section(ObjectNode n, String field, T value, BiConsumer<ObjectNode, T> render) {
        ObjectNode child = object();
        render.accept(child, value);
        n.set(field, child);
    }
}
