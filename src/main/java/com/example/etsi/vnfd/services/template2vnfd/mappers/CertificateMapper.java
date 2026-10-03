package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.CertificateDesc;
import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.toscatype.node.Certificate;
import java.util.Map;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;

/**
 * SOL001 V5.4.1 clause 6.8.19 {@code Certificate} to IFA011 V5.4.1 clause 7.1.19.2
 * {@code CertificateDesc}.
 */
public final class CertificateMapper implements NodeMapper<Certificate, CertificateDesc> {


    /** IFA011 clause 7.1.19.2. */
    @Override
    public Class<Certificate> nodeType() {
        return Certificate.class;
    }

    @Override
    public String id(Certificate node) {
        return VnfdUtils.nodeId(node);
    }

    @Override
    public void contribute(VnfdElements pool, String id, CertificateDesc element) {
        pool.addCertificateDesc(id, element);
    }

    @Override
    public CertificateDesc map(Certificate node, FlavourContext context) {
        CertificateDesc.Builder builder =
                CertificateDesc.builder(VnfdUtils.nodeId(node));
        Certificate.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName()).certificateType(p.getCertificateType());
            if (p.getCertificateBaseProfile() != null) {
                builder.certificateBaseProfile(
                        ToscaBindModule.mapper().convertValue(p.getCertificateBaseProfile(), Map.class));
            }
            if (p.getCsrRequirements() != null) {
                p.getCsrRequirements().forEach(r -> builder.addCsrRequirement(
                        ToscaBindModule.mapper().convertValue(r, Map.class)));
            }
        }
        return builder.build();
    }
}
