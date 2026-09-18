package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lays the defaults a type declares under the values a template assigns.
 *
 * <p>Not a nicety. SOL001 V5.4.1 Annex A.23 writes a VNF node template that assigns only
 * {@code flavour_description}, leaving {@code descriptor_id}, {@code provider},
 * {@code software_version} and the rest on the VNF-specific node type. Binding the template alone
 * yields a VNFD with no identifier at all. Clause 6.11.2 makes that arrangement the normal one, not
 * an edge case, since it requires the VNF node type to be derived from {@code tosca.nodes.nfv.VNF}.
 *
 * <p>Applied before binding rather than after: merging two maps is exactly the semantics wanted -
 * the template wins where it speaks - and it needs no reflection over the bound object.
 */
public final class TypeDefaults {

    private final TypeHierarchy hierarchy;

    public TypeDefaults(TypeHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * The assigned properties with type defaults filled in.
     *
     * @param declaredType the type the template declares, which may be a vendor type
     * @param assigned     the {@code properties} block of the template
     */
    public Map<String, Object> apply(String declaredType, Map<String, Object> assigned) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (Map.Entry<String, PropertyDef> e
                : hierarchy.effectivePropertiesOfAnyType(declaredType).entrySet()) {
            e.getValue().defaultValue().ifPresent(value -> merged.put(e.getKey(), value));
        }
        if (assigned != null) {
            merged.putAll(assigned);
        }
        return merged;
    }
}
