package com.example.etsi.vnfd.csar;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A VNF package laid out as a directory tree, i.e. an already-extracted CSAR.
 *
 * <p>Paths are package-internal and canonical, as produced by {@link PathResolver#normalize}:
 * forward slashes, no leading slash, e.g. {@code Definitions/main.yaml}.
 *
 * <p>SOL004 V5.1.1 clause 4.1.1 also allows a zip archive and a root-YAML layout without
 * {@code TOSCA-Metadata}. Neither is supported: an archive is expected to be extracted before it
 * gets here, and {@link ToscaMeta#parse} refuses a package with no {@code TOSCA.meta}.
 * [VERSION MISMATCH] SOL004 has no V5.4.1 release; V5.1.1 is the latest.
 *
 * <p>The directory is walked once at construction and the result is fixed: a package is onboarded
 * material, not a working directory, so re-reading it per lookup would only buy the ability to
 * observe someone editing it mid-parse.
 */
public final class DirectoryCsarReader {

    private final Path root;
    private final String name;
    private final Set<String> entries;

    public DirectoryCsarReader(Path root) {
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not an extracted VNF package directory: " + root);
        }
        this.root = root.toAbsolutePath().normalize();
        this.name = this.root.getFileName() == null
                ? this.root.toString()
                : this.root.getFileName().toString();
        this.entries = Collections.unmodifiableSet(scan(this.root));
    }

    private static Set<String> scan(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> PathResolver.normalize(root.relativize(p).toString()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot scan VNF package directory: " + root, e);
        }
    }

    /** Human-readable identifier used in diagnostics and findings. */
    public String name() {
        return name;
    }

    /** File contents, or empty when the path does not exist. */
    public Optional<byte[]> read(String path) {
        String canonical = PathResolver.normalize(path);
        if (!entries.contains(canonical)) {
            return Optional.empty();
        }
        Path target = root.resolve(canonical).normalize();
        // normalize() already refuses to climb above the root; this is the belt-and-braces check
        // in case a symlink inside the directory points outside it.
        if (!target.startsWith(root)) {
            throw new CsarSecurityException("Entry resolves outside the package root: " + path);
        }
        try {
            return Optional.of(Files.readAllBytes(target));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read package entry: " + canonical, e);
        }
    }

    /** Convenience for text files; VNF package descriptors and metadata are UTF-8. */
    public Optional<String> readText(String path) {
        return read(path).map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "DirectoryCsarReader(" + name + ", " + entries.size() + " entries)";
    }
}
