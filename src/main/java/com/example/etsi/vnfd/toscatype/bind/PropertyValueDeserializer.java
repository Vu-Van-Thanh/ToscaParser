package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.template.value.Literal;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.services.pkg2template.TemplateReader;
import com.example.etsi.vnfd.template.value.Quantity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Binds a property value that may be a literal or a TOSCA function.
 *
 * <p>SOL001 V5.4.1 clause 5.9 permits {@code get_input}, {@code get_property},
 * {@code get_attribute}, {@code get_artifact} and the intrinsic functions anywhere a value is
 * expected. A field typed {@code String} would make Jackson fail on
 * {@code name: { get_input: vduName }}; a field typed {@code PropertyValue<String>} keeps the
 * expression and its resolution state instead.
 *
 * <p>Contextual because the target type of the value is the type argument of the field:
 * {@code PropertyValue<Integer>} must come back holding an {@code Integer}, not whatever YAML
 * produced. {@code Quantity} is the one target that is derived rather than cast - it is the parsed
 * form of a {@code scalar-unit.size}.
 */
public final class PropertyValueDeserializer extends JsonDeserializer<PropertyValue<?>>
        implements ContextualDeserializer {

    private final JavaType valueType;

    public PropertyValueDeserializer() {
        this(null);
    }

    private PropertyValueDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType wrapper = property != null ? property.getType() : context.getContextualType();
        JavaType argument = wrapper != null && wrapper.containedTypeCount() > 0
                ? wrapper.containedType(0)
                : null;
        return new PropertyValueDeserializer(argument);
    }

    @Override
    public PropertyValue<?> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        Object raw = parser.readValueAs(Object.class);
        PropertyValue<Object> parsed = TemplateReader.parsePropertyValue(raw);
        if (!parsed.isResolved()) {
            // An expression still to be evaluated: nothing to convert, and converting would either
            // invent a value or throw away the expression the caller needs later.
            return parsed;
        }
        return Literal.of(convert(parsed.resolved().orElse(null), context), raw);
    }

    private Object convert(Object value, DeserializationContext context) {
        if (value == null || valueType == null) {
            return value;
        }
        Class<?> target = valueType.getRawClass();
        if (target == Quantity.class) {
            // TOSCA 1.3 clause 3.3.6: "<scalar> <unit>". A non-conformant spelling is still read,
            // with a finding, rather than failing the parse.
            return value instanceof String
                    ? TemplateReader.parseScalarUnit((String) value).orElse(null)
                    : null;
        }
        if (target.isInstance(value)) {
            return value;
        }
        if (target == String.class) {
            return String.valueOf(value);
        }
        if (target == Integer.class && value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (target == Long.class && value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (target == Boolean.class) {
            return Boolean.valueOf(String.valueOf(value));
        }
        if (target == BigDecimal.class && value instanceof Number) {
            return new BigDecimal(String.valueOf(value));
        }
        return context.getConfig().getTypeFactory() == null
                ? value
                : convertLoosely(value, target);
    }

    private Object convertLoosely(Object value, Class<?> target) {
        try {
            return target.cast(value);
        } catch (ClassCastException e) {
            return value;
        }
    }

    /** Whether a value survived as a literal - used by callers that need the plain value. */
    public static Optional<Object> literalOf(PropertyValue<?> value) {
        return value == null ? Optional.empty() : Optional.ofNullable(value.resolved().orElse(null));
    }
}
