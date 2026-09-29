package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.InstantiationLevel;
import com.example.etsi.vnfd.model.VduProfile;
import com.example.etsi.vnfd.model.VirtualLinkProfile;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.VnfLcmOperationsConfiguration;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import com.example.etsi.vnfd.services.template2vnfd.VnfdElements;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.services.template2vnfd.validator.SpecRuleValidator;
import com.example.etsi.vnfd.services.template2vnfd.mappers.MciopMapper;
import com.example.etsi.vnfd.template.ParameterDefinition;
import com.example.etsi.vnfd.template.value.FunctionCall;
import com.example.etsi.vnfd.template.value.FunctionName;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.toscatype.node.Vnf;
import com.example.etsi.vnfd.toscatype.node.VnfVirtualLink;
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
public final class DeploymentFlavourMapper {

    private DeploymentFlavourMapper() {
    }

    public static VnfDf map(FlavourContext context, VnfdElements merged) {
        String flavourId = flavourId(context).orElse("");
        VnfDf.Builder builder = VnfDf.builder(flavourId);
        builder.sourceFile(context.template().file());
        flavourDescription(context).ifPresent(builder::description);

        PolicyMapper.AffinityAssignment affinity = PolicyMapper.affinity(context);
        affinity.groups().forEach(builder::addAffinityGroup);

        Map<String, Integer> minInstances = new LinkedHashMap<>();
        for (VduOsContainerDeployableUnit vdu : context.vdus()) {
            VduProfile profile = vduProfile(vdu, affinity);
            builder.addVduProfile(profile);
            profile.getMinNumberOfInstances().flatMap(v -> v.resolved())
                    .ifPresent(min -> minInstances.put(profile.getVduId(), min));
        }

        Map<String, Integer> deploymentOrder =
                MciopMapper.deploymentOrder(context, context.findings());
        for (Mciop mciop : context.mciops()) {
            MciopMapper.check(mciop, context);
            builder.addMciopProfile(MciopMapper.mapProfile(mciop, deploymentOrder, context));
        }

        // IFA011 clause 7.1.8.2.2 virtualLinkProfile: SOL001 clause 6.8.9 carries the same data as
        // the vl_profile property of the virtual link, so the profile is read from the node the
        // flavour references rather than from a separate element.
        for (VnfVirtualLink link : context.virtualLinks().values()) {
            builder.addVirtualLinkProfile(virtualLinkProfile(link, affinity));
        }

        PolicyMapper.scalingAspects(context).forEach(builder::addScalingAspect);
        PolicyMapper.initialDelta(context).ifPresent(builder::initialDelta);

        // IFA011 Table 7.1.8.2.2-1 puts deployableModule on the flavour, not on the VNFD: the set
        // of optional VDUs is what a consumer selects when instantiating this flavour.
        context.deployableModules().forEach(m ->
                builder.addDeployableModule(DeployableModuleMapper.map(m)));

        List<InstantiationLevel> levels = PolicyMapper.instantiationLevels(context);
        if (levels.isEmpty()) {
            levels = Collections.singletonList(PolicyMapper.synthesiseLevel(minInstances));
        }
        levels.forEach(builder::addInstantiationLevel);
        PolicyMapper.defaultInstantiationLevelId(context).ifPresent(builder::defaultInstantiationLevelId);

        // IFA011 clause 7.1.5.2.2 puts vnfLcmOperationsConfiguration on the flavour, while SOL001
        // declares lcm_operations_configuration as a property of the VNF node type. Consistent,
        // since one service template is one flavour (clauses 6.11.2 and 6.11.3).
        context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getLcmOperationsConfiguration())
                .ifPresent(cfg -> builder.vnfLcmOperationsConfiguration(lcmOperationsConfig(cfg)));

