package com.example.etsi.vnfd.demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds the VNF packages bundled with the parser repository.
 *
 * <p>Two roots, kept apart because they mean different things: the packages under
 * {@code docs/etsi-context/testdata/} are meant to parse cleanly, while those under
 * {@code src/test/resources/negative/} each break exactly one rule on purpose. A caller comparing
 * output across packages needs to know which is which before reading the findings.
 *
 * <p>A directory counts as a package when it contains {@code TOSCA-Metadata/TOSCA.meta}. That is
 * the same thing the library insists on, so this listing cannot offer something the parser would
 * then refuse for a structural reason.
 */
public final class PackageCatalog {

    private static final String POSITIVE_ROOT = "docs/etsi-context/testdata";
    private static final String NEGATIVE_ROOT = "src/test/resources/negative";
    private static final String MARKER = "TOSCA-Metadata/TOSCA.meta";

    private final Path repoRoot;

    public PackageCatalog(Path repoRoot) {
        this.repoRoot = repoRoot;
    }

    /** Walks up from the working directory until the fixture root appears. */
    public static PackageCatalog discover() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve(POSITIVE_ROOT))) {
                return new PackageCatalog(current);
            }
            current = current.getParent();
        }
        throw new IllegalStateException(
                "Cannot locate the parser repository from " + Paths.get("").toAbsolutePath()
                        + " - expected to find " + POSITIVE_ROOT + " in some parent directory");
    }

    public Path repoRoot() {
        return repoRoot;
    }

    /** Every package found, keyed by directory name, positives first. */
    public Map<String, Entry> all() {
        Map<String, Entry> out = new LinkedHashMap<>();
        scan(repoRoot.resolve(POSITIVE_ROOT), Kind.POSITIVE, out);
        scan(repoRoot.resolve(NEGATIVE_ROOT), Kind.NEGATIVE, out);
        return Collections.unmodifiableMap(out);
    }

    public List<Entry> list() {
        return new ArrayList<>(all().values());
    }

    public Optional<Entry> find(String name) {
        return Optional.ofNullable(all().get(name));
    }

    private void scan(Path root, Kind kind, Map<String, Entry> into) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> children = Files.list(root)) {
            children.filter(Files::isDirectory)
                    .filter(p -> Files.isRegularFile(p.resolve(MARKER)))
                    .sorted()
                    .forEach(p -> into.put(p.getFileName().toString(),
                            new Entry(p.getFileName().toString(), p, kind)));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot list packages under " + root, e);
        }
    }

    /** What a package is expected to demonstrate. */
    public enum Kind {
        /** Conformant, or at worst carrying known warnings. */
        POSITIVE,
        /** Breaks exactly one SHALL, to prove the corresponding rule fires. */
        NEGATIVE
    }

    public static final class Entry {

        private final String name;
        private final Path dir;
        private final Kind kind;

        Entry(String name, Path dir, Kind kind) {
            this.name = name;
            this.dir = dir;
            this.kind = kind;
        }

        public String name() {
            return name;
        }

        public Path dir() {
            return dir;
        }

        public Kind kind() {
            return kind;
        }

        @Override
        public String toString() {
            return name + " (" + kind.name().toLowerCase() + ")";
        }
    }
}
