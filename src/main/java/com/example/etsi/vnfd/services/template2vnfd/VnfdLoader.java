package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LifeCycleManagementScript;
import com.example.etsi.vnfd.ParseResult;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;

/**
 * Turns a parsed TOSCA package into a VNFD.
 *
 * <p>The entry point of the second half of the library. {@code YamlService} answers what the package
 * says; this answers what it means. The split follows the two specifications - everything up to
 * {@link ServiceToscaTemplate} is TOSCA and SOL001 vocabulary, everything produced here is IFA011.
 *
 * <p>Each service template is one deployment flavour (SOL001 V5.4.1 clauses 6.11.2 and 6.11.3), so
 * flavours are mapped one at a time and their contributions merged by identifier: IFA011 clause
 * 7.1.2.2 keeps the descriptors themselves - vdu, osContainerDesc, swImageDesc, the connection
 * points - at VNFD level, shared by every flavour.
 */
public final class VnfdLoader {

    private final ObjectMapper mapper = ToscaBindModule.mapper();

    /** Parses a package, reporting only what this stage notices. */
    public ParseResult load(ServiceToscaTemplate template) {
        return load(template, new Findings());
    }

    /**
     * Parses into a collector the caller already holds, so package-level findings survive.
     *
     * <p>Reading the package and interpreting it are two stages with their own findings - a missing
     * manifest is noticed by {@code YamlService}, long before any VNFD exists. Passing the same
     * collector to both is what puts them in one result.
     */
    public ParseResult load(ServiceToscaTemplate template, Findings findings) {
        List<ToscaDescriptorTemplate> flavours = template.flavourTemplates();
        if (flavours.isEmpty()) {
            throw new VnfdParseException("The package contains no service template with a "
                    + "topology_template; there is nothing to parse into a VNFD");
        }

        TypeReader.Hierarchy hierarchy = new TypeReader.Hierarchy(template.typeRegistry());
        NodeBinder binder = new NodeBinder(hierarchy, NodeTypes.ALL, findings);
        FlavourProcessor processor =
                new FlavourProcessor(new SwImageMapper(mapper), new StorageMapper(mapper));
        DeploymentFlavourMapper flavourMapper =
                new DeploymentFlavourMapper(hierarchy, mapper);

        Vnfd.Builder builder = Vnfd.builder();
        CrossFlavourElements merged = new CrossFlavourElements();
        Set<String> mciopIds = new LinkedHashSet<>();
        Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
        Map<String, SecurityGroupRule> securityGroupRules = new LinkedHashMap<>();
        Map<String, VnfPackageChangeInfo> packageChanges = new LinkedHashMap<>();
        boolean headerRead = false;

        for (ToscaDescriptorTemplate flavour : flavours) {
            FlavourContext context = new FlavourContext(flavour, binder, hierarchy, findings);

            if (!headerRead) {
                headerRead = readHeader(context, builder, findings);
            }

            List<Vdu> flavourVdus = processor.process(context, merged);

            DeploymentFlavourMapper.Result result = flavourMapper.map(context);
            SpecRules.flavour(result.df, flavourVdus, findings);
            builder.addDf(result.df);
            result.scripts.forEach(builder::addLcmOpParameterMappingScript);
            mciopIds.addAll(result.mciopIds);
            result.mciopArtifacts.forEach(a -> mciopArtifacts.putIfAbsent(a.getMciopId(), a));
            result.securityGroupRules.forEach(r ->
                    securityGroupRules.putIfAbsent(r.getSecurityGroupRuleId(), r));
            result.packageChanges.forEach(c ->
                    packageChanges.putIfAbsent(c.getChangeId(), c));
        }

        merged.drainInto(builder);
        securityGroupRules.values().forEach(builder::addSecurityGroupRule);
        packageChanges.values().forEach(builder::addVnfPackageChangeInfo);
        mciopIds.forEach(builder::addMciopId);

        if (!mciopArtifacts.isEmpty()) {
            VnfdExtensions.Builder extensions = VnfdExtensions.builder();
            mciopArtifacts.values().forEach(extensions::addMciopArtifacts);
            builder.extensions(extensions.build());
        }

        Vnfd vnfd = builder.build();
        SpecRules.note6(vnfd, findings);
        return new ParseResult(vnfd, template, findings);
    }

    /**
     * The VNF header and the lifecycle scripts, read from the first flavour that declares a VNF node.
     *
     * <p>IFA011 clause 7.1.2.2 keeps lifeCycleManagementScript at VNFD level, and SOL001 clause
     * 6.11.2 gives every flavour template the same VNF node type, so the scripts are read once with
     * the header rather than once per flavour. Which flavour that is, is not fixed: it is the first
     * one that has a VNF node, and the file a C24 finding names is that flavour's file.
     *
     * @return whether the header was read, which latches it off for the flavours that follow
     */
    private boolean readHeader(FlavourContext context, Vnfd.Builder builder, Findings findings) {
        Optional<Vnf> vnf = context.vnf();
        if (!vnf.isPresent()) {
            return false;
        }
        VnfHeaderMapper.map(vnf.get(), builder);
        for (LifeCycleManagementScript script : LcmMapper.map(vnf.get())) {
            SpecRules.lifecycleScriptHasEvent(script, context.template().file(), findings);
            builder.addLifeCycleManagementScript(script);
        }
        return true;
    }
}

