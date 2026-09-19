package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.fixture.Fixtures;
import com.example.etsi.vnfd.typedef.ArtifactTypeDef;
import com.example.etsi.vnfd.typedef.EtsiTypes;
import com.example.etsi.vnfd.typedef.GroupTypeDef;
import com.example.etsi.vnfd.typedef.InterfaceTypeDef;
import com.example.etsi.vnfd.typedef.PolicyTypeDef;
import com.example.etsi.vnfd.typedef.NodeTypeDef;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.RequirementDefinition;
import com.example.etsi.vnfd.typedef.TypeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the ETSI type definitions as a package ships them.
 *
 * <p>Nothing is bundled with the library, so these types reach the registry the only way any type
 * does: the descriptor imports {@code etsi_nfv_sol001_vnfd_types.yaml}, that file is in the
 * package, and it imports {@code etsi_nfv_sol001_common_types.yaml} in turn. Reading them through
 * a real package rather than off the classpath means this test fails if that chain ever breaks.
 */
class EtsiTypeDefinitionsTest {

    private static TypeRegistry registry;

    @BeforeAll
    static void loadTypesFromPackage() {
        registry = new PackageReader(Fixtures.packageDir(Fixtures.SIMPLE_WEB_CNF))
                .parse()
                .typeRegistry();
    }

    @Test
    @DisplayName("the import chain reaches both definition files")
    void loadsBothFiles() {
        // Cp lives in the common types file, VduCp in the VNFD types file. Requiring one of each
        // proves the second hop of the chain happened, not just the first.
        assertThat(registry.nodeType(EtsiTypes.CP)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.VDU_CP)).isPresent();
    }

    @Test
    @DisplayName("every CNF node type this library maps is present")
    void registersCnfNodeTypes() {
        assertThat(registry.nodeType(EtsiTypes.VNF)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.VDU_OS_CONTAINER)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.MCIOP)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.VDU_CP)).isPresent();
        assertThat(registry.nodeType(EtsiTypes.CP)).isPresent();
    }

    @Test
    @DisplayName("Mciop declares associatedVdu with occurrences [1, UNBOUNDED] and a target node type")
    void mciopRequirementCarriesCardinalityAndTargetType() {
        // Both facts live only on the requirement definition. Modelling requirements solely as
        // assignments would lose them, and with them the basis for two conformance checks.
        RequirementDefinition associatedVdu = registry.nodeType(EtsiTypes.MCIOP)
                .flatMap(t -> t.requirement(EtsiTypes.REQ_ASSOCIATED_VDU))
                .orElseThrow(AssertionError::new);

        assertThat(associatedVdu.minOccurrences()).isEqualTo(1);
        assertThat(associatedVdu.maxOccurrences()).isEqualTo(Integer.MAX_VALUE);
        assertThat(associatedVdu.allowsMultiple())
                .as("SOL001 Annex A.23 declares associatedVdu twice on one Mciop")
                .isTrue();
        assertThat(associatedVdu.node()).contains(EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT);
        assertThat(associatedVdu.capability()).contains(EtsiTypes.CAPABILITY_ASSOCIABLE_VDU);
    }

    @Test
    @DisplayName("the container requirement of a deployable unit permits many containers")
    void containerRequirementIsUnbounded() {
        RequirementDefinition container = registry.nodeType(EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT)
                .flatMap(t -> t.requirement(EtsiTypes.REQ_CONTAINER))
                .orElseThrow(AssertionError::new);

        assertThat(container.minOccurrences()).isZero();
        assertThat(container.maxOccurrences()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("HelmChart declares the file extensions its artifact may use")
    void helmChartDeclaresFileExtensions() {
        ArtifactTypeDef helmChart = registry.artifactType(EtsiTypes.ARTIFACT_HELM_CHART)
                .orElseThrow(AssertionError::new);

        assertThat(helmChart.fileExt()).containsExactlyInAnyOrder("tar", "tar.gz", "tgz");
        assertThat(helmChart.acceptsFileName("simple-web-cnf-1.0.0.tgz")).isTrue();
        assertThat(helmChart.acceptsFileName("README.txt")).isFalse();
    }

    @Test
    @DisplayName("affinity policies declare Mciop among their targets")
    void affinityPolicyTargetsIncludeMciop() {
        // SOL001 Table 6.1-1 NOTE 3 routes MciopProfile.affinityOrAntiAffinityGroupId through an
        // affinity policy rather than the Mciop node, so the mapper reads these targets.
        PolicyTypeDef affinity = registry.policyType(EtsiTypes.POLICY_AFFINITY_RULE)
                .orElseThrow(AssertionError::new);

        assertThat(affinity.targets()).contains(EtsiTypes.MCIOP,
                EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT);
    }

    @Test
    @DisplayName("Vnflcm declares both operations and notifications")
    void vnflcmDeclaresOperationsAndNotifications() {
        InterfaceTypeDef vnflcm = registry.interfaceType(EtsiTypes.INTERFACE_VNFLCM)
                .orElseThrow(AssertionError::new);

        assertThat(vnflcm.operations()).containsKeys("instantiate", "instantiate_start",
                "instantiate_end", "terminate", "scale");
        assertThat(vnflcm.notifications())
                .as("SOL001 clause 6.7.1 gives Vnflcm notifications as well as operations")
                .containsKey("change_current_package_notification");
    }

    @Test
    @DisplayName("PlacementGroup declares which node types may be members")
    void placementGroupDeclaresMembers() {
        GroupTypeDef placement = registry.groupType(EtsiTypes.GROUP_PLACEMENT)
                .orElseThrow(AssertionError::new);

        assertThat(placement.members()).contains(EtsiTypes.MCIOP,
                EtsiTypes.VDU_OS_CONTAINER_DEPLOYABLE_UNIT);
    }

    @Test
    @DisplayName("the vnfm_info pattern constraint is loaded and does reject GenericVnfm")
    void vnfmInfoPatternIsEnforceable() {
        // Every bundled example package writes vnfm_info: [ GenericVnfm ], which the pattern on
        // tosca.nodes.nfv.VNF.vnfm_info does not accept. Loading the constraint is what lets the
        // parser report that rather than pass it through unnoticed.
        PropertyDef vnfmInfo = registry.nodeType(EtsiTypes.VNF)
                .map(t -> t.properties().get(EtsiTypes.PROP_VNFM_INFO))
                .orElseThrow(AssertionError::new);

        PropertyDef entry = vnfmInfo.entrySchema().orElseThrow(AssertionError::new);
        assertThat(entry.constraints()).isNotEmpty();
        assertThat(TypeReader.validate(entry.constraints().get(0), "GenericVnfm")).isPresent();
        assertThat(TypeReader.validate(entry.constraints().get(0), "0:MyCompany-1.0.0")).isEmpty();
    }
}
