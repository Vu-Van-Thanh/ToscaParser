package com.example.etsi.vnfd.validation;

import java.util.Objects;
import java.util.Optional;

/**
 * Where in the VNF package something came from.
 *
 * <p>Attached to every finding and carried on model elements so a parse result can always be traced
 * back to the line of descriptor that produced it.
 */
public final class SourceRef {

    private final String file;
    private final String element;

    private SourceRef(String file, String element) {
        this.file = file;
        this.element = element;
    }

    /** Refers to a whole file, e.g. {@code TOSCA-Metadata/TOSCA.meta}. */
    public static SourceRef ofFile(String file) {
        return new SourceRef(file, null);
    }

    /** Refers to a named element inside a file, e.g. the node template {@code web_mciop}. */
    public static SourceRef of(String file, String element) {
        return new SourceRef(file, element);
    }

    public String file() {
        return file;
    }

    /** Node template, policy, artifact or property name, when the finding is that specific. */
    public Optional<String> element() {
        return Optional.ofNullable(element);
    }

    /** A copy pointing at a named element of the same file. */
    public SourceRef withElement(String newElement) {
        return new SourceRef(file, newElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SourceRef)) {
            return false;
        }
        SourceRef other = (SourceRef) o;
        return Objects.equals(file, other.file) && Objects.equals(element, other.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, element);
    }

    @Override
    public String toString() {
        return element == null ? file : file + "#" + element;
    }
}
