package com.example.etsi.vnfd.template.value;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses TOSCA {@code scalar-unit.size} literals such as {@code "128 MiB"}.
 *
 * <p>TOSCA Simple Profile YAML 1.3 writes the grammar as {@code <scalar> <unit>}, i.e. with
 * whitespace. Descriptors in the wild routinely omit it, so the parser accepts both and records
 * which form was used via {@link Quantity#hasCanonicalSpacing()}. Rejecting the compact form
 * outright would fail real packages over a cosmetic detail; accepting it silently would hide a
 * conformance problem, so the information is surfaced instead.
 */
public final class ScalarUnitParser {

    /** Number, optional whitespace, unit. Group 1 = magnitude, group 2 = whitespace, group 3 = unit. */
    private static final Pattern SCALAR_UNIT =
            Pattern.compile("^\\s*([0-9]+(?:\\.[0-9]+)?)(\\s*)([A-Za-z]+)\\s*$");

    private ScalarUnitParser() {
    }

    /**
     * Parses a size literal.
     *
     * @return the parsed quantity, or empty when the text is not a size literal at all (unknown
     *         unit, missing number, wrong shape). The caller decides whether that is an error.
     */
    public static Optional<Quantity> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher m = SCALAR_UNIT.matcher(text);
        if (!m.matches()) {
            return Optional.empty();
        }
        Optional<SizeUnit> unit = SizeUnit.fromSymbol(m.group(3));
        if (!unit.isPresent()) {
            return Optional.empty();
        }
        BigDecimal magnitude = new BigDecimal(m.group(1));
        boolean canonicalSpacing = !m.group(2).isEmpty();
        return Optional.of(new Quantity(text.trim(), magnitude, unit.get(), canonicalSpacing));
    }

    /**
     * True when the text looks like it was meant to be a size literal, i.e. a number immediately
     * followed by letters. Used to tell "this property was left as a plain number" apart from
     * "this property has a malformed unit", which deserve different findings.
     */
    public static boolean looksLikeSizeLiteral(String text) {
        return text != null && SCALAR_UNIT.matcher(text).matches();
    }
}
