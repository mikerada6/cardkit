package org.rezatron.cardkit.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Require} — the CardKit null-policy helper.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>{@link Require#nonNull} — rejects null, returns the value unchanged when non-null.</li>
 *   <li>{@link Require#nonNullElements} — rejects a null collection; rejects a collection
 *       containing null elements; returns the collection unchanged when valid.</li>
 * </ul>
 */
class RequireTest {

    // -----------------------------------------------------------------------
    // nonNull
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Require.nonNull")
    class NonNullTests {

        @Test
        @DisplayName("nonNull_nonNullValue_returnsValue")
        void nonNull_nonNullValue_returnsValue() {
            String value = "hello";
            assertThat(Require.nonNull(value, "value")).isSameAs(value);
        }

        @Test
        @DisplayName("nonNull_nullValue_throwsNullPointerException")
        void nonNull_nullValue_throwsNullPointerException() {
            assertThatThrownBy(() -> Require.nonNull(null, "myParam"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("myParam");
        }

        @Test
        @DisplayName("nonNull_nullValue_messageContainsParamName")
        void nonNull_nullValue_messageContainsParamName() {
            assertThatThrownBy(() -> Require.nonNull(null, "card"))
                    .hasMessageContaining("card");
        }
    }

    // -----------------------------------------------------------------------
    // nonNullElements
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Require.nonNullElements")
    class NonNullElementsTests {

        @Test
        @DisplayName("nonNullElements_validList_returnsCollection")
        void nonNullElements_validList_returnsCollection() {
            List<String> list = List.of("a", "b", "c");
            assertThat(Require.nonNullElements(list, "items")).isSameAs(list);
        }

        @Test
        @DisplayName("nonNullElements_nullCollection_throwsNullPointerException")
        void nonNullElements_nullCollection_throwsNullPointerException() {
            assertThatThrownBy(() -> Require.nonNullElements(null, "cards"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("cards");
        }

        @Test
        @DisplayName("nonNullElements_collectionWithNullElement_throwsIllegalArgumentException")
        void nonNullElements_collectionWithNullElement_throwsIllegalArgumentException() {
            // Arrays.asList allows null elements; List.of does not
            List<String> withNull = Arrays.asList("a", null, "c");
            assertThatThrownBy(() -> Require.nonNullElements(withNull, "cards"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cards[1]");
        }

        @Test
        @DisplayName("nonNullElements_emptyCollection_isValid")
        void nonNullElements_emptyCollection_isValid() {
            List<String> empty = List.of();
            assertThat(Require.nonNullElements(empty, "cards")).isSameAs(empty);
        }
    }

    // -----------------------------------------------------------------------
    // rejection-path logging (docs/conventions.md §Logging Conventions)
    // -----------------------------------------------------------------------

    /**
     * Exercises the real SLF4J provider (slf4j-simple, test scope), which
     * {@code src/test/resources/simplelogger.properties} configures to write
     * {@link Require} DEBUG output to {@code target/require-test.log}. Each test
     * uses a <em>unique</em> parameter name so its assertions are independent of
     * test execution order: a rejection path must contribute exactly one DEBUG
     * line naming that parameter (and the index for collection elements), and a
     * happy path must contribute none.
     */
    @Nested
    @DisplayName("rejection-path logging")
    class LoggingTests {

        private static final Path LOG_FILE = Path.of("target", "require-test.log");

        /** Reads the accumulated slf4j-simple log file (empty string if not yet created). */
        private String logContents() {
            try {
                return Files.exists(LOG_FILE) ? Files.readString(LOG_FILE, StandardCharsets.UTF_8) : "";
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private long debugLinesNaming(String token) {
            return logContents().lines()
                    .filter(line -> line.contains("DEBUG") && line.contains(token))
                    .count();
        }

        @Test
        @DisplayName("nonNull_nullValue_logsSingleDebugLineNamingParameter")
        void nonNull_nullValue_logsSingleDebugLineNamingParameter() {
            assertThatThrownBy(() -> Require.nonNull(null, "nonNullRejectParam"))
                    .isInstanceOf(NullPointerException.class);
            assertThat(debugLinesNaming("nonNullRejectParam")).isEqualTo(1L);
        }

        @Test
        @DisplayName("nonNull_nonNullValue_logsNothing")
        void nonNull_nonNullValue_logsNothing() {
            Require.nonNull("present", "nonNullHappyParam");
            assertThat(logContents()).doesNotContain("nonNullHappyParam");
        }

        @Test
        @DisplayName("nonNullElements_nullCollection_logsSingleDebugLineNamingParameter")
        void nonNullElements_nullCollection_logsSingleDebugLineNamingParameter() {
            assertThatThrownBy(() -> Require.nonNullElements(null, "nullCollectionParam"))
                    .isInstanceOf(NullPointerException.class);
            assertThat(debugLinesNaming("nullCollectionParam")).isEqualTo(1L);
        }

        @Test
        @DisplayName("nonNullElements_nullElement_logsSingleDebugLineNamingIndex")
        void nonNullElements_nullElement_logsSingleDebugLineNamingIndex() {
            assertThatThrownBy(() -> Require.nonNullElements(Arrays.asList("a", null, "c"), "elementCollectionParam"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(debugLinesNaming("elementCollectionParam[1]")).isEqualTo(1L);
        }

        @Test
        @DisplayName("nonNullElements_validCollection_logsNothing")
        void nonNullElements_validCollection_logsNothing() {
            Require.nonNullElements(List.of("a", "b", "c"), "validCollectionParam");
            assertThat(logContents()).doesNotContain("validCollectionParam");
        }
    }
}

