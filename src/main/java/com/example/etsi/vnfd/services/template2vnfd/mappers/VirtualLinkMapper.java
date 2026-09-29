package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;

/**
 * SOL001 V5.4.1 clause 6.8.9 {@code VnfVirtualLink} to IFA011 V5.4.1 clause 7.1.7.2
 * {@code VnfVirtualLinkDesc}.
 *
 * <p>{@code vl_profile} is deliberately not read here: IFA011 clause 7.1.8.2.2 puts the profile in
 * {@code VnfDf.virtualLinkProfile}, not in the descriptor, so the flavour mapper reads it - the same
 * split as {@code vdu_profile}.
 */
public final class VirtualLinkMapper implements NodeMapper<VnfVirtualLink, VnfVirtualLinkDesc> {


    @Override
    public Class<VnfVirtualLink> nodeType() {
        return VnfVirtualLink.class;
    }

    @Override
    public String id(VnfVirtualLink node) {
        return VnfdUtils.virtualLinkDescId(node);
    }

    @Override
    public void contribute(VnfdElements pool, String id, VnfVirtualLinkDesc element) {
        pool.addIntVirtualLinkDesc(id, element);
    }

    @Override
    public VnfVirtualLinkDesc map(VnfVirtualLink node, FlavourContext context) {
        VnfVirtualLinkDesc.Builder builder =
                VnfVirtualLinkDesc.builder(VnfdUtils.virtualLinkDescId(node));
        VnfVirtualLink.Properties p = node.getProperties();
        if (p != null) {
            builder.description(p.getDescription());
        }
        return builder.build();
    }
}
