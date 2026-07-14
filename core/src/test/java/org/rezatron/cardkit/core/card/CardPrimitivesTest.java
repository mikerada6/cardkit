package org.rezatron.cardkit.core.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Unit tests for the {@code core.card} value objects: {@link Suit}, {@link Rank},
 * and {@link Card}.
 *
 * <p>Naming convention: {@code unitOfWork_condition_expectedResult}.</p>
 */
@DisplayName("Card primitives (Suit, Rank, Card)")
class CardPrimitivesTest {

    // -----------------------------------------------------------------------
    // Suit
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Suit enum")
    class SuitTests {

        @Test
        @DisplayName("suitValues_allFourPresent_exactlyFourSuits")
        void suitValues_allFourPresent_exactlyFourSuits() {
            Set<String> names = Arrays.stream(Suit.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            assertThat(names).containsExactlyInAnyOrder("CLUBS", "DIAMONDS", "HEARTS", "SPADES");
            assertThat(Suit.values()).hasSize(4);
        }

        @Test
        @DisplayName("suitValueOf_validName_returnsCorrectConstant")
        void suitValueOf_validName_returnsCorrectConstant() {
            assertThat(Suit.valueOf("CLUBS")).isEqualTo(Suit.CLUBS);
            assertThat(Suit.valueOf("DIAMONDS")).isEqualTo(Suit.DIAMONDS);
            assertThat(Suit.valueOf("HEARTS")).isEqualTo(Suit.HEARTS);
            assertThat(Suit.valueOf("SPADES")).isEqualTo(Suit.SPADES);
        }
    }

    // -----------------------------------------------------------------------
    // Rank
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Rank enum")
    class RankTests {

        private static final List<String> EXPECTED_RANK_NAMES = List.of(
                "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
                "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"
        );

        @Test
        @DisplayName("rankValues_allThirteenPresent_exactlyThirteenRanks")
        void rankValues_allThirteenPresent_exactlyThirteenRanks() {
            Set<String> names = Arrays.stream(Rank.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            assertThat(names).containsExactlyInAnyOrderElementsOf(EXPECTED_RANK_NAMES);
            assertThat(Rank.values()).hasSize(13);
        }

        @Test
        @DisplayName("rankOrdinal_twoToAce_ascendingPipOrder")
        void rankOrdinal_twoToAce_ascendingPipOrder() {
            // Declaration order must be TWO(0) … ACE(12)
            Rank[] values = Rank.values();
            assertThat(values[0]).isEqualTo(Rank.TWO);
            assertThat(values[12]).isEqualTo(Rank.ACE);

            // Verify strictly ascending ordinals
            for (int i = 1; i < values.length; i++) {
                assertThat(values[i].ordinal()).isGreaterThan(values[i - 1].ordinal());
            }
        }

        // -- Ace-high strength ---------------------------------------------------

        @Test
        @DisplayName("aceHighStrength_allRanks_twoIs2AndAceIs14")
        void aceHighStrength_allRanks_twoIs2AndAceIs14() {
            assertThat(Rank.TWO.aceHighStrength()).isEqualTo(2);
            assertThat(Rank.THREE.aceHighStrength()).isEqualTo(3);
            assertThat(Rank.TEN.aceHighStrength()).isEqualTo(10);
            assertThat(Rank.JACK.aceHighStrength()).isEqualTo(11);
            assertThat(Rank.QUEEN.aceHighStrength()).isEqualTo(12);
            assertThat(Rank.KING.aceHighStrength()).isEqualTo(13);
            assertThat(Rank.ACE.aceHighStrength()).isEqualTo(14);
        }

        @Test
        @DisplayName("aceHighStrength_allRanks_strictlyAscendingFromTwoToAce")
        void aceHighStrength_allRanks_strictlyAscendingFromTwoToAce() {
            Rank[] values = Rank.values(); // TWO … ACE
            for (int i = 1; i < values.length; i++) {
                assertThat(values[i].aceHighStrength())
                        .as("rank %s should have higher ace-high strength than %s",
                                values[i], values[i - 1])
                        .isGreaterThan(values[i - 1].aceHighStrength());
            }
        }

        @Test
        @DisplayName("aceHighStrength_range_allValuesBetween2And14")
        void aceHighStrength_range_allValuesBetween2And14() {
            for (Rank r : Rank.values()) {
                assertThat(r.aceHighStrength())
                        .as("ace-high strength of %s must be in [2..14]", r)
                        .isBetween(2, 14);
            }
        }

        // -- Ace-low strength ----------------------------------------------------

        @Test
        @DisplayName("aceLowStrength_ace_returns1")
        void aceLowStrength_ace_returns1() {
            assertThat(Rank.ACE.aceLowStrength()).isEqualTo(1);
        }

        @Test
        @DisplayName("aceLowStrength_nonAceRanks_sameAsAceHighStrength")
        void aceLowStrength_nonAceRanks_sameAsAceHighStrength() {
            for (Rank r : Rank.values()) {
                if (r != Rank.ACE) {
                    assertThat(r.aceLowStrength())
                            .as("non-ace rank %s: aceLowStrength must equal aceHighStrength", r)
                            .isEqualTo(r.aceHighStrength());
                }
            }
        }

        @Test
        @DisplayName("aceLowStrength_range_allValuesBetween1And13")
        void aceLowStrength_range_allValuesBetween1And13() {
            for (Rank r : Rank.values()) {
                assertThat(r.aceLowStrength())
                        .as("ace-low strength of %s must be in [1..13]", r)
                        .isBetween(1, 13);
            }
        }

        /**
         * Key acceptance criterion: an evaluator can order A-2-3-4-5 correctly
         * using ace-low strength (ace = 1 is the lowest), and can order
         * 10-J-Q-K-A correctly using ace-high strength (ace = 14 is the highest).
         */
        @Test
        @DisplayName("aceStrength_bothContexts_wheelAndBroadwaySortCorrectly")
        void aceStrength_bothContexts_wheelAndBroadwaySortCorrectly() {
            // Wheel: A-2-3-4-5 with ace-low — ace must be LOWEST
            List<Rank> wheel = List.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE);
            List<Integer> wheelStrengths = wheel.stream()
                    .map(Rank::aceLowStrength)
                    .sorted()
                    .toList();
            assertThat(wheelStrengths).containsExactly(1, 2, 3, 4, 5);
            assertThat(Rank.ACE.aceLowStrength())
                    .isLessThan(Rank.TWO.aceLowStrength()); // ace is lowest in this context

            // Broadway: 10-J-Q-K-A with ace-high — ace must be HIGHEST
            List<Rank> broadway = List.of(Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE);
            List<Integer> broadwayStrengths = broadway.stream()
                    .map(Rank::aceHighStrength)
                    .sorted()
                    .toList();
            assertThat(broadwayStrengths).containsExactly(10, 11, 12, 13, 14);
            assertThat(Rank.ACE.aceHighStrength())
                    .isGreaterThan(Rank.KING.aceHighStrength()); // ace is highest in this context
        }
    }

