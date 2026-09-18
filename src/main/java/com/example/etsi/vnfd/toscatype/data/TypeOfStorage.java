package com.example.etsi.vnfd.toscatype.data;

/**
 * Values of {@code VirtualStorageDesc.typeOfStorage}, IFA011 V5.4.1 Table 7.1.9.4.2.2-1.
 *
 * <p>Never read from a property. SOL001 V5.4.1 splits storage into three node types - cl. 6.8.4
 * {@code Vdu.VirtualBlockStorage}, cl. 6.8.5 {@code Vdu.VirtualObjectStorage} and cl. 6.8.6
 * {@code Vdu.VirtualFileStorage} - so the kind of storage follows from which type the descriptor
 * used, and the corresponding {@code virtual_*_storage_data} property is present only for that one.
 */
public enum TypeOfStorage {
    BLOCK,
    OBJECT,
    FILE
}