/**
 * The VNFD-level elements, gathered from every deployment flavour.
 *
 * <p>IFA011 V5.4.1 clause 7.1.2.2 keeps the VDU, the descriptors and the connection points at VNFD
 * level, while SOL001 clauses 6.11.2 and 6.11.3 make one service template one flavour - so a
 * two-level design writes the same VDU once per flavour and the VNFD must still hold it once.
 * Deciding which of those writings survives is the whole job of this class: the first declaration
 * wins, and the order of first appearance is the order the VNFD lists them in.
 *
 * <p>Flavour-level content does not come through here. A {@code VduProfile} describing the same VDU
 * differently in each flavour is not a duplicate - it is two profiles, and IFA011 clause 7.1.8.2.2
 * wants both.
 */
final class CrossFlavourElements {

    private final Map<String, Vdu> vdus = new LinkedHashMap<>();
    private final Map<String, OsContainerDesc> containers = new LinkedHashMap<>();
    private final Map<String, SwImageDesc> images = new LinkedHashMap<>();
    private final Map<String, VduCpd> vduCpds = new LinkedHashMap<>();
    private final Map<String, VnfExtCpd> extCpds = new LinkedHashMap<>();
    private final Map<String, VnfVirtualLinkDesc> links = new LinkedHashMap<>();
    private final Map<String, VirtualStorageDesc> storages = new LinkedHashMap<>();
    private final Map<String, VipCpd> vipCpds = new LinkedHashMap<>();
    private final Map<String, VirtualCpd> virtualCpds = new LinkedHashMap<>();
    private final Map<String, CertificateDesc> certificates = new LinkedHashMap<>();

    /**
     * Contributes a VDU, and returns the one the VNFD will hold.
     *
     * <p>Which is <em>not</em> the argument when an earlier flavour already declared this
     * identifier, and the caller wants the returned object rather than the one it passed in:
     * {@code SpecRules.flavour} asks C28 about the VNFD-level element, and the VNFD holds one per
     * identifier however many flavours wrote it.
     *
     * <p>Every {@code add} returns its winner for the same reason, so that one functional shape
     * fits them all and {@link FlavourProcessor} can hold the node-type mappings as data.
     */
    Vdu addVdu(String id, Vdu vdu) {
        vdus.putIfAbsent(id, vdu);
        return vdus.get(id);
    }

    OsContainerDesc addOsContainerDesc(String id, OsContainerDesc desc) {
        containers.putIfAbsent(id, desc);
        return containers.get(id);
    }

    SwImageDesc addSwImageDesc(String id, SwImageDesc desc) {
        images.putIfAbsent(id, desc);
        return images.get(id);
    }

    VduCpd addVduCpd(String id, VduCpd cpd) {
        vduCpds.putIfAbsent(id, cpd);
        return vduCpds.get(id);
    }

    VnfExtCpd addVnfExtCpd(String id, VnfExtCpd cpd) {
        extCpds.putIfAbsent(id, cpd);
        return extCpds.get(id);
    }

    VnfVirtualLinkDesc addIntVirtualLinkDesc(String id, VnfVirtualLinkDesc desc) {
        links.putIfAbsent(id, desc);
        return links.get(id);
    }

    VirtualStorageDesc addVirtualStorageDesc(String id, VirtualStorageDesc desc) {
        storages.putIfAbsent(id, desc);
        return storages.get(id);
    }

    VipCpd addVipCpd(String id, VipCpd cpd) {
        vipCpds.putIfAbsent(id, cpd);
        return vipCpds.get(id);
    }

    VirtualCpd addVirtualCpd(String id, VirtualCpd cpd) {
        virtualCpds.putIfAbsent(id, cpd);
        return virtualCpds.get(id);
    }

    CertificateDesc addCertificateDesc(String id, CertificateDesc desc) {
        certificates.putIfAbsent(id, desc);
        return certificates.get(id);
    }

    /** Hands every element to the builder, in order of first declaration. */
    void drainInto(Vnfd.Builder builder) {
        vdus.values().forEach(builder::addVdu);
        containers.values().forEach(builder::addOsContainerDesc);
        images.values().forEach(builder::addSwImageDesc);
        vduCpds.values().forEach(builder::addVduCpd);
        extCpds.values().forEach(builder::addVnfExtCpd);
        links.values().forEach(builder::addIntVirtualLinkDesc);
        storages.values().forEach(builder::addVirtualStorageDesc);
        vipCpds.values().forEach(builder::addVipCpd);
        virtualCpds.values().forEach(builder::addVirtualCpd);
        certificates.values().forEach(builder::addCertificateDesc);
    }
}
