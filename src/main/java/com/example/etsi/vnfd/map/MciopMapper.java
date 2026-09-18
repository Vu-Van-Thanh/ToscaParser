package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.LcmOpParameterMappingScript;
import com.example.etsi.vnfd.model.MciopProfile;
import com.example.etsi.vnfd.model.ext.MciopArtifacts;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.artifact.HelmParamMappingScript;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
final class MciopMapper {

    /** SOL001 clause 6.3.4.1 defines three ordered input parameters. */
    private static final int HELM_SCRIPT_ARITY = 3;

    private final ArtifactSelector artifacts;
    private final ObjectMapper mapper;

    MciopMapper(ArtifactSelector artifacts, ObjectMapper mapper) {
        this.artifacts = artifacts;
        this.mapper = mapper;
    }

    /** The cardinality rules of clauses 6.8.14.6 and 6.8.14.7 - see {@link SpecRules#mciop}. */
    void check(Mciop node, com.example.etsi.vnfd.validation.Findings findings) {
        SpecRules.mciop(node, artifacts, findings);
    }

    MciopProfile mapProfile(Mciop node) {
        MciopProfile.Builder builder = MciopProfile.builder(IdRegistry.mciopId(node));

        // Table 6.8.14.4-1 gives associatedVdu occurrences [1, UNBOUNDED]; Annex A.23 declares the
        // key twice on one Mciop, which is why the bound field is a list.
        if (node.getRequirements() != null) {
            FlavourContext.orEmpty(node.getRequirements().getAssociatedVdu())
                    .forEach(builder::addAssociatedVdu);
        }

        artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE)
                .map(ArtifactSelector::pathOf)
                .ifPresent(builder::mciopParameterMappingRule);
        artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT)
                .map(ArtifactDefinition::name)
                .ifPresent(builder::lcmOpParameterMappingScriptId);

        return builder.build();
    }

    /**
     * The parameter mapping script as the VNFD-level information element.
     *
     * <p>SOL001 clause 6.3.4 {@code HelmParamMappingScript} maps onto IFA011 clause 7.1.20
     * {@code LcmOpParameterMappingScript}, but with the three-parameter calling convention of
     * clause 6.3.4.1 rather than the four of IFA011 clause 7.1.20.1 - hence the recorded arity.
     */
    Optional<LcmOpParameterMappingScript> mapScript(Mciop node) {
        return artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT).map(definition -> {
            HelmParamMappingScript.Properties p = mapper.convertValue(
                    Collections.singletonMap("properties", definition.properties()),
                    HelmParamMappingScript.class).getProperties();
            String language = p == null || p.getLanguage() == null
                    ? null
                    : p.getLanguage().resolved().orElse(null);
            return LcmOpParameterMappingScript.of(
                    IdRegistry.lcmOpParameterMappingScriptId(definition),
                    ArtifactSelector.pathOf(definition),
                    language,
                    LcmOpParameterMappingScript.ScriptKind.HELM_PARAM_MAPPING,
                    HELM_SCRIPT_ARITY);
        });
    }

    /** The package-relative paths of the MCIOP artifacts - see the class javadoc for why. */
    Optional<MciopArtifacts> mapArtifacts(Mciop node) {
        Optional<ArtifactDefinition> chart = artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_CHART);
        Optional<ArtifactDefinition> script =
                artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT);
        Optional<ArtifactDefinition> rule =
                artifacts.ofType(node, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE);
        if (!chart.isPresent() && !script.isPresent() && !rule.isPresent()) {
            return Optional.empty();
        }
        MciopArtifacts.Builder builder = MciopArtifacts.builder(IdRegistry.mciopId(node));
        chart.ifPresent(c -> builder.packagePath(ArtifactSelector.pathOf(c))
                .packageArtifactName(c.name())
                .packageArtifactType(c.type()));
        script.ifPresent(s -> builder.paramMappingScriptPath(ArtifactSelector.pathOf(s)));
        rule.ifPresent(r -> builder.paramMappingRulePath(ArtifactSelector.pathOf(r)));
        return Optional.of(builder.build());
    }

    /** Every MCIOP id of the flavour, in declaration order. */
    static List<String> idsOf(List<Mciop> mciops) {
        List<String> ids = new java.util.ArrayList<>();
        mciops.forEach(m -> ids.add(IdRegistry.mciopId(m)));
        return ids;
    }
}
