package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.services.template2vnfd.FlavourContext;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.services.template2vnfd.validator.SpecRuleValidator;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.artifact.HelmParamMappingScript;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * SOL001 V5.4.1 clause 6.8.14 {@code Mciop} to IFA011 V5.4.1 clause 7.1.8.20 {@code MciopProfile},
 * plus the two things that profile cannot hold.
 *
 * <p>Clause 6.8.14.1 is explicit that the node type "does not correspond to an information element
 * defined in ETSI GS NFV-IFA 011" and is only "capable of being profiled by the properties of the
 * MciopProfile", so one node produces three outputs:
 *
 * <ul>
 *   <li>the {@code MciopProfile}, which belongs to a deployment flavour;
 *   <li>an {@code LcmOpParameterMappingScript} (IFA011 clause 7.1.20), which belongs to the VNFD;
 *   <li>{@code MciopArtifacts}, which belongs nowhere in IFA011 - Table 7.1.8.20.2-1 has exactly
 *       six attributes and none holds the path of the Helm chart, yet
 *       {@code helm install {RELEASE} {CHART}} needs it.
 * </ul>
 */
public final class MciopMapper {

    /** SOL001 clause 6.3.4.1 defines three ordered input parameters. */
    private static final int HELM_SCRIPT_ARITY = 3;

    private MciopMapper() {
    }

    /** The cardinality rules of clauses 6.8.14.6 and 6.8.14.7 - see {@link SpecRuleValidator#mciop}. */
    public static void check(Mciop node, FlavourContext context) {
        SpecRuleValidator.mciop(node, context);
    }

