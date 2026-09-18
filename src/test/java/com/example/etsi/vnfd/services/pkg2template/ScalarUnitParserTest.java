package com.example.etsi.vnfd.services.pkg2template;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.template.value.Quantity;
import com.example.etsi.vnfd.template.value.SizeUnit;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScalarUnitParserTest {

    @ParameterizedTest(name = "{0} -> {1} bytes")
    @CsvSource({
            "1 B,            1",
            "64 MiB,         67108864",
            "128 MiB,        134217728",
            "256 MiB,        268435456",
            "10 GB,          10000000000",
            "10 GiB,         10737418240",
            "1 kB,           1000",
            "1 KiB,          1024",
    })
    @DisplayName("parses TOSCA scalar-unit.size and normalises to bytes")
    void parsesCanonicalLiterals(String text, long expectedBytes) {
        Optional<Quantity> q = TemplateReader.parseScalarUnit(text);
        assertThat(q).isPresent();
        assertThat(q.get().normalizedBytes()).isEqualTo(expectedBytes);
        assertThat(q.get().hasCanonicalSpacing()).isTrue();
        assertThat(q.get().originalText()).isEqualTo(text.trim());
    }

    @Test
    @DisplayName("MB and MiB are not the same size")
    void decimalAndBinaryUnitsDiffer() {
        long mb = TemplateReader.parseScalarUnit("128 MB").orElseThrow(AssertionError::new).normalizedBytes();
        long mib = TemplateReader.parseScalarUnit("128 MiB").orElseThrow(AssertionError::new).normalizedBytes();
        assertThat(mb).isEqualTo(128_000_000L);
        assertThat(mib).isEqualTo(134_217_728L);
        assertThat(mib).isNotEqualTo(mb);
    }

    @Test
    @DisplayName("accepts the compact form but flags it: the three bundled fixtures write \"128MB\"")
    void acceptsMissingSpaceButFlagsIt() {
        Optional<Quantity> q = TemplateReader.parseScalarUnit("128MB");
        assertThat(q).isPresent();
        assertThat(q.get().normalizedBytes()).isEqualTo(128_000_000L);
        assertThat(q.get().hasCanonicalSpacing())
                .as("TOSCA 1.3 grammar is <scalar> <unit>; caller should raise a finding")
                .isFalse();
    }

    @ParameterizedTest
    @CsvSource({"128", "MiB", "128 XB", "''", "12 8 MiB"})
    @DisplayName("rejects values that are not size literals")
    void rejectsNonSizeLiterals(String text) {
        assertThat(TemplateReader.parseScalarUnit(text)).isEmpty();
    }

    @Test
    void rejectsNull() {
        assertThat(TemplateReader.parseScalarUnit(null)).isEmpty();
    }
}
