package com.example.etsi.vnfd.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mutable collector for findings produced while parsing.
 *
 * <p>Passed down through the parse rather than returned from every method, because a single parse
 * produces findings at every layer - package, YAML, type resolution, mapping - and threading a
 * result wrapper through all of them would obscure the actual logic.
 */
public final class Findings {

    private final List<Finding> findings = new ArrayList<>();

    public void add(Finding finding) {
        findings.add(finding);
    }

    public void error(String ruleId, String clause, String message, SourceRef source) {
        add(Finding.error(ruleId, clause, message, source));
    }

    public void warn(String ruleId, String clause, String message, SourceRef source) {
        add(Finding.warn(ruleId, clause, message, source));
    }

    public void info(String ruleId, String clause, String message, SourceRef source) {
        add(Finding.info(ruleId, clause, message, source));
    }

    /** Snapshot in the order findings were reported. */
    public List<Finding> asList() {
        return Collections.unmodifiableList(new ArrayList<>(findings));
    }

    public List<Finding> withSeverity(Severity severity) {
        return findings.stream()
                .filter(f -> f.severity() == severity)
                .collect(Collectors.toList());
    }

    public boolean hasErrors() {
        return findings.stream().anyMatch(f -> f.severity() == Severity.ERROR);
    }

    public boolean isEmpty() {
        return findings.isEmpty();
    }

    public int size() {
        return findings.size();
    }

    @Override
    public String toString() {
        return findings.stream().map(Finding::toString).collect(Collectors.joining("\n"));
    }
}
