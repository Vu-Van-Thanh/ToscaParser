package com.example.etsi.vnfd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code MciopProfile}, IFA011 V5.4.1 clause 7.1.8.20.
 *
 * <p>Six attributes, exactly as Table 7.1.8.20.2-1 lists them. None of them is a file reference:
 * {@code mciopId} "identifies the MCIOP in the VNF package" but does not locate it. Where the Helm
 * chart actually lives is therefore not expressible here, and is carried outside the information
 * model instead.
 *
 * <p>Which VDUs appear in {@link #getAssociatedVdu()} is the structural signal that separates a
 * deployment flavour where every VDU is deployed by an MCIOP from one where only some are. When
 * the associated VDUs are a strict subset of the flavour's VDU profiles, the remaining VDUs are
 * realised directly from their {@code OsContainerDesc}.
 */
public final class MciopProfile {

    private final String mciopId;
    private final Integer deploymentOrder;
    private final List<String> affinityOrAntiAffinityGroupId;
    private final List<String> associatedVdu;
    private final String mciopParameterMappingRule;
    private final String lcmOpParameterMappingScriptId;

    private MciopProfile(Builder builder) {
        this.mciopId = Objects.requireNonNull(builder.mciopId, "mciopId");
        this.deploymentOrder = builder.deploymentOrder;
        this.affinityOrAntiAffinityGroupId =
                Collections.unmodifiableList(new ArrayList<>(builder.affinityOrAntiAffinityGroupId));
        this.associatedVdu = Collections.unmodifiableList(new ArrayList<>(builder.associatedVdu));
        this.mciopParameterMappingRule = builder.mciopParameterMappingRule;
        this.lcmOpParameterMappingScriptId = builder.lcmOpParameterMappingScriptId;
    }

    public static Builder builder(String mciopId) {
        return new Builder(mciopId);
    }

    /** Mandatory. Identifies the MCIOP in the VNF package. */
    public String getMciopId() {
        return mciopId;
    }

    /**
     * Order relative to other MCIOPs; a lower value deploys earlier.
     *
     * <p>[ASSUMPTION] Derived from {@code dependency} requirements between {@code Mciop} nodes.
     * SOL001 clause 6.8.14.7 says that requirement "may be used towards other Mciop nodes to
     * express the order of deployment" but does not define how to turn a dependency graph into an
     * integer, and the node type has no property to read it from.
     */
    public Optional<Integer> getDeploymentOrder() {
        return Optional.ofNullable(deploymentOrder);
    }

    /**
     * Affinity groups this MCIOP belongs to.
     *
     * <p>Filled from affinity policies. SOL001 Table 6.1-1 NOTE 3 states that while
     * {@code deploymentOrder} and {@code associatedVdu} map onto the {@code Mciop} node,
     * "the affinityOrAntiAffinityGroupId is mapped to tosca.policies.nfv.AffinityRule or
     * tosca.policies.nfv.AntiAffinityRule".
     */
    public List<String> getAffinityOrAntiAffinityGroupId() {
        return affinityOrAntiAffinityGroupId;
    }

    /**
     * VDUs deployed using this MCIOP, in declaration order.
     *
     * <p>The TOSCA requirement has occurrences {@code [1, UNBOUNDED]} and SOL001 Annex A.23
     * declares it twice on one node, so more than one entry is normal rather than exceptional.
     */
    public List<String> getAssociatedVdu() {
        return associatedVdu;
    }

    /** Reference to a file in the VNF package holding mapping rules for the mapping script. */
    public Optional<String> getMciopParameterMappingRule() {
        return Optional.ofNullable(mciopParameterMappingRule);
    }

    /** References the script invoked before a command is issued towards the CISM. */
    public Optional<String> getLcmOpParameterMappingScriptId() {
        return Optional.ofNullable(lcmOpParameterMappingScriptId);
    }

    @Override
    public String toString() {
        return mciopId + " -> " + associatedVdu;
    }

    /** Builder for {@link MciopProfile}. */
    public static final class Builder {
        private final String mciopId;
        private Integer deploymentOrder;
        private final List<String> affinityOrAntiAffinityGroupId = new ArrayList<>();
        private final List<String> associatedVdu = new ArrayList<>();
        private String mciopParameterMappingRule;
        private String lcmOpParameterMappingScriptId;

        private Builder(String mciopId) {
            this.mciopId = mciopId;
        }

        public Builder deploymentOrder(Integer value) {
            this.deploymentOrder = value;
            return this;
        }

        public Builder addAffinityGroup(String groupId) {
            if (!affinityOrAntiAffinityGroupId.contains(groupId)) {
                affinityOrAntiAffinityGroupId.add(groupId);
            }
            return this;
        }

        public Builder addAssociatedVdu(String vduId) {
            associatedVdu.add(vduId);
            return this;
        }

        public Builder mciopParameterMappingRule(String value) {
            this.mciopParameterMappingRule = value;
            return this;
        }

        public Builder lcmOpParameterMappingScriptId(String value) {
            this.lcmOpParameterMappingScriptId = value;
            return this;
        }

        public MciopProfile build() {
            return new MciopProfile(this);
        }
    }
}
