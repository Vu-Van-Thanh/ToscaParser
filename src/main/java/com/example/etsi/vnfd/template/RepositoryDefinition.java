package com.example.etsi.vnfd.template;

import java.util.Optional;

/**
 * A {@code repositories} entry: a named external location artifacts may be fetched from.
 *
 * <p>TOSCA 1.3 keyname. SOL001 V5.4.1 clauses 6.11.2 and 6.11.3 do not use it, and this library
 * never fetches anything from outside the package, so a repository is recorded but not followed.
 * An artifact that names one is reported rather than silently treated as package-local.
 */
public final class RepositoryDefinition {

    private final String name;
    private String url;
    private String description;

    public RepositoryDefinition(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String url() {
        return url;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " -> " + url;
    }
}
