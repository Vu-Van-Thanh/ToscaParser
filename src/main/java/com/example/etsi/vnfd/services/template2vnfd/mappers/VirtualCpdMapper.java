package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import java.util.Map;
import com.example.etsi.vnfd.model.VirtualCpd;
import com.example.etsi.vnfd.toscatype.node.VirtualCp;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/** SOL001 V5.4.1 clause 6.8.15 {@code VirtualCp} to IFA011 clause 7.1.18.2 {@code VirtualCpd}.
 *
 * <p>Like VipCp it targets rather than binds - here the deployable units implementing the
 * service. */
final class VirtualCpdMapper extends CpdMapper<VirtualCp, VirtualCpd> {

    @Override
    public Class<VirtualCp> nodeType() {
        return VirtualCp.class;
    }

    @Override
    public void contribute(VnfdElements pool, String id, VirtualCpd element) {
        pool.addVirtualCpd(id, element);
    }

    @Override
    public VirtualCpd map(VirtualCp node, FlavourContext context) {
        VirtualCpd.Builder builder = VirtualCpd.builder(VnfdUtils.cpdId(node));
        applyCommon(node, builder);

        VirtualCp.Properties p = node.getProperties();
        if (p != null && p.getAdditionalServiceData() != null) {
            p.getAdditionalServiceData().forEach(d -> builder.addAdditionalServiceData(
                    ToscaBindModule.mapper().convertValue(d, Map.class)));
        }
        VirtualCp.Requirements r = node.getRequirements();
        if (r != null) {
            VnfdUtils.orEmpty(r.getTarget()).forEach(builder::addVdu);
        }
        return builder.build();
    
    }
}
