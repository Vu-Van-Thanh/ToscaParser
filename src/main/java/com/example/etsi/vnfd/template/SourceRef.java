package com.example.etsi.vnfd.template;

/**
 * Where inside the package a TOSCA element was declared.
 *
 * <p>Distinct from {@link com.example.etsi.vnfd.validation.SourceRef}, which is the reporting-side
 * type; this one travels with the DOM so any layer can say which file and element it is looking at.
 */
public final class SourceRef {

    private final String file;
    private final String element;

    private SourceRef(String file, String element) {
        this.file = file;
        this.element = element;
    }

    public static SourceRef of(String file, String element) {
        return new SourceRef(file, element);
    }

    public static SourceRef ofFile(String file) {
        return new SourceRef(file, null);
    }

    /** Package-internal path of the service template file. */
    public String file() {
        return file;
    }

    /** Name of the element within that file, when applicable. */
    public String element() {
        return element;
    }

    /** Converts to the reporting-side reference used by findings. */
    public com.example.etsi.vnfd.validation.SourceRef toFindingRef() {
        return element == null
                ? com.example.etsi.vnfd.validation.SourceRef.ofFile(file)
                : com.example.etsi.vnfd.validation.SourceRef.of(file, element);
    }

    @Override
    public String toString() {
        return element == null ? file : file + "#" + element;
    }
}
