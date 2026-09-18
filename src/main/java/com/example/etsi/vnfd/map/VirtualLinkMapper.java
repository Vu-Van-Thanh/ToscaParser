package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;

/**
 * SOL001 V5.4.1 clause 6.8.9 {@code VnfVirtualLink} to IFA011 V5.4.1 clause 7.1.7.2
 * {@code VnfVirtualLinkDesc}.
 *
 * <p>{@code vl_profile} is deliberately not read here: IFA011 clause 7.1.8.2.2 puts the profile in
 * {@code VnfDf.virtualLinkProfile}, not in the descriptor, so the flavour mapper reads it - the same
 * split as {@code vdu_profile}.
 */
final class VirtualLinkMapper {

    private VirtualLinkMapper() {
    }

    static VnfVirtualLinkDesc map(VnfVirtualLink node) {
        VnfVirtualLinkDesc.Builder builder =
                VnfVirtualLinkDesc.builder(IdRegistry.virtualLinkDescId(node));
        VnfVirtualLink.Properties p = node.getProperties();
        if (p != null) {
            builder.description(p.getDescription());
        }
        return builder.build();
    }
}
