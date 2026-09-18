package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code group_types} entry.
 *
 * <p>{@link #members()} matters for affinity resolution: SOL001 V5.4.1 declares
 * {@code tosca.groups.nfv.PlacementGroup} with members drawn from {@code Vdu.Compute},
 * {@code Vdu.OsContainerDeployableUnit}, {@code VnfVirtualLink} and {@code Mciop}, and an affinity
 * policy may target the group rather than the nodes.
 */
public final class GroupTypeDef extends AbstractTypeDef {

    private final List<String> members = new ArrayList<>();

    public GroupTypeDef(String name) {
        super(name);
    }

    /** Node types permitted as members of this group. */
    public List<String> members() {
        return members;
    }
}
