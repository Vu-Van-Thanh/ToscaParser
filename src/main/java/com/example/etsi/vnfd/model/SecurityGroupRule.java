package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@code SecurityGroupRule}, IFA011 V5.4.1 clause 7.1.6.9.
 *
 * <p>Clause 7.1.6.9.1: matching criteria for ingress and/or egress traffic to and from the connection
 * points it is applied to, under "a permissive model where all security group rules applied to a CP
 * are dealt with in an OR logic fashion".
 *
 * <p>The NOTE in that clause matters for CNF: "For VDUs based on one or set of OS containers,
 * associating different security group rules to different CPs of a VDU might not be supported by all
 * underlying container technologies."
 *
 * <p>Comes from a {@code tosca.policies.nfv.SecurityGroupRule} policy, SOL001 V5.4.1 clause 6.10.13,
 * whose targets are the connection points the rule applies to.
 */
public final class SecurityGroupRule {

    private final String securityGroupRuleId;
    private final PropertyValue<String> description;
    private final PropertyValue<String> direction;
    private final PropertyValue<String> etherType;
    private final PropertyValue<String> protocol;
    private final PropertyValue<Integer> portRangeMin;
    private final PropertyValue<Integer> portRangeMax;
    private final List<String> targets;

    private SecurityGroupRule(Builder builder) {
        this.securityGroupRuleId = builder.securityGroupRuleId;
        this.description = builder.description;
        this.direction = builder.direction;
        this.etherType = builder.etherType;
        this.protocol = builder.protocol;
        this.portRangeMin = builder.portRangeMin;
        this.portRangeMax = builder.portRangeMax;
        this.targets = Collections.unmodifiableList(new ArrayList<>(builder.targets));
    }

    public static Builder builder(String securityGroupRuleId) {
        return new Builder(securityGroupRuleId);
    }

    /** Identifier of this rule. Mandatory, 1. */
    public String getSecurityGroupRuleId() {
        return securityGroupRuleId;
    }

    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    /** INGRESS or EGRESS. 0..1, "Defaults to INGRESS". */
    public Optional<PropertyValue<String>> getDirection() {
        return Optional.ofNullable(direction);
    }

    /** The protocol carried over the Ethernet layer. 0..1. */
    public Optional<PropertyValue<String>> getEtherType() {
        return Optional.ofNullable(etherType);
    }

    /** The protocol carried over the IP layer. 0..1. */
    public Optional<PropertyValue<String>> getProtocol() {
        return Optional.ofNullable(protocol);
    }

    public Optional<PropertyValue<Integer>> getPortRangeMin() {
        return Optional.ofNullable(portRangeMin);
    }

    public Optional<PropertyValue<Integer>> getPortRangeMax() {
        return Optional.ofNullable(portRangeMax);
    }

    /**
     * The connection points this rule is applied to.
     *
     * <p>[PROJECT-SPECIFIC] Not an attribute of the IFA011 element: there the reference runs the
     * other way, from {@code VduCpd.securityGroupRuleId} and {@code VnfExtCpd.securityGroupRuleId}
     * back to the rule. Kept because SOL001 states it as the policy targets, and dropping it would
     * mean the application of a rule could only be recovered by scanning every connection point.
     */
    public List<String> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        return "SecurityGroupRule(" + securityGroupRuleId + ", targets=" + targets + ")";
    }

    /** Builder for {@link SecurityGroupRule}. */
    public static final class Builder {
        private final String securityGroupRuleId;
        private PropertyValue<String> description;
        private PropertyValue<String> direction;
        private PropertyValue<String> etherType;
        private PropertyValue<String> protocol;
        private PropertyValue<Integer> portRangeMin;
        private PropertyValue<Integer> portRangeMax;
        private final List<String> targets = new ArrayList<>();

        private Builder(String securityGroupRuleId) {
            this.securityGroupRuleId = securityGroupRuleId;
        }

        public Builder description(PropertyValue<String> value) {
            this.description = value;
            return this;
        }

        public Builder direction(PropertyValue<String> value) {
            this.direction = value;
            return this;
        }

        public Builder etherType(PropertyValue<String> value) {
            this.etherType = value;
            return this;
        }

        public Builder protocol(PropertyValue<String> value) {
            this.protocol = value;
            return this;
        }

        public Builder portRangeMin(PropertyValue<Integer> value) {
            this.portRangeMin = value;
            return this;
        }

        public Builder portRangeMax(PropertyValue<Integer> value) {
            this.portRangeMax = value;
            return this;
        }

        public Builder addTarget(String value) {
            targets.add(value);
            return this;
        }

        public SecurityGroupRule build() {
            return new SecurityGroupRule(this);
        }
    }
}
