package com.example.etsi.vnfd.services.template2vnfd;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VnfDf;
import com.example.etsi.vnfd.model.Vnfd;
import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.node.Mciop;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduOsContainer;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.validation.Findings;
import com.example.etsi.vnfd.validation.SourceRef;
import java.util.List;

/**
 * The conformance rules that only this stage can see.
 *
 * <p>Kept apart from the mappers on purpose. A mapper answers what IFA011 calls a thing, and reads
 * one element; these rules are statements about what a descriptor must contain, and most of them
 * span elements - whether some MCIOP covers this VDU, whether the flavour declares more levels than
 * it names a default for. Mixing the two would bury the citations inside the translation.
 *
 * <p>Checks that a type definition already expresses - {@code required}, {@code constraints} - are
 * not here; {@code ConstraintChecker} runs those against the {@code TypeRegistry} while binding, so
 * they need no Java. What remains is what the YAML type files cannot say.
 */
final class SpecRules {

    private SpecRules() {
    }

    /**
     * IFA011 V5.4.1 clause 7.1.6.2.2 Note 10: when a VDU is realized as OS containers and
     * {@code osContainerDesc} is not present, the MciopProfile associated with the VDU shall be
     * present.
     *
     * <p>Reported here rather than from SOL001 clause 6.8.13.7, which asks the same question of the
     * whole service template: a flavour holding one MCIOP-realized VDU and one OsContainer-realized
     * VDU satisfies 6.8.13.7 while still leaving the first VDU undescribed.
     */
    static void mciopCoverage(VduOsContainerDeployableUnit vdu, LcmRealizationPath path,
            Findings findings) {
        if (path == LcmRealizationPath.UNDETERMINED) {
            findings.error("C2", "IFA011 V5.4.1 cl. 7.1.6.2.2 Note 10",
                    "VDU " + vdu.getKey() + " declares no container requirement and no Mciop is "
                            + "associated with it, so nothing describes how it is realized",
                    ref(vdu));
        }
    }

    /**
     * IFA011 V5.4.1 clause 7.1.6.2.2: {@code mcioIdentificationData} shall be present when the VDU
     * is realized by one or a set of OS containers. Every VDU in scope of this library is.
     */
    static void mcioIdentificationData(VduOsContainerDeployableUnit vdu, Findings findings) {
        boolean present = vdu.getProperties() != null
                && vdu.getProperties().getMcioIdentificationData() != null;
        if (!present) {
            findings.error("C19", "IFA011 V5.4.1 cl. 7.1.6.2.2",
                    "VDU " + vdu.getKey() + " has no mcio_identification_data, so no MCIO of the "
                            + "CISM can be matched back to it",
                    ref(vdu));
        }
    }

    /**
     * SOL001 V5.4.1 clause 6.8.12.6: a {@code Vdu.OsContainer} shall contain one artifact of type
     * {@code tosca.artifacts.nfv.SwImage}, and at most one.
     */
    static void swImage(VduOsContainer container, ArtifactSelector artifacts, Findings findings) {
        List<ArtifactDefinition> images =
                artifacts.allOfType(container, EtsiTypes.ARTIFACT_SW_IMAGE);
        if (images.isEmpty()) {
            findings.error("C6", "SOL001 V5.4.1 cl. 6.8.12.6",
                    "Vdu.OsContainer " + container.getKey() + " declares no SwImage artifact",
                    ref(container));
        } else if (images.size() > 1) {
            findings.error("C6", "SOL001 V5.4.1 cl. 6.8.12.6",
                    "Vdu.OsContainer " + container.getKey() + " declares " + images.size()
                            + " SwImage artifacts; at most one is allowed",
                    ref(container));
        }
    }

    /**
     * SOL001 V5.4.1 clause 6.8.14.6 gives {@code associatedVdu} occurrences [1, UNBOUNDED], and
     * clause 6.8.14.7 caps each artifact type at one per {@code Mciop} while making
     * {@code HelmParamMappingRule} meaningful only alongside a {@code HelmParamMappingScript}.
     */
    static void mciop(Mciop mciop, ArtifactSelector artifacts, Findings findings) {
        boolean associated = mciop.getRequirements() != null
                && !FlavourContext.orEmpty(mciop.getRequirements().getAssociatedVdu()).isEmpty();
        if (!associated) {
            findings.error("C8", "SOL001 V5.4.1 cl. 6.8.14.6",
                    "Mciop " + mciop.getKey() + " is associated with no VDU; occurrences are "
                            + "[1, UNBOUNDED]",
                    ref(mciop));
        }

        int charts = artifacts.allOfType(mciop, EtsiTypes.ARTIFACT_HELM_CHART).size();
        if (charts > 1) {
            findings.error("C4", "SOL001 V5.4.1 cl. 6.8.14.7",
                    "Mciop " + mciop.getKey() + " declares " + charts + " HelmChart artifacts; at "
                            + "most one is allowed",
                    ref(mciop));
        }

        boolean rule = !artifacts.allOfType(mciop, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_RULE)
                .isEmpty();
        boolean script = !artifacts.allOfType(mciop, EtsiTypes.ARTIFACT_HELM_PARAM_MAPPING_SCRIPT)
                .isEmpty();
        if (rule && !script) {
            findings.error("C5", "SOL001 V5.4.1 cl. 6.8.14.7",
                    "Mciop " + mciop.getKey() + " declares a HelmParamMappingRule but no "
                            + "HelmParamMappingScript to consume it",
                    ref(mciop));
        }
    }

    /**
     * Two statements of IFA011 V5.4.1 clause 7.1.8.2.2: {@code defaultInstantiationLevelId} shall be
     * present if there are multiple instantiationLevel entries, and {@code mciopProfile} shall be
     * present if the DF references, via the vduProfile, containerized workloads based on a MCIOP.
     */
    static void flavour(VnfDf df, List<Vdu> vdus, Findings findings) {
        SourceRef source = df.getSourceFile().map(SourceRef::ofFile).orElse(null);

        if (df.getInstantiationLevel().size() > 1
                && !df.getDefaultInstantiationLevelId().isPresent()) {
            findings.error("C12", "IFA011 V5.4.1 cl. 7.1.8.2.2",
                    "Flavour " + df.getFlavourId() + " declares "
                            + df.getInstantiationLevel().size()
                            + " instantiation levels but names no default level",
                    source);
        }

        if (df.getMciopProfile().isEmpty()) {
            for (Vdu vdu : vdus) {
                if (vdu.getLcmRealizationPath() == LcmRealizationPath.MCIOP_CISM) {
                    findings.error("C28", "IFA011 V5.4.1 cl. 7.1.8.2.2",
                            "Flavour " + df.getFlavourId() + " references VDU " + vdu.getVduId()
                                    + " as an MCIOP-based workload but declares no mciopProfile",
                            source);
                }
            }
        }
    }

    /**
     * IFA011 V5.4.1 clause 7.1.2.2 Note 6: one of virtualComputeDesc, osContainerDesc or mciopId
     * shall contain at least one element. Only the latter two exist in a CNF, so a VNFD declaring
     * neither describes no workload at all.
     */
    static void note6(Vnfd vnfd, Findings findings) {
        if (vnfd.getOsContainerDesc().isEmpty() && vnfd.getMciopId().isEmpty()) {
            findings.error("C1", "IFA011 V5.4.1 cl. 7.1.2.2 Note 6",
                    "The VNFD declares neither osContainerDesc nor mciopId, so no workload is "
                            + "described",
                    null);
        }
    }

    private static SourceRef ref(NfvNode node) {
        return node.getSource() == null ? null : node.getSource().toFindingRef();
    }
}
