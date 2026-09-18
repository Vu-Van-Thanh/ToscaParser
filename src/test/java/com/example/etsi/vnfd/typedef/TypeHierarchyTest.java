package com.example.etsi.vnfd.typedef;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercises the bundled ETSI catalogue alone, with no VNF package involved.
 *
 * <p>That is the situation every bundled example package puts the parser in: they import
 * {@code etsi_nfv_sol001_vnfd_types.yaml} without shipping it, so the built-in copy is the only
 * source of ETSI type definitions.
 */
class TypeHierarchyTest {

    private static TypeRegistry registry;
    private static TypeHierarchy hierarchy;

    @BeforeAll
    static void loadCatalogue() {
        TypeRegistryBuilder builder = new TypeRegistryBuilder();
        EtsiTypeCatalogue.addTo(builder);
        registry = builder.build();
        hierarchy = new TypeHierarchy(registry);
    }

    @Test
    @DisplayName("the bundled catalogue resolves every connection point type up to tosca.nodes.nfv.Cp")
    void connectionPointTypesResolveToCp() {
        // Cp is declared in the common types file, the CP types in the VNFD types file. If only the
        // latter were bundled, every one of these would dead-end and no CP could be classified.
        for (String cpType : Arrays.asList(EtsiTypes.VDU_CP, EtsiTypes.VNF_EXT_CP, EtsiTypes.VIP_CP,
                EtsiTypes.VIRTUAL_CP, EtsiTypes.VDU_SUB_CP)) {
            assertThat(hierarchy.isDerivedFrom(cpType, EtsiTypes.CP))
                    .as("%s must resolve to %s", cpType, EtsiTypes.CP)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("VduSubCp reaches Cp through VduCp, not directly")
    void resolvesThroughIntermediateType() {
        assertThat(hierarchy.ancestry(EtsiTypes.VDU_SUB_CP))
                .containsExactly(EtsiTypes.VDU_SUB_CP, EtsiTypes.VDU_CP, EtsiTypes.CP,
                        "tosca.nodes.Root");
    }

    @Test
    @DisplayName("a vendor node type derived from tosca.nodes.nfv.VNF is recognised as a VNF")
    void recognisesVendorDerivedVnfType() {
        // Reproduces what the bundled packages do: the VNF node declares a vendor type, so matching
        // on the literal string "tosca.nodes.nfv.VNF" would find nothing.
        TypeRegistryBuilder builder = new TypeRegistryBuilder();
        EtsiTypeCatalogue.addTo(builder);
        builder.add(vendorNodeType("ExampleCorp.SimpleWebCnf.1_0", EtsiTypes.VNF), "package");
        TypeHierarchy local = new TypeHierarchy(builder.build());

        assertThat(local.isDerivedFrom("ExampleCorp.SimpleWebCnf.1_0", EtsiTypes.VNF)).isTrue();
        assertThat(local.isDerivedFrom("ExampleCorp.SimpleWebCnf.1_0", EtsiTypes.MCIOP)).isFalse();
    }

    @Test
    @DisplayName("an unknown type is not treated as derived from anything")
    void unknownTypeIsNotDerived() {
        assertThat(hierarchy.isDerivedFrom("Vendor.Unknown.1_0", EtsiTypes.VNF)).isFalse();
        assertThat(hierarchy.ancestry("Vendor.Unknown.1_0")).containsExactly("Vendor.Unknown.1_0");
    }

    @Test
    @DisplayName("a derived_from cycle terminates instead of looping")
    void cyclicDerivedFromTerminates() {
        TypeRegistryBuilder builder = new TypeRegistryBuilder();
        builder.add(vendorNodeType("A", "B"), "package");
        builder.add(vendorNodeType("B", "A"), "package");
        TypeHierarchy local = new TypeHierarchy(builder.build());

        assertThat(local.ancestry("A")).containsExactly("A", "B");
        assertThat(local.isDerivedFrom("A", EtsiTypes.VNF)).isFalse();
    }

    private static java.util.Map<String, Object> vendorNodeType(String name, String parent) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("derived_from", parent);
        java.util.Map<String, Object> nodeTypes = new java.util.LinkedHashMap<>();
        nodeTypes.put(name, body);
        java.util.Map<String, Object> document = new java.util.LinkedHashMap<>();
        document.put("node_types", nodeTypes);
        return document;
    }
}
