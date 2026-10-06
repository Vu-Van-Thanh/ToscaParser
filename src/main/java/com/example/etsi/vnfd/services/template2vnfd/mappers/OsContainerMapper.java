package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;

/**
 * SOL001 V5.4.1 clause 6.8.12 {@code Vdu.OsContainer} to IFA011 V5.4.1 clause 7.1.6.13
 * {@code OsContainerDesc}.
 *
 * <p>The CPU and the memory properties are typed differently on purpose: Table 6.8.12.2-1 declares
 * the CPU ones as {@code integer} in milli-CPU, while memory and ephemeral storage are
 * {@code scalar-unit.size}. Collapsing both to a number would lose the unit.
 */
public final class OsContainerMapper implements NodeMapper<VduOsContainer, OsContainerDesc> {


    @Override
    public Class<VduOsContainer> nodeType() {
        return VduOsContainer.class;
    }

    @Override
    public String id(VduOsContainer node) {
        return VnfdUtils.nodeId(node);
    }

    @Override
    public void contribute(VnfdElements pool, String id, OsContainerDesc element) {
        pool.addOsContainerDesc(id, element);
    }

    @Override
    public OsContainerDesc map(VduOsContainer node, FlavourContext context) {
        OsContainerDesc.Builder builder = OsContainerDesc.builder(VnfdUtils.nodeId(node));

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
            if (p.getExtendedResourceRequests() != null) {
                p.getExtendedResourceRequests()
                        .forEach(r -> builder.addExtendedResourceRequest(PlainValues.asMap(r)));
            }
            if (p.getHugePagesResources() != null) {
                p.getHugePagesResources()
                        .forEach(h -> builder.addHugePageResource(PlainValues.asMap(h)));
            }
            if (p.getCpuPinningRequirements() != null) {
                builder.cpuPinningRequirements(PlainValues.asMap(p.getCpuPinningRequirements()));
            }
        }

        // IFA011 clause 7.1.6.13.2 makes swImageDesc M,1; SOL001 clause 6.8.12.6 requires the
        // artifact and caps it at one. The id is the node template name, not the artifact name.
        if (context.artifactOfType(node, EtsiTypes.ARTIFACT_SW_IMAGE).isPresent()) {
            builder.swImageDesc(VnfdUtils.nodeId(node));
        }

        return builder.build();
    }
}
