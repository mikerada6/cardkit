package org.rezatron.cardkit.core.card;

/**
 * The thirteen ranks of a standard 52-card deck, each carrying numeric strength
 * data for use in both <em>ace-high</em> and <em>ace-low</em> evaluation contexts.
 *
 * <h2>Ace duality</h2>
 * <p>
 * Ace is high for standard poker ranking ({@code ACE = 14}) and low for
 * ace-to-five low hands and the "wheel" straight ({@code A-2-3-4-5}, where
 * ace counts as {@code 1}).  This enum does <strong>not</strong> fix a single
 * natural order — it instead exposes two strength accessors so that evaluators
 * can choose the appropriate context:
 * </p>
 * <ul>
 *   <li>{@link #aceHighStrength()} — standard pip value; ACE = 14.</li>
 *   <li>{@link #aceLowStrength()} — ace-to-five value; ACE = 1, all others unchanged.</li>
 * </ul>
 *
 * <h2>Enum declaration order</h2>
 * <p>
 * Ranks are declared {@code TWO…ACE} — weakest to strongest in the ace-high
 * sense — so that the natural {@link #ordinal()} gives an ascending pip order
 * ({@code 0 = TWO, …, 12 = ACE}).  Evaluators may use either the ordinal or
 * the strength accessors; the strength accessors are preferred for clarity.
 * </p>
 *
 * <p>See {@code docs/data-model.md §Core Types} and
 * {@code docs/conventions.md §Domain Naming}.</p>
 */
public enum Rank {

    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14);

    /** Standard pip / face value (TWO=2 … ACE=14). */
    private final int pips;

    Rank(int pips) {
        this.pips = pips;
    }

    /**
     * Returns this rank's strength in an <em>ace-high</em> context.
     *
     * <p>ACE = 14; TWO = 2; all face cards are 11–13.</p>
     *
     * @return strength value {@code [2..14]}
     */
    public int aceHighStrength() {
        return pips;
    }

    /**
     * Returns this rank's strength in an <em>ace-low</em> (ace-to-five) context.
     *
     * <p>ACE = 1; TWO = 2; all others unchanged.  Used by the low-hand ranker
     * and the "wheel" ({@code A-2-3-4-5}) straight detector.</p>
     *
     * @return strength value {@code [1..13]}
     */
    public int aceLowStrength() {
        return (this == ACE) ? 1 : pips;
    }
}

