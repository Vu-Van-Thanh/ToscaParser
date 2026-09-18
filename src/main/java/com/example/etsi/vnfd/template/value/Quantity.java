package com.example.etsi.vnfd.template.value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A TOSCA {@code scalar-unit.size} value.
 *
 * <p>Both the original text and a byte-normalised form are retained on purpose. IFA011 V5.4.1
 * describes the corresponding attributes only as "Number (e.g. in MB)" without fixing a unit, while
 * TOSCA lets the descriptor pick any of B/kB/KiB/MB/MiB/GB/GiB/TB/TiB. Collapsing to a bare number
 * would throw away which unit the VNF provider actually wrote.
 */
public final class Quantity {

    private final String originalText;
    private final BigDecimal magnitude;
    private final SizeUnit unit;
    private final boolean canonicalSpacing;

    Quantity(String originalText, BigDecimal magnitude, SizeUnit unit, boolean canonicalSpacing) {
        this.originalText = originalText;
        this.magnitude = magnitude;
        this.unit = unit;
        this.canonicalSpacing = canonicalSpacing;
    }

    /** The value exactly as written in the descriptor, e.g. {@code "128 MiB"}. */
    public String originalText() {
        return originalText;
    }

    /** The numeric part, e.g. {@code 128}. */
    public BigDecimal magnitude() {
        return magnitude;
    }

    /** The unit as written, e.g. {@link SizeUnit#MIB}. */
    public SizeUnit unit() {
        return unit;
    }

    /** The value in bytes, for comparisons and arithmetic. */
    public long normalizedBytes() {
        return magnitude.multiply(new BigDecimal(unit.multiplier())).longValueExact();
    }

    /**
     * False when the descriptor omitted the space between number and unit (e.g. {@code "128MB"}).
     * TOSCA Simple Profile YAML 1.3 writes the grammar as {@code <scalar> <unit>}; the parser is
     * lenient about it but the caller should raise a finding.
     */
    public boolean hasCanonicalSpacing() {
        return canonicalSpacing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quantity)) {
            return false;
        }
        Quantity other = (Quantity) o;
        return unit == other.unit && magnitude.compareTo(other.magnitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(magnitude.stripTrailingZeros(), unit);
    }

    @Override
    public String toString() {
        return originalText;
    }
}
