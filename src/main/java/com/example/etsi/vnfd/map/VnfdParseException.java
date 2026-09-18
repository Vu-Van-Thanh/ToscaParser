package com.example.etsi.vnfd.map;

/**
 * The package could not be read at all.
 *
 * <p>Reserved for structural failure: a broken archive, malformed YAML, a missing
 * {@code Entry-Definitions}. A descriptor that parses but breaks a rule of SOL001 or IFA011 does
 * not throw - the result carries the VNFD and the caller decides what to do about the violation.
 */
public class VnfdParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VnfdParseException(String message) {
        super(message);
    }

    public VnfdParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
