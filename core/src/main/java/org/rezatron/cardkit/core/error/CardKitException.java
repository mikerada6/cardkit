package org.rezatron.cardkit.core.error;

/**
 * Base unchecked exception for all CardKit domain rule and programming violations.
 *
 * <p>Callers should catch this type when they want to handle <em>any</em> domain error.
 * Prefer catching the more specific subtypes when the handling differs:</p>
 * <ul>
 *   <li>{@link RuleViolationException} — a game-rule invariant was broken at runtime
 *       (e.g. dealing from an exhausted source, acting out of phase).</li>
 *   <li>{@link InvalidHandException} — a {@code Hand} was constructed with an illegal
 *       set of cards (wrong count, duplicates, etc.).</li>
 * </ul>
 *
 * <p><strong>Game outcomes are never exceptions.</strong> Values such as {@code RoundOutcome}
 * (WIN/LOSE/PUSH) and {@code HighLowResult} are modelled as return values; only
 * violations of invariants throw.</p>
 */
public class CardKitException extends RuntimeException {

    /**
     * Constructs a {@code CardKitException} with the given detail message.
     *
     * @param message human-readable description of the violation; must not be {@code null}
     */
    public CardKitException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code CardKitException} with a detail message and a cause.
     *
     * @param message human-readable description of the violation; must not be {@code null}
     * @param cause   the underlying throwable that triggered this exception
     */
    public CardKitException(String message, Throwable cause) {
        super(message, cause);
    }
}

