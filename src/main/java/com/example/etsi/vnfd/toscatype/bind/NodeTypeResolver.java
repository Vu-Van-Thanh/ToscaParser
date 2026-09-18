package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.toscatype.node.EtsiNodeType;
import com.example.etsi.vnfd.toscatype.node.NfvNode;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Decides which SOL001 class a node template belongs to.
 *
 * <p>By {@code derived_from}, never by comparing type names - see {@link EtsiNodeType} for why.
 * When a declared type derives from several of the registered ETSI types, the nearest ancestor
 * wins, which is what separates {@code VduSubCp} from {@code VduCp} from {@code Cp} without anyone
 * having to state a precedence.
 */
public final class NodeTypeResolver {

    private final TypeHierarchy hierarchy;
    private final Map<String, Class<? extends NfvNode>> byEtsiType;

    public NodeTypeResolver(TypeHierarchy hierarchy, List<Class<? extends NfvNode>> classes) {
        this.hierarchy = hierarchy;
        Map<String, Class<? extends NfvNode>> map = new LinkedHashMap<>();
        for (Class<? extends NfvNode> type : classes) {
            EtsiNodeType annotation = type.getAnnotation(EtsiNodeType.class);
            if (annotation == null) {
                throw new IllegalStateException(type.getName() + " has no @EtsiNodeType");
            }
            map.put(annotation.value(), type);
        }
        this.byEtsiType = Collections.unmodifiableMap(map);
    }

    /** The class for a declared type, or empty when it derives from none of the ETSI node types. */
    public Optional<Class<? extends NfvNode>> resolve(String declaredType) {
        if (declaredType == null) {
            return Optional.empty();
        }
        return hierarchy.nearestAncestorAmong(declaredType, byEtsiType.keySet())
                .map(byEtsiType::get);
    }

    /** The ETSI type a declared type was recognised as. */
    public Optional<String> etsiTypeOf(String declaredType) {
        return declaredType == null
                ? Optional.empty()
                : hierarchy.nearestAncestorAmong(declaredType, byEtsiType.keySet());
    }

    /** The registered ETSI types, for diagnostics and for the property cross-check test. */
    public Map<String, Class<? extends NfvNode>> registered() {
        return byEtsiType;
    }
}
