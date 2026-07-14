package org.rezatron.cardkit.games.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit scaffold enforcing the allowed module dependency direction and
 * visibility boundaries in the CardKit library.
 *
 * <p>Rules (permissive scaffold — tighten as types land):
 * <ol>
 *   <li><strong>games → core, never the reverse.</strong>
 *       No class in {@code org.rezatron.cardkit.core} may depend on anything
 *       in {@code org.rezatron.cardkit.games}.
 *   <li><strong>.internal non-export.</strong>
 *       No class outside a module's own {@code .internal} sub-tree may import
 *       from another module's {@code .internal} packages.
 * </ol>
 *
 * <p>Runs in the fast lane ({@code ./mvnw verify}) via maven-surefire-plugin.
 * The integration acceptance suite lives in {@code acceptance/*IT.java} and
 * runs only under {@code ./mvnw verify -P it}.
 */
class ModuleDependencyRulesTest {

    /**
     * Import all CardKit classes from both modules.
     * The {@code games} module depends on {@code core} in the Maven reactor, so
     * core bytecode is always on the test classpath here.
     */
    private static JavaClasses allCardKitClasses;

    @BeforeAll
    static void importClasses() {
        allCardKitClasses = new ClassFileImporter()
                .importPackages("org.rezatron.cardkit");
    }

    /**
     * The {@code core} module must never depend on {@code games} — the
     * dependency arrow is one-way: games → core.
     *
     * <p>{@code allowEmptyShould(true)}: the codebase starts near-empty;
     * once domain types land the rule will enforce itself automatically.
     */
    @Test
    void core_mustNotDependOn_games() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("org.rezatron.cardkit.core..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.rezatron.cardkit.games..")
                .allowEmptyShould(true);

        rule.check(allCardKitClasses);
    }

    /**
     * Internal packages of the {@code core} module are private to that module.
     * No class in {@code games} may reference them.
     *
     * <p>{@code allowEmptyShould(true)}: tightened automatically once
     * {@code core.*.internal} types are introduced.
     */
    @Test
    void coreInternal_mustNotBeAccessedFrom_games() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("org.rezatron.cardkit.games..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.rezatron.cardkit.core..internal..")
                .allowEmptyShould(true);

        rule.check(allCardKitClasses);
    }

    /**
     * Internal packages of the {@code games} module are private to that module.
     * No class outside {@code games} (e.g., future consumers) may reference them.
     * Since {@code core} must not depend on {@code games} at all (first rule),
     * this is belt-and-suspenders for any other future peer module.
     *
     * <p>{@code allowEmptyShould(true)}: tightened automatically once
     * {@code games.*.internal} types are introduced.
     */
    @Test
    void gamesInternal_mustNotBeAccessedFrom_core() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("org.rezatron.cardkit.core..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.rezatron.cardkit.games..internal..")
                .allowEmptyShould(true);

        rule.check(allCardKitClasses);
    }
}

