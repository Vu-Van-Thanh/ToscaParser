package com.example.etsi.vnfd.typedef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A property declaration inside a type definition.
 *
 * <p>Supplies the three things reading a property assignment needs: the declared type, the default
 * to fall back on when a template omits the property, and the constraints to check the value
 * against.
 *
 * <p>The default matters more than it looks. SOL001 V5.4.1 Annex A.23 writes a VNF node template
 * that assigns only {@code flavour_description}, leaving {@code descriptor_id}, {@code provider}
 * and the rest to defaults declared on the VNF-specific node type. A reader that looks only at node
 * template properties gets nothing for those.
 */
public final class PropertyDef {

    private final String name;
    private String type;
    private String description;
    private Boolean required;
    private Object defaultValue;
    private String status;
    private final List<Constraint> constraints = new ArrayList<>();
    private PropertyDef entrySchema;
    private PropertyDef keySchema;
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    PropertyDef(String name) {
        this.name = name;
    }

    /**
     * This declaration laid over an inherited one.
     *
     * <p>A derived type refines a property rather than replacing it. SOL001 V5.4.1 Annex A.23
     * writes {@code vnfm_info: [ MyCompanyVnfm ]} on the VNF-specific node type - a bare value that
     * states a default and nothing else. Taking that declaration wholesale would drop the schema
     * {@code tosca.nodes.nfv.VNF} declares for the same property, including the pattern its entries
     * must match, and the check for it would then silently never run.
     *
     * <p>So each facet falls back to the inherited declaration when this one does not state it, and
     * constraints accumulate: a derived type may narrow, never widen.
     */
    PropertyDef refining(PropertyDef inherited) {
        if (inherited == null) {
            return this;
        }
        PropertyDef merged = new PropertyDef(name);
        merged.type = type != null ? type : inherited.type;
        merged.description = description != null ? description : inherited.description;
        merged.required = required != null ? required : inherited.required;
        merged.defaultValue = defaultValue != null ? defaultValue : inherited.defaultValue;
        merged.status = status != null ? status : inherited.status;
        merged.constraints.addAll(inherited.constraints);
        merged.constraints.addAll(constraints);
        merged.entrySchema = entrySchema != null
                ? entrySchema.refining(inherited.entrySchema)
                : inherited.entrySchema;
        merged.keySchema = keySchema != null
                ? keySchema.refining(inherited.keySchema)
                : inherited.keySchema;
        merged.metadata.putAll(inherited.metadata);
        merged.metadata.putAll(metadata);
        return merged;
    }

    public String name() {
        return name;
    }

    /** Declared type, e.g. {@code string}, {@code scalar-unit.size}, {@code tosca.datatypes.nfv.VduProfile}. */
    public String type() {
        return type;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** TOSCA defaults {@code required} to true when the keyname is absent. */
    public boolean isRequired() {
        return required == null || required;
    }

    /** Whether the descriptor stated {@code required} explicitly. */
    public Optional<Boolean> declaredRequired() {
        return Optional.ofNullable(required);
    }

    public Optional<Object> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public Optional<String> status() {
        return Optional.ofNullable(status);
    }

    public List<Constraint> constraints() {
        return constraints;
    }

    /** Element schema of a list or map property. */
    public Optional<PropertyDef> entrySchema() {
        return Optional.ofNullable(entrySchema);
    }

    public Optional<PropertyDef> keySchema() {
        return Optional.ofNullable(keySchema);
    }

    /**
     * Property metadata. SOL001 V5.4.1 clause 5.7.5 uses {@code sensitive: "true"} here to mark a
     * property as holding security-sensitive information.
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /** True when clause 5.7.5 marks this property security-sensitive. */
    public boolean isSensitive() {
        Object sensitive = metadata.get("sensitive");
        return sensitive != null && Boolean.parseBoolean(String.valueOf(sensitive));
    }

    void setType(String type) {
        this.type = type;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setRequired(Boolean required) {
        this.required = required;
    }

    void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    void setStatus(String status) {
        this.status = status;
    }

    void setEntrySchema(PropertyDef entrySchema) {
        this.entrySchema = entrySchema;
    }

    void setKeySchema(PropertyDef keySchema) {
        this.keySchema = keySchema;
    }

    @Override
    public String toString() {
        return name + ": " + type + (isRequired() ? " (required)" : "");
    }
}
