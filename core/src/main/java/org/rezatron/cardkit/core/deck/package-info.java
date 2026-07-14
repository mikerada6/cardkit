/**
 * Dealing sources for CardKit: a single-deck {@link org.rezatron.cardkit.core.deck.Deck}
 * and a multi-deck {@link org.rezatron.cardkit.core.deck.Shoe}.
 *
 * <h2>Design constraints</h2>
 * <ul>
 *   <li>Neither {@code Deck} nor {@code Shoe} instantiate an RNG — all non-determinism
 *       is supplied via an injected {@link org.rezatron.cardkit.core.random.RandomSource}
 *       (see <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>).</li>
 *   <li>Shuffling is Fisher–Yates over the injected source; {@code java.util.Collections.shuffle}
 *       is explicitly forbidden (see {@code docs/conventions.md §Randomness Usage Rule}).</li>
 *   <li>Dealing from an exhausted source throws
 *       {@link org.rezatron.cardkit.core.error.RuleViolationException}.</li>
 * </ul>
 *
 * <p>See {@code docs/data-model.md §Deck}, {@code docs/data-model.md §Shoe},
 * and {@code docs/data-model.md §Invariants}.</p>
 */
package org.rezatron.cardkit.core.deck;

