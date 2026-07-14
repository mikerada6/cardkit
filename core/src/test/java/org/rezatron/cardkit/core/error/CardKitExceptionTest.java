package org.rezatron.cardkit.core.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@code core.error} exception hierarchy.
 *
 * <p>Verifies that:</p>
 * <ul>
 *   <li>All exceptions carry the supplied detail message.</li>
 *   <li>All exceptions carry a supplied cause when one is provided.</li>
 *   <li>The inheritance hierarchy is correct ({@code RuleViolationException} and
 *       {@link InvalidHandException} extend {@code CardKitException}; all extend
 *       {@code RuntimeException}).</li>
 *   <li>All types are unchecked — no {@code throws} declaration needed at a catch site.</li>
 * </ul>
 */
class CardKitExceptionTest {

    // -----------------------------------------------------------------------
    // CardKitException
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("CardKitException")
    class CardKitExceptionTests {

        @Test
        @DisplayName("constructor_messageOnly_storesMessage")
        void constructor_messageOnly_storesMessage() {
            var ex = new CardKitException("base error");
            assertThat(ex.getMessage()).isEqualTo("base error");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("constructor_messageAndCause_storesBoth")
        void constructor_messageAndCause_storesBoth() {
            var cause = new IllegalStateException("root");
            var ex = new CardKitException("base error", cause);
            assertThat(ex.getMessage()).isEqualTo("base error");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("isInstanceOf_runtimeException")
        void isInstanceOf_runtimeException() {
            assertThat(new CardKitException("x")).isInstanceOf(RuntimeException.class);
        }
    }

    // -----------------------------------------------------------------------
    // RuleViolationException
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("RuleViolationException")
    class RuleViolationExceptionTests {

        @Test
        @DisplayName("constructor_messageOnly_storesMessage")
        void constructor_messageOnly_storesMessage() {
            var ex = new RuleViolationException("deck exhausted");
            assertThat(ex.getMessage()).isEqualTo("deck exhausted");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("constructor_messageAndCause_storesBoth")
        void constructor_messageAndCause_storesBoth() {
            var cause = new IllegalStateException("root");
            var ex = new RuleViolationException("out of phase", cause);
            assertThat(ex.getMessage()).isEqualTo("out of phase");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("isSubtypeOf_cardKitException")
        void isSubtypeOf_cardKitException() {
            assertThat(new RuleViolationException("x"))
                    .isInstanceOf(CardKitException.class)
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("isCaughtAs_cardKitException")
        void isCaughtAs_cardKitException() {
            assertThatThrownBy(() -> { throw new RuleViolationException("exhausted deck"); })
                    .isInstanceOf(CardKitException.class)
                    .hasMessage("exhausted deck");
        }
    }

    // -----------------------------------------------------------------------
    // InvalidHandException
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("InvalidHandException")
    class InvalidHandExceptionTests {

        @Test
        @DisplayName("constructor_messageOnly_storesMessage")
        void constructor_messageOnly_storesMessage() {
            var ex = new InvalidHandException("wrong card count");
            assertThat(ex.getMessage()).isEqualTo("wrong card count");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("constructor_messageAndCause_storesBoth")
        void constructor_messageAndCause_storesBoth() {
            var cause = new IllegalArgumentException("dup");
            var ex = new InvalidHandException("duplicate card", cause);
            assertThat(ex.getMessage()).isEqualTo("duplicate card");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("isSubtypeOf_cardKitException")
        void isSubtypeOf_cardKitException() {
            assertThat(new InvalidHandException("x"))
                    .isInstanceOf(CardKitException.class)
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("isCaughtAs_cardKitException")
        void isCaughtAs_cardKitException() {
            assertThatThrownBy(() -> { throw new InvalidHandException("bad hand"); })
                    .isInstanceOf(CardKitException.class)
                    .hasMessage("bad hand");
        }
    }
}

