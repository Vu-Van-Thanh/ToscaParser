package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code VnfPackageChangeInfo}, IFA011 V5.4.1 clause 7.1.15.2.
 *
 * <p>The processes and rules for changing a VNF instance to a different VNF package. Comes from a
 * {@code tosca.policies.nfv.VnfPackageChange} policy, SOL001 V5.4.1 clause 6.10.15.
 *
 * <p>{@code selector} and {@code componentMapping} are carried as maps rather than re-modelled.
 * IFA011 gives each its own table - 7.1.15.3 VersionSelector and 7.1.15.4 ComponentMapping - and
 * passing them through whole is both fewer places to lose a field and a smaller surface to keep in
 * step with a new release of the specification.
 */
public final class VnfPackageChangeInfo {

    private final String changeId;
    private final List<Map<String, Object>> selector;
    private final PropertyValue<String> modificationQualifier;
    private final PropertyValue<String> additionalModificationDescription;
    private final List<Map<String, Object>> componentMapping;
    private final PropertyValue<String> destinationFlavourId;

    private VnfPackageChangeInfo(Builder builder) {
        this.changeId = builder.changeId;
        this.selector = Collections.unmodifiableList(new ArrayList<>(builder.selector));
        this.modificationQualifier = builder.modificationQualifier;
        this.additionalModificationDescription = builder.additionalModificationDescription;
        this.componentMapping =
                Collections.unmodifiableList(new ArrayList<>(builder.componentMapping));
        this.destinationFlavourId = builder.destinationFlavourId;
    }

    public static Builder builder(String changeId) {
        return new Builder(changeId);
    }

    /**
     * Identifier of this change.
     *
     * <p>[ASSUMPTION] The policy name. IFA011 Table 7.1.15.2.2-1 declares no identifier attribute of
     * its own, so something has to name the entry for a consumer to refer to it.
     */
    public String getChangeId() {
        return changeId;
    }

    /** Which source and destination VNFD combinations this applies to. Mandatory, 1..N. */
    public List<Map<String, Object>> getSelector() {
        return selector;
    }

    /** The type of modification the change results in. Mandatory, 1. */
    public Optional<PropertyValue<String>> getModificationQualifier() {
        return Optional.ofNullable(modificationQualifier);
    }

    /** Additional description a VNF provider may give. 0..N in IFA011; one entry in SOL001. */
    public Optional<PropertyValue<String>> getAdditionalModificationDescription() {
        return Optional.ofNullable(additionalModificationDescription);
    }

    /** Mapping of identifiers between the source and destination packages. 0..N. */
    public List<Map<String, Object>> getComponentMapping() {
        return componentMapping;
    }

    /** The deployment flavour in the destination package. Mandatory, 1. */
    public Optional<PropertyValue<String>> getDestinationFlavourId() {
        return Optional.ofNullable(destinationFlavourId);
    }

    @Override
    public String toString() {
        return "VnfPackageChangeInfo(" + changeId + ")";
    }

    /** Builder for {@link VnfPackageChangeInfo}. */
    public static final class Builder {
        private final String changeId;
        private final List<Map<String, Object>> selector = new ArrayList<>();
        private PropertyValue<String> modificationQualifier;
        private PropertyValue<String> additionalModificationDescription;
        private final List<Map<String, Object>> componentMapping = new ArrayList<>();
        private PropertyValue<String> destinationFlavourId;

        private Builder(String changeId) {
            this.changeId = changeId;
        }

        public Builder addSelector(Map<String, Object> value) {
            if (value != null) {
                selector.add(value);
            }
            return this;
        }

        public Builder modificationQualifier(PropertyValue<String> value) {
            this.modificationQualifier = value;
            return this;
        }

        public Builder additionalModificationDescription(PropertyValue<String> value) {
            this.additionalModificationDescription = value;
            return this;
        }

        public Builder addComponentMapping(Map<String, Object> value) {
            if (value != null) {
                componentMapping.add(value);
            }
            return this;
        }

        public Builder destinationFlavourId(PropertyValue<String> value) {
            this.destinationFlavourId = value;
            return this;
        }

        public VnfPackageChangeInfo build() {
            return new VnfPackageChangeInfo(this);
        }
    }
}
