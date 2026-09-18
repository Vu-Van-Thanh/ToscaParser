package com.example.etsi.vnfd.toscatype.bind;

import com.example.etsi.vnfd.template.value.PropertyValue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * The Jackson configuration used to bind TOSCA declarations onto the SOL001 classes.
 *
 * <p>Kept here rather than annotated onto the classes so the model stays free of binding concerns,
 * and so there is one place that says how a descriptor is read.
 */
public final class ToscaBindModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public ToscaBindModule() {
        super("etsi-tosca-bind");
        addDeserializer(PropertyValue.class, new PropertyValueDeserializer());
    }


    /**
     * A mapper configured for descriptor binding.
     *
     * <p>Unknown properties are ignored on purpose: a descriptor may carry vendor keynames, and
     * TOSCA 1.3 has keynames SOL001 never uses. Failing on them would reject valid packages; what
     * matters instead is that every property the ETSI type declares is read, which
     * {@code ConstraintChecker} verifies from the type definitions.
     */
    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ToscaBindModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper;
    }
}
