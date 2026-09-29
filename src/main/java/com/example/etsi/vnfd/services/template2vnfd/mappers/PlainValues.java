package com.example.etsi.vnfd.services.template2vnfd.mappers;

import com.example.etsi.vnfd.services.template2vnfd.ToscaBindModule;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Map;

/**
 * A bound SOL001 datatype as a plain map, property values intact.
 */
final class PlainValues {

    /**
     * A mapper that writes a {@code PropertyValue} as what the descriptor wrote.
     *
     * <p>Several IFA011 attributes are typed as an opaque map - {@code VirtualStorageDesc.storageData},
     * {@code VirtualLinkProfile.maxBitrateRequirements} - and the bound SOL001 datatype behind them
     * holds {@code PropertyValue} fields. Converting through the binding mapper would serialise each
     * one as a bean, {@code {"resolved":true,"deferred":false}}, losing the value outright. Writing
     * {@code raw()} keeps both cases usable: a literal stays the number or string that was written,
     * an unresolved function stays the function.
     */
    private static final ObjectMapper PLAIN = plainMapper();

    private PlainValues() {
    }

    /** A bound SOL001 datatype as a plain map, property values intact. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object value) {
        return value == null ? null : PLAIN.convertValue(value, Map.class);
    }

    private static ObjectMapper plainMapper() {
        SimpleModule module = new SimpleModule("plain-property-values");
        module.addSerializer(PropertyValue.class, new JsonSerializer<PropertyValue>() {
            @Override
            public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                provider.defaultSerializeValue(value.raw(), gen);
            }
        });
        return ToscaBindModule.mapper().copy().registerModule(module);
    }
}
