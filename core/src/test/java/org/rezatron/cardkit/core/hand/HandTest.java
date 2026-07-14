package org.rezatron.cardkit.core.hand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.OptionalInt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.rezatron.cardkit.core.card.Card;
import org.rezatron.cardkit.core.card.Rank;
import org.rezatron.cardkit.core.card.Suit;
import org.rezatron.cardkit.core.error.InvalidHandException;

/**
 * Unit tests for {@link Hand}. Fully offline — no network, no infrastructure.
 */
@DisplayName("Hand")
class HandTest {

    private static final Card ACE_SPADES = new Card(Rank.ACE, Suit.SPADES);
    private static final Card KING_HEARTS = new Card(Rank.KING, Suit.HEARTS);
    private static final Card TWO_CLUBS = new Card(Rank.TWO, Suit.CLUBS);

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("default constructor yields an empty, unbounded hand")
        void constructor_default_emptyUnbounded() {
            Hand hand = new Hand();
            assertThat(hand.isEmpty()).isTrue();
            assertThat(hand.size()).isZero();
            assertThat(hand.capacity()).isEqualTo(OptionalInt.empty());
        }

        @Test
        @DisplayName("capacity constructor records the fixed capacity")
        void constructor_capacity_recordsCapacity() {
            Hand hand = new Hand(5);
            assertThat(hand.capacity()).isEqualTo(OptionalInt.of(5));
            assertThat(hand.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("capacity constructor rejects a negative capacity")
        void constructor_negativeCapacity_throwsIllegalArgument() {
            assertThatThrownBy(() -> new Hand(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("capacity");
        }

        @Test
        @DisplayName("capacity of zero permits no cards")
        void constructor_zeroCapacity_rejectsFirstAdd() {
            Hand hand = new Hand(0);
            assertThatThrownBy(() -> hand.add(ACE_SPADES))
                    .isInstanceOf(InvalidHandException.class);
        }

        @Test
        @DisplayName("collection constructor seeds the hand in iteration order")
        void constructor_collection_seedsInOrder() {
            Hand hand = new Hand(List.of(ACE_SPADES, KING_HEARTS, TWO_CLUBS));
            assertThat(hand.cards()).containsExactly(ACE_SPADES, KING_HEARTS, TWO_CLUBS);
        }

        @Test
        @DisplayName("collection constructor rejects a duplicate card")
        void constructor_collectionWithDuplicate_throwsInvalidHand() {
            assertThatThrownBy(() -> new Hand(List.of(ACE_SPADES, ACE_SPADES)))
                    .isInstanceOf(InvalidHandException.class)
                    .hasMessageContaining(ACE_SPADES.toString());
        }

        @Test
        @DisplayName("collection constructor rejects a null collection")
        void constructor_nullCollection_throwsNpe() {
            assertThatThrownBy(() -> new Hand((java.util.Collection<Card>) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Add / remove lifecycle
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Add")
    class AddTests {

        @Test
        @DisplayName("add appends cards and preserves insertion order")
        void add_appendsInInsertionOrder() {
            Hand hand = new Hand();
            hand.add(KING_HEARTS);
            hand.add(ACE_SPADES);
            hand.add(TWO_CLUBS);
            assertThat(hand.size()).isEqualTo(3);
            assertThat(hand.cards()).containsExactly(KING_HEARTS, ACE_SPADES, TWO_CLUBS);
        }

        @Test
        @DisplayName("add rejects a null card via requireNonNull")
        void add_nullCard_throwsNpe() {
            Hand hand = new Hand();
            assertThatThrownBy(() -> hand.add(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("card");
        }

        @Test
        @DisplayName("add rejects a duplicate card")
        void add_duplicateCard_throwsInvalidHand() {
            Hand hand = new Hand();
            hand.add(ACE_SPADES);
            assertThatThrownBy(() -> hand.add(ACE_SPADES))
                    .isInstanceOf(InvalidHandException.class)
                    .hasMessageContaining(ACE_SPADES.toString());
            assertThat(hand.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("add rejects a card once the fixed capacity is reached")
        void add_beyondCapacity_throwsInvalidHand() {
            Hand hand = new Hand(2);
            hand.add(ACE_SPADES);
            hand.add(KING_HEARTS);
            assertThatThrownBy(() -> hand.add(TWO_CLUBS))
                    .isInstanceOf(InvalidHandException.class)
                    .hasMessageContaining("capacity");
            assertThat(hand.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("unbounded hand accepts many cards")
        void add_unbounded_acceptsManyCards() {
            Hand hand = new Hand();
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    hand.add(new Card(rank, suit));
                }
            }
            assertThat(hand.size()).isEqualTo(52);
        }
    }

    @Nested
    @DisplayName("Remove")
    class RemoveTests {

        @Test
        @DisplayName("remove returns true and shrinks the hand for a present card")
        void remove_presentCard_returnsTrueAndShrinks() {
            Hand hand = new Hand(List.of(ACE_SPADES, KING_HEARTS));
            boolean removed = hand.remove(ACE_SPADES);
            assertThat(removed).isTrue();
            assertThat(hand.cards()).containsExactly(KING_HEARTS);
        }

        @Test
        @DisplayName("remove returns false for a card not in the hand")
        void remove_absentCard_returnsFalse() {
            Hand hand = new Hand(List.of(KING_HEARTS));
            assertThat(hand.remove(ACE_SPADES)).isFalse();
            assertThat(hand.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("a removed card may be added again (frees the duplicate slot)")
        void remove_thenReadd_isAllowed() {
            Hand hand = new Hand();
            hand.add(ACE_SPADES);
            hand.remove(ACE_SPADES);
            hand.add(ACE_SPADES);
            assertThat(hand.cards()).containsExactly(ACE_SPADES);
        }

        @Test
        @DisplayName("remove rejects a null card via requireNonNull")
        void remove_nullCard_throwsNpe() {
            Hand hand = new Hand();
            assertThatThrownBy(() -> hand.remove(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("card");
        }
    }

    @Nested
    @DisplayName("Clear")
    class ClearTests {

        @Test
        @DisplayName("clear empties the hand but keeps the capacity")
        void clear_emptiesHandKeepsCapacity() {
            Hand hand = new Hand(5);
            hand.add(ACE_SPADES);
            hand.add(KING_HEARTS);
            hand.clear();
            assertThat(hand.isEmpty()).isTrue();
            assertThat(hand.capacity()).isEqualTo(OptionalInt.of(5));
        }
    }

    // -------------------------------------------------------------------------
    // Views & queries
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Views")
    class ViewTests {

        @Test
        @DisplayName("cards() on an empty hand returns an empty, non-null list")
        void cards_emptyHand_returnsEmptyNotNull() {
            Hand hand = new Hand();
            assertThat(hand.cards()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("cards() returns an unmodifiable list")
        void cards_returnedList_isUnmodifiable() {
            Hand hand = new Hand(List.of(ACE_SPADES));
            List<Card> view = hand.cards();
            assertThatThrownBy(() -> view.add(KING_HEARTS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("cards() is a snapshot: later mutation does not affect a prior view")
        void cards_isSnapshot_notLiveView() {
            Hand hand = new Hand();
            hand.add(ACE_SPADES);
            List<Card> snapshot = hand.cards();
            hand.add(KING_HEARTS);
            assertThat(snapshot).containsExactly(ACE_SPADES);
            assertThat(hand.cards()).containsExactly(ACE_SPADES, KING_HEARTS);
        }

        @Test
        @DisplayName("contains reflects membership")
        void contains_reflectsMembership() {
            Hand hand = new Hand(List.of(ACE_SPADES));
            assertThat(hand.contains(ACE_SPADES)).isTrue();
            assertThat(hand.contains(KING_HEARTS)).isFalse();
        }

        @Test
        @DisplayName("contains rejects a null card")
        void contains_nullCard_throwsNpe() {
            Hand hand = new Hand();
            assertThatThrownBy(() -> hand.contains(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