    // -----------------------------------------------------------------------
    // Card
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Card record")
    class CardTests {

        @Test
        @DisplayName("card_constructor_storesRankAndSuit")
        void card_constructor_storesRankAndSuit() {
            Card card = new Card(Rank.ACE, Suit.SPADES);

            assertThat(card.rank()).isEqualTo(Rank.ACE);
            assertThat(card.suit()).isEqualTo(Suit.SPADES);
        }

        @Test
        @DisplayName("card_equalsByValue_twoInstancesSameRankSuit")
        void card_equalsByValue_twoInstancesSameRankSuit() {
            Card a = new Card(Rank.ACE, Suit.SPADES);
            Card b = new Card(Rank.ACE, Suit.SPADES);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("card_notEqual_differentRankSameSuit")
        void card_notEqual_differentRankSameSuit() {
            Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
            Card kingSpades = new Card(Rank.KING, Suit.SPADES);

            assertThat(aceSpades).isNotEqualTo(kingSpades);
        }

        @Test
        @DisplayName("card_notEqual_sameRankDifferentSuit")
        void card_notEqual_sameRankDifferentSuit() {
            Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
            Card aceHearts = new Card(Rank.ACE, Suit.HEARTS);

            assertThat(aceSpades).isNotEqualTo(aceHearts);
        }

        @Test
        @DisplayName("card_nullRank_throwsNullPointerException")
        void card_nullRank_throwsNullPointerException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Card(null, Suit.CLUBS))
                    .withMessageContaining("rank");
        }

        @Test
        @DisplayName("card_nullSuit_throwsNullPointerException")
        void card_nullSuit_throwsNullPointerException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Card(Rank.TWO, null))
                    .withMessageContaining("suit");
        }

        @Test
        @DisplayName("card_toString_compactLabelContainsRankAndSuitSymbol")
        void card_toString_compactLabelContainsRankAndSuitSymbol() {
            assertThat(new Card(Rank.ACE, Suit.SPADES).toString()).isEqualTo("ACE♠");
            assertThat(new Card(Rank.TEN, Suit.HEARTS).toString()).isEqualTo("TEN♥");
            assertThat(new Card(Rank.TWO, Suit.CLUBS).toString()).isEqualTo("TWO♣");
            assertThat(new Card(Rank.KING, Suit.DIAMONDS).toString()).isEqualTo("KING♦");
        }

        @Test
        @DisplayName("card_usableAsHashMapKey_valueBasedBehavior")
        void card_usableAsHashMapKey_valueBasedBehavior() {
            Card a = new Card(Rank.FIVE, Suit.DIAMONDS);
            Card b = new Card(Rank.FIVE, Suit.DIAMONDS);

            Set<Card> set = new HashSet<>();
            set.add(a);

            assertThat(set).contains(b); // equal-by-value: b finds the slot occupied by a
            assertThat(set).hasSize(1);
        }
    }

    // -----------------------------------------------------------------------
    // 52 distinct combinations
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("52-card enumeration (Deck construction readiness)")
    class DeckEnumerationTests {

        @Test
        @DisplayName("rankSuitCombinations_allCombinations_exactly52Distinct")
        void rankSuitCombinations_allCombinations_exactly52Distinct() {
            Set<Card> all = new HashSet<>();
            for (Rank r : Rank.values()) {
                for (Suit s : Suit.values()) {
                    all.add(new Card(r, s));
                }
            }

            assertThat(all).hasSize(52);
        }

        @Test
        @DisplayName("rankSuitCombinations_eachRankHasFourSuits")
        void rankSuitCombinations_eachRankHasFourSuits() {
            for (Rank r : Rank.values()) {
                long count = Arrays.stream(Suit.values())
                        .map(s -> new Card(r, s))
                        .distinct()
                        .count();
                assertThat(count)
                        .as("rank %s should have exactly 4 distinct suit combinations", r)
                        .isEqualTo(4);
            }
        }

        @Test
        @DisplayName("rankSuitCombinations_eachSuitHasThirteenRanks")
        void rankSuitCombinations_eachSuitHasThirteenRanks() {
            for (Suit s : Suit.values()) {
                long count = Arrays.stream(Rank.values())
                        .map(r -> new Card(r, s))
                        .distinct()
                        .count();
                assertThat(count)
                        .as("suit %s should have exactly 13 distinct rank combinations", s)
                        .isEqualTo(13);
            }
        }
    }
}

