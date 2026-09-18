package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One deployment flavour, assembled from everything the flavour topology declares.
 *
 * <p>The only mapper that reads no single node type. IFA011 V5.4.1 clause 7.1.8.2.2 collects into
 * {@code VnfDf} the parts of the descriptor that say how the VNF is deployed rather than what it is
 * made of - the profiles, the levels, the scaling aspects - and each sits on a different element in
 * TOSCA. SOL001 clauses 6.11.2 and 6.11.3 both make one service template one flavour, so the scope
 * of this mapper is exactly one service template.
 *
 * <p>Order matters once: the affinity assignments are read before any profile is built, because
 * SOL001 Table 6.1-1 NOTE 3 puts {@code affinityOrAntiAffinityGroupId} on the profile while the
 * policy names the profiled element as its target. Reading the policies first keeps the profiles
 * immutable instead of building them and patching them afterwards.
 */
final class DeploymentFlavourMapper {

    private final PolicyMapper policies;
    private final MciopMapper mciops;

    DeploymentFlavourMapper(TypeHierarchy hierarchy, ArtifactSelector artifacts, ObjectMapper mapper) {
        this.policies = new PolicyMapper(hierarchy, mapper);
        this.mciops = new MciopMapper(artifacts, mapper);
    }

    /** A mapped flavour plus the elements IFA011 keeps at VNFD level. */
    static final class Result {
        final VnfDf df;
        final List<LcmOpParameterMappingScript> scripts = new ArrayList<>();
        final List<MciopArtifacts> mciopArtifacts = new ArrayList<>();
        final List<String> mciopIds = new ArrayList<>();

        Result(VnfDf df) {
            this.df = df;
        }
    }

    Result map(FlavourContext context) {
        String flavourId = flavourId(context).orElse("");
        VnfDf.Builder builder = VnfDf.builder(flavourId);
        builder.sourceFile(context.template().file());
        flavourDescription(context).ifPresent(builder::description);

        PolicyMapper.AffinityAssignment affinity = policies.affinity(context);
        affinity.groups().forEach(builder::addAffinityGroup);

        Map<String, Integer> minInstances = new LinkedHashMap<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            VduProfile profile = vduProfile(vdu, affinity);
            builder.addVduProfile(profile);
            profile.getMinNumberOfInstances().flatMap(v -> v.resolved())
                    .ifPresent(min -> minInstances.put(profile.getVduId(), min));
        }

        for (Mciop mciop : context.mciops()) {
            mciops.check(mciop, context.findings());
            builder.addMciopProfile(mciops.mapProfile(mciop));
        }

        policies.scalingAspects(context).forEach(builder::addScalingAspect);

        List<InstantiationLevel> levels = policies.instantiationLevels(context);
        if (levels.isEmpty()) {
            levels = Collections.singletonList(PolicyMapper.synthesiseLevel(minInstances));
        }
        levels.forEach(builder::addInstantiationLevel);
        policies.defaultInstantiationLevelId(context).ifPresent(builder::defaultInstantiationLevelId);

        Result result = new Result(builder.build());
        for (Mciop mciop : context.mciops()) {
            result.mciopIds.add(IdRegistry.mciopId(mciop));
            mciops.mapScript(mciop).ifPresent(result.scripts::add);
            mciops.mapArtifacts(mciop).ifPresent(result.mciopArtifacts::add);
        }
        return result;
    }

    /**
     * Which flavour this service template describes.
     *
     * <p>Three sources in order: {@code substitution_mappings.substitution_filter}, the form clause
     * 6.11.2 d) prescribes for a lower-level template; {@code properties.flavour_id} on the VNF node
     * template, which the single-flavour design of clause 6.11.3 uses; and the {@code default} on
     * the VNF node type, which {@code TypeDefaults} has already laid underneath by the time the
     * node was bound.
     */
    private Optional<String> flavourId(FlavourContext context) {
        Optional<String> fromFilter = context.topology().substitutionMappings()
                .flatMap(m -> m.filterEqualValue("flavour_id"));
        if (fromFilter.isPresent()) {
            return fromFilter;
        }
        return context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourId())
                .flatMap(v -> v == null ? Optional.empty() : v.resolved());
    }

    private Optional<String> flavourDescription(FlavourContext context) {
        return context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourDescription())
                .flatMap(v -> v == null ? Optional.<String>empty() : v.resolved());
    }

    /** SOL001 Table 6.8.13.2-1 {@code vdu_profile} to IFA011 clause 7.1.8.3 {@code VduProfile}. */
    private VduProfile vduProfile(VduOsContainerDeployableUnit vdu,
            PolicyMapper.AffinityAssignment affinity) {
        VduProfile.Builder builder = VduProfile.builder(IdRegistry.vduId(vdu));
        if (vdu.getProperties() != null && vdu.getProperties().getVduProfile() != null) {
            com.example.etsi.vnfd.toscatype.data.VduProfile p = vdu.getProperties().getVduProfile();
            builder.minNumberOfInstances(p.getMinNumberOfInstances())
                   .maxNumberOfInstances(p.getMaxNumberOfInstances());
            FlavourContext.orEmpty(p.getModifyCapacityAttributesOp())
                    .forEach(builder::addModifyCapacityAttributesOp);
        }
        affinity.groupsOf(vdu.getKey()).forEach(builder::addAffinityGroup);
        return builder.build();
    }
}
