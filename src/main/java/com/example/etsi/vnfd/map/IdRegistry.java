package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.toscatype.policy.NfvPolicy;

/**
 * Every rule this library uses to derive an IFA011 identifier from a TOSCA declaration.
 *
 * <p>Gathered in one class on purpose. The rules are nearly all the same - "the node template name
 * is the id" - but only one of them is stated outright by the specification, and scattering them
 * across the mappers would make it impossible to see which is which.
 */
final class IdRegistry {

    private IdRegistry() {
    }

    /**
     * [VERIFIED] SOL001 V5.4.1 clause 6.8.12.6: the node template name of a
     * {@code Vdu.OsContainer} "fulfils the purpose of the 'id' attribute of the SwImageDesc
     * information element and hence it will be used in APIs to identify the software image id from
     * the VNFD perspective".
     */
    static String swImageDescId(NfvNode owningNode) {
        return owningNode.getKey();
    }

    /** [ASSUMPTION] The node template name. SOL001 states the rule only for SwImageDesc. */
    static String vduId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String osContainerDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualStorageDescId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String cpdId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] As above. */
    static String virtualLinkDescId(NfvNode node) {
        return node.getKey();
    }

    /**
     * [ASSUMPTION] The node template name.
     *
     * <p>IFA011 V5.4.1 clause 7.1.8.20.2 says {@code mciopId} "identifies the MCIOP in the VNF
     * package" without saying how. The node template name is the only stable handle a descriptor
     * offers, and SOL001 Table 6.1-1 NOTE 3 maps {@code associatedVdu} and {@code deploymentOrder}
     * onto this node type, so the profile is built around it either way.
     */
    static String mciopId(NfvNode node) {
        return node.getKey();
    }

    /** [ASSUMPTION] The artifact definition name. */
    static String lcmOpParameterMappingScriptId(ArtifactDefinition artifact) {
        return artifact.name();
    }

    /**
     * [ASSUMPTION] The policy name.
     *
     * <p>SOL001 Table 6.1-1 NOTE 3 states that {@code affinityOrAntiAffinityGroupId} maps to an
     * {@code AffinityRule} or {@code AntiAffinityRule} policy, but not what the group id is.
     */
    static String affinityGroupId(NfvPolicy policy) {
        return policy.getKey();
    }

    /** [ASSUMPTION] Interface plus operation, e.g. {@code Vnflcm.instantiate_start}. */
    static String lcmScriptId(String interfaceName, String operationName) {
        return interfaceName + "." + operationName;
    }
}
