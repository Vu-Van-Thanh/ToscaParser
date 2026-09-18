package com.example.etsi.vnfd.validation;

import java.util.Objects;
import java.util.Optional;

/**
 * One conformance observation about a VNF package.
 *
 * <p>Every finding carries the specification clause it comes from. Where no clause backs a check,
 * {@link #clause()} states the label instead - {@code [ASSUMPTION]}, {@code [PROJECT-SPECIFIC]},
 * {@code [MANO INTERPRETATION]} or {@code [VERSION MISMATCH]} - so a reader can always tell a
 * normative requirement from a decision this library made.
 */
public final class Finding {

    private final String ruleId;
    private final Severity severity;
    private final String clause;
    private final String message;
    private final SourceRef source;

    private Finding(String ruleId, Severity severity, String clause, String message, SourceRef source) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.clause = Objects.requireNonNull(clause, "clause");
        this.message = Objects.requireNonNull(message, "message");
        this.source = source;
    }

    public static Finding of(String ruleId, Severity severity, String clause, String message,
                             SourceRef source) {
        return new Finding(ruleId, severity, clause, message, source);
    }

    public static Finding error(String ruleId, String clause, String message, SourceRef source) {
        return new Finding(ruleId, Severity.ERROR, clause, message, source);
    }

    public static Finding warn(String ruleId, String clause, String message, SourceRef source) {
        return new Finding(ruleId, Severity.WARN, clause, message, source);
    }

    public static Finding info(String ruleId, String clause, String message, SourceRef source) {
        return new Finding(ruleId, Severity.INFO, clause, message, source);
    }

    /** Stable identifier, e.g. {@code C02}. Used to disable a rule without editing code. */
    public String ruleId() {
        return ruleId;
    }

    public Severity severity() {
        return severity;
    }

    /**
     * The specification clause that justifies this finding, e.g.
     * {@code "IFA011 V5.4.1 cl. 7.1.6.2.2 Note 10"}, or a label when nothing normative backs it.
     */
    public String clause() {
        return clause;
    }

    public String message() {
        return message;
    }

    public Optional<SourceRef> source() {
        return Optional.ofNullable(source);
    }

    @Override
    public String toString() {
        return severity + " " + ruleId + " [" + clause + "] " + message
                + (source == null ? "" : " (" + source + ")");
    }
}
