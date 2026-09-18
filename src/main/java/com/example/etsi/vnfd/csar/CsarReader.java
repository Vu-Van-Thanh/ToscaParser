package com.example.etsi.vnfd.csar;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only access to the contents of a VNF package.
 *
 * <p>Four methods, no filesystem assumptions. Nothing above this interface knows where a package
 * lives - every other stage asks for a path and gets bytes - so a host keeping packages in object
 * storage, a database or GridFS implements this and the rest of the library works unchanged, with
 * no extraction to a temporary directory. {@link DirectoryCsarReader} is the one implementation
 * shipped here, not the only one allowed.
 *
 * <p>Paths are package-internal and canonical, as produced by {@link PathResolver#normalize}:
 * forward slashes, no leading slash, e.g. {@code Definitions/main.yaml}. An implementation is
 * responsible for presenting its entries that way, so that a path taken from one package reads the
 * same as a path taken from another.
 *
 * <p>SOL004 V5.1.1 clause 4.1.1 also allows a root-YAML layout with no {@code TOSCA-Metadata}
 * directory. It is not supported: {@link ToscaMeta#parse} refuses a package without
 * {@code TOSCA.meta}. [VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.
 */
public interface CsarReader {

    /** Human-readable identifier used in diagnostics and findings, e.g. the package directory name. */
    String name();

    /** Every file in the package, as canonical package-internal paths. Directories are not listed. */
    Set<String> entries();

    /** Whether a file exists at this canonical path. */
    boolean exists(String path);

    /** File contents, or empty when the path does not exist. */
    Optional<byte[]> read(String path);

    /** Convenience for text files; VNF package descriptors and metadata are UTF-8. */
    default Optional<String> readText(String path) {
        return read(path).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * Opens an extracted package directory.
     *
     * <p>A convenience for the common case, not the only way in: a caller with its own source
     * constructs its own implementation and never touches this method.
     */
    static CsarReader of(Path directory) {
        return new DirectoryCsarReader(directory);
    }
}
