package com.example.etsi.vnfd.fixture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Locates the example VNF packages.
 *
 * <p>Both roots sit under {@code docs/etsi-context/} and are read in place rather than copied under
 * {@code src/test/resources}, so there is exactly one copy of each package and the tests exercise
 * the same bytes a reviewer reads. Since each package ships its own copy of the ETSI type
 * definitions, a copy under the test resources would also be several megabytes re-copied into
 * {@code target/} on every build.
 */
public final class Fixtures {

    public static final String SIMPLE_WEB_CNF = "ExampleCorp_SimpleWebCnf_vnf_pkg";
    public static final String REGULAR_CNF = "ExampleCorp_RegularCnf_vnf_pkg";
    public static final String HYBRID_WEB_CNF = "ExampleCorp_HybridWebCnf_vnf_pkg";

    /** The only bundled package with more than one deployment flavour. */
    public static final String MULTI_DF_CNF = "ExampleCorp_MultiDfCnf_vnf_pkg";

    private static final String CONFORMANT = "docs/etsi-context/testdata";
    private static final String NEGATIVE = "docs/etsi-context/testdata-negative";

    private Fixtures() {
    }

    /** Repository root, found by walking up until {@code docs/etsi-context/testdata} appears. */
    public static Path repoRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve(CONFORMANT))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root from " + Paths.get("").toAbsolutePath());
    }

    /** Directory of one package that is meant to parse cleanly. */
    public static Path packageDir(String packageName) {
        return resolve(CONFORMANT, packageName);
    }

    /** Directory of one package that breaks exactly one rule on purpose. */
    public static Path negativePackageDir(String packageName) {
        return resolve(NEGATIVE, packageName);
    }

    private static Path resolve(String root, String packageName) {
        Path dir = repoRoot().resolve(root).resolve(packageName);
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("No such fixture package: " + dir);
        }
        return dir;
    }

}
