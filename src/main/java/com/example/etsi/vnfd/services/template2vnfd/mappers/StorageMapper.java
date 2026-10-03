package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.model.Vdu;
import com.example.etsi.vnfd.model.VirtualStorageDesc;
import com.example.etsi.vnfd.services.template2vnfd.VnfdUtils;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.toscatype.data.TypeOfStorage;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.node.VduVirtualBlockStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualFileStorage;
import com.example.etsi.vnfd.toscatype.node.VduVirtualObjectStorage;
import java.util.Optional;

/**
 * SOL001 V5.4.1 clauses 6.8.4 / 6.8.5 / 6.8.6 {@code Vdu.Virtual*Storage} to IFA011 V5.4.1 clause
 * 7.1.9.4.2 {@code VirtualStorageDesc}.
 *
 * <p>The kind of storage comes from the node TYPE, not from a property: SOL001 gives block, object
 * and file storage three separate node types, each with its own data property, while IFA011 has one
 * information element carrying a {@code typeOfStorage}. Walking {@code derived_from} is what turns
 * one into the other, so a vendor type derived from any of the three still classifies.
 */
public final class StorageMapper {

    private StorageMapper() {
    }

    /** Empty when the node is not one of the three storage types. */
    public static Optional<VirtualStorageDesc> map(NfvNode node) {
        if (node instanceof VduVirtualBlockStorage) {
            VduVirtualBlockStorage.Properties p = ((VduVirtualBlockStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.BLOCK,
                    p == null ? null : p.getVirtualBlockStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        if (node instanceof VduVirtualObjectStorage) {
            VduVirtualObjectStorage.Properties p = ((VduVirtualObjectStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.OBJECT,
                    p == null ? null : p.getVirtualObjectStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        if (node instanceof VduVirtualFileStorage) {
            VduVirtualFileStorage.Properties p = ((VduVirtualFileStorage) node).getProperties();
            return Optional.of(build(node, TypeOfStorage.FILE,
                    p == null ? null : p.getVirtualFileStorageData(),
                    p == null ? null : p.getPerVnfcInstance(),
                    p == null ? null : p.getNfviMaintenanceInfo()));
        }
        return Optional.empty();
    }

    private static VirtualStorageDesc build(NfvNode node, TypeOfStorage type,
            Object storageData, PropertyValue<Boolean> perVnfcInstance, Object maintenance) {
        VirtualStorageDesc.Builder builder =
                VirtualStorageDesc.builder(VnfdUtils.nodeId(node), type);
        if (storageData != null) {
            builder.storageData(PlainValues.asMap(storageData));
        }
        if (perVnfcInstance != null) {
            builder.perVnfcInstance(perVnfcInstance);
        }
        if (maintenance != null) {
            builder.nfviMaintenanceInfo(PlainValues.asMap(maintenance));
        }
        return builder.build();
    }

}
