package org.rezatron.cardkit.core.card;

import java.util.Objects;

/**
 * An immutable value object representing a single playing card.
 *
 * <h2>Value semantics</h2>
 * <p>
 * Two {@code Card} instances are equal when they have the same {@link Rank} and
 * {@link Suit}, regardless of which {@code Deck} or {@code Shoe} they were dealt
 * from.  {@code Card} has no notion of source deck identity.  Because Java
 * {@code record} provides structural equality automatically, {@code equals} and
 * {@code hashCode} are value-based out of the box.
 * </p>
 *
 * <h2>Immutability</h2>
 * <p>
 * {@code record} components are {@code final}; no setters exist.  Instances are
 * safe to share freely across threads.
 * </p>
 *
 * <h2>Convenience</h2>
 * <p>
 * {@link #toString()} returns a compact human-readable label, e.g. {@code "ACE♠"}
 * or {@code "TEN♥"}, useful in logs and test assertions.
 * </p>
 *
 * <p>See {@code docs/data-model.md §Core Types} and
 * {@code docs/conventions.md §Immutability & Null Policy}.</p>
 *
 * @param rank the rank of this card; never {@code null}
 * @param suit the suit of this card; never {@code null}
 */
public record Card(Rank rank, Suit suit) {

    /**
     * Compact canonical constructor — validates that neither component is {@code null}.
     *
     * @param rank the rank; must not be {@code null}
     * @param suit the suit; must not be {@code null}
     * @throws NullPointerException if {@code rank} or {@code suit} is {@code null}
     */
    public Card {
        Objects.requireNonNull(rank, "rank must not be null");
        Objects.requireNonNull(suit, "suit must not be null");
    }

    /**
     * Returns a compact, human-readable representation, e.g. {@code "ACE♠"} or {@code "TEN♥"}.
     *
     * @return compact card label
     */
    @Override
    public String toString() {
        return rank.name() + suitSymbol(suit);
    }

    private static String suitSymbol(Suit s) {
        return switch (s) {
            case CLUBS    -> "♣";
            case DIAMONDS -> "♦";
            case HEARTS   -> "♥";
            case SPADES   -> "♠";
        };
    }
}

