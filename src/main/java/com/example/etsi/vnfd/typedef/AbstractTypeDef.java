package com.example.etsi.vnfd.typedef;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fields every TOSCA type definition shares.
 *
 * <p>{@code derived_from} is the important one: it is what lets a reader recognise
 * {@code ExampleCorp.SimpleWebCnf.1_0} as a {@code tosca.nodes.nfv.VNF}, and every node
 * classification decision in this library goes through it rather than comparing type names.
 */
public abstract class AbstractTypeDef {

    private final String name;
    private String derivedFrom;
    private String description;
    private String version;
    private final Map<String, Object> metadata = new LinkedHashMap<>();
    private final Map<String, PropertyDef> properties = new LinkedHashMap<>();
    private final Map<String, PropertyDef> attributes = new LinkedHashMap<>();
    private String declaredIn;

    protected AbstractTypeDef(String name) {
        this.name = name;
    }

    /** Fully qualified type name, e.g. {@code tosca.nodes.nfv.Vdu.OsContainerDeployableUnit}. */
    public String name() {
        return name;
    }

    /** Immediate parent type, or empty for a root type. */
    public Optional<String> derivedFrom() {
        return Optional.ofNullable(derivedFrom);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<String> version() {
        return Optional.ofNullable(version);
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    /** Properties declared by this type itself, not counting inherited ones. */
    public Map<String, PropertyDef> properties() {
        return properties;
    }

    public Map<String, PropertyDef> attributes() {
        return attributes;
    }

    /**
     * Which file of the package declared this type.
     * Useful when a package redefines a standard type and the parse needs explaining.
     */
    public Optional<String> declaredIn() {
        return Optional.ofNullable(declaredIn);
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDeclaredIn(String declaredIn) {
        this.declaredIn = declaredIn;
    }

    @Override
    public String toString() {
        return name + (derivedFrom == null ? "" : " extends " + derivedFrom);
    }
}
