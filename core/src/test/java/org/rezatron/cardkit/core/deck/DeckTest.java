package org.rezatron.cardkit.core.deck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rezatron.cardkit.core.card.Card;
import org.rezatron.cardkit.core.card.Rank;
import org.rezatron.cardkit.core.card.Suit;
import org.rezatron.cardkit.core.error.RuleViolationException;
import org.rezatron.cardkit.core.random.SeededRandomSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Deck}.
 *
 * <p>All shuffle tests use {@link SeededRandomSource} for full determinism and
 * offline execution (no network, no infrastructure).</p>
 */
@DisplayName("Deck")
class DeckTest {

    // -------------------------------------------------------------------------
    // Construction invariants
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("constructs deck with exactly 52 cards")
        void constructsDeckWithExactly52Cards() {
            Deck deck = new Deck(new SeededRandomSource(1L));
            assertThat(deck.cardsRemaining()).isEqualTo(52);
            assertThat(deck.size()).isEqualTo(52);
        }

        @Test
        @DisplayName("deck contains all 52 distinct (Rank, Suit) combinations")
        void deckContainsAll52DistinctCards() {
            Deck deck = new Deck(new SeededRandomSource(1L));

            // Collect all cards by dealing them
            List<Card> dealt = new ArrayList<>(52);
            while (!deck.isExhausted()) {
                dealt.add(deck.deal());
            }

            // Must be exactly 52
            assertThat(dealt).hasSize(52);

            // Must all be unique
            Set<Card> unique = new HashSet<>(dealt);
            assertThat(unique).hasSize(52);

            // Must cover every (Rank, Suit) combination
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    assertThat(unique).contains(new Card(rank, suit));
                }
            }
        }

        @Test
        @DisplayName("constructor throws NullPointerException for null randomSource")
        void constructor_nullRandomSource_throws() {
            assertThatThrownBy(() -> new Deck(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Shuffle correctness
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Shuffle")
    class ShuffleTests {

        @Test
        @DisplayName("shuffle is a permutation: multiset of cards is preserved")
        void shuffle_isPermutation_multisetPreserved() {
            Deck deck = new Deck(new SeededRandomSource(42L));

            // Collect unshuffled order
            List<Card> before = new ArrayList<>(52);
            while (!deck.isExhausted()) {
                before.add(deck.deal());
            }

            // Shuffle and collect again
            deck.shuffle();
            List<Card> after = new ArrayList<>(52);
            while (!deck.isExhausted()) {
                after.add(deck.deal());
            }

            // Same multiset (all 52 cards present after shuffle)
            assertThat(after).hasSize(52);
            assertThat(new HashSet<>(after)).isEqualTo(new HashSet<>(before));
        }

        @Test
        @DisplayName("shuffle with same seed produces identical order")
        void shuffle_sameSeed_reproducibleOrder() {
            SeededRandomSource rng1 = new SeededRandomSource(12345L);
            SeededRandomSource rng2 = new SeededRandomSource(12345L);

            Deck deck1 = new Deck(rng1);
            Deck deck2 = new Deck(rng2);

            deck1.shuffle();
            deck2.shuffle();

            List<Card> order1 = dealAll(deck1);
            List<Card> order2 = dealAll(deck2);

            assertThat(order1).isEqualTo(order2);
        }

        @Test
        @DisplayName("shuffle resets cursor so all 52 cards are available")
        void shuffle_resetsDrawCursorToFullDeck() {
            Deck deck = new Deck(new SeededRandomSource(1L));

            // Deal some cards
            deck.deal();
            deck.deal();
            deck.deal();
            assertThat(deck.cardsRemaining()).isEqualTo(49);

            // Shuffle — all 52 must become available again
            deck.shuffle();
            assertThat(deck.cardsRemaining()).isEqualTo(52);
        }

        @Test
        @DisplayName("shuffle with different seeds produces different orders (probabilistically)")
        void shuffle_differentSeeds_likelyDifferentOrder() {
            Deck deck1 = new Deck(new SeededRandomSource(1L));
            Deck deck2 = new Deck(new SeededRandomSource(999L));
            deck1.shuffle();
            deck2.shuffle();

            List<Card> order1 = dealAll(deck1);
            List<Card> order2 = dealAll(deck2);

            // Two different seeds will produce different permutations
            // (collision is astronomically unlikely with seed distance)
            assertThat(order1).isNotEqualTo(order2);
        }
    }

    // -------------------------------------------------------------------------
    // Deal behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Deal")
    class DealTests {

        @Test
        @DisplayName("deal removes cards from available pool")
        void deal_reducesCardsRemaining() {
            Deck deck = new Deck(new SeededRandomSource(7L));
            deck.shuffle();

            for (int expected = 51; expected >= 0; expected--) {
                deck.deal();
                assertThat(deck.cardsRemaining()).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("dealt cards are disjoint from remaining cards")
        void deal_dealtCardsDisjointFromRemaining() {
            Deck deck = new Deck(new SeededRandomSource(99L));
            deck.shuffle();

            // Deal half the deck
            List<Card> dealt = new ArrayList<>(26);
            for (int i = 0; i < 26; i++) {
                dealt.add(deck.deal());
            }

            // Collect remaining
            List<Card> remaining = dealAll(deck);

            // No overlap
            Set<Card> dealtSet = new HashSet<>(dealt);
            Set<Card> remainingSet = new HashSet<>(remaining);
            assertThat(dealtSet).doesNotContainAnyElementsOf(remainingSet);
        }

        @Test
        @DisplayName("dealing from exhausted deck throws RuleViolationException")
        void deal_exhaustedDeck_throwsRuleViolationException() {
            Deck deck = new Deck(new SeededRandomSource(3L));
            deck.shuffle();
            dealAll(deck);

            assertThat(deck.isExhausted()).isTrue();
            assertThatThrownBy(deck::deal)
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessageContaining("exhausted");
        }

        @Test
        @DisplayName("isExhausted returns false before all cards are dealt")
        void isExhausted_returnsFalseWhenCardsRemain() {
            Deck deck = new Deck(new SeededRandomSource(1L));
            deck.shuffle();
            assertThat(deck.isExhausted()).isFalse();
        }

        @Test
        @DisplayName("isExhausted returns true after all 52 cards are dealt")
        void isExhausted_returnsTrueWhenEmpty() {
            Deck deck = new Deck(new SeededRandomSource(1L));
            deck.shuffle();
            dealAll(deck);
            assertThat(deck.isExhausted()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Reset behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Reset")
    class ResetTests {

        @Test
        @DisplayName("reset restores all 52 cards without reshuffling")
        void reset_restoresAllCards() {
            Deck deck = new Deck(new SeededRandomSource(5L));
            deck.shuffle();

            // Deal some cards
            deck.deal();
            deck.deal();
            assertThat(deck.cardsRemaining()).isEqualTo(50);

            // Reset — all 52 back
            deck.reset();
            assertThat(deck.cardsRemaining()).isEqualTo(52);
            assertThat(deck.isExhausted()).isFalse();
        }

        @Test
        @DisplayName("reset preserves card order from last shuffle")
        void reset_preservesShuffledOrder() {
            Deck deck = new Deck(new SeededRandomSource(11L));
            deck.shuffle();

            // Record full order
            List<Card> firstPass = dealAll(deck);
            assertThat(deck.isExhausted()).isTrue();

            // Reset and deal again — same order
            deck.reset();
            List<Card> secondPass = dealAll(deck);

            assertThat(secondPass).isEqualTo(firstPass);
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static List<Card> dealAll(Deck deck) {
        List<Card> cards = new ArrayList<>();
        while (!deck.isExhausted()) {
            cards.add(deck.deal());
        }
        return cards;
    }
}

