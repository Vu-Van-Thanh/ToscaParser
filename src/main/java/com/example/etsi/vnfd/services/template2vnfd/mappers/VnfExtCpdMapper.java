package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/**
 * SOL001 V5.4.1 clause 6.8.2 to IFA011 clause 7.1.3.2 {@code VnfExtCpd} - from either of its two
 * grammars.
 *
 * <p>Not a {@link NodeMapper}, because it does not read one node type. Clause 6.8.2.8 lets a
 * service template omit the VnfExtCp node entirely and expose a VduCp through
 * {@code substitution_mappings} instead, and SOL001 Table 6.1-1 lists both as sources of a
 * VnfExtCpd. They write to the same list, so the caller reads them in one pass - which is what
 * keeps them in declaration order rather than grouped by which grammar produced them.
 */
public final class VnfExtCpdMapper {

    private VnfExtCpdMapper() {
    }

    /** SOL001 clause 6.8.2 VnfExtCp, declared explicitly. */
    public static VnfExtCpd fromNode(VnfExtCp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(VnfdUtils.nodeId(node));
        CpdMapper.applyCommon(node, builder);
        if (node.getRequirements() != null) {
            // SOL001 Table 6.8.2.4-1 names them internal_virtual_link and external_virtual_link;
            // the internal one is what IFA011 calls intVirtualLinkDesc.
            VnfdUtils.first(node.getRequirements().getInternalVirtualLink())
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
    public static VnfExtCpd fromExposed(Cp node) {
        VnfExtCpd.Builder builder = VnfExtCpd.builder(VnfdUtils.nodeId(node));
        CpdMapper.applyCommon(node, builder);
        builder.intCpd(VnfdUtils.nodeId(node));
        builder.exposedThroughSubstitution(true);
        if (node instanceof VduCp && ((VduCp) node).getRequirements() != null) {
            VnfdUtils.first(((VduCp) node).getRequirements().getVirtualLink())
                    .ifPresent(builder::intVirtualLinkDesc);
        }
        return builder.build();
    
    }
}
