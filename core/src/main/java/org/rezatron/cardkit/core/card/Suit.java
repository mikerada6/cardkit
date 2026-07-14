package org.rezatron.cardkit.core.card;

/**
 * The four suits of a standard 52-card deck.
 *
 * <p>Suits carry no inherent ordering relative to one another in CardKit — suits
 * are equal partners for evaluation purposes. The enum declaration order
 * (alphabetical) must not be construed as a ranking.</p>
 *
 * <p>See {@code docs/data-model.md §Core Types} and
 * {@code docs/conventions.md §Domain Naming}.</p>
 */
public enum Suit {

    /** ♣ */
    CLUBS,

    /** ♦ */
    DIAMONDS,

    /** ♥ */
    HEARTS,

    /** ♠ */
    SPADES
}

