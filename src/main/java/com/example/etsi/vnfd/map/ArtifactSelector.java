package com.example.etsi.vnfd.map;

import com.example.etsi.vnfd.template.ArtifactDefinition;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Finds the artifacts of a node by ETSI type.
 *
 * <p>Artifacts cannot be looked up by name: the name of an artifact definition is the VNFD author's
 * choice - the bundled packages write {@code sw_image} and {@code web_helm_chart} - while SOL001
 * always speaks of the type. Clause 6.8.12.6 asks for "an artifact of type
 * tosca.artifacts.nfv.SwImage"; clause 6.8.14.7 caps each type on an {@code Mciop} at one.
 *
 * <p>Matching walks {@code derived_from} like everything else, so a vendor artifact type derived
 * from an ETSI one is still found.
 */
final class ArtifactSelector {

    private final TypeHierarchy hierarchy;

    ArtifactSelector(TypeHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /** Every artifact of the given type, in declaration order. */
    List<ArtifactDefinition> allOfType(NfvNode node, String etsiArtifactType) {
        Map<String, ArtifactDefinition> declared = node.getArtifacts();
        if (declared == null || declared.isEmpty()) {
            return Collections.emptyList();
        }
        List<ArtifactDefinition> out = new ArrayList<>();
        for (ArtifactDefinition artifact : declared.values()) {
            if (hierarchy.isDerivedFrom(artifact.type(), etsiArtifactType)) {
                out.add(artifact);
            }
        }
        return out;
    }

    /**
     * The single artifact of that type.
     *
     * <p>More than one is a rule violation, not a parse failure, so the first is returned and the
     * caller checks {@link #allOfType} when it needs to report the cardinality.
     */
    Optional<ArtifactDefinition> ofType(NfvNode node, String etsiArtifactType) {
        List<ArtifactDefinition> all = allOfType(node, etsiArtifactType);
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /** The package-root-relative path of an artifact, falling back to the reference as written. */
    static String pathOf(ArtifactDefinition artifact) {
        return artifact.resolvedFile().orElse(artifact.file());
    }
}
