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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Shoe}.
 *
 * <p>All shuffle tests use {@link SeededRandomSource} for full determinism and
 * offline execution (no network, no infrastructure).</p>
 */
@DisplayName("Shoe")
class ShoeTest {

    // -------------------------------------------------------------------------
    // Factory / construction invariants
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Construction (ofDecks factory)")
    class ConstructionTests {

        @Test
        @DisplayName("ofDecks(1) yields exactly 52 cards")
        void ofDecks_1_yields52Cards() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(1L));
            assertThat(shoe.cardsRemaining()).isEqualTo(52);
            assertThat(shoe.totalCards()).isEqualTo(52);
            assertThat(shoe.deckCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("ofDecks(6) yields exactly 312 cards")
        void ofDecks_6_yields312Cards() {
            Shoe shoe = Shoe.ofDecks(6, new SeededRandomSource(1L));
            assertThat(shoe.cardsRemaining()).isEqualTo(312);
            assertThat(shoe.totalCards()).isEqualTo(312);
            assertThat(shoe.deckCount()).isEqualTo(6);
        }

        @Test
        @DisplayName("ofDecks(N) yields N × 52 cards for arbitrary N")
        void ofDecks_n_yieldsNTimes52Cards() {
            for (int n = 1; n <= 8; n++) {
                Shoe shoe = Shoe.ofDecks(n, new SeededRandomSource(1L));
                assertThat(shoe.totalCards())
                        .as("N=%d", n)
                        .isEqualTo(n * 52);
                assertThat(shoe.cardsRemaining())
                        .as("N=%d remaining", n)
                        .isEqualTo(n * 52);
            }
        }

        @Test
        @DisplayName("ofDecks(2) allows up to 2 copies of the same card (legal)")
        void ofDecks_2_allowsDuplicatesAcrossDecks() {
            Shoe shoe = Shoe.ofDecks(2, new SeededRandomSource(1L));
            List<Card> all = dealAll(shoe);

            // Build frequency map
            Map<Card, Integer> freq = new HashMap<>();
            for (Card c : all) {
                freq.merge(c, 1, Integer::sum);
            }

            // Every card must appear exactly twice
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    Card card = new Card(rank, suit);
                    assertThat(freq.get(card))
                            .as("frequency of %s", card)
                            .isEqualTo(2);
                }
            }
        }

        @Test
        @DisplayName("ofDecks(N) each card appears exactly N times")
        void ofDecks_n_eachCardAppearsNTimes() {
            int n = 4;
            Shoe shoe = Shoe.ofDecks(n, new SeededRandomSource(1L));
            List<Card> all = dealAll(shoe);

            Map<Card, Integer> freq = new HashMap<>();
            for (Card c : all) {
                freq.merge(c, 1, Integer::sum);
            }

            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    assertThat(freq.get(new Card(rank, suit)))
                            .as("N=%d frequency", n)
                            .isEqualTo(n);
                }
            }
        }

        @Test
        @DisplayName("ofDecks with null randomSource throws NullPointerException")
        void ofDecks_nullRandomSource_throws() {
            assertThatThrownBy(() -> Shoe.ofDecks(1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ofDecks with deckCount < 1 throws IllegalArgumentException")
        void ofDecks_invalidDeckCount_throws() {
            assertThatThrownBy(() -> Shoe.ofDecks(0, new SeededRandomSource(1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("deckCount must be >= 1");

            assertThatThrownBy(() -> Shoe.ofDecks(-3, new SeededRandomSource(1L)))
                    .isInstanceOf(IllegalArgumentException.class);
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
            Shoe shoe = Shoe.ofDecks(2, new SeededRandomSource(42L));

            List<Card> before = dealAll(shoe);

            shoe.shuffle();
            List<Card> after = dealAll(shoe);

            assertThat(after).hasSize(104);
            // Same multiset
            Map<Card, Integer> freqBefore = frequencies(before);
            Map<Card, Integer> freqAfter = frequencies(after);
            assertThat(freqAfter).isEqualTo(freqBefore);
        }

        @Test
        @DisplayName("shuffle with same seed produces identical order")
        void shuffle_sameSeed_reproducibleOrder() {
            Shoe shoe1 = Shoe.ofDecks(2, new SeededRandomSource(77777L));
            Shoe shoe2 = Shoe.ofDecks(2, new SeededRandomSource(77777L));

            shoe1.shuffle();
            shoe2.shuffle();

            assertThat(dealAll(shoe1)).isEqualTo(dealAll(shoe2));
        }

        @Test
        @DisplayName("shuffle resets cursor so all N×52 cards are available")
        void shuffle_resetsDrawCursor() {
            Shoe shoe = Shoe.ofDecks(3, new SeededRandomSource(1L));

            // Deal some cards
            shoe.deal();
            shoe.deal();
            assertThat(shoe.cardsRemaining()).isEqualTo(154);

            shoe.shuffle();
            assertThat(shoe.cardsRemaining()).isEqualTo(156);
        }
    }

    // -------------------------------------------------------------------------
    // Deal behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Deal")
    class DealTests {

        @Test
        @DisplayName("deal reduces cardsRemaining by one per call")
        void deal_reducesCardsRemaining() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(1L));
            shoe.shuffle();

            for (int expected = 51; expected >= 0; expected--) {
                shoe.deal();
                assertThat(shoe.cardsRemaining()).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("dealt cards are disjoint in position from remaining (no re-deal before reset)")
        void deal_dealtPositionsDisjointFromRemaining() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(55L));
            shoe.shuffle();

            // Deal first 26
            List<Card> dealt = new ArrayList<>(26);
            for (int i = 0; i < 26; i++) {
                dealt.add(shoe.deal());
            }
            // Collect remaining 26
            List<Card> remaining = dealAll(shoe);

            // Total is 52
            assertThat(dealt.size() + remaining.size()).isEqualTo(52);
            // All 52 unique cards covered (single deck)
            Set<Card> all = new HashSet<>(dealt);
            all.addAll(remaining);
            assertThat(all).hasSize(52);
        }

        @Test
        @DisplayName("dealing from exhausted shoe throws RuleViolationException")
        void deal_exhaustedShoe_throwsRuleViolationException() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(2L));
            shoe.shuffle();
            dealAll(shoe);

            assertThat(shoe.isExhausted()).isTrue();
            assertThatThrownBy(shoe::deal)
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessageContaining("exhausted");
        }

        @Test
        @DisplayName("isExhausted returns false while cards remain")
        void isExhausted_falseWhenCardsRemain() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(1L));
            assertThat(shoe.isExhausted()).isFalse();
        }

        @Test
        @DisplayName("isExhausted returns true after all cards dealt")
        void isExhausted_trueWhenEmpty() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(1L));
            dealAll(shoe);
            assertThat(shoe.isExhausted()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Reset behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Reset")
    class ResetTests {

        @Test
        @DisplayName("reset restores all N×52 cards without reshuffling")
        void reset_restoresAllCards() {
            Shoe shoe = Shoe.ofDecks(2, new SeededRandomSource(8L));
            shoe.shuffle();

            shoe.deal();
            shoe.deal();
            assertThat(shoe.cardsRemaining()).isEqualTo(102);

            shoe.reset();
            assertThat(shoe.cardsRemaining()).isEqualTo(104);
            assertThat(shoe.isExhausted()).isFalse();
        }

        @Test
        @DisplayName("reset preserves card order from last shuffle")
        void reset_preservesShuffledOrder() {
            Shoe shoe = Shoe.ofDecks(1, new SeededRandomSource(13L));
            shoe.shuffle();

            List<Card> firstPass = dealAll(shoe);
            shoe.reset();
            List<Card> secondPass = dealAll(shoe);

            assertThat(secondPass).isEqualTo(firstPass);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<Card> dealAll(Shoe shoe) {
        List<Card> cards = new ArrayList<>();
        while (!shoe.isExhausted()) {
            cards.add(shoe.deal());
        }
        return cards;
    }

    private static Map<Card, Integer> frequencies(List<Card> cards) {
        Map<Card, Integer> freq = new HashMap<>();
        for (Card c : cards) {
            freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }
}

