package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.Cpd;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import java.util.Map;

/**
 * The connection points, SOL001 V5.4.1 clauses 6.8.8 and 6.8.2 to IFA011 clauses 7.1.6.4 and 7.1.3.2.
 *
 * <p>One node template can become two information elements. SOL001 clause 6.8.2.8 lets a service
 * template omit a VnfExtCp node entirely and expose a VduCp through {@code substitution_mappings}
 * instead, and SOL001 Table 6.1-1 lists both VnfExtCp and VduCp as sources of a {@code VnfExtCpd}.
 * Such a connection point is emitted as a {@code VduCpd} - it still binds a VDU - and as a
 * {@code VnfExtCpd}, with {@code exposedThroughSubstitution} recording which grammar produced it.
 */
final class CpMapper {

    private CpMapper() {
    }

    /** SOL001 clause 6.8.8 VduCp (and clause 6.8.11 VduSubCp) to VduCpd. */
    static VduCpd mapVduCp(VduCp node) {
        VduCpd.Builder builder = VduCpd.builder(IdRegistry.cpdId(node));
        common(node, builder);
        if (node.getRequirements() != null) {
            FlavourContext.first(node.getRequirements().getVirtualBinding())
                    .ifPresent(builder::vduId);
            FlavourContext.first(node.getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    }

    /** SOL001 clause 6.8.2 VnfExtCp, declared explicitly, to VnfExtCpd. */
    static VnfExtCpd mapVnfExtCp(VnfExtCp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(IdRegistry.cpdId(node));
        common(node, builder);
        if (node.getRequirements() != null) {
            // SOL001 Table 6.8.2.4-1 names them internal_virtual_link and external_virtual_link;
            // the internal one is what IFA011 calls intVirtualLinkDesc.
            FlavourContext.first(node.getRequirements().getInternalVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        builder.exposedThroughSubstitution(false);
        return builder.build();
    }

    /**
     * A connection point exposed through {@code substitution_mappings} rather than by a VnfExtCp
     * node - SOL001 clause 6.8.2.8. {@code intCpd} points back at the internal connection point the
     * external one stands for, which is what lets a consumer follow it down to its VDU.
     */
    static VnfExtCpd mapExposedCp(Cp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(IdRegistry.cpdId(node));
        common(node, builder);
        builder.intCpd(IdRegistry.cpdId(node));
        builder.exposedThroughSubstitution(true);
        if (node instanceof VduCp && ((VduCp) node).getRequirements() != null) {
            FlavourContext.first(((VduCp) node).getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    }

    /** What every connection point descriptor shares - IFA011 clause 7.1.6.3 {@code Cpd}. */
    private static void common(Cp node, Cpd.AbstractBuilder<?> builder) {
        Cp.Properties p = node.getProperties();
        if (p == null) {
            return;
        }
        FlavourContext.orEmpty(p.getLayerProtocols()).forEach(builder::addLayerProtocol);
        builder.cpRole(p.getRole());
        builder.description(p.getDescription());
        builder.trunkMode(p.getTrunkMode());
        if (p.getProtocol() != null) {
            for (Object protocol : p.getProtocol()) {
                builder.addProtocol(java.util.Collections.singletonMap("protocol", protocol));
            }
        }
    }
}
