package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.model.Vnfd;
import java.util.LinkedHashMap;
import java.util.Map;

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
     */
    Vdu addVdu(String id, Vdu vdu) {
        vdus.putIfAbsent(id, vdu);
        return vdus.get(id);
    }

    void addOsContainerDesc(String id, OsContainerDesc desc) {
        containers.putIfAbsent(id, desc);
    }

    void addSwImageDesc(String id, SwImageDesc desc) {
        images.putIfAbsent(id, desc);
    }

    void addVduCpd(String id, VduCpd cpd) {
        vduCpds.putIfAbsent(id, cpd);
    }

    void addVnfExtCpd(String id, VnfExtCpd cpd) {
        extCpds.putIfAbsent(id, cpd);
    }

    void addIntVirtualLinkDesc(String id, VnfVirtualLinkDesc desc) {
        links.putIfAbsent(id, desc);
    }

    void addVirtualStorageDesc(String id, VirtualStorageDesc desc) {
        storages.putIfAbsent(id, desc);
    }

    void addVipCpd(String id, VipCpd cpd) {
        vipCpds.putIfAbsent(id, cpd);
    }

    void addVirtualCpd(String id, VirtualCpd cpd) {
        virtualCpds.putIfAbsent(id, cpd);
    }

    void addCertificateDesc(String id, CertificateDesc desc) {
        certificates.putIfAbsent(id, desc);
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
