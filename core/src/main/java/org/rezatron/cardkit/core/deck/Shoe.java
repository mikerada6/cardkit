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
 * A multi-deck shoe: {@code N ≥ 1} standard 52-card decks combined into a
 * single dealing source.
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Total card count is exactly {@code N × 52}.</li>
 *   <li>The same card may appear up to {@code N} times across the shoe —
 *       this is legal for Blackjack and multi-deck poker games, not a violation.</li>
 *   <li>Dealing from an exhausted shoe throws {@link RuleViolationException}.</li>
 * </ul>
 *
 * <h2>Shuffling</h2>
 * <p>Shuffling uses an explicit Fisher–Yates algorithm over the injected
 * {@link RandomSource}. The shoe never instantiates its own RNG — see
 * <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>.</p>
 *
 * <h2>Construction</h2>
 * <p>Use the static factory {@link #ofDecks(int, RandomSource)} rather than a
 * constructor, keeping the API intent explicit and allowing future cached
 * construction strategies.</p>
 *
 * <h2>Thread safety</h2>
 * <p>{@code Shoe} is <strong>not</strong> thread-safe.</p>
 *
 * <p>See {@code docs/data-model.md §Shoe} and {@code docs/data-model.md §Invariants}.</p>
 */
public final class Shoe {

    private static final Logger log = LoggerFactory.getLogger(Shoe.class);

    /** Number of cards in one standard deck. */
    private static final int CARDS_PER_DECK = 52;

    private final int deckCount;
    private final RandomSource randomSource;

    /** Combined cards from all N decks. */
    private final List<Card> cards;

    /** Index of the next card to deal. */
    private int cursor;

    private Shoe(int deckCount, RandomSource randomSource) {
        this.deckCount = deckCount;
        this.randomSource = randomSource;
        this.cards = buildShoe(deckCount);
        this.cursor = 0;
        log.debug("Shoe created: {} decks, {} cards total", deckCount, cards.size());
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Creates an unshuffled shoe of {@code deckCount} standard 52-card decks.
     *
     * <p>Call {@link #shuffle()} before dealing to randomise the card order.</p>
     *
     * @param deckCount    number of decks to combine; must be {@code ≥ 1}
     * @param randomSource the randomness source used by {@link #shuffle()}; never {@code null}
     * @return a new {@code Shoe} with {@code deckCount × 52} cards
     * @throws NullPointerException     if {@code randomSource} is {@code null}
     * @throws IllegalArgumentException if {@code deckCount < 1}
     */
    public static Shoe ofDecks(int deckCount, RandomSource randomSource) {
        Objects.requireNonNull(randomSource, "randomSource must not be null");
        if (deckCount < 1) {
            throw new IllegalArgumentException(
                    "deckCount must be >= 1, got: " + deckCount);
        }
        return new Shoe(deckCount, randomSource);
    }

    // -------------------------------------------------------------------------
    // Mutation
    // -------------------------------------------------------------------------

    /**
     * Shuffles all {@code N × 52} cards using Fisher–Yates over the injected
     * {@link RandomSource}.
     *
     * <p>Any previously dealt cards are returned to the pool; the draw cursor is
     * reset to the beginning of the shoe.</p>
     */
    public void shuffle() {
        cursor = 0;
        Deck.fisherYates(cards, randomSource);
        log.debug("Shoe shuffled; {} cards available", cardsRemaining());
    }

    /**
     * Deals the top card from the shoe, removing it from the available pool.
     *
     * @return the next card
     * @throws RuleViolationException if the shoe is exhausted
     */
    public Card deal() {
        if (isExhausted()) {
            log.debug("Deal attempted on exhausted shoe ({} decks)", deckCount);
            throw new RuleViolationException(
                    "Shoe is exhausted: all " + cards.size()
                    + " cards have been dealt. Call reset() or shuffle() before dealing again.");
        }
        Card card = cards.get(cursor++);
        log.trace("Dealt {} from shoe; {} cards remaining", card, cardsRemaining());
        return card;
    }

    /**
     * Resets the shoe so that all {@code N × 52} cards are available again without
     * re-shuffling.
     *
     * <p>Card order is preserved from the last shuffle (or construction order if
     * never shuffled). Call {@link #shuffle()} after this to randomise again.</p>
     */
    public void reset() {
        cursor = 0;
        log.debug("Shoe reset; {} cards available", cardsRemaining());
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns the number of cards that have not yet been dealt.
     *
     * @return remaining cards, between 0 and {@code N × 52} inclusive
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
     * Returns the number of decks this shoe was constructed with.
     *
     * @return deck count, always {@code ≥ 1}
     */
    public int deckCount() {
        return deckCount;
    }

    /**
     * Returns the total number of cards in this shoe ({@code N × 52}).
     *
     * @return total card count
     */
    public int totalCards() {
        return deckCount * CARDS_PER_DECK;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Builds the combined list of cards from N full decks. */
    private static List<Card> buildShoe(int deckCount) {
        List<Card> shoe = new ArrayList<>(deckCount * CARDS_PER_DECK);
        for (int d = 0; d < deckCount; d++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    shoe.add(new Card(rank, suit));
                }
            }
        }
        return shoe;
    }
}