        VnfDf df = builder.build();
        SpecRuleValidator.flavourIdentified(df, context.template().file(), context.findings());
        // IFA011 Table 7.1.2.2-1 keeps both of these at VNFD level, although SOL001 declares them as
        // policies inside a service template - one template being one flavour - so they are written
        // straight to the pool rather than carried back out for the caller to redistribute.
        PolicyMapper.securityGroupRules(context).forEach(merged::addSecurityGroupRule);
        PolicyMapper.packageChanges(context).forEach(merged::addVnfPackageChangeInfo);
        for (Mciop mciop : context.mciops()) {
            merged.addMciopId(VnfdUtils.mciopId(mciop));
            MciopMapper.mapScript(mciop, context).ifPresent(merged::addLcmOpParameterMappingScript);
            MciopMapper.mapArtifacts(mciop, context).ifPresent(merged::addMciopArtifacts);
        }
        return df;
    }

    /**
     * Which flavour this service template describes.
     *
     * <p>Three sources in order: {@code substitution_mappings.substitution_filter}, the form clause
     * 6.11.2 d) prescribes for a lower-level template; {@code properties.flavour_id} on the VNF node
     * template, which the single-flavour design of clause 6.11.3 uses; and the {@code default} on
     * the VNF node type, which {@code TypeReader.Hierarchy.fillPropertyDefaultsWithAncestors} has
     * already laid underneath by the time the node was bound.
     */
    private static Optional<String> flavourId(FlavourContext context) {
        Optional<String> fromFilter = context.topology().substitutionMappings()
                .flatMap(m -> m.filterEqualValue("flavour_id"));
        if (fromFilter.isPresent()) {
            return fromFilter;
        }

        PropertyValue<String> declared = context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourId())
                .orElse(null);
        if (declared == null) {
            return Optional.empty();
        }
        if (declared.isResolved()) {
            return declared.resolved();
        }
        // The property is a TOSCA function. SOL001 V5.4.1 Table 5.9-2 lists VNF.flavour_id as one of
        // the four places get_input is permitted, so a conformant descriptor can legitimately land
        // here - and IFA011 clause 7.1.8.2.2 still makes flavourId M,1, so an empty identifier is
        // not an acceptable answer.
        //
        // [MANO INTERPRETATION] The declared default of the input is used. It is part of the
        // descriptor rather than a runtime value, and TOSCA Simple Profile YAML 1.3 clause 3.6.11
        // defines it as the value to use when the consumer supplies none. No value is substituted
        // from anywhere outside the package.
        return inputDefault(context, declared);
    }

    /** The {@code default} of the input a {@code get_input} names, when the input declares one. */
    private static Optional<String> inputDefault(FlavourContext context, PropertyValue<String> value) {
        if (!(value instanceof FunctionCall)) {
            return Optional.empty();
        }
        FunctionCall<String> call = (FunctionCall<String>) value;
        if (call.name() != FunctionName.GET_INPUT || call.args().isEmpty()) {
            return Optional.empty();
        }
        return call.args().get(0).resolved()
                .map(String::valueOf)
                .flatMap(inputName -> Optional
                        .ofNullable(context.topology().inputs().get(inputName))
                        .flatMap(ParameterDefinition::defaultValue)
                        .map(String::valueOf));
    }

    private static Optional<String> flavourDescription(FlavourContext context) {
        return context.vnf()
                .map(Vnf::getProperties)
                .map(p -> p.getFlavourDescription())
                .flatMap(v -> v == null ? Optional.<String>empty() : v.resolved());
    }

    /**
     * SOL001 {@code lcm_operations_configuration} to IFA011 clause 7.1.5.2
     * {@code VnfLcmOperationsConfiguration}.
     *
     * <p>Twelve sub-elements, each 0..1 and each a bag of operation-specific settings. Carried as
     * written: nothing here reads an individual setting, and re-modelling twelve tables to pass
     * them through would be twelve chances to lose one.
     */
    @SuppressWarnings("unchecked")
    private static VnfLcmOperationsConfiguration lcmOperationsConfig(Object raw) {
        VnfLcmOperationsConfiguration.Builder builder = VnfLcmOperationsConfiguration.builder();
        Map<String, Object> asMap = ToscaBindModule.mapper().convertValue(raw, Map.class);
        if (asMap != null) {
            asMap.forEach((attribute, value) -> {
                if (value instanceof Map) {
                    builder.mergeOpConfig(attribute, (Map<String, Object>) value);
                }
            });
        }
        return builder.build();
    }

    /** SOL001 clause 6.8.9 {@code vl_profile} to IFA011 clause 7.1.8.13 {@code VirtualLinkProfile}. */
    private static VirtualLinkProfile virtualLinkProfile(VnfVirtualLink link,
            PolicyMapper.AffinityAssignment affinity) {
        VirtualLinkProfile.Builder builder =
                VirtualLinkProfile.builder(VnfdUtils.virtualLinkDescId(link));
        if (link.getProperties() != null && link.getProperties().getVlProfile() != null) {
            com.example.etsi.vnfd.toscatype.data.VlProfile p = link.getProperties().getVlProfile();
            builder.maxBitrateRequirements(PlainValues.asMap(p.getMaxBitrateRequirements()))
                   .minBitrateRequirements(PlainValues.asMap(p.getMinBitrateRequirements()))
                   .qos(PlainValues.asMap(p.getQos()));
        }
        // SOL001 Table 6.1-1 NOTE 3: the affinity group lands on the profile, named by a policy
        // that targets the virtual link.
        affinity.groupsOf(link.getKey()).forEach(builder::addAffinityGroup);
        return builder.build();
    }

    /** SOL001 Table 6.8.13.2-1 {@code vdu_profile} to IFA011 clause 7.1.8.3 {@code VduProfile}. */
    private static VduProfile vduProfile(VduOsContainerDeployableUnit vdu,
            PolicyMapper.AffinityAssignment affinity) {
        VduProfile.Builder builder = VduProfile.builder(VnfdUtils.vduId(vdu));
        if (vdu.getProperties() != null && vdu.getProperties().getVduProfile() != null) {
            com.example.etsi.vnfd.toscatype.data.VduProfile p = vdu.getProperties().getVduProfile();
            builder.minNumberOfInstances(p.getMinNumberOfInstances())
                   .maxNumberOfInstances(p.getMaxNumberOfInstances());
            VnfdUtils.orEmpty(p.getModifyCapacityAttributesOp())
                    .forEach(builder::addModifyCapacityAttributesOp);
        }
        affinity.groupsOf(vdu.getKey()).forEach(builder::addAffinityGroup);
        return builder.build();
    }
}
