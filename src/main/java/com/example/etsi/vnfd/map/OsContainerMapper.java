package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.typedef.EtsiTypes;

/**
 * SOL001 V5.4.1 clause 6.8.12 {@code Vdu.OsContainer} to IFA011 V5.4.1 clause 7.1.6.13
 * {@code OsContainerDesc}.
 *
 * <p>The CPU and the memory properties are typed differently on purpose: Table 6.8.12.2-1 declares
 * the CPU ones as {@code integer} in milli-CPU, while memory and ephemeral storage are
 * {@code scalar-unit.size}. Collapsing both to a number would lose the unit.
 */
final class OsContainerMapper {

    private OsContainerMapper() {
    }

    static OsContainerDesc map(VduOsContainer node, ArtifactSelector artifacts) {
        OsContainerDesc.Builder builder = OsContainerDesc.builder(IdRegistry.osContainerDescId(node));

        VduOsContainer.Properties p = node.getProperties();
        if (p != null) {
            builder.name(p.getName())
                   .description(p.getDescription())
                   .requestedCpuResources(p.getRequestedCpuResources())
                   .cpuResourceLimit(p.getCpuResourceLimit())
                   .requestedMemoryResources(p.getRequestedMemoryResources())
                   .memoryResourceLimit(p.getMemoryResourceLimit())
                   .requestedEphemeralStorageResources(p.getRequestedEphemeralStorageResources())
                   .ephemeralStorageResourceLimit(p.getEphemeralStorageResourceLimit());
        }

        // IFA011 clause 7.1.6.13.2 makes swImageDesc M,1; SOL001 clause 6.8.12.6 requires the
        // artifact and caps it at one. The id is the node template name, not the artifact name.
        if (artifacts.ofType(node, EtsiTypes.ARTIFACT_SW_IMAGE).isPresent()) {
            builder.swImageDesc(IdRegistry.swImageDescId(node));
        }

        return builder.build();
    }
}
