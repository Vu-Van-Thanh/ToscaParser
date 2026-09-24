package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.services.pkg2template.TypeReader;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.template.PolicyDefinition;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The stateless helpers of the mapping stage: identifiers, null-coalescing, artifact lookup, and
 * the one derivation that belongs to no single node type.
 *
 * <p>The identifier rules are gathered rather than scattered because only <em>one</em> of them is
 * stated outright by the specification - {@code swImageDescId}, marked [VERIFIED] - and the other
 * twelve are [ASSUMPTION]. Spread across the mappers it would be impossible to see which is which,
 * so each keeps the javadoc that says where it comes from.
 */
final class VnfdUtils {

    private VnfdUtils() {
    }

    // ============================================================================================
    // IDENTIFIERS
    // ============================================================================================

    /**
     * [VERIFIED] SOL001 V5.4.1 clause 6.8.12.6: the node template name of a
     * {@code Vdu.OsContainer} "fulfils the purpose of the 'id' attribute of the SwImageDesc
     * information element and hence it will be used in APIs to identify the software image id from
     * the VNFD perspective".
     */
    static String swImageDescId(NfvNode owningNode) {
        return owningNode.getKey();
    }

    /** [ASSUMPTION] The node template name. SOL001 states the rule only for SwImageDesc. */
    static String vduId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String osContainerDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualStorageDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String cpdId(NfvNode node) {
        return node.getKey();
    }

    /**
     * {@code CertificateDesc.id}, IFA011 clause 7.1.19.2.2 - M,1.
     *
     * <p>[ASSUMPTION] The node template name. SOL001 clause 6.8.19 does not say how the identifier
     * is derived; the only place SOL001 states that rule outright is clause 6.8.12.6, for
     * SwImageDesc.
     */
    static String certificateDescId(NfvNode node) {
        return node.getKey();
    }

    /**
     * {@code DeployableModule.deployableModuleId}, IFA011 clause 7.1.8.24.2 - M,1.
     *
     * <p>[ASSUMPTION] The node template name, for the same reason as above. Note the identifier has
     * to agree with whatever {@code VduProfile.deployableModule} names, since that is the reference
     * IFA011 uses to attach a VDU to a module.
     */
    static String deployableModuleId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualLinkDescId(NfvNode node) {
        return node.getKey();
    }

