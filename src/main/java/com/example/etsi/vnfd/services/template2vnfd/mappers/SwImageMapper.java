package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.artifact.SwImage;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import java.util.Collections;

/**
 * SOL001 V5.4.1 clause 6.3.1 {@code tosca.artifacts.nfv.SwImage} to IFA011 V5.4.1 clause 7.1.6.5
 * {@code SwImageDesc}.
 *
 * <p>The identifier is the one rule SOL001 states outright rather than leaving to the data model:
 * clause 6.8.12.6 says the node template name of the owning {@code Vdu.OsContainer} "fulfils the
 * purpose of the 'id' attribute of the SwImageDesc information element". So the id comes from the
 * node, not from the artifact definition's own name.
 */
public final class SwImageMapper {

    private SwImageMapper() {
    }

    public static SwImageDesc map(ArtifactDefinition definition, NfvNode owner) {
        SwImage.Properties p = ToscaBindModule.mapper().convertValue(
                Collections.singletonMap("properties", definition.properties()),
                SwImage.class).getProperties();

        SwImageDesc.Builder builder = SwImageDesc.builder(VnfdUtils.nodeId(owner));
        if (p != null) {
            builder.name(p.getName())
                   .version(p.getVersion())
                   .provider(p.getProvider())
                   .checksum(p.getChecksum())
                   .containerFormat(p.getContainerFormat())
                   .diskFormat(p.getDiskFormat())
                   .size(p.getSize())
                   .minDisk(p.getMinDisk())
                   .minRam(p.getMinRam())
                   .operatingSystem(p.getOperatingSystem());
        }
        return builder
                // IFA011 clause 7.1.6.5.2 swImage is "a reference to the actual software image";
                // the resolved form is what a consumer can act on.
                .swImage(VnfdUtils.pathOf(definition))
                .build();
    }
}
