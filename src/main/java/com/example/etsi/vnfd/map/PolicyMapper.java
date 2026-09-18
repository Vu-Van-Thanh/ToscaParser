package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.AffinityOrAntiAffinityGroup;
import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.ScalingAspect;
import com.example.etsi.vnfd.model.VduLevel;
import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.toscatype.policy.AffinityRule;
import com.example.etsi.vnfd.toscatype.policy.InstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.ScalingAspects;
import com.example.etsi.vnfd.toscatype.policy.VduInstantiationLevels;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The policies of one flavour, SOL001 V5.4.1 clause 6.10 to the IFA011 clause 7.1.8 elements of a
 * {@code VnfDf}.
 *
 * <p>Policies are the part of the descriptor furthest from the information model. A TOSCA policy
 * names its targets and carries a map keyed by level or aspect id; IFA011 wants the transpose - a
 * level that lists its VDUs. So each method reads every policy of one kind at once rather than
 * mapping them one by one.
 */
final class PolicyMapper {

    /** SOL001 V5.4.1 clause 6.9.1. Expanded before a policy target is interpreted. */
    private static final String PLACEMENT_GROUP = "tosca.groups.nfv.PlacementGroup";

    private final TypeHierarchy hierarchy;
    private final ObjectMapper mapper;

    PolicyMapper(TypeHierarchy hierarchy, ObjectMapper mapper) {
        this.hierarchy = hierarchy;
        this.mapper = mapper;
    }

