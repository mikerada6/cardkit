package org.rezatron.cardkit.core.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit rules that enforce the CardKit Randomness Usage Rule for the {@code core} module.
 *
 * <h2>Rules enforced (main sources only)</h2>
 * <ul>
 *   <li>No class may reference {@code java.util.Random} — use an injected
 *       {@code RandomSource} instead.</li>
 *   <li>No class may call {@code Math.random()} — use an injected
 *       {@code RandomSource} instead.</li>
 *   <li>No class may call {@code java.util.Collections.shuffle} — the
 *       Fisher–Yates shuffle in {@code Deck}/{@code Shoe} must use the
 *       injected {@code RandomSource}.</li>
 * </ul>
 *
 * <p>See {@code docs/conventions.md §Randomness Usage Rule} and
 * <a href="../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>.</p>
 */
@DisplayName("Core module — Randomness convention (ArchUnit)")
class RandomnessConventionTest {

    private static JavaClasses coreMainClasses;

    @BeforeAll
    static void importClasses() {
        // Only scan production (main) classes — test-only types such as SeededRandomSource
        // are permitted to use SplittableRandom and similar constructs.
        coreMainClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.rezatron.cardkit.core");
    }

    @Test
    @DisplayName("noMainClass_usesJavaUtilRandom")
    void noMainClass_usesJavaUtilRandom() {
        ArchRule rule = noClasses()
                .should().accessClassesThat().haveFullyQualifiedName("java.util.Random")
                .because(
                        "all randomness must go through the injected RandomSource seam; "
                        + "use SecureRandomSource in production (see docs/conventions.md §Randomness Usage Rule)");
        rule.allowEmptyShould(true).check(coreMainClasses);
    }

    @Test
    @DisplayName("noMainClass_callsMathRandom")
    void noMainClass_callsMathRandom() {
        ArchRule rule = noClasses()
                .should().callMethod(Math.class, "random")
                .because(
                        "Math.random() delegates to a shared java.util.Random; "
                        + "use the injected RandomSource instead (see docs/conventions.md §Randomness Usage Rule)");
        rule.allowEmptyShould(true).check(coreMainClasses);
    }

    @Test
    @DisplayName("noMainClass_usesCollectionsShuffle")
    void noMainClass_usesCollectionsShuffle() {
        ArchRule rule = noClasses()
                .should().callMethodWhere(
                        com.tngtech.archunit.base.DescribedPredicate.describe(
                                "is Collections.shuffle",
                                m -> m.getOwner().getFullName().equals("java.util.Collections")
                                     && m.getName().startsWith("shuffle")))
                .because(
                        "Collections.shuffle uses an internal java.util.Random; "
                        + "implement Fisher-Yates over the injected RandomSource instead "
                        + "(see docs/conventions.md §Randomness Usage Rule)");
        rule.allowEmptyShould(true).check(coreMainClasses);
    }
}

