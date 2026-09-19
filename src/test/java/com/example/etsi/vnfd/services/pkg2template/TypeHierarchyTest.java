package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.typedef.TypeRegistry;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Walks {@code derived_from} over the types a real package supplies.
 *
 * <p>The types come from the package because nothing is bundled with the library: the descriptor
 * imports {@code etsi_nfv_sol001_vnfd_types.yaml}, the package ships it, and it imports the common
 * types file in turn. So these chains are only walkable if that import chain held.
 */
class TypeHierarchyTest {

    private static TypeRegistry registry;
    private static TypeReader.Hierarchy hierarchy;

    @BeforeAll
    static void loadTypesFromPackage() {
        registry = new PackageReader(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF))
                .parse()
                .typeRegistry();
        hierarchy = new TypeReader.Hierarchy(registry);
    }

    @Test
    @DisplayName("every connection point type resolves up to tosca.nodes.nfv.Cp")
    void connectionPointTypesResolveToCp() {
        // Cp is declared in the common types file, the CP types in the VNFD types file. If the
        // package shipped only the latter, every one of these would dead-end and no CP could be
        // classified.
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
        // Not a synthetic type: the fixture's descriptor really declares this one, because SOL001
        // V5.4.1 clause 6.11.2 requires a VNF node type to be VNF-specific. Matching on the literal
        // string "tosca.nodes.nfv.VNF" would therefore find nothing.
        assertThat(hierarchy.isDerivedFrom("ExampleCorp.SimpleWebCnf.1_0", EtsiTypes.VNF)).isTrue();
        assertThat(hierarchy.isDerivedFrom("ExampleCorp.SimpleWebCnf.1_0", EtsiTypes.MCIOP)).isFalse();
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
        TypeReader builder = new TypeReader();
        builder.add(vendorNodeType("A", "B"), "package");
        builder.add(vendorNodeType("B", "A"), "package");
        TypeReader.Hierarchy local = new TypeReader.Hierarchy(builder.build());

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
