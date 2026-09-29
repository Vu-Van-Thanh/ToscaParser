package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.VipCpd;
import com.example.etsi.vnfd.toscatype.node.VipCp;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/** SOL001 V5.4.1 clause 6.8.10 {@code VipCp} to IFA011 clause 7.1.17.2 {@code VipCpd}.
 *
 * <p>Cannot go through the VduCp path: it carries a {@code target} requirement naming the
 * VduCps that will share the address, where a VduCp carries {@code virtual_binding}. */
final class VipCpdMapper extends CpdMapper<VipCp, VipCpd> {

    @Override
    public Class<VipCp> nodeType() {
        return VipCp.class;
    }

    @Override
    public void contribute(VnfdElements pool, String id, VipCpd element) {
        pool.addVipCpd(id, element);
    }

    @Override
    public VipCpd map(VipCp node, FlavourContext context) {
        VipCpd.Builder builder = VipCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);

        VipCp.Properties p = node.getProperties();
        if (p != null) {
            builder.dedicatedIpAddress(p.getDedicatedIpAddress())
                   .vipFunction(p.getVipFunction());
        }
        VipCp.Requirements r = node.getRequirements();
        if (r != null) {
            // Table 6.8.10.4-1: target has occurrences [1, UNBOUNDED] and points at VduCp nodes,
            // which is exactly IFA011 intCpd (M,1..N, a reference to VduCpd).
            VnfdUtils.orEmpty(r.getTarget()).forEach(builder::addIntCpd);
            VnfdUtils.first(r.getVirtualLink()).ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    
    }
}
