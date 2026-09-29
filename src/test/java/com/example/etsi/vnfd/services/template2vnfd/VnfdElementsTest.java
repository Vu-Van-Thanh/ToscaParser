package com.example.etsi.vnfd.services.template2vnfd;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.etsi.vnfd.model.Vdu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The one contract no package can demonstrate.
 *
 * <p>C28 asks about the VNFD-level VDU, so when two flavours declare the same identifier the answer
 * has to be the first declaration - and {@code FlavourProcessor} reads it back out of the pool on
 * that assumption. No bundled package reaches C28 (see {@code testdata-negative/README.md}), and
 * MultiFlavourTest pins the element that lands in the VNFD rather than the one handed to
 * SpecRuleValidator, so this is the only place the rule itself is stated.
 */
class VnfdElementsTest {

    @Test
    @DisplayName("The first declaration of an identifier is the one the VNFD keeps")
    void firstDeclarationWins() {
        VnfdElements pool = new VnfdElements();
        Vdu first = Vdu.builder("SharedVdu").build();
        Vdu second = Vdu.builder("SharedVdu").build();

        pool.addVdu("SharedVdu", first);
        pool.addVdu("SharedVdu", second);

        assertThat(pool.vdu("SharedVdu")).isSameAs(first);
    }
}
