package org.rezatron.cardkit.core.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit rules enforcing the CardKit logging convention for the {@code core} module.
 *
 * <h2>Rules enforced</h2>
 * <ol>
 *   <li>No class in {@code core} may reference a concrete logging implementation
 *       ({@code java.util.logging}, {@code log4j}, {@code logback}) — only the SLF4J
 *       <em>API</em> is permitted.</li>
 *   <li>No class may use {@code System.out} or {@code System.err} as a substitute for
 *       structured logging.</li>
 * </ol>
 */
@DisplayName("Core module — SLF4J logging convention (ArchUnit)")
class LoggingConventionTest {

    private static JavaClasses coreClasses;

    @BeforeAll
    static void importClasses() {
        coreClasses = new ClassFileImporter()
                .importPackages("org.rezatron.cardkit.core");
    }

    @Test
    @DisplayName("noClass_usesLog4jDirectly")
    void noClass_usesLog4jDirectly() {
        ArchRule rule = noClasses()
                .should().accessClassesThat().resideInAPackage("org.apache.log4j..")
                .because("the library must log through the SLF4J facade only (see docs/conventions.md)");
        rule.allowEmptyShould(true).check(coreClasses);
    }

    @Test
    @DisplayName("noClass_usesLog4j2Directly")
    void noClass_usesLog4j2Directly() {
        ArchRule rule = noClasses()
                .should().accessClassesThat().resideInAPackage("org.apache.logging.log4j..")
                .because("the library must log through the SLF4J facade only (see docs/conventions.md)");
        rule.allowEmptyShould(true).check(coreClasses);
    }

    @Test
    @DisplayName("noClass_usesJavaUtilLoggingDirectly")
    void noClass_usesJavaUtilLoggingDirectly() {
        ArchRule rule = noClasses()
                .should().accessClassesThat().resideInAPackage("java.util.logging..")
                .because("the library must log through the SLF4J facade only (see docs/conventions.md)");
        rule.allowEmptyShould(true).check(coreClasses);
    }

    @Test
    @DisplayName("noClass_usesSystemOutOrErr")
    void noClass_usesSystemOutOrErr() {
        ArchRule rule = noClasses()
                .should().accessClassesThat().haveFullyQualifiedName("java.io.PrintStream")
                .because("use SLF4J loggers instead of System.out / System.err");
        rule.allowEmptyShould(true).check(coreClasses);
    }
}

