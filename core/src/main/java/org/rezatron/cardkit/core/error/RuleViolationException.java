package org.rezatron.cardkit.core.error;

/**
 * Thrown when a caller violates a domain rule at runtime.
 *
 * <p>Examples include (but are not limited to):</p>
 * <ul>
 *   <li>Dealing a card from a {@code Deck} or {@code Shoe} that has been exhausted.</li>
 *   <li>Performing an action (hit, stand, raise) when the round/session is not in the
 *       expected phase.</li>
 *   <li>Seating more players than the table permits.</li>
 * </ul>
 *
 * <p>This is distinct from {@link InvalidHandException}, which is reserved for structural
 * problems discovered during object <em>construction</em>.</p>
 */
public class RuleViolationException extends CardKitException {

    /**
     * Constructs a {@code RuleViolationException} with the given detail message.
     *
     * @param message human-readable description of the violated rule; must not be {@code null}
     */
    public RuleViolationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code RuleViolationException} with a detail message and a cause.
     *
     * @param message human-readable description of the violated rule; must not be {@code null}
     * @param cause   the underlying throwable that triggered this exception
     */
    public RuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}

