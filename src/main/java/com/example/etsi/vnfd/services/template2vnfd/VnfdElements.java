package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.Vnfd;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;

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
public final class VnfdElements {

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

    // Not descriptors of anything a flavour declares directly - SOL001 lets a security group rule
    // or a package change policy sit inside one service template while IFA011 Table 7.1.2.2-1
    // keeps both at VNFD level, and clause 7.1.20 does the same for the Helm parameter mapping
    // scripts. Same rule as everything above: first declaration wins.
    private final Map<String, SecurityGroupRule> securityGroupRules = new LinkedHashMap<>();
    private final Map<String, VnfPackageChangeInfo> packageChanges = new LinkedHashMap<>();
    private final Map<String, LcmOpParameterMappingScript> scripts = new LinkedHashMap<>();
    private final Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
    private final Set<String> mciopIds = new LinkedHashSet<>();

    public void addVdu(String id, Vdu vdu) {
        vdus.putIfAbsent(id, vdu);
    }

    public void addOsContainerDesc(String id, OsContainerDesc desc) {
        containers.putIfAbsent(id, desc);
    }

    public void addSwImageDesc(String id, SwImageDesc desc) {
        images.putIfAbsent(id, desc);
    }

    public void addVduCpd(String id, VduCpd cpd) {
        vduCpds.putIfAbsent(id, cpd);
    }

    public void addVnfExtCpd(String id, VnfExtCpd cpd) {
        extCpds.putIfAbsent(id, cpd);
    }

    public void addIntVirtualLinkDesc(String id, VnfVirtualLinkDesc desc) {
        links.putIfAbsent(id, desc);
    }

    public void addVirtualStorageDesc(String id, VirtualStorageDesc desc) {
        storages.putIfAbsent(id, desc);
    }

    public void addVipCpd(String id, VipCpd cpd) {
        vipCpds.putIfAbsent(id, cpd);
    }

    public void addVirtualCpd(String id, VirtualCpd cpd) {
        virtualCpds.putIfAbsent(id, cpd);
    }

    public void addCertificateDesc(String id, CertificateDesc desc) {
        certificates.putIfAbsent(id, desc);
    }

    /** The identifier comes from the rule itself, so the caller need not repeat it. */
    public void addSecurityGroupRule(SecurityGroupRule rule) {
        securityGroupRules.putIfAbsent(rule.getSecurityGroupRuleId(), rule);
    }

    public void addVnfPackageChangeInfo(VnfPackageChangeInfo change) {
        packageChanges.putIfAbsent(change.getChangeId(), change);
    }

    public void addLcmOpParameterMappingScript(LcmOpParameterMappingScript script) {
        scripts.putIfAbsent(script.getLcmOpParameterMappingScriptId(), script);
    }

    public void addMciopArtifacts(MciopArtifacts artifacts) {
        mciopArtifacts.putIfAbsent(artifacts.getMciopId(), artifacts);
    }

    public void addMciopId(String id) {
        mciopIds.add(id);
    }

    /**
     * The VDU the VNFD will hold under this identifier.
     *
     * <p>Not necessarily the one the caller contributed. When an earlier flavour already declared
     * the identifier, that declaration is the one that survives - and
     * {@code SpecRuleValidator.validateFlavour} asks C28 about the surviving element, not about the
     * one this flavour built.
     */
    public Vdu vdu(String id) {
        return vdus.get(id);
    }

    /** Hands every element to the builder, in order of first declaration. */
    void applyTo(Vnfd.Builder builder) {
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
        securityGroupRules.values().forEach(builder::addSecurityGroupRule);
        packageChanges.values().forEach(builder::addVnfPackageChangeInfo);
        scripts.values().forEach(builder::addLcmOpParameterMappingScript);
        mciopIds.forEach(builder::addMciopId);
        if (!mciopArtifacts.isEmpty()) {
            VnfdExtensions.Builder extensions = VnfdExtensions.builder();
            mciopArtifacts.values().forEach(extensions::addMciopArtifacts);
            builder.extensions(extensions.build());
        }
    }
}
