package org.rezatron.cardkit.core.random;

import java.util.SplittableRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <strong>Test-only</strong> deterministic {@link RandomSource} backed by
 * {@link SplittableRandom} with a fixed seed.
 *
 * <p><strong>WARNING — never wire this into a production code path.</strong>
 * It exists solely to make shuffle-dependent tests reproducible and offline.
 * See <a href="../../../../../../../../docs/adr/0003-injectable-secure-randomness.md">ADR-0003</a>
 * and {@code docs/conventions.md §Randomness Usage Rule}.</p>
 *
 * <h2>Determinism guarantee</h2>
 * <p>Two instances constructed with the same {@code seed} will produce
 * identical sequences of {@link #nextInt(int)} calls, regardless of JVM
 * version or platform. This allows tests to assert specific card orderings
 * after a shuffle.</p>
 *
 * <h2>Why {@code SplittableRandom} and not {@code java.util.Random}?</h2>
 * <p>{@code java.util.Random} is explicitly prohibited by convention. By contrast,
 * {@link SplittableRandom} is a high-quality, non-synchronized, non-linear PRNG
 * that is not covered by the prohibition and is well-suited for single-threaded
 * deterministic use in tests.</p>
 *
 * @see SecureRandomSource
 */
public final class SeededRandomSource implements RandomSource {

    private static final Logger log = LoggerFactory.getLogger(SeededRandomSource.class);

    private final SplittableRandom delegate;
    private final long seed;

    /**
     * Creates a {@code SeededRandomSource} with the given fixed seed.
     *
     * @param seed the seed value; any {@code long} is valid. The same seed
     *             will always produce the same sequence of {@link #nextInt(int)}
     *             values.
     */
    public SeededRandomSource(long seed) {
        this.seed = seed;
        this.delegate = new SplittableRandom(seed);
        log.debug("SeededRandomSource initialised [TEST-ONLY]; seed={}", seed);
    }

    /**
     * Returns the seed supplied at construction. Useful for reproducing a
     * failing test by recording and replaying the seed.
     *
     * @return the seed passed to {@link #SeededRandomSource(long)}
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Returns a deterministic uniformly distributed {@code int} in
     * {@code [0, bound)}.
     *
     * @param bound the exclusive upper bound; must be positive
     * @return a value {@code r} satisfying {@code 0 <= r < bound}
     * @throws IllegalArgumentException if {@code bound <= 0}
     */
    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive, got: " + bound);
        }
        return delegate.nextInt(bound);
    }
}

