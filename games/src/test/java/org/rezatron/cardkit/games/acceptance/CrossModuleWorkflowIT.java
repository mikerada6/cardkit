package org.rezatron.cardkit.games.acceptance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Placeholder cross-module acceptance test.
 *
 * <p>This class is intentionally near-empty. It exists to keep the
 * integration lane ({@code ./mvnw verify -P it}) green while the suite is
 * unpopulated. Real workflow tests land here in stories 0009 and 0017.
 *
 * <p>Naming convention: the {@code IT} suffix causes maven-failsafe-plugin to
 * include this class <em>only</em> in the integration lane — it is
 * <strong>never</strong> executed by maven-surefire-plugin in the fast lane.
 */
class CrossModuleWorkflowIT {

    /**
     * Asserts that the integration lane is wired correctly.
     * Replaced by real cross-module acceptance tests in stories 0009 and 0017.
     */
    @Test
    void placeholder_integrationLane_isGreenAndEmpty() {
        // Intentionally empty — populated by stories 0009 and 0017.
        assertThat(true).as("integration lane placeholder").isTrue();
    }
}

