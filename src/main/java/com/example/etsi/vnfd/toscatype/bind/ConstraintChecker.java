package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.template.SourceRef;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.PropertyValueParser;
import com.example.etsi.vnfd.template.value.ScalarUnitParser;
import com.example.etsi.vnfd.typedef.Constraint;
import com.example.etsi.vnfd.typedef.PropertyDef;
import com.example.etsi.vnfd.typedef.TypeHierarchy;
import com.example.etsi.vnfd.validation.Findings;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Checks assigned properties against what their type declares.
 *
 * <p>Both what is checked and what counts as a violation come from the type definitions rather than
 * from annotations on the model. {@code required: true} and {@code constraints:} are written in
 * {@code etsi_nfv_sol001_vnfd_types.yaml}; stating them again in Java would create a second copy of
 * the same rule, and the two drift the moment ETSI publishes a new version.
 *
 * <p>A value still bound to an input or a runtime attribute is skipped: SOL001 V5.4.1 clause 5.9
 * allows the expression, and there is nothing to check until something evaluates it.
 */
public final class ConstraintChecker {

    private static final String CLAUSE_CONSTRAINTS = "TOSCA Simple Profile YAML 1.3 cl. 3.6.3";
    private static final String CLAUSE_REQUIRED = "TOSCA Simple Profile YAML 1.3 cl. 3.6.2";
    private static final String CLAUSE_SCALAR_UNIT = "TOSCA Simple Profile YAML 1.3 cl. 3.3.6";
    private static final String SCALAR_UNIT_SIZE = "scalar-unit.size";

    private final TypeHierarchy hierarchy;
    private final Findings findings;

    public ConstraintChecker(TypeHierarchy hierarchy, Findings findings) {
        this.hierarchy = hierarchy;
        this.findings = findings;
    }

    /**
     * @param declaredType the type the declaration names, which may be a vendor type
     * @param merged       the properties after {@link TypeDefaults} has been applied
     */
    public void check(String declaredType, Map<String, Object> merged, SourceRef source) {
        Map<String, PropertyDef> declared = hierarchy.effectivePropertiesOfAnyType(declaredType);
        for (Map.Entry<String, PropertyDef> e : declared.entrySet()) {
            PropertyDef def = e.getValue();
            Object assigned = merged.get(e.getKey());

            if (assigned == null) {
                if (def.isRequired()) {
                    findings.error("TOSCA02", CLAUSE_REQUIRED,
                            "Required property " + e.getKey() + " is missing on a node of type "
                                    + declaredType,
                            ref(source));
                }
                continue;
            }

            PropertyValue<Object> parsed = PropertyValueParser.parse(assigned);
            if (!parsed.isResolved()) {
                continue;
            }
            Object candidate = parsed.resolved().orElse(null);
            checkScalarUnit(e.getKey(), def, candidate, source);
            checkAll(e.getKey(), def.constraints(), candidate, source);
            def.entrySchema().ifPresent(entry -> {
                if (candidate instanceof List) {
                    for (Object element : (List<?>) candidate) {
                        checkAll(e.getKey(), entry.constraints(), element, source);
                    }
                }
            });
        }
    }

    /**
     * TOSCA 1.3 clause 3.3.6 spells a scalar-unit as {@code <scalar> <unit>}, with the space.
     *
     * <p>A package writing {@code 128MB} is still readable, and rejecting it would be worse than
     * saying so - all three bundled packages write it that way - but it is not conformant, and a
     * consumer comparing descriptors from different vendors should know.
     */
    private void checkScalarUnit(String name, PropertyDef def, Object candidate, SourceRef source) {
        if (!SCALAR_UNIT_SIZE.equals(def.type()) || !(candidate instanceof String)) {
            return;
        }
        ScalarUnitParser.parse((String) candidate)
                .filter(q -> !q.hasCanonicalSpacing())
                .ifPresent(q -> findings.warn("TOSCA01", CLAUSE_SCALAR_UNIT,
                        "Property " + name + " writes " + q.originalText()
                                + " without a space between the value and the unit",
                        ref(source)));
    }

    private void checkAll(String name, List<Constraint> constraints, Object candidate,
            SourceRef source) {
        for (Constraint constraint : constraints) {
            Optional<String> violation = constraint.validate(candidate);
            violation.ifPresent(message -> findings.warn("TOSCA03", CLAUSE_CONSTRAINTS,
                    "Property " + name + " violates constraint " + message, ref(source)));
        }
    }

    private static com.example.etsi.vnfd.validation.SourceRef ref(SourceRef source) {
        return source == null ? null : source.toFindingRef();
    }
}
