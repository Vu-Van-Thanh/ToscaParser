package com.example.etsi.vnfd.template;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entry of a {@code substitution_filter}, e.g. {@code - flavour_id: { equal: simple }}.
 *
 * <p>This is how the two-level VNFD design identifies a deployment flavour. SOL001 V5.4.1 clause
 * 6.11.2 requires a lower-level service template to carry substitution mappings indicating "a
 * flavour_id property and its value as defined in substitution_filter which identifies the DF
 * corresponding to this low level template within the VNFD".
 *
 * <p>It is the authoritative source for the flavour identifier: in the multi-flavour example of
 * SOL001 Annex A.2 the lower-level template for the simple flavour never assigns {@code flavour_id}
 * on its VNF node template, so reading only node properties would get the wrong answer.
 */
public final class PropertyFilter {

    private final String propertyName;
    private final Map<String, Object> constraints = new LinkedHashMap<>();

    public PropertyFilter(String propertyName) {
        this.propertyName = propertyName;
    }

    /** The filtered property, e.g. {@code flavour_id}. */
    public String propertyName() {
        return propertyName;
    }

    /** Constraint operators keyed by name, e.g. {@code equal -> simple}. */
    public Map<String, Object> constraints() {
        return constraints;
    }

    /** The value of an {@code equal} constraint, which is the form SOL001 uses for flavour_id. */
    public Object equalValue() {
        return constraints.get("equal");
    }

    @Override
    public String toString() {
        return propertyName + constraints;
    }
}
