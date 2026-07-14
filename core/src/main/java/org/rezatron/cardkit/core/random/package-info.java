/**
 * The single randomness seam for all CardKit shuffling operations.
 *
 * <p>This package exposes three types:</p>
 * <ul>
 *   <li>{@link org.rezatron.cardkit.core.random.RandomSource} — the interface
 *       all shuffling code depends on.</li>
 *   <li>{@link org.rezatron.cardkit.core.random.SecureRandomSource} — production
 *       implementation backed by {@code java.security.SecureRandom}.</li>
 *   <li>{@link org.rezatron.cardkit.core.random.SeededRandomSource} —
 *       <strong>test-only</strong> deterministic PRNG; never use in production.</li>
 * </ul>
 *
 * <p>See <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>
 * and {@code docs/conventions.md §Randomness Usage Rule} for the full rationale.</p>
 */
package org.rezatron.cardkit.core.random;

