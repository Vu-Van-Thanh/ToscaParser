package com.example.etsi.vnfd.model;

import com.example.etsi.vnfd.toscatype.data.TypeOfStorage;
import com.example.etsi.vnfd.template.value.PropertyValue;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code VirtualStorageDesc}, IFA011 V5.4.1 clause 7.1.9.4.
 *
 * <p>One element with an enumerated {@code typeOfStorage}, where TOSCA has three node types. Which
 * node type a descriptor used is therefore what sets the enumeration, and the corresponding data
 * structure is the one clause 7.1.9.4.2.2 says "shall be present" for that value.
 */
public final class VirtualStorageDesc {

    private final String id;
    private final TypeOfStorage typeOfStorage;
    private final Map<String, Object> storageData;
    private final PropertyValue<Boolean> perVnfcInstance;
    private final Map<String, Object> nfviMaintenanceInfo;
    private final String swImageDesc;

    private VirtualStorageDesc(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.typeOfStorage = Objects.requireNonNull(builder.typeOfStorage, "typeOfStorage");
        this.storageData = builder.storageData;
        this.perVnfcInstance = builder.perVnfcInstance;
        this.nfviMaintenanceInfo = builder.nfviMaintenanceInfo;
        this.swImageDesc = builder.swImageDesc;
    }

    public static Builder builder(String id, TypeOfStorage typeOfStorage) {
        return new Builder(id, typeOfStorage);
    }

    /** Mandatory. [ASSUMPTION] Taken from the storage node template name. */
    public String getId() {
        return id;
    }

    /** Mandatory. BLOCK, OBJECT or FILE, decided by which TOSCA node type was used. */
    public TypeOfStorage getTypeOfStorage() {
        return typeOfStorage;
    }

    /** The block, object or file storage details, whichever the type calls for. */
    public Map<String, Object> getStorageData() {
        return storageData == null ? Collections.emptyMap() : storageData;
    }

    /** Whether one storage instance exists per VNFC instance. Defaults to true. */
    public Optional<PropertyValue<Boolean>> getPerVnfcInstance() {
        return Optional.ofNullable(perVnfcInstance);
    }

    public Map<String, Object> getNfviMaintenanceInfo() {
        return nfviMaintenanceInfo == null ? Collections.emptyMap() : nfviMaintenanceInfo;
    }

    /**
     * Software image stored on this volume.
     *
     * <p>SOL001 V5.4.1 clause 6.8.4.7 permits one only on block storage attached to a
     * {@code Vdu.Compute}: "otherwise, such artifact definition shall not be present". Storage
     * attached to a container deployable unit carrying one is a conformance error rather than a
     * parse failure.
     */
    public Optional<String> getSwImageDesc() {
        return Optional.ofNullable(swImageDesc);
    }

    @Override
    public String toString() {
        return id + " (" + typeOfStorage + ")";
    }

    /** Builder for {@link VirtualStorageDesc}. */
    public static final class Builder {
        private final String id;
        private final TypeOfStorage typeOfStorage;
        private Map<String, Object> storageData;
        private PropertyValue<Boolean> perVnfcInstance;
        private Map<String, Object> nfviMaintenanceInfo;
        private String swImageDesc;

        private Builder(String id, TypeOfStorage typeOfStorage) {
            this.id = id;
            this.typeOfStorage = typeOfStorage;
        }

        public Builder storageData(Map<String, Object> value) {
            this.storageData = value;
            return this;
        }

        public Builder perVnfcInstance(PropertyValue<Boolean> value) {
            this.perVnfcInstance = value;
            return this;
        }

        public Builder nfviMaintenanceInfo(Map<String, Object> value) {
            this.nfviMaintenanceInfo = value;
            return this;
        }

        public Builder swImageDesc(String value) {
            this.swImageDesc = value;
            return this;
        }

        public VirtualStorageDesc build() {
            return new VirtualStorageDesc(this);
        }
    }
}
