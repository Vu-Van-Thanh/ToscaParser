package com.example.etsi.vnfd.csar;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Normalises and resolves paths inside a VNF package.
 *
 * <p>All package-internal paths are expressed with {@code /} and are relative to the package root,
 * with no leading slash. {@code TOSCA.meta} keys and TOSCA {@code imports} are relative to the file
 * that contains them, not to the root, so resolution always needs the referring file.
 *
 * <p>SOL004 V5.1.1 clause 4.1.2.1 states that artifacts may be "pointed to by relative path names
 * through artifact definitions in one of the TOSCA definitions files"; it does not define the
 * resolution algorithm, so the rules below follow ordinary relative-path semantics.
 * [VERSION MISMATCH] SOL004 V5.1.1.
 */
public final class PathResolver {

    private PathResolver() {
    }

    /**
     * Canonical form of a package-internal path: forward slashes, no leading slash, no {@code .}
     * or {@code ..} segments left.
     *
     * @throws CsarSecurityException if the path climbs above the package root
     */
    public static String normalize(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        String unified = path.replace('\\', '/').trim();
        Deque<String> stack = new ArrayDeque<>();
        for (String segment : unified.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                if (stack.isEmpty()) {
                    throw new CsarSecurityException(
                            "Path escapes the VNF package root: " + path);
                }
                stack.removeLast();
                continue;
            }
            stack.addLast(segment);
        }
        return String.join("/", stack);
    }

    /**
     * Resolves {@code reference} as written inside the file at {@code fromFile}.
     *
     * <p>Example: a {@code HelmChart} artifact in
     * {@code Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml} writing
     * {@code ../Artifacts/Charts/simple-web-cnf-1.0.0.tgz} resolves to
     * {@code Artifacts/Charts/simple-web-cnf-1.0.0.tgz}.
     *
     * <p>An absolute-looking reference ({@code /Artifacts/x}) is treated as root-relative rather
     * than as a filesystem path, since nothing outside the archive is addressable.
     *
     * @param fromFile package-internal path of the file containing the reference
     * @param reference the relative path as written in that file
     * @throws CsarSecurityException if the result escapes the package root
     */
    public static String resolve(String fromFile, String reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        String unified = reference.replace('\\', '/').trim();
        if (unified.startsWith("/")) {
            return normalize(unified);
        }
        String parent = parentOf(fromFile);
        String joined = parent.isEmpty() ? unified : parent + "/" + unified;
        return normalize(joined);
    }

    /** Directory part of a package-internal file path, or empty for a root-level file. */
    public static String parentOf(String filePath) {
        if (filePath == null) {
            return "";
        }
        String unified = filePath.replace('\\', '/').trim();
        int slash = unified.lastIndexOf('/');
        return slash < 0 ? "" : unified.substring(0, slash);
    }

    /** Last path segment, e.g. {@code chart.tgz}. */
    public static String fileNameOf(String filePath) {
        String unified = filePath.replace('\\', '/').trim();
        int slash = unified.lastIndexOf('/');
        return slash < 0 ? unified : unified.substring(slash + 1);
    }
}
