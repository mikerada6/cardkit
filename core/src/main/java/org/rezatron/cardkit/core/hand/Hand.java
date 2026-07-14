package org.rezatron.cardkit.core.hand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

import org.rezatron.cardkit.core.card.Card;
import org.rezatron.cardkit.core.error.InvalidHandException;
import org.rezatron.cardkit.core.util.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic, mutable, ordered collection of {@link Card}s held by a participant.
 *
 * <p>{@code Hand} is the <strong>game-agnostic</strong> base that game-specific hands
 * (poker, blackjack) build on. It carries <em>no</em> scoring, ranking, or variant
 * knowledge — it only enforces the structural invariants shared by every hand:
 * insertion order, no duplicate cards, an optional maximum size, and the CardKit
 * null policy.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Cards are held in <strong>insertion order</strong>; {@link #cards()} reflects
 *       the order in which they were added.</li>
 *   <li>No duplicate cards: adding a {@link Card} already present throws
 *       {@link InvalidHandException}. (Value equality — two aces of spades are the
 *       same card; see {@link Card}.)</li>
 *   <li>If constructed with a fixed capacity, adding beyond it throws
 *       {@link InvalidHandException}. A hand built with {@link #Hand()} is unbounded.</li>
 *   <li>{@code null} cards are rejected via {@link Require#nonNull}
 *       ({@link NullPointerException}).</li>
 * </ul>
 *
 * <h2>Null policy</h2>
 * <p>{@link #cards()} never returns {@code null}; an empty hand yields an empty,
 * unmodifiable list. Arguments are validated at entry
 * ({@code docs/conventions.md §Immutability & Null Policy}).</p>
 *
 * <h2>Mutation</h2>
 * <p>Mutation is confined to the lifecycle methods {@link #add(Card)},
 * {@link #remove(Card)}, and {@link #clear()}. A {@code Hand} is a mutable domain
 * object with <strong>identity</strong> (not a value object): it is not compared by
 * value and inherits reference {@code equals}/{@code hashCode}
 * ({@code docs/data-model.md §Value Objects vs. Entities}).</p>
 *
 * <h2>Thread safety</h2>
 * <p>{@code Hand} is <strong>not</strong> thread-safe. External synchronisation is
 * required if shared across threads.</p>
 *
 * <p>See {@code docs/data-model.md §Value Objects vs. Entities},
 * {@code docs/data-model.md §Relationships & Ownership}, and
 * {@code docs/conventions.md §Immutability & Null Policy}.</p>
 */
public class Hand {

    private static final Logger log = LoggerFactory.getLogger(Hand.class);

    /** Sentinel maximum-size value indicating the hand imposes no capacity limit. */
    private static final int UNBOUNDED = -1;

    /** Backing store; index 0 is the first card added. Never {@code null}. */
    private final List<Card> cards = new ArrayList<>();

    /** Maximum number of cards, or {@link #UNBOUNDED} when the hand has no fixed limit. */
    private final int maxCards;

    /**
     * Creates an empty, <strong>unbounded</strong> hand.
     *
     * <p>Suitable for hands that grow without a fixed limit (e.g. a blackjack hand
     * that hits repeatedly). Use {@link #Hand(int)} to cap the size.</p>
     */
    public Hand() {
        this.maxCards = UNBOUNDED;
        log.debug("Hand created: unbounded");
    }

    /**
     * Creates an empty hand with a fixed maximum size.
     *
     * @param capacity the maximum number of cards this hand may hold; must be {@code >= 0}
     * @throws IllegalArgumentException if {@code capacity} is negative
     */
    public Hand(int capacity) {
        if (capacity < 0) {
            log.debug("Rejected hand construction: capacity {} is negative", capacity);
            throw new IllegalArgumentException("capacity must be >= 0, was " + capacity);
        }
        this.maxCards = capacity;
        log.debug("Hand created: capacity {}", capacity);
    }

    /**
     * Creates an unbounded hand seeded with the given cards, in iteration order.
     *
     * <p>Each card is validated as it is added, so a {@code null} element or a
     * duplicate in {@code initialCards} fails construction with the same exception
     * the corresponding {@link #add(Card)} would throw.</p>
     *
     * @param initialCards the cards to seed the hand with; must not be {@code null}
     *                     and must contain no {@code null} or duplicate cards
     * @throws NullPointerException if {@code initialCards} or any element is {@code null}
     * @throws InvalidHandException if {@code initialCards} contains a duplicate card
     */
    public Hand(Collection<Card> initialCards) {
        Require.nonNull(initialCards, "initialCards");
        this.maxCards = UNBOUNDED;
        for (Card card : initialCards) {
            add(card);
        }
        log.debug("Hand created from {} card(s)", cards.size());
    }

    // -------------------------------------------------------------------------
    // Lifecycle mutation
    // -------------------------------------------------------------------------

    /**
     * Adds a card to the end of the hand.
     *
     * @param card the card to add; must not be {@code null}
     * @throws NullPointerException if {@code card} is {@code null}
     * @throws InvalidHandException if {@code card} is already present, or the hand is
     *                              at its fixed capacity
     */
    public void add(Card card) {
        Require.nonNull(card, "card");
        if (cards.contains(card)) {
            log.debug("Rejected duplicate card {} for hand", card);
            throw new InvalidHandException("Card already present in hand: " + card);
        }
        if (maxCards != UNBOUNDED && cards.size() >= maxCards) {
            log.debug("Rejected card {}: hand at capacity {}", card, maxCards);
            throw new InvalidHandException(
                    "Hand is at capacity (" + maxCards + "); cannot add " + card);
        }
        cards.add(card);
        log.trace("Added {} to hand; size now {}", card, cards.size());
    }

    /**
     * Removes the given card from the hand, if present.
     *
     * @param card the card to remove; must not be {@code null}
     * @return {@code true} if the card was present and removed, {@code false} otherwise
     * @throws NullPointerException if {@code card} is {@code null}
     */
    public boolean remove(Card card) {
        Require.nonNull(card, "card");
        boolean removed = cards.remove(card);
        if (removed) {
            log.trace("Removed {} from hand; size now {}", card, cards.size());
        }
        return removed;
    }

    /**
     * Removes every card, leaving an empty hand. The capacity (if any) is unchanged.
     */
    public void clear() {
        int had = cards.size();
        cards.clear();
        log.debug("Hand cleared; {} card(s) removed", had);
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable snapshot of this hand's cards, in insertion order.
     *
     * <p>Never {@code null}; an empty hand yields an empty list. The returned list is
     * a defensive copy — later mutation of the hand does not affect it, and it cannot
     * be used to mutate the hand.</p>
     *
     * @return an unmodifiable, ordered view of the cards; never {@code null}
     */
    public List<Card> cards() {
        return List.copyOf(cards);
    }

    /**
     * Returns whether the hand currently contains the given card.
     *
     * @param card the card to test for; must not be {@code null}
     * @return {@code true} if present
     * @throws NullPointerException if {@code card} is {@code null}
     */
    public boolean contains(Card card) {
        Require.nonNull(card, "card");
        return cards.contains(card);
    }

    /**
     * Returns the number of cards currently in the hand.
     *
     * @return the card count, {@code >= 0}
     */
    public int size() {
        return cards.size();
    }

    /**
     * Returns whether the hand holds no cards.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Returns this hand's fixed capacity, if it has one.
     *
     * @return the maximum size as an {@link OptionalInt}, or {@link OptionalInt#empty()}
     *         for an unbounded hand
     */
    public OptionalInt capacity() {
        return maxCards == UNBOUNDED ? OptionalInt.empty() : OptionalInt.of(maxCards);
    }

    /**
     * Returns a compact, human-readable representation, e.g. {@code "Hand[ACE♠, TEN♥]"}.
     *
     * @return compact hand label
     */
    @Override
    public String toString() {
        return "Hand" + cards;
    }
}
