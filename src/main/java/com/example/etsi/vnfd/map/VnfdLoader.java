package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.OsContainerDesc;
import com.example.etsi.vnfd.model.SwImageDesc;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfVirtualLinkDesc;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.model.ext.VnfdExtensions;
import com.example.etsi.vnfd.template.ServiceToscaTemplate;
import com.example.etsi.vnfd.template.ToscaDescriptorTemplate;
import com.example.etsi.vnfd.toscatype.bind.NodeBinder;
import com.example.etsi.vnfd.toscatype.bind.NodeTypes;
import com.example.etsi.vnfd.toscatype.bind.ToscaBindModule;
import com.example.etsi.vnfd.toscatype.node.Cp;
import com.example.etsi.vnfd.toscatype.node.VduCp;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfExtCp;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.example.etsi.vnfd.validation.Findings;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Turns a parsed TOSCA package into a VNFD.
 *
 * <p>The entry point of the second half of the library. {@code YamlService} answers what the package
 * says; this answers what it means. The split follows the two specifications - everything up to
 * {@link ServiceToscaTemplate} is TOSCA and SOL001 vocabulary, everything produced here is IFA011.
 *
 * <p>Each service template is one deployment flavour (SOL001 V5.4.1 clauses 6.11.2 and 6.11.3), so
 * flavours are mapped one at a time and their contributions merged by identifier: IFA011 clause
 * 7.1.2.2 keeps the descriptors themselves - vdu, osContainerDesc, swImageDesc, the connection
 * points - at VNFD level, shared by every flavour.
 */
public final class VnfdLoader {

    private final ObjectMapper mapper = ToscaBindModule.mapper();

    /** Parses a package, reporting only what this stage notices. */
    public ParseResult load(ServiceToscaTemplate template) {
        return load(template, new Findings());
    }

    /**
     * Parses into a collector the caller already holds, so package-level findings survive.
     *
     * <p>Reading the package and interpreting it are two stages with their own findings - a missing
     * manifest is noticed by {@code YamlService}, long before any VNFD exists. Passing the same
     * collector to both is what puts them in one result.
     */
    public ParseResult load(ServiceToscaTemplate template, Findings findings) {
        List<ToscaDescriptorTemplate> flavours = template.flavourTemplates();
        if (flavours.isEmpty()) {
            throw new VnfdParseException("The package contains no service template with a "
                    + "topology_template; there is nothing to parse into a VNFD");
        }

        TypeHierarchy hierarchy = new TypeHierarchy(template.typeRegistry());
        ArtifactSelector artifacts = new ArtifactSelector(hierarchy);
        NodeBinder binder = new NodeBinder(hierarchy, NodeTypes.ALL, findings);
        SwImageMapper swImages = new SwImageMapper(mapper);
        DeploymentFlavourMapper flavourMapper =
                new DeploymentFlavourMapper(hierarchy, artifacts, mapper);

        Vnfd.Builder builder = Vnfd.builder();
        Merged merged = new Merged();
        Set<String> mciopIds = new LinkedHashSet<>();
        Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
        boolean headerRead = false;

        for (ToscaDescriptorTemplate flavour : flavours) {
            FlavourContext context = new FlavourContext(flavour, binder, findings);

            Optional<Vnf> vnf = context.vnf();
            if (vnf.isPresent() && !headerRead) {
                VnfHeaderMapper.map(vnf.get(), builder);
                headerRead = true;
            }

            List<Vdu> flavourVdus = collectNodes(context, artifacts, swImages, merged);

            DeploymentFlavourMapper.Result result = flavourMapper.map(context);
            SpecRules.flavour(result.df, flavourVdus, findings);
            builder.addDf(result.df);
            result.scripts.forEach(builder::addLcmOpParameterMappingScript);
            mciopIds.addAll(result.mciopIds);
            result.mciopArtifacts.forEach(a -> mciopArtifacts.putIfAbsent(a.getMciopId(), a));
        }

        merged.vdus.values().forEach(builder::addVdu);
        merged.containers.values().forEach(builder::addOsContainerDesc);
        merged.images.values().forEach(builder::addSwImageDesc);
        merged.vduCpds.values().forEach(builder::addVduCpd);
        merged.extCpds.values().forEach(builder::addVnfExtCpd);
        merged.links.values().forEach(builder::addIntVirtualLinkDesc);
        mciopIds.forEach(builder::addMciopId);

        if (!mciopArtifacts.isEmpty()) {
            VnfdExtensions.Builder extensions = VnfdExtensions.builder();
            mciopArtifacts.values().forEach(extensions::addMciopArtifacts);
            builder.extensions(extensions.build());
        }

        Vnfd vnfd = builder.build();
        SpecRules.note6(vnfd, findings);
        return new ParseResult(vnfd, template, findings);
    }

    /** Maps every node of one flavour into the shared pool, and returns that flavour's VDUs. */
    private List<Vdu> collectNodes(FlavourContext context, ArtifactSelector artifacts,
            SwImageMapper swImages, Merged merged) {

        List<Vdu> flavourVdus = new ArrayList<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            String id = IdRegistry.vduId(vdu);
            merged.vdus.putIfAbsent(id, VduMapper.map(vdu, context));
            flavourVdus.add(merged.vdus.get(id));
        }
        for (VduOsContainer container : context.containers().values()) {
            SpecRules.swImage(container, artifacts, context.findings());
            merged.containers.putIfAbsent(IdRegistry.osContainerDescId(container),
                    OsContainerMapper.map(container, artifacts));
            artifacts.ofType(container, EtsiTypes.ARTIFACT_SW_IMAGE).ifPresent(image ->
                    merged.images.putIfAbsent(IdRegistry.swImageDescId(container),
                            swImages.map(image, container)));
        }
        for (Cp cp : context.connectionPoints().values()) {
            if (cp instanceof VduCp) {
                merged.vduCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapVduCp((VduCp) cp));
            }
            // SOL001 clause 6.8.2.8: a VduCp exposed through substitution_mappings is also an
            // external CP, so one node template becomes two information elements.
            if (cp instanceof VnfExtCp) {
                merged.extCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapVnfExtCp((VnfExtCp) cp));
            } else if (context.isExternallyExposed(cp.getKey())) {
                merged.extCpds.putIfAbsent(IdRegistry.cpdId(cp), CpMapper.mapExposedCp(cp));
            }
        }
        for (VnfVirtualLink link : context.virtualLinks().values()) {
            merged.links.putIfAbsent(IdRegistry.virtualLinkDescId(link), VirtualLinkMapper.map(link));
        }
        return flavourVdus;
    }

    /**
     * The VNFD-level elements, keyed by identifier so two flavours describing the same VDU
     * contribute it once. First declaration wins; order of first appearance is kept.
     */
    private static final class Merged {
        final Map<String, Vdu> vdus = new LinkedHashMap<>();
        final Map<String, OsContainerDesc> containers = new LinkedHashMap<>();
        final Map<String, SwImageDesc> images = new LinkedHashMap<>();
        final Map<String, VduCpd> vduCpds = new LinkedHashMap<>();
        final Map<String, VnfExtCpd> extCpds = new LinkedHashMap<>();
        final Map<String, VnfVirtualLinkDesc> links = new LinkedHashMap<>();
    }
}
