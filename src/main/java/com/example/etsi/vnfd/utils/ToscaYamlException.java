package com.example.etsi.vnfd.utils;



/** Thrown when a file in the package is not usable YAML at all. */
public class ToscaYamlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ToscaYamlException(String message) {
        super(message);
    }

    public ToscaYamlException(String message, Throwable cause) {
        super(message, cause);
    }
}
