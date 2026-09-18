package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * {@code DeployableModule}, IFA011 V5.4.1 clause 7.1.8.24.
 *
 * <p>Clause 7.1.8.24.1: "a set of optional VDUs within a VNF deployment flavour. The VNFCs based on
 * VDUs that are represented by a DeployableModule are only instantiated if the DeployableModule is
 * selected by the consumer of the VNF lifecycle management interface."
 *
 * <p>An attribute of {@link VnfDf} rather than of the VNFD, per Table 7.1.8.2.2-1. Comes from a
 * {@code tosca.nodes.nfv.DeployableModule} node template, SOL001 V5.4.1 clause 6.8.16, whose
 * {@code member} requirement has occurrences [1, UNBOUNDED].
 */
public final class DeployableModule {

    private final String deployableModuleId;
    private final PropertyValue<String> name;
    private final PropertyValue<String> description;
    private final List<String> member;

    private DeployableModule(Builder builder) {
        this.deployableModuleId = builder.deployableModuleId;
        this.name = builder.name;
        this.description = builder.description;
        this.member = Collections.unmodifiableList(new ArrayList<>(builder.member));
    }

    public static Builder builder(String deployableModuleId) {
        return new Builder(deployableModuleId);
    }

    /**
     * Identifier of this DeployableModule. Mandatory, 1.
     *
     * <p>[ASSUMPTION] The node template name, as for every other identifier this library derives
     * apart from SwImageDesc.
     */
    public String getDeployableModuleId() {
        return deployableModuleId;
    }

    /** Name of the DeployableModule. Mandatory, 1. */
    public Optional<PropertyValue<String>> getName() {
        return Optional.ofNullable(name);
    }

    /** What the VNFCs deployed with these VDUs do. 0..1. */
    public Optional<PropertyValue<String>> getDescription() {
        return Optional.ofNullable(description);
    }

    /**
     * The VDUs in this module.
     *
     * <p>[PROJECT-SPECIFIC] Not an attribute of the IFA011 information element: there the membership
     * runs the other way, through {@code VduProfile.deployableModule}. Carried here as well because
     * SOL001 clause 6.8.16 states it on the node, and losing it would mean a module whose members
     * can only be found by scanning every profile.
     */
    public List<String> getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "DeployableModule(" + deployableModuleId + ", member=" + member + ")";
    }

    /** Builder for {@link DeployableModule}. */
    public static final class Builder {
        private final String deployableModuleId;
        private PropertyValue<String> name;
        private PropertyValue<String> description;
        private final List<String> member = new ArrayList<>();

        private Builder(String deployableModuleId) {
            this.deployableModuleId = deployableModuleId;
        }

        public Builder name(PropertyValue<String> value) {
            this.name = value;
            return this;
        }

        public Builder description(PropertyValue<String> value) {
            this.description = value;
            return this;
        }

        public Builder addMember(String value) {
            member.add(value);
            return this;
        }

        public DeployableModule build() {
            return new DeployableModule(this);
        }
    }
}