    public static MciopProfile mapProfile(Mciop node, Map<String, Integer> deploymentOrder,
            FlavourContext context) {
        MciopProfile.Builder builder = MciopProfile.builder(VnfdUtils.nodeId(node));
        Integer order = deploymentOrder.get(node.getKey());
        if (order != null) {
            builder.deploymentOrder(order);
        }

        // Table 6.8.14.4-1 gives associatedVdu occurrences [1, UNBOUNDED]; Annex A.23 declares the
        // key twice on one Mciop, which is why the bound field is a list.
        if (node.getRequirements() != null) {
            VnfdUtils.orEmpty(node.getRequirements().getAssociatedVdu())
                    .forEach(builder::addAssociatedVdu);
        }

        context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE)
                .map(VnfdUtils::pathOf)
                .ifPresent(builder::mciopParameterMappingRule);
        context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT)
                .map(ArtifactDefinition::name)
                .ifPresent(builder::lcmOpParameterMappingScriptId);

        return builder.build();
    }


    /**
     * Deployment order of the MCIOPs of one flavour, from the {@code dependency} requirements
     * between them.
     *
     * <p>SOL001 V5.4.1 clause 6.8.14.7: "The dependency requirement as defined in
     * TOSCA-Simple-Profile-YAML-v1.3 may be used towards other Mciop nodes to express the order of
     * deployment." It says the requirement expresses an order; it does not say how that order
     * becomes the integer IFA011 clause 7.1.8.20.2 calls {@code deploymentOrder}.
     *
     * <p>[ASSUMPTION] A topological rank numbered from zero: an MCIOP depending on nothing is 0, and
     * one depending on others is one past the highest of them. MCIOPs at the same rank have no
     * ordering between them, which is what "may be deployed together" looks like as a number.
     *
     * <p>Only emitted when at least one dependency exists. A flavour whose MCIOPs declare no order
     * gets no deploymentOrder at all, rather than every profile claiming rank 0 - saying nothing is
     * more accurate than saying they are all first.
     */
    public static Map<String, Integer> deploymentOrder(FlavourContext context, Findings findings) {
        Map<String, List<String>> dependencies = new LinkedHashMap<>();
        boolean any = false;
        for (Mciop mciop : context.mciops()) {
            List<String> targets = new ArrayList<>();
            for (String target : context.rawRequirementTargets(mciop.getKey(), "dependency")) {
                // Clause 6.8.14.7 scopes this to other Mciop nodes; a dependency on anything else
                // is a TOSCA ordering statement this library has no reading for.
                if (context.mciops().stream().anyMatch(m -> m.getKey().equals(target))) {
                    targets.add(target);
                    any = true;
                }
            }
            dependencies.put(mciop.getKey(), targets);
        }
        if (!any) {
            return Collections.emptyMap();
        }

        Map<String, Integer> ranks = new LinkedHashMap<>();
        for (String key : dependencies.keySet()) {
            rank(key, dependencies, ranks, new LinkedHashSet<>(), context, findings);
        }
        return ranks;
    }

    private static int rank(String key, Map<String, List<String>> dependencies,
            Map<String, Integer> ranks, Set<String> visiting, FlavourContext context,
            Findings findings) {
        Integer known = ranks.get(key);
        if (known != null) {
            return known;
        }
        if (!visiting.add(key)) {
            // A cycle has no deployment order at all - every member would have to precede itself.
            findings.error("C30", "SOL001 V5.4.1 cl. 6.8.14.7",
                    "Mciop " + key + " takes part in a cycle of dependency requirements, so no "
                            + "order of deployment can be derived",
                    SourceRef.ofFile(context.template().file()));
            ranks.put(key, 0);
            return 0;
        }
        int order = 0;
        for (String target : dependencies.getOrDefault(key, Collections.emptyList())) {
            order = Math.max(order, rank(target, dependencies, ranks, visiting, context, findings) + 1);
        }
        visiting.remove(key);
        ranks.put(key, order);
        return order;
    }

    /**
     * The parameter mapping script as the VNFD-level information element.
     *
     * <p>SOL001 clause 6.3.4 {@code HelmParamMappingScript} maps onto IFA011 clause 7.1.20
     * {@code LcmOpParameterMappingScript}, but with the three-parameter calling convention of
     * clause 6.3.4.1 rather than the four of IFA011 clause 7.1.20.1 - hence the recorded arity.
     */
    public static Optional<LcmOpParameterMappingScript> mapScript(Mciop node, FlavourContext context) {
        return context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT).map(definition -> {
            HelmParamMappingScript.Properties p = ToscaBindModule.mapper().convertValue(
                    Collections.singletonMap("properties", definition.properties()),
                    HelmParamMappingScript.class).getProperties();
            String language = p == null || p.getLanguage() == null
                    ? null
                    : p.getLanguage().resolved().orElse(null);
            return LcmOpParameterMappingScript.of(
                    VnfdUtils.lcmOpParameterMappingScriptId(definition),
                    VnfdUtils.pathOf(definition),
                    language,
                    LcmOpParameterMappingScript.ScriptKind.HELM_PARAM_MAPPING,
                    HELM_SCRIPT_ARITY);
        });
    }

    /** The package-relative paths of the MCIOP artifacts - see the class javadoc for why. */
    public static Optional<MciopArtifacts> mapArtifacts(Mciop node, FlavourContext context) {
        Optional<ArtifactDefinition> chart = context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_CHART);
        Optional<ArtifactDefinition> script =
                context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT);
        Optional<ArtifactDefinition> rule =
                context.artifactOfType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE);
        if (!chart.isPresent() && !script.isPresent() && !rule.isPresent()) {
            return Optional.empty();
        }
        MciopArtifacts.Builder builder = MciopArtifacts.builder(VnfdUtils.nodeId(node));
        chart.ifPresent(c -> builder.packagePath(VnfdUtils.pathOf(c))
                .packageArtifactName(c.name())
                .packageArtifactType(c.type()));
        script.ifPresent(s -> builder.paramMappingScriptPath(VnfdUtils.pathOf(s)));
        rule.ifPresent(r -> builder.paramMappingRulePath(VnfdUtils.pathOf(r)));
        return Optional.of(builder.build());
    }

}
