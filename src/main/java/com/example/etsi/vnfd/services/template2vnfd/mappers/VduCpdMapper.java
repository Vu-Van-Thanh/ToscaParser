package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/** SOL001 V5.4.1 clause 6.8.8 {@code VduCp} (and clause 6.8.11 {@code VduSubCp}) to IFA011
 * clause 7.1.6.4 {@code VduCpd}. */
final class VduCpdMapper extends CpdMapper<VduCp, VduCpd> {

    @Override
    public Class<VduCp> nodeType() {
        return VduCp.class;
    }

    @Override
    public void contribute(VnfdElements pool, String id, VduCpd element) {
        pool.addVduCpd(id, element);
    }

    @Override
    public VduCpd map(VduCp node, FlavourContext context) {
        VduCpd.Builder builder = VduCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);
        if (node.getRequirements() != null) {
            VnfdUtils.first(node.getRequirements().getVirtualBinding())
                    .ifPresent(builder::vduId);
            VnfdUtils.first(node.getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    
    }
}
