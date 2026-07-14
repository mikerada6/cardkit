package org.rezatron.cardkit.games;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Reactor smoke test for the {@code cardkit-games} module. Like its core counterpart it asserts
 * nothing about the domain (none yet) - it keeps the fast test lane honest for this module and
 * proves the {@code games -> core} dependency resolves in the reactor.
 */
class GamesSmokeTest {

    @Test
    void reactor_gamesModule_isWiredForTests() {
        assertThat(getClass().getPackageName()).isEqualTo("org.rezatron.cardkit.games");
    }
}
