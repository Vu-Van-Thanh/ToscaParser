package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.DeployableModule;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;

/**
 * SOL001 V5.4.1 clause 6.8.16 {@code DeployableModule} to IFA011 V5.4.1 clause 7.1.8.24 -
 * an attribute of the deployment flavour, not of the VNFD.
 */
public final class DeployableModuleMapper {

    private DeployableModuleMapper() {
    }

    /** IFA011 clause 7.1.8.24 - an attribute of the deployment flavour, not of the VNFD. */
    public static DeployableModule map(
            com.example.etsi.vnfd.toscatype.node.DeployableModule node) {
        DeployableModule.Builder builder =
                DeployableModule.builder(VnfdUtils.deployableModuleId(node));
        com.example.etsi.vnfd.toscatype.node.DeployableModule.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName()).description(p.getDescription());
        }
        com.example.etsi.vnfd.toscatype.node.DeployableModule.Requirements r = node.getRequirements();
        if (r != null) {
            // Table 6.8.16.4-1: member has occurrences [1, UNBOUNDED].
            VnfdUtils.orEmpty(r.getMember()).forEach(builder::addMember);
        }
        return builder.build();
    }
}
