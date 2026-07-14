package org.rezatron.cardkit.core.util;

import java.util.Collection;
import java.util.Objects;

/**
 * Lightweight precondition helpers that enforce the CardKit null-policy at public API boundaries.
 *
 * <h2>Null policy</h2>
 * <ul>
 *   <li>All public API parameters are validated at entry with these helpers.</li>
 *   <li>"Absent" results are expressed via {@link java.util.Optional} or empty collections —
 *       never by returning {@code null}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   public void deal(Card card) {
 *       Require.nonNull(card, "card");
 *       // ... implementation
 *   }
 *
 *   public Hand(List<Card> cards) {
 *       Require.nonNullElements(cards, "cards");
 *       // ... implementation
 *   }
 * }</pre>
 *
 * <p>This class is intentionally small — it delegates to {@link Objects#requireNonNull} and
 * provides consistent, readable error messages. Do not add domain logic here.</p>
 */
public final class Require {

    private Require() {
        // utility class — no instances
    }

    /**
     * Validates that {@code value} is not {@code null}.
     *
     * @param <T>       the type of the value
     * @param value     the value to check
     * @param paramName the parameter name to include in the error message
     * @return {@code value}, guaranteed non-null
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static <T> T nonNull(T value, String paramName) {
        return Objects.requireNonNull(value, paramName + " must not be null");
    }

    /**
     * Validates that {@code collection} is not {@code null} and contains no {@code null} elements.
     *
     * @param <T>        the element type
     * @param collection the collection to check
     * @param paramName  the parameter name to include in the error message
     * @return {@code collection}, guaranteed non-null and free of null elements
     * @throws NullPointerException     if {@code collection} itself is {@code null}
     * @throws IllegalArgumentException if any element of {@code collection} is {@code null}
     */
    public static <T extends Collection<?>> T nonNullElements(T collection, String paramName) {
        Objects.requireNonNull(collection, paramName + " must not be null");
        int index = 0;
        for (Object element : collection) {
            if (element == null) {
                throw new IllegalArgumentException(
                        paramName + "[" + index + "] must not be null");
            }
            index++;
        }
        return collection;
    }
}

