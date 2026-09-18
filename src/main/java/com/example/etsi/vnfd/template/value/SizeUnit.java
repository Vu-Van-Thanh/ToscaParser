package com.example.etsi.vnfd.template.value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Units valid for TOSCA {@code scalar-unit.size}, per TOSCA Simple Profile YAML 1.3.
 *
 * <p>Decimal units are powers of 1000, binary units powers of 1024. The distinction matters:
 * {@code 128 MB} and {@code 128 MiB} differ by about 7%, which is enough to change a capacity
 * decision.
 */
public enum SizeUnit {

    B("B", 1L),
    KB("kB", 1000L),
    KIB("KiB", 1024L),
    MB("MB", 1000L * 1000L),
    MIB("MiB", 1024L * 1024L),
    GB("GB", 1000L * 1000L * 1000L),
    GIB("GiB", 1024L * 1024L * 1024L),
    TB("TB", 1000L * 1000L * 1000L * 1000L),
    TIB("TiB", 1024L * 1024L * 1024L * 1024L);

    private static final Map<String, SizeUnit> BY_SYMBOL;

    static {
        Map<String, SizeUnit> bySymbol = new LinkedHashMap<>();
        for (SizeUnit u : values()) {
            bySymbol.put(u.symbol.toLowerCase(), u);
        }
        BY_SYMBOL = Collections.unmodifiableMap(bySymbol);
    }

    private final String symbol;
    private final long multiplier;

    SizeUnit(String symbol, long multiplier) {
        this.symbol = symbol;
        this.multiplier = multiplier;
    }

    public String symbol() {
        return symbol;
    }

    /** Bytes per one of this unit. */
    public long multiplier() {
        return multiplier;
    }

    /**
     * Looks up a unit symbol. Matching is case-insensitive so that a descriptor writing
     * {@code "128 mib"} still parses, but the canonical casing is kept on the enum.
     */
    public static Optional<SizeUnit> fromSymbol(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_SYMBOL.get(symbol.trim().toLowerCase()));
    }
}
