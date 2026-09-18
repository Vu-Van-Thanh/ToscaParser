package com.example.etsi.vnfd.csar;

import java.io.IOException;
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
 * <p>The directory is walked once at construction and the result is fixed: a package is onboarded
 * material, not a working directory, so re-reading it per lookup would only buy the ability to
 * observe someone editing it mid-parse.
 */
public final class DirectoryCsarReader implements CsarReader {

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

    @Override
    public String name() {
        return name;
    }

    @Override
    public Set<String> entries() {
        return entries;
    }

    @Override
    public boolean exists(String path) {
        return entries.contains(PathResolver.normalize(path));
    }

    @Override
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

    @Override
    public String toString() {
        return "DirectoryCsarReader(" + name + ", " + entries.size() + " entries)";
    }
}
