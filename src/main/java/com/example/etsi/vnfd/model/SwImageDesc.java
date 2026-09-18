package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.toscatype.data.ChecksumData;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Quantity;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code SwImageDesc}, IFA011 V5.4.1 clause 7.1.6.5.
 *
 * <p>The identifier does not come from the artifact. SOL001 V5.4.1 clause 6.8.12.6 states that the
 * node template name of the owning {@code Vdu.OsContainer} "fulfils the purpose of the 'id'
 * attribute of the SwImageDesc information element and hence it will be used in APIs to identify
 * the software image id from the VNFD perspective". Clause 6.8.3.7 says the same for
 * {@code Vdu.Compute}, and clause 6.8.4.7 for {@code Vdu.VirtualBlockStorage}: one rule, three
 * sources, and in every case the owning node template rather than the artifact definition.
 */
public final class SwImageDesc {

    private final String id;
    private final PropertyValue<String> name;
    private final PropertyValue<String> version;
    private final PropertyValue<String> provider;
    private final ChecksumData checksum;
    private final PropertyValue<String> containerFormat;
    private final PropertyValue<String> diskFormat;
    private final PropertyValue<Quantity> size;
    private final PropertyValue<Quantity> minDisk;
    private final PropertyValue<Quantity> minRam;
    private final PropertyValue<String> operatingSystem;
    private final String swImage;

    private SwImageDesc(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = builder.name;
        this.version = builder.version;
        this.provider = builder.provider;
        this.checksum = builder.checksum;
        this.containerFormat = builder.containerFormat;
        this.diskFormat = builder.diskFormat;
        this.size = builder.size;
        this.minDisk = builder.minDisk;
        this.minRam = builder.minRam;
        this.operatingSystem = builder.operatingSystem;
        this.swImage = builder.swImage;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Mandatory. The node template name of the owning node, per SOL001 clause 6.8.12.6. */
    public String getId() {
        return id;
    }

    public Optional<PropertyValue<String>> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<PropertyValue<String>> getVersion() {
        return Optional.ofNullable(version);
    }

    public Optional<PropertyValue<String>> getProvider() {
        return Optional.ofNullable(provider);
    }

    public Optional<ChecksumData> getChecksum() {
        return Optional.ofNullable(checksum);
    }

    public Optional<PropertyValue<String>> getContainerFormat() {
        return Optional.ofNullable(containerFormat);
    }

    public Optional<PropertyValue<String>> getDiskFormat() {
        return Optional.ofNullable(diskFormat);
    }

    public Optional<PropertyValue<Quantity>> getSize() {
        return Optional.ofNullable(size);
    }

    public Optional<PropertyValue<Quantity>> getMinDisk() {
        return Optional.ofNullable(minDisk);
    }

    public Optional<PropertyValue<Quantity>> getMinRam() {
        return Optional.ofNullable(minRam);
    }

    public Optional<PropertyValue<String>> getOperatingSystem() {
        return Optional.ofNullable(operatingSystem);
    }

    /** The image reference, resolved against the package root. */
    public Optional<String> getSwImage() {
        return Optional.ofNullable(swImage);
    }

    @Override
    public String toString() {
        return id + " -> " + swImage;
    }

    /** Builder for {@link SwImageDesc}. */
    public static final class Builder {
        private final String id;
        private PropertyValue<String> name;
        private PropertyValue<String> version;
        private PropertyValue<String> provider;
        private ChecksumData checksum;
        private PropertyValue<String> containerFormat;
        private PropertyValue<String> diskFormat;
        private PropertyValue<Quantity> size;
        private PropertyValue<Quantity> minDisk;
        private PropertyValue<Quantity> minRam;
        private PropertyValue<String> operatingSystem;
        private String swImage;

        private Builder(String id) {
            this.id = id;
        }

        public Builder name(PropertyValue<String> value) {
            this.name = value;
            return this;
        }

        public Builder version(PropertyValue<String> value) {
            this.version = value;
            return this;
        }

        public Builder provider(PropertyValue<String> value) {
            this.provider = value;
            return this;
        }

        public Builder checksum(ChecksumData value) {
            this.checksum = value;
            return this;
        }

        public Builder containerFormat(PropertyValue<String> value) {
            this.containerFormat = value;
            return this;
        }

        public Builder diskFormat(PropertyValue<String> value) {
            this.diskFormat = value;
            return this;
        }

        public Builder size(PropertyValue<Quantity> value) {
            this.size = value;
            return this;
        }

        public Builder minDisk(PropertyValue<Quantity> value) {
            this.minDisk = value;
            return this;
        }

        public Builder minRam(PropertyValue<Quantity> value) {
            this.minRam = value;
            return this;
        }

        public Builder operatingSystem(PropertyValue<String> value) {
            this.operatingSystem = value;
            return this;
        }

        public Builder swImage(String value) {
            this.swImage = value;
            return this;
        }

        public SwImageDesc build() {
            return new SwImageDesc(this);
        }
    }
}
