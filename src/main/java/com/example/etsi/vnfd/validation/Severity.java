package com.example.etsi.vnfd.validation;

/**
 * How serious a finding is.
 *
 * <p>Severity is reported, never acted on: this library does not decide whether a package is
 * acceptable. The host application maps severities onto its own policy, so a deployment that must
 * accept a legacy package can downgrade a rule instead of patching the parser.
 */
public enum Severity {

    /** The descriptor violates a "shall" requirement, or is internally inconsistent. */
    ERROR,

    /** Conformance issue that does not prevent building a usable VNFD. */
    WARN,

    /**
     * Something the caller should know about the parse itself: a value was synthesised, an
     * assumption was applied, or a spec deviation was detected and accommodated.
     */
    INFO
}
