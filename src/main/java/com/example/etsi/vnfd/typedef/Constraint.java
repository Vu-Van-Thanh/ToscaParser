package com.example.etsi.vnfd.typedef;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * One property constraint from a type definition.
 *
 * <p>These are worth evaluating rather than merely recording. Two of the bundled example packages
 * violate a constraint that SOL001 states in the official type definitions file: every one writes
 * {@code vnfm_info: [ GenericVnfm ]}, which does not match the pattern declared on
 * {@code tosca.nodes.nfv.VNF.vnfm_info}.
 *
 * <p>A violation never stops the parse. The value is mapped as written and the mismatch is
 * reported, because the library describes what a descriptor says, it does not correct it.
 */
public final class Constraint {

    private final ConstraintKind kind;
    private final String rawKey;
    private final Object value;

    Constraint(ConstraintKind kind, String rawKey, Object value) {
        this.kind = kind;
        this.rawKey = rawKey;
        this.value = value;
    }

    public static Constraint of(String key, Object value) {
        ConstraintKind kind = ConstraintKind.fromKey(key).orElse(ConstraintKind.UNKNOWN);
        return new Constraint(kind, key, value);
    }

    public ConstraintKind kind() {
        return kind;
    }

    /** The operator exactly as written; meaningful when the kind is UNKNOWN. */
    public String rawKey() {
        return rawKey;
    }

    public Object value() {
        return value;
    }

    /**
     * Checks a value against this constraint.
     *
     * @return empty when the value satisfies the constraint, otherwise a description of the
     *     mismatch suitable for a finding message. An unknown operator never fails, since failing
     *     on an operator this library does not implement would reject valid descriptors.
     */
    public Optional<String> validate(Object candidate) {
        if (candidate == null) {
            return Optional.empty();
        }
        switch (kind) {
            case EQUAL:
                return equalsValue(candidate, value) ? Optional.empty()
                        : Optional.of("expected " + value + " but was " + candidate);
            case VALID_VALUES:
                return validateValidValues(candidate);
            case PATTERN:
                return validatePattern(candidate);
            case MIN_LENGTH:
                return validateLength(candidate, true);
            case MAX_LENGTH:
                return validateLength(candidate, false);
            case GREATER_THAN:
                return compareNumeric(candidate, c -> c > 0, "greater than");
            case GREATER_OR_EQUAL:
                return compareNumeric(candidate, c -> c >= 0, "greater than or equal to");
            case LESS_THAN:
                return compareNumeric(candidate, c -> c < 0, "less than");
            case LESS_OR_EQUAL:
                return compareNumeric(candidate, c -> c <= 0, "less than or equal to");
            default:
                return Optional.empty();
        }
    }

    private Optional<String> validateValidValues(Object candidate) {
        if (!(value instanceof Collection)) {
            return Optional.empty();
        }
        Collection<?> allowed = (Collection<?>) value;
        if (candidate instanceof Collection) {
            for (Object element : (Collection<?>) candidate) {
                if (allowed.stream().noneMatch(a -> equalsValue(element, a))) {
                    return Optional.of("value " + element + " is not one of " + allowed);
                }
            }
            return Optional.empty();
        }
        return allowed.stream().anyMatch(a -> equalsValue(candidate, a)) ? Optional.empty()
                : Optional.of("value " + candidate + " is not one of " + allowed);
    }

    private Optional<String> validatePattern(Object candidate) {
        if (value == null) {
            return Optional.empty();
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(String.valueOf(value));
        } catch (PatternSyntaxException e) {
            return Optional.empty();
        }
        if (candidate instanceof Collection) {
            for (Object element : (Collection<?>) candidate) {
                if (!pattern.matcher(String.valueOf(element)).matches()) {
                    return Optional.of("value " + element + " does not match pattern " + value);
                }
            }
            return Optional.empty();
        }
        return pattern.matcher(String.valueOf(candidate)).matches() ? Optional.empty()
                : Optional.of("value " + candidate + " does not match pattern " + value);
    }

    private Optional<String> validateLength(Object candidate, boolean minimum) {
        Optional<BigDecimal> bound = toNumber(value);
        if (!bound.isPresent()) {
            return Optional.empty();
        }
        int actual;
        if (candidate instanceof Collection) {
            actual = ((Collection<?>) candidate).size();
        } else if (candidate instanceof CharSequence) {
            actual = ((CharSequence) candidate).length();
        } else {
            return Optional.empty();
        }
        int limit = bound.get().intValue();
        if (minimum && actual < limit) {
            return Optional.of("length " + actual + " is below the minimum " + limit);
        }
        if (!minimum && actual > limit) {
            return Optional.of("length " + actual + " exceeds the maximum " + limit);
        }
        return Optional.empty();
    }

    private Optional<String> compareNumeric(Object candidate,
                                            java.util.function.IntPredicate accept,
                                            String description) {
        Optional<BigDecimal> left = toNumber(candidate);
        Optional<BigDecimal> right = toNumber(value);
        if (!left.isPresent() || !right.isPresent()) {
            return Optional.empty();
        }
        return accept.test(left.get().compareTo(right.get())) ? Optional.empty()
                : Optional.of("value " + candidate + " is not " + description + " " + value);
    }

    private static Optional<BigDecimal> toNumber(Object o) {
        if (o instanceof Number) {
            return Optional.of(new BigDecimal(o.toString()));
        }
        if (o instanceof CharSequence) {
            try {
                return Optional.of(new BigDecimal(o.toString().trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean equalsValue(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        Optional<BigDecimal> na = toNumber(a);
        Optional<BigDecimal> nb = toNumber(b);
        if (na.isPresent() && nb.isPresent()) {
            return na.get().compareTo(nb.get()) == 0;
        }
        return String.valueOf(a).equals(String.valueOf(b));
    }

    /** A list form of a single-value constraint, for callers building messages. */
    public List<Object> asList() {
        return value instanceof List ? (List<Object>) value : java.util.Collections.singletonList(value);
    }

    @Override
    public String toString() {
        return rawKey + ": " + value;
    }
}
