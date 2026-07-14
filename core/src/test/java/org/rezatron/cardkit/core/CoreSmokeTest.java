package org.rezatron.cardkit.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Reactor smoke test for the {@code cardkit-core} module. It asserts nothing about the domain
 * (there is none yet) - it exists so the fast test lane exercises this module and fails loudly if
 * the module ever stops compiling or its test wiring breaks.
 */
class CoreSmokeTest {

    @Test
    void reactor_coreModule_isWiredForTests() {
        assertThat(getClass().getPackageName()).isEqualTo("org.rezatron.cardkit.core");
    }
}
