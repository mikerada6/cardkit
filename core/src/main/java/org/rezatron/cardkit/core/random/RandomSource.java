package org.rezatron.cardkit.core.random;

/**
 * The single randomness seam through which all CardKit shuffling flows.
 *
 * <h2>Design rationale</h2>
 * <p>
 * Per <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>,
 * all non-determinism is funnelled through this interface so that:
 * </p>
 * <ul>
 *   <li>Production code uses {@link SecureRandomSource} (backed by
 *       {@code java.security.SecureRandom}) — cryptographically secure.</li>
 *   <li>Tests inject {@link SeededRandomSource} to obtain a fixed, reproducible
 *       shuffle sequence without touching production entropy.</li>
 * </ul>
 *
 * <h2>Prohibited alternatives</h2>
 * <p>Direct use of {@code java.util.Random}, {@code Math.random()}, or
 * {@code Collections.shuffle} is forbidden throughout the codebase
 * (see {@code docs/conventions.md §Randomness Usage Rule}).</p>
 *
 * <h2>Operations</h2>
 * <p>The interface intentionally exposes only the minimal surface that a
 * Fisher–Yates shuffle requires. Additional operations may be added here
 * when a concrete new use case demands them.</p>
 */
public interface RandomSource {

    /**
     * Returns a uniformly distributed pseudorandom {@code int} value in the
     * range {@code [0, bound)}.
     *
     * <p>This is the primitive operation required by Fisher–Yates: given a
     * remaining-length {@code n}, pick a random index {@code i} such that
     * {@code 0 <= i < n}.</p>
     *
     * @param bound the upper bound (exclusive); must be positive ({@code > 0})
     * @return a uniformly distributed value {@code r} satisfying
     *         {@code 0 <= r < bound}
     * @throws IllegalArgumentException if {@code bound} is not positive
     */
    int nextInt(int bound);
}

