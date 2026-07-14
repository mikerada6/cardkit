package org.rezatron.cardkit.core.error;

/**
 * Thrown when a {@code Hand} is constructed with an illegal set of cards.
 *
 * <p>Examples include (but are not limited to):</p>
 * <ul>
 *   <li>Wrong number of hole cards for the requested poker variant.</li>
 *   <li>Duplicate cards within the same hand or between hole cards and the board.</li>
 *   <li>A {@code null} card reference inside the card list.</li>
 * </ul>
 *
 * <p>This is distinct from {@link RuleViolationException}, which covers rule violations
 * discovered during the <em>lifecycle</em> of a round/session (e.g. acting out of phase),
 * rather than during object construction.</p>
 */
public class InvalidHandException extends CardKitException {

    /**
     * Constructs an {@code InvalidHandException} with the given detail message.
     *
     * @param message human-readable description of the construction error; must not be {@code null}
     */
    public InvalidHandException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code InvalidHandException} with a detail message and a cause.
     *
     * @param message human-readable description of the construction error; must not be {@code null}
     * @param cause   the underlying throwable that triggered this exception
     */
    public InvalidHandException(String message, Throwable cause) {
        super(message, cause);
    }
}