    /**
     * The instantiation levels of the flavour.
     *
     * <p>Table 7.1.8.7.2-1 makes {@code levelId} M,1, {@code description} M,1 and {@code vduLevel}
     * M,1..N, so a level naming no VDU is not a valid element - which is why the per-VDU policies
     * are folded into the levels rather than kept beside them.
     */
    List<InstantiationLevel> instantiationLevels(FlavourContext context) {
        Map<String, InstantiationLevel.Builder> builders = new LinkedHashMap<>();

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_INSTANTIATION_LEVELS)) {
            InstantiationLevels policy = bind(definition, InstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, level) -> {
                InstantiationLevel.Builder b =
                        builders.computeIfAbsent(levelId, InstantiationLevel::builder);
                if (level != null && level.getDescription() != null) {
                    b.description(level.getDescription().resolved().orElse(null));
                }
            });
        }

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_INSTANTIATION_LEVELS)) {
            VduInstantiationLevels policy = bind(definition, VduInstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, vduLevel) -> {
                InstantiationLevel.Builder b = builders.get(levelId);
                if (b == null || vduLevel == null || vduLevel.getNumberOfInstances() == null) {
                    return;
                }
                Integer count = vduLevel.getNumberOfInstances().resolved().orElse(null);
                if (count != null) {
                    for (String vduId : FlavourContext.orEmpty(definition.targets())) {
                        b.addVduLevel(VduLevel.of(vduId, count));
                    }
                }
            });
        }

        List<InstantiationLevel> levels = new ArrayList<>();
        builders.values().forEach(b -> levels.add(b.build()));
        return levels;
    }

    /** The default level id, when an InstantiationLevels policy names one. */
    Optional<String> defaultInstantiationLevelId(FlavourContext context) {
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_INSTANTIATION_LEVELS)) {
            InstantiationLevels policy = bind(definition, InstantiationLevels.class);
            if (policy.getProperties() != null && policy.getProperties().getDefaultLevel() != null) {
                Optional<String> value = policy.getProperties().getDefaultLevel().resolved();
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The single level a descriptor without any InstantiationLevels policy implies.
     *
     * <p>[MANO INTERPRETATION] IFA011 clause 7.1.8.2.2 requires at least one level and Table
     * 7.1.8.7.2-1 requires each to carry at least one VduLevel, so an empty list cannot be emitted.
     * The count comes from {@code VduProfile.minNumberOfInstances} - the smallest deployment that
     * still satisfies the flavour - and the result is flagged so a consumer can tell it apart from
     * a level the descriptor actually declared.
     */
    static InstantiationLevel synthesiseLevel(Map<String, Integer> minInstancesByVdu) {
        InstantiationLevel.Builder builder = InstantiationLevel.builder("default")
                .description("Synthesised - the descriptor declares no InstantiationLevels policy")
                .synthesised(true);
        minInstancesByVdu.forEach((vduId, count) -> builder.addVduLevel(VduLevel.of(vduId, count)));
        return builder.build();
    }

    /** The scaling aspects, SOL001 clause 6.10.5. */
    List<ScalingAspect> scalingAspects(FlavourContext context) {
        List<ScalingAspect> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_SCALING_ASPECTS)) {
            ScalingAspects policy = bind(definition, ScalingAspects.class);
            if (policy.getProperties() == null || policy.getProperties().getAspects() == null) {
                continue;
            }
            policy.getProperties().getAspects().forEach((id, aspect) -> {
                ScalingAspect.Builder b = ScalingAspect.builder(id);
                if (aspect != null) {
                    resolved(aspect.getName()).ifPresent(b::name);
                    resolved(aspect.getDescription()).ifPresent(b::description);
                    if (aspect.getMaxScaleLevel() != null) {
                        aspect.getMaxScaleLevel().resolved().ifPresent(b::maxScaleLevel);
                    }
                }
                out.add(b.build());
            });
        }
        return out;
    }

    /**
     * The affinity groups, and which element each applies to.
     *
     * <p>SOL001 Table 6.1-1 NOTE 3: {@code affinityOrAntiAffinityGroupId} is not a property of the
     * profiled element - the policy names its targets, so the assignment has to be read backwards.
     * The returned map is keyed by node template name, which is also the id of the VduProfile,
     * MciopProfile or VirtualLinkProfile that target becomes.
     */
    AffinityAssignment affinity(FlavourContext context) {
        List<AffinityOrAntiAffinityGroup> groups = new ArrayList<>();
        Map<String, List<String>> byTarget = new LinkedHashMap<>();

        for (PolicyDefinition definition : context.policies()) {
            boolean anti = hierarchy.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_ANTI_AFFINITY_RULE);
            boolean plain = hierarchy.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_AFFINITY_RULE);
            if (!anti && !plain) {
                continue;
            }
            // Both types declare the same properties, so one class reads either.
            AffinityRule policy = bind(definition, AffinityRule.class);
            String groupId = definition.name();
            String scope = policy.getProperties() == null
                    ? null
                    : resolved(policy.getProperties().getScope()).orElse(null);

            groups.add(AffinityOrAntiAffinityGroup.of(groupId,
                    anti ? AffinityOrAntiAffinityGroup.AffinityType.ANTI_AFFINITY
                         : AffinityOrAntiAffinityGroup.AffinityType.AFFINITY,
                    scope));
            for (String target : expand(definition.targets(), context)) {
                byTarget.computeIfAbsent(target, key -> new ArrayList<>()).add(groupId);
            }
        }
        return new AffinityAssignment(groups, byTarget);
    }

    /** Replaces every PlacementGroup target by its members - SOL001 clause 6.9.1. */
    private List<String> expand(List<String> targets, FlavourContext context) {
        List<String> out = new ArrayList<>();
        for (String target : FlavourContext.orEmpty(targets)) {
            GroupDefinition group = context.topology().groups().get(target);
            if (group != null && hierarchy.isDerivedFrom(group.type(), PLACEMENT_GROUP)) {
                out.addAll(group.members());
            } else {
                out.add(target);
            }
        }
        return out;
    }

    private List<PolicyDefinition> of(FlavourContext context, String etsiType) {
        List<PolicyDefinition> out = new ArrayList<>();
        for (PolicyDefinition definition : context.policies()) {
            if (hierarchy.isDerivedFrom(definition.type(), etsiType)) {
                out.add(definition);
            }
        }
        return out;
    }

    private <T> T bind(PolicyDefinition definition, Class<T> target) {
        return mapper.convertValue(
                Collections.singletonMap("properties", definition.properties()), target);
    }

    private static Optional<String> resolved(
            com.example.etsi.vnfd.template.value.PropertyValue<String> value) {
        return value == null ? Optional.empty() : value.resolved();
    }

    /** Affinity groups together with the elements each one applies to. */
    static final class AffinityAssignment {

        private final List<AffinityOrAntiAffinityGroup> groups;
        private final Map<String, List<String>> byTarget;

        AffinityAssignment(List<AffinityOrAntiAffinityGroup> groups,
                Map<String, List<String>> byTarget) {
            this.groups = groups;
            this.byTarget = byTarget;
        }

        List<AffinityOrAntiAffinityGroup> groups() {
            return groups;
        }

        List<String> groupsOf(String nodeTemplateName) {
            return byTarget.getOrDefault(nodeTemplateName, Collections.emptyList());
        }
    }
}
