package com.example.etsi.vnfd.csar;

/**
 * Thrown when a path inside a VNF package tries to escape the package root.
 *
 * <p>A descriptor controls artifact paths, and those paths are relative
 * ({@code file: ../Artifacts/Charts/chart.tgz}). A malicious or broken package can write
 * {@code ../../../etc/passwd}. Resolution refuses rather than reading outside the archive.
 */
public class CsarSecurityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CsarSecurityException(String message) {
        super(message);
    }
}
