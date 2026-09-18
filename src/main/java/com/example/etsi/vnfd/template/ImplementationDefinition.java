package com.example.etsi.vnfd.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@code implementation} of an interface operation or notification.
 *
 * <p>Decisive for the VNFD: an operation that declares an implementation becomes a
 * {@code LifeCycleManagementScript} (IFA011 V5.4.1 clause 7.1.13.2, whose {@code script} attribute
 * is mandatory), whereas an operation that only declares {@code inputs} does not. All three bundled
 * example packages declare {@code inputs} without an implementation, and so produce no lifecycle
 * script at all.
 *
 * <p>May be written as a bare artifact or file name, or as a map with {@code primary},
 * {@code dependencies}, {@code timeout} and {@code operation_host}.
 */
public final class ImplementationDefinition {

    private String primary;
    private String resolvedPrimary;
    private final List<String> dependencies = new ArrayList<>();
    private Integer timeout;
    private String operationHost;

    public ImplementationDefinition() {
    }

    /** The artifact or file name as written. */
    public String primary() {
        return primary;
    }

    /** {@link #primary()} resolved against the package root, when it names a file in the package. */
    public Optional<String> resolvedPrimary() {
        return Optional.ofNullable(resolvedPrimary);
    }

    public List<String> dependencies() {
        return dependencies;
    }

    public Optional<Integer> timeout() {
        return Optional.ofNullable(timeout);
    }

    public Optional<String> operationHost() {
        return Optional.ofNullable(operationHost);
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public void setResolvedPrimary(String resolvedPrimary) {
        this.resolvedPrimary = resolvedPrimary;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setOperationHost(String operationHost) {
        this.operationHost = operationHost;
    }

    @Override
    public String toString() {
        return primary;
    }
}
