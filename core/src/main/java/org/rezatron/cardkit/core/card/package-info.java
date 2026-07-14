/**
 * Atomic value objects of the CardKit domain: {@link org.rezatron.cardkit.core.card.Suit},
 * {@link org.rezatron.cardkit.core.card.Rank}, and {@link org.rezatron.cardkit.core.card.Card}.
 *
 * <p>All types in this package are immutable value objects (Java {@code record}s
 * or {@code enum}s).  They carry no evaluation logic — ranking and comparison are
 * the responsibility of the evaluator layer (stories 0009+).</p>
 *
 * <p>See {@code docs/data-model.md §Core Types}.</p>
 */
package org.rezatron.cardkit.core.card;

