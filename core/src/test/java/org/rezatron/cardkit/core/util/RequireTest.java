package org.rezatron.cardkit.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}

