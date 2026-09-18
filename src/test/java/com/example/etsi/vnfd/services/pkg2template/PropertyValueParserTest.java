package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.template.value.FunctionCall;
import com.example.etsi.vnfd.template.value.FunctionName;
import com.example.etsi.vnfd.template.value.Kind;
import com.example.etsi.vnfd.template.value.PropertyValue;
import com.example.etsi.vnfd.template.value.Resolution;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PropertyValueParserTest {

    private static Map<String, Object> singleEntry(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

    @Test
    @DisplayName("plain scalars are literals")
    void scalarIsLiteral() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue("web-vdu");

        assertThat(v.kind()).isEqualTo(Kind.LITERAL);
        assertThat(v.resolution()).isEqualTo(Resolution.RESOLVED_STATIC);
        assertThat(v.resolved()).contains("web-vdu");
        assertThat(v.isResolved()).isTrue();
    }

    @Test
    @DisplayName("get_input is tagged INPUT_BOUND and never evaluated")
    void getInputIsInputBound() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(singleEntry("get_input", "image_tag"));

        assertThat(v.kind()).isEqualTo(Kind.FUNCTION);
        assertThat(v.resolution()).isEqualTo(Resolution.INPUT_BOUND);
        assertThat(v.resolved()).isEmpty();
        assertThat(v.isDeferred()).isTrue();

        FunctionCall<?> call = (FunctionCall<?>) v;
        assertThat(call.name()).isEqualTo(FunctionName.GET_INPUT);
        assertThat(call.args()).hasSize(1);
        assertThat(call.args().get(0).resolved().orElse(null)).isEqualTo("image_tag");
    }

    @Test
    @DisplayName("get_attribute is tagged RUNTIME_BOUND: no VNF instance exists while parsing")
    void getAttributeIsRuntimeBound() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(
                singleEntry("get_attribute", Arrays.asList("SELF", "scale_status")));

        assertThat(v.resolution()).isEqualTo(Resolution.RUNTIME_BOUND);
        assertThat(((FunctionCall<?>) v).args()).hasSize(2);
    }

    @Test
    @DisplayName("get_property args keep their order")
    void getPropertyKeepsArgOrder() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(
                singleEntry("get_property", Arrays.asList("SELF", "vdu_profile", "min_number_of_instances")));

        FunctionCall<?> call = (FunctionCall<?>) v;
        assertThat(call.name()).isEqualTo(FunctionName.GET_PROPERTY);
        assertThat(call.args()).extracting(a -> (Object) a.resolved().orElse(null))
                .containsExactly("SELF", "vdu_profile", "min_number_of_instances");
    }

    @Test
    @DisplayName("a single-key map whose key is not a function stays a literal")
    void singleKeyMapIsNotAutomaticallyAFunction() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(singleEntry("associated_layer_protocol", "ipv4"));

        assertThat(v.kind()).isEqualTo(Kind.LITERAL);
        assertThat(v.resolved()).isPresent();
    }

    @Test
    @DisplayName("a multi-key map is a literal even if one key is a function name")
    void multiKeyMapIsLiteral() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("get_input", "x");
        m.put("something_else", "y");

        assertThat(TemplateReader.parsePropertyValue(m).kind()).isEqualTo(Kind.LITERAL);
    }

    @Test
    @DisplayName("a function nested inside a list is still detected")
    void detectsFunctionNestedInList() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(
                Arrays.asList("literal", singleEntry("get_input", "proto")));

        assertThat(v.kind()).isEqualTo(Kind.LITERAL);
        List<?> items = (List<?>) v.resolved().orElseThrow(AssertionError::new);
        assertThat(items.get(0)).isEqualTo("literal");
        assertThat(items.get(1)).isInstanceOf(FunctionCall.class);
        assertThat(((FunctionCall<?>) items.get(1)).resolution()).isEqualTo(Resolution.INPUT_BOUND);
    }

    @Test
    @DisplayName("nested function inside concat args round-trips")
    void detectsNestedFunctionInConcat() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(
                singleEntry("concat", Arrays.asList("v", singleEntry("get_input", "tag"))));

        FunctionCall<?> concat = (FunctionCall<?>) v;
        assertThat(concat.name()).isEqualTo(FunctionName.CONCAT);
        assertThat(concat.args()).hasSize(2);
        assertThat(concat.args().get(1)).isInstanceOf(FunctionCall.class);
        assertThat(concat.args().get(1).resolution()).isEqualTo(Resolution.INPUT_BOUND);
    }

    @Test
    @DisplayName("get_operation_output parses as UNKNOWN: valid TOSCA, absent from SOL001 Table 5.9-1")
    void nonSol001FunctionIsFlagged() {
        PropertyValue<Object> v = TemplateReader.parsePropertyValue(
                singleEntry("get_operation_output", Arrays.asList("SELF", "Standard", "create", "out")));

        assertThat(v.kind()).isEqualTo(Kind.FUNCTION);
        FunctionCall<?> call = (FunctionCall<?>) v;
        assertThat(call.name()).isEqualTo(FunctionName.UNKNOWN);
        assertThat(call.rawKey()).isEqualTo("get_operation_output");
    }

    @Test
    @DisplayName("the original YAML is always retained")
    void keepsRawValue() {
        Map<String, Object> raw = singleEntry("get_input", "image_tag");
        assertThat(TemplateReader.parsePropertyValue(raw).raw()).isSameAs(raw);
    }
}
