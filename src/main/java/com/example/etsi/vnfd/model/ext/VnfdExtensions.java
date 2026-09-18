package com.example.etsi.vnfd.model.ext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Everything a consumer needs that IFA011 has no place for.
 *
 * <p>[PROJECT-SPECIFIC] Kept apart from the standard model on purpose. A reader of a parsed VNFD
 * can then tell at a glance which values come from a normative information element and which are
 * this library's or a product's addition, instead of finding invented attributes mixed in among
 * real ones.
 *
 * <p>This is also where a host application adds its own descriptors - product-specific connection
 * point or virtual link types, trigger policies, indicator templates - without disturbing the
 * standard part.
 */
public final class VnfdExtensions {

    private final Map<String, MciopArtifacts> mciopArtifacts;
    private final Map<String, Object> vendorExtensions;

    private VnfdExtensions(Builder builder) {
        this.mciopArtifacts = Collections.unmodifiableMap(new LinkedHashMap<>(builder.mciopArtifacts));
        this.vendorExtensions =
                Collections.unmodifiableMap(new LinkedHashMap<>(builder.vendorExtensions));
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Package file locations for each MCIOP, keyed by {@code mciopId}. */
    public Map<String, MciopArtifacts> getMciopArtifacts() {
        return mciopArtifacts;
    }

    /** The files of one MCIOP. */
    public Optional<MciopArtifacts> getMciopArtifacts(String mciopId) {
        return Optional.ofNullable(mciopArtifacts.get(mciopId));
    }

    /** Room for a host application to attach its own data without changing this library. */
    public Map<String, Object> getVendorExtensions() {
        return vendorExtensions;
    }

    public boolean isEmpty() {
        return mciopArtifacts.isEmpty() && vendorExtensions.isEmpty();
    }

    @Override
    public String toString() {
        return "VnfdExtensions" + mciopArtifacts.keySet();
    }

    /** Builder for {@link VnfdExtensions}. */
    public static final class Builder {
        private final Map<String, MciopArtifacts> mciopArtifacts = new LinkedHashMap<>();
        private final Map<String, Object> vendorExtensions = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder addMciopArtifacts(MciopArtifacts value) {
            mciopArtifacts.put(value.getMciopId(), value);
            return this;
        }

        public Builder putVendorExtension(String key, Object value) {
            vendorExtensions.put(key, value);
            return this;
        }

        public VnfdExtensions build() {
            return new VnfdExtensions(this);
        }
    }
}
