package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code policy_types} entry.
 *
 * <p>{@link #targets()} is read directly when resolving affinity. SOL001 V5.4.1 clause 6.10.10
 * declares {@code AffinityRule} and {@code AntiAffinityRule} with
 * {@code targets: [ Vdu.Compute, VnfVirtualLink, PlacementGroup, Mciop,
 * Vdu.OsContainerDeployableUnit, PaasServiceRequest ]}, which is what tells the mapper that an
 * affinity policy may legitimately point at an {@code Mciop} and that the resulting group
 * identifier belongs on {@code MciopProfile.affinityOrAntiAffinityGroupId}.
 */
public final class PolicyTypeDef extends AbstractTypeDef {

    private final List<String> targets = new ArrayList<>();
    private final Map<String, Object> triggers = new LinkedHashMap<>();

    public PolicyTypeDef(String name) {
        super(name);
    }

    /** Node or group types this policy type may be applied to. */
    public List<String> targets() {
        return targets;
    }

    public Map<String, Object> triggers() {
        return triggers;
    }
}
