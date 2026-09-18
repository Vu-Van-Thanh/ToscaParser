package com.example.etsi.vnfd.fixture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Locates the bundled example VNF packages.
 *
 * <p>They are read from {@code docs/etsi-context/testdata/} in place rather than copied under
 * {@code src/test/resources}, so there is exactly one copy of each package and the tests exercise
 * the same bytes a reviewer reads.
 */
public final class Fixtures {

    public static final String SIMPLE_WEB_CNF = "ExampleCorp_SimpleWebCnf_vnf_pkg";
    public static final String REGULAR_CNF = "ExampleCorp_RegularCnf_vnf_pkg";
    public static final String HYBRID_WEB_CNF = "ExampleCorp_HybridWebCnf_vnf_pkg";

    private Fixtures() {
    }

    /** Repository root, found by walking up until {@code docs/etsi-context/testdata} appears. */
    public static Path repoRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("docs/etsi-context/testdata"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root from " + Paths.get("").toAbsolutePath());
    }

    /** Directory of one bundled package. */
    public static Path packageDir(String packageName) {
        Path dir = repoRoot().resolve("docs/etsi-context/testdata").resolve(packageName);
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("No such fixture package: " + dir);
        }
        return dir;
    }

}
