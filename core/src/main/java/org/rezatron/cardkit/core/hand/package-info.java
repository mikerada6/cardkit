/**
 * The generic, game-agnostic {@link org.rezatron.cardkit.core.hand.Hand} — a mutable,
 * ordered collection of {@link org.rezatron.cardkit.core.card.Card}s.
 *
 * <h2>Design constraints</h2>
 * <ul>
 *   <li>{@code Hand} carries no scoring, ranking, or variant knowledge; game-specific
 *       hands (poker, blackjack) in the {@code games} module build on it.</li>
 *   <li>Mutation is confined to the lifecycle methods
 *       ({@code add}, {@code remove}, {@code clear}); a {@code Hand} is a mutable
 *       domain object with identity, not a value object
 *       (see {@code docs/data-model.md §Value Objects vs. Entities}).</li>
 *   <li>Size and duplicate invariants are enforced at the boundary; violations throw
 *       {@link org.rezatron.cardkit.core.error.InvalidHandException}. {@code null}
 *       cards are rejected via {@link org.rezatron.cardkit.core.util.Require}.</li>
 *   <li>Never returns a {@code null} collection — an empty hand yields an empty view
 *       (see {@code docs/conventions.md §Immutability & Null Policy}).</li>
 * </ul>
 *
 * <p>See {@code docs/data-model.md §Value Objects vs. Entities} and
 * {@code docs/data-model.md §Relationships & Ownership}.</p>
 */
package org.rezatron.cardkit.core.hand;
