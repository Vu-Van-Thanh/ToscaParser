package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.Cpd;
import com.example.etsi.vnfd.toscatype.data.CpProtocolData;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/**
 * What every connection point descriptor shares - IFA011 V5.4.1 clause 7.1.6.3 {@code Cpd}.
 *
 * <p>SOL001 gives the connection points five node types over four IFA011 elements, and the
 * attributes of {@code Cpd} itself are the same for all of them. Holding them here is what lets
 * each subclass be only the part that differs.
 *
 * @param <N> the SOL001 connection point type
 * @param <E> the IFA011 descriptor it produces
 */
abstract class CpdMapper<N extends Cp, E> implements NodeMapper<N, E> {

    /** [ASSUMPTION] The node template name - see {@code VnfdUtils.cpdId}. */
    @Override
    public String id(N node) {
        return VnfdUtils.nodeId(node);
    }

    /** The attributes of {@code Cpd}, which every connection point descriptor inherits. */
    static void applyCommon(Cp node, Cpd.AbstractBuilder<?> builder) {
        Cp.Properties p = node.getProperties();
        if (p == null) {
            return;
        }
        VnfdUtils.orEmpty(p.getLayerProtocols()).forEach(builder::addLayerProtocol);
        builder.cpRole(p.getRole());
        builder.description(p.getDescription());
        builder.trunkMode(p.getTrunkMode());
        if (p.getProtocol() != null) {
            for (CpProtocolData protocol : p.getProtocol()) {
                builder.addProtocol(PlainValues.asMap(protocol));
            }
        }
    }
}
