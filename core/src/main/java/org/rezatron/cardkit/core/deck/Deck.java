package org.rezatron.cardkit.core.deck;

import org.rezatron.cardkit.core.card.Card;
import org.rezatron.cardkit.core.card.Rank;
import org.rezatron.cardkit.core.card.Suit;
import org.rezatron.cardkit.core.error.RuleViolationException;
import org.rezatron.cardkit.core.random.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A standard 52-card deck with mutable draw state.
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Always constructed with exactly 52 distinct {@code (Rank, Suit)} cards;
 *       no duplicates within a single deck.</li>
 *   <li>A dealt card is not re-dealt until {@link #reset()} (or {@link #shuffle()}) is called.</li>
 *   <li>Dealing from an exhausted deck throws
 *       {@link RuleViolationException}.</li>
 * </ul>
 *
 * <h2>Shuffling</h2>
 * <p>Shuffling uses an explicit Fisher–Yates algorithm over the injected
 * {@link RandomSource}. The deck never instantiates its own RNG — see
 * <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>.</p>
 *
 * <h2>Thread safety</h2>
 * <p>{@code Deck} is <strong>not</strong> thread-safe. External synchronisation
 * is required if shared across threads.</p>
 *
 * <p>See {@code docs/data-model.md §Deck} and {@code docs/data-model.md §Invariants}.</p>
 */
public final class Deck {

    private static final Logger log = LoggerFactory.getLogger(Deck.class);

    /** Total cards in one standard deck. */
    static final int DECK_SIZE = 52;

    private final RandomSource randomSource;

    /** Ordered card array; index 0 is the "bottom", index {@code cursor-1} is the last dealt. */
    private final List<Card> cards;

    /** Index of the next card to deal. {@code cursor == cards.size()} means exhausted. */
    private int cursor;

    /**
     * Constructs a new, unshuffled deck of 52 distinct cards.
     *
     * <p>Cards are ordered by suit then rank at construction. Call {@link #shuffle()}
     * before dealing to randomise the order.</p>
     *
     * @param randomSource the randomness source used by {@link #shuffle()}; never {@code null}
     * @throws NullPointerException if {@code randomSource} is {@code null}
     */
    public Deck(RandomSource randomSource) {
        Objects.requireNonNull(randomSource, "randomSource must not be null");
        this.randomSource = randomSource;
        this.cards = buildFullDeck();
        this.cursor = 0;
        log.debug("Deck created: {} cards", DECK_SIZE);
    }

    // -------------------------------------------------------------------------
    // Mutation
    // -------------------------------------------------------------------------

    /**
     * Shuffles all 52 cards using Fisher–Yates over the injected {@link RandomSource}.
     *
     * <p>Any previously dealt cards are returned to the pool; the draw cursor is
     * reset to the beginning of the deck.</p>
     */
    public void shuffle() {
        cursor = 0;
        fisherYates(cards, randomSource);
        log.debug("Deck shuffled; {} cards available", cardsRemaining());
    }

    /**
     * Deals the top card from the deck, removing it from the available pool.
     *
     * @return the next card
     * @throws RuleViolationException if the deck is exhausted
     */
    public Card deal() {
        if (isExhausted()) {
            log.debug("Deal attempted on exhausted deck");
            throw new RuleViolationException(
                    "Deck is exhausted: all 52 cards have been dealt. Call reset() or shuffle() before dealing again.");
        }
        Card card = cards.get(cursor++);
        log.trace("Dealt {} from deck; {} cards remaining", card, cardsRemaining());
        return card;
    }

    /**
     * Resets the deck so that all 52 cards are available again without re-shuffling.
     *
     * <p>Card order is preserved from the last shuffle (or construction order if
     * never shuffled). Call {@link #shuffle()} after this to randomise again.</p>
     */
    public void reset() {
        cursor = 0;
        log.debug("Deck reset; {} cards available", cardsRemaining());
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns the number of cards that have not yet been dealt.
     *
     * @return remaining cards, between 0 and 52 inclusive
     */
    public int cardsRemaining() {
        return cards.size() - cursor;
    }

    /**
     * Returns {@code true} when every card has been dealt and no cards remain.
     *
     * @return {@code true} if exhausted
     */
    public boolean isExhausted() {
        return cursor >= cards.size();
    }

    /**
     * Returns the total number of cards this deck was constructed with (always 52).
     *
     * @return 52
     */
    public int size() {
        return DECK_SIZE;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the canonical 52-card set: all (Rank, Suit) combinations.
     * Iteration order: for each Suit, all Ranks (TWO…ACE).
     */
    private static List<Card> buildFullDeck() {
        List<Card> deck = new ArrayList<>(DECK_SIZE);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        return deck;
    }

    /**
     * In-place Fisher–Yates (Knuth) shuffle over a mutable list.
     *
     * <p>Uses the injected {@link RandomSource} exclusively — no internal RNG.</p>
     *
     * @param list   the list to shuffle in place; must not be {@code null}
     * @param rng    the randomness source; must not be {@code null}
     */
    static void fisherYates(List<Card> list, RandomSource rng) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Card tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}

