package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.model.LcmRealizationPath;
import com.example.etsi.vnfd.toscatype.node.VduOsContainerDeployableUnit;
import java.util.List;

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
final class LcmRealizationResolver {

    private LcmRealizationResolver() {
    }

    static LcmRealizationPath resolve(VduOsContainerDeployableUnit vdu,
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

    static List<String> containerTargets(VduOsContainerDeployableUnit vdu) {
        return vdu.getRequirements() == null
                ? java.util.Collections.emptyList()
                : FlavourContext.orEmpty(vdu.getRequirements().getContainer());
    }
}