    /**
     * [ASSUMPTION] The node template name.
     *
     * <p>IFA011 V5.4.1 clause 7.1.8.20.2 says {@code mciopId} "identifies the MCIOP in the VNF
     * package" without saying how. The node template name is the only stable handle a descriptor
     * offers, and SOL001 Table 6.1-1 NOTE 3 maps {@code associatedVdu} and {@code deploymentOrder}
     * onto this node type, so the profile is built around it either way.
     */
    static String mciopId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] The artifact definition name. */
    static String lcmOpParameterMappingScriptId(ArtifactDefinition artifact) {
        return artifact.name();
    }

    /**
     * [ASSUMPTION] The policy name.
     *
     * <p>SOL001 Table 6.1-1 NOTE 3 states that {@code affinityOrAntiAffinityGroupId} maps to an
     * {@code AffinityRule} or {@code AntiAffinityRule} policy, but not what the group id is.
     */
    static String affinityGroupId(PolicyDefinition definition) {
        return definition.name();
    }

    /**
     * [ASSUMPTION] Interface plus operation, e.g. {@code Vnflcm.instantiate_start}.
     *
     * <p>IFA011 makes it 0..1 - "shall be present if there is the need to reference this script
     * from another information element" - and says nothing about its form.
     */
    static String lcmScriptId(String interfaceName, String operationName) {
        return interfaceName + "." + operationName;
    }

    // ============================================================================================
    // NULL-COALESCING
    // ============================================================================================

    static Optional<String> first(List<String> values) {
        return values == null || values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    static List<String> orEmpty(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }

    // ============================================================================================
    // ARTIFACTS
    // ============================================================================================

    /** The package-root-relative path of an artifact, falling back to the reference as written. */
    static String pathOf(ArtifactDefinition artifact) {
        return artifact.resolvedFile().orElse(artifact.file());
    }

    /**
     * Every artifact of the given type declared on a node, in declaration order.
     *
     * <p>Artifacts cannot be looked up by name: the name of an artifact definition is the VNFD
     * author's choice - the bundled packages write {@code sw_image} and {@code web_helm_chart} -
     * while SOL001 always speaks of the type. Clause 6.8.12.6 asks for "an artifact of type
     * tosca.artifacts.nfv.SwImage"; clause 6.8.14.7 caps each type on an {@code Mciop} at one.
     *
     * <p>Matching walks {@code derived_from} like everything else, so a vendor artifact type
     * derived from an ETSI one is still found.
     */
    static List<ArtifactDefinition> artifactsOfType(TypeReader.Hierarchy hierarchy, NfvNode node,
            String etsiArtifactType) {
        Map<String, ArtifactDefinition> declared = node.getArtifacts();
        if (declared == null || declared.isEmpty()) {
            return Collections.emptyList();
        }
        List<ArtifactDefinition> out = new ArrayList<>();
        for (ArtifactDefinition artifact : declared.values()) {
            if (hierarchy.isDerivedFrom(artifact.type(), etsiArtifactType)) {
                out.add(artifact);
            }
        }
        return out;
    }

    /**
     * The single artifact of that type.
     *
     * <p>More than one is a rule violation, not a parse failure, so the first is returned and the
     * caller checks {@link #artifactsOfType} when it needs to report the cardinality.
     */
    static Optional<ArtifactDefinition> artifactOfType(TypeReader.Hierarchy hierarchy, NfvNode node,
            String etsiArtifactType) {
        List<ArtifactDefinition> all = artifactsOfType(hierarchy, node, etsiArtifactType);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    // ============================================================================================
    // LIFECYCLE REALIZATION
    // ============================================================================================

/**
 * Which CISM interface a VDU will be managed through.
 *
 * <p>[MANO INTERPRETATION] No attribute of IFA011 says "this VDU uses Helm". The conclusion follows
 * from two normative statements read together: IFA011 V5.4.1 clause 7.1.6.2.2 Note 10 - "In case
 * the VDU to be deployed is realized as OS containers and osContainerDesc is not present, the
 * MciopProfile associated with the VDU shall be present" - and SOL018 V5.4.1 clause 6.2.1.1, which
 * says the CISM exposes "management service interfaces on different abstraction levels. One
 * abstraction level are the MCIOPs, the other abstraction level are the MCIOs".
 *
 * <p>Worth deriving because the two paths differ in practice: through an MCIOP the VNFM runs the
 * parameter mapping script and then operates on a whole Helm release (SOL018 clause 7), while a VDU
 * described by an OsContainer is realized MCIO by MCIO through the Kubernetes API (clause 8), with
 * no equivalent of a rollback.
 *
 * <p>Note 10 is stated per VDU, and nothing requires the VDUs of one flavour to agree - so one
 * flavour may legitimately hold both kinds.
 */
    static LcmRealizationPath lcmRealizationPath(VduOsContainerDeployableUnit vdu,
            List<String> associatedMciops) {
        if (!associatedMciops.isEmpty()) {
            return LcmRealizationPath.MCIOP_CISM;
        }
        if (!containerTargets(vdu).isEmpty()) {
            return LcmRealizationPath.DIRECT_MCIO_CISM;
        }
        // Neither, which SpecRules reports as C2; the path itself is simply not derivable.
        return LcmRealizationPath.UNDETERMINED;
    }

    private static List<String> containerTargets(VduOsContainerDeployableUnit vdu) {
        return vdu.getRequirements() == null
                ? Collections.emptyList()
                : orEmpty(vdu.getRequirements().getContainer());
    }
}
