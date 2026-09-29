package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.AffinityOrAntiAffinityGroup;
import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.ScaleInfo;
import com.example.etsi.vnfd.model.ScalingAspect;
import com.example.etsi.vnfd.model.ScalingDelta;
import com.example.etsi.vnfd.model.SecurityGroupRule;
import com.example.etsi.vnfd.model.VduCpd;
import com.example.etsi.vnfd.model.VduLevel;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VirtualLinkBitRateLevel;
import com.example.etsi.vnfd.model.VirtualLinkProfile;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.VnfExtCpd;
import com.example.etsi.vnfd.model.VnfPackageChangeInfo;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.template.GroupDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.policy.AffinityRule;
import com.example.etsi.vnfd.toscatype.policy.InstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.ScalingAspects;
import com.example.etsi.vnfd.toscatype.policy.VduInitialDelta;
import com.example.etsi.vnfd.toscatype.policy.VduInstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.VduScalingAspectDeltas;
import com.example.etsi.vnfd.toscatype.policy.VirtualLinkInstantiationLevels;
import com.example.etsi.vnfd.toscatype.policy.VnfPackageChange;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
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

    private PolicyMapper() {
    }

    /**
     * The instantiation levels of the flavour.
     *
     * <p>Table 7.1.8.7.2-1 makes {@code levelId} M,1, {@code description} M,1 and {@code vduLevel}
     * M,1..N, so a level naming no VDU is not a valid element - which is why the per-VDU policies
     * are folded into the levels rather than kept beside them.
     */
    static List<InstantiationLevel> instantiationLevels(FlavourContext context) {
        Map<String, InstantiationLevel.Builder> builders = new LinkedHashMap<>();

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_INSTANTIATION_LEVELS)) {
            InstantiationLevels policy = bind(definition, InstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, level) -> {
                InstantiationLevel.Builder b =
                        builders.computeIfAbsent(levelId, InstantiationLevel::builder);
                if (level == null) {
                    return;
                }
                if (level.getDescription() != null) {
                    b.description(level.getDescription().resolved().orElse(null));
                }
                // IFA011 clause 7.1.8.7.2 gives InstantiationLevel.scaleInfo 0..N: for each aspect,
                // the scale level this instantiation level corresponds to. SOL001 clause 6.10.1
                // writes it as a map keyed by aspectId.
                if (level.getScaleInfo() != null) {
                    level.getScaleInfo().forEach((aspectId, info) -> {
                        if (info == null || info.getScaleLevel() == null) {
                            return;
                        }
                        info.getScaleLevel().resolved()
                                .ifPresent(lvl -> b.addScaleInfo(ScaleInfo.of(aspectId, lvl)));
                    });
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
                    for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                        b.addVduLevel(VduLevel.of(vduId, count));
                    }
                }
            });
        }

        applyVirtualLinkLevels(context, builders);

        List<InstantiationLevel> levels = new ArrayList<>();
        builders.values().forEach(b -> levels.add(b.build()));
        return levels;
    }

    /** The default level id, when an InstantiationLevels policy names one. */
    static Optional<String> defaultInstantiationLevelId(FlavourContext context) {
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

    /**
     * SOL001 V5.4.1 clause 6.10.6 {@code VduScalingAspectDeltas} to the {@code ScalingDelta}
     * elements of IFA011 V5.4.1 clause 7.1.10.4, keyed by the aspect they belong to.
     *
     * <p>SOL001 writes {@code deltas} as a map whose key is the scalingDeltaId and whose value gives
     * the instance count, with the policy targets naming the VDUs. IFA011 turns that inside out:
     * one ScalingDelta carries a {@code vduDelta} entry per VDU. So two policies naming the same
     * delta id for different VDUs are one delta with two entries, which is why this accumulates
     * rather than builds each policy independently.
     */
    static Map<String, List<ScalingDelta>> scalingDeltas(FlavourContext context) {
        Map<String, Map<String, ScalingDelta.Builder>> byAspect = new LinkedHashMap<>();

        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_SCALING_ASPECT_DELTAS)) {
            VduScalingAspectDeltas policy = bind(definition, VduScalingAspectDeltas.class);
            if (policy.getProperties() == null || policy.getProperties().getAspect() == null
                    || policy.getProperties().getDeltas() == null) {
                continue;
            }
            String aspectId = policy.getProperties().getAspect().resolved().orElse(null);
            if (aspectId == null) {
                continue;
            }
            Map<String, ScalingDelta.Builder> deltas =
                    byAspect.computeIfAbsent(aspectId, k -> new LinkedHashMap<>());
            policy.getProperties().getDeltas().forEach((deltaId, level) -> {
                if (level == null || level.getNumberOfInstances() == null) {
                    return;
                }
                Integer count = level.getNumberOfInstances().resolved().orElse(null);
                if (count == null) {
                    return;
                }
                ScalingDelta.Builder b =
                        deltas.computeIfAbsent(deltaId, ScalingDelta::builder);
                for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                    b.addVduDelta(VduLevel.of(vduId, count));
                }
            });
        }

        Map<String, List<ScalingDelta>> out = new LinkedHashMap<>();
        byAspect.forEach((aspectId, deltas) -> {
            List<ScalingDelta> built = new ArrayList<>();
            deltas.values().forEach(b -> built.add(b.build()));
            out.put(aspectId, built);
        });
        return out;
    }

    /**
     * SOL001 V5.4.1 clause 6.10.8 {@code VduInitialDelta} to IFA011 {@code VnfDf.initialDelta}.
     *
     * <p>IFA011 clause 7.1.8.2.2 describes it as "the minimum size of the VNF (i.e. scale level zero
     * for all scaling aspects)", so the per-VDU policies of one flavour make up a single delta
     * rather than one each.
     *
     * <p>[ASSUMPTION] Its {@code scalingDeltaId}. IFA011 makes the identifier mandatory and SOL001
     * gives the policy no name for it, so a fixed one is used.
     */
    static Optional<ScalingDelta> initialDelta(FlavourContext context) {
        ScalingDelta.Builder builder = ScalingDelta.builder("initial_delta");
        boolean any = false;
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VDU_INITIAL_DELTA)) {
            VduInitialDelta policy = bind(definition, VduInitialDelta.class);
            if (policy.getProperties() == null || policy.getProperties().getInitialDelta() == null) {
                continue;
            }
            com.example.etsi.vnfd.toscatype.data.VduLevel level =
                    policy.getProperties().getInitialDelta();
            if (level.getNumberOfInstances() == null) {
                continue;
            }
            Integer count = level.getNumberOfInstances().resolved().orElse(null);
            if (count == null) {
                continue;
            }
            for (String vduId : VnfdUtils.orEmpty(definition.targets())) {
                builder.addVduDelta(VduLevel.of(vduId, count));
                any = true;
            }
        }
        return any ? Optional.of(builder.build()) : Optional.empty();
    }

    /**
     * SOL001 V5.4.1 clause 6.10.3 {@code VirtualLinkInstantiationLevels} to
     * {@code InstantiationLevel.virtualLinkBitRateLevel}, IFA011 clause 7.1.10.5.
     *
     * <p>Folded into the levels an {@code InstantiationLevels} policy already declared, for the same
     * reason as the VDU levels: a level id nothing declared is a reference to a level that does not
     * exist.
     */
    static void applyVirtualLinkLevels(FlavourContext context,
            Map<String, InstantiationLevel.Builder> builders) {
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VL_INSTANTIATION_LEVELS)) {
            VirtualLinkInstantiationLevels policy =
                    bind(definition, VirtualLinkInstantiationLevels.class);
            if (policy.getProperties() == null || policy.getProperties().getLevels() == null) {
                continue;
            }
            policy.getProperties().getLevels().forEach((levelId, level) -> {
                InstantiationLevel.Builder b = builders.get(levelId);
                if (b == null || level == null) {
                    return;
                }
                // The SOL001 level wraps the requirements in a bitrate_requirements field; IFA011
                // clause 7.1.10.5.2 has VirtualLinkBitRateLevel carry them directly, so unwrap.
                Map<String, Object> bitrate = PlainValues.asMap(level.getBitrateRequirements());
                for (String vlId : VnfdUtils.orEmpty(definition.targets())) {
                    b.addVirtualLinkBitRateLevel(VirtualLinkBitRateLevel.of(vlId, bitrate));
                }
            });
        }
    }

    /**
     * SOL001 V5.4.1 clause 6.10.13 {@code SecurityGroupRule} to IFA011 clause 7.1.6.9.
     *
     * <p>[ASSUMPTION] {@code securityGroupRuleId} is the policy name. IFA011 makes the identifier
     * mandatory and NOTE 3 of Table 7.1.6.9.2-1 relies on it - "Different VduCpd or VnfExtCpd with
     * the same value of securityGroupRuleId imply they belong to the same security group" - but
     * SOL001 states no derivation.
     */
    static List<SecurityGroupRule> securityGroupRules(FlavourContext context) {
        List<SecurityGroupRule> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_SECURITY_GROUP_RULE)) {
            com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule policy =
                    bind(definition, com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule.class);
            SecurityGroupRule.Builder builder = SecurityGroupRule.builder(definition.name());
            if (policy.getProperties() != null) {
                com.example.etsi.vnfd.toscatype.policy.SecurityGroupRule.Properties p =
                        policy.getProperties();
                builder.description(p.getDescription())
                       .direction(p.getDirection())
                       .etherType(p.getEtherType())
                       .protocol(p.getProtocol())
                       .portRangeMin(p.getPortRangeMin())
                       .portRangeMax(p.getPortRangeMax());
            }
            VnfdUtils.orEmpty(definition.targets()).forEach(builder::addTarget);
            out.add(builder.build());
        }
        return out;
    }

    /**
     * SOL001 V5.4.1 clause 6.10.15 {@code VnfPackageChange} to IFA011 clause 7.1.15.2
     * {@code VnfPackageChangeInfo}.
     */
    static List<VnfPackageChangeInfo> packageChanges(FlavourContext context) {
        List<VnfPackageChangeInfo> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_VNF_PACKAGE_CHANGE)) {
            VnfPackageChange policy = bind(definition, VnfPackageChange.class);
            VnfPackageChangeInfo.Builder builder = VnfPackageChangeInfo.builder(definition.name());
            if (policy.getProperties() != null) {
                VnfPackageChange.Properties p = policy.getProperties();
                builder.modificationQualifier(p.getModificationQualifier())
                       .additionalModificationDescription(p.getAdditionalModificationDescription())
                       .destinationFlavourId(p.getDestinationFlavourId());
                if (p.getSelector() != null) {
                    p.getSelector().forEach(sel -> builder.addSelector(PlainValues.asMap(sel)));
                }
                if (p.getComponentMappings() != null) {
                    p.getComponentMappings()
                            .forEach(cm -> builder.addComponentMapping(PlainValues.asMap(cm)));
                }
            }
            out.add(builder.build());
        }
        return out;
    }

    static List<ScalingAspect> scalingAspects(FlavourContext context) {
        List<ScalingAspect> out = new ArrayList<>();
        for (PolicyDefinition definition : of(context, EtsiTypes.POLICY_SCALING_ASPECTS)) {
            ScalingAspects policy = bind(definition, ScalingAspects.class);
            if (policy.getProperties() == null || policy.getProperties().getAspects() == null) {
                continue;
            }
            Map<String, List<ScalingDelta>> deltasByAspect = scalingDeltas(context);
            policy.getProperties().getAspects().forEach((id, aspect) -> {
                ScalingAspect.Builder b = ScalingAspect.builder(id);
                deltasByAspect.getOrDefault(id, Collections.emptyList()).forEach(b::addDelta);
                if (aspect != null) {
                    resolved(aspect.getName()).ifPresent(b::name);
                    resolved(aspect.getDescription()).ifPresent(b::description);
                    if (aspect.getMaxScaleLevel() != null) {
                        aspect.getMaxScaleLevel().resolved().ifPresent(b::maxScaleLevel);
                    }
                    // IFA011 clause 7.1.8.8.2 stepDeltas: the scaling deltas applied for the
                    // successive scaling steps of this aspect, in order.
                    VnfdUtils.orEmpty(aspect.getStepDeltas()).forEach(b::addStepDelta);
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
    static AffinityAssignment affinity(FlavourContext context) {
        List<AffinityOrAntiAffinityGroup> groups = new ArrayList<>();
        Map<String, List<String>> byTarget = new LinkedHashMap<>();

        for (PolicyDefinition definition : context.policies()) {
            boolean anti = context.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_ANTI_AFFINITY_RULE);
            boolean plain = context.isDerivedFrom(
                    definition.type(), EtsiTypes.POLICY_AFFINITY_RULE);
            if (!anti && !plain) {
                continue;
            }
            // Both types declare the same properties, so one class reads either.
            AffinityRule policy = bind(definition, AffinityRule.class);
            String groupId = VnfdUtils.affinityGroupId(definition);
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
    private static List<String> expand(List<String> targets, FlavourContext context) {
        List<String> out = new ArrayList<>();
        for (String target : VnfdUtils.orEmpty(targets)) {
            GroupDefinition group = context.topology().groups().get(target);
            if (group != null && context.isDerivedFrom(group.type(), PLACEMENT_GROUP)) {
                out.addAll(group.members());
            } else {
                out.add(target);
            }
        }
        return out;
    }

    private static List<PolicyDefinition> of(FlavourContext context, String etsiType) {
        List<PolicyDefinition> out = new ArrayList<>();
        for (PolicyDefinition definition : context.policies()) {
            if (context.isDerivedFrom(definition.type(), etsiType)) {
                out.add(definition);
            }
        }
        return out;
    }

    private static <T> T bind(PolicyDefinition definition, Class<T> target) {
        return ToscaBindModule.mapper().convertValue(
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
