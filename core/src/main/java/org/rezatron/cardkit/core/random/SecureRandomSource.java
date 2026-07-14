package org.rezatron.cardkit.core.random;

import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Production {@link RandomSource} backed by {@code java.security.SecureRandom}.
 *
 * <p>This is the only {@link RandomSource} implementation that should be used in
 * production code paths. It delegates every call to a single shared
 * {@link SecureRandom} instance; {@code SecureRandom} is thread-safe, so this
 * class is safe for concurrent use without additional synchronisation.</p>
 *
 * <h2>Seeding</h2>
 * <p>The underlying {@link SecureRandom} is created with the JVM's default
 * algorithm (typically {@code NativePRNGNonBlocking} on POSIX or
 * {@code Windows-PRNG} on Windows). No explicit seed is supplied — the JVM
 * self-seeds from the OS entropy pool. This satisfies the brief's requirement
 * for cryptographically secure shuffles.</p>
 *
 * <h2>Observability</h2>
 * <p>A single DEBUG log is emitted on construction, recording the algorithm
 * selected by the JVM. This aids diagnostics without producing noise during
 * normal operation.</p>
 *
 * @see SeededRandomSource
 */
public final class SecureRandomSource implements RandomSource {

    private static final Logger log = LoggerFactory.getLogger(SecureRandomSource.class);

    private final SecureRandom delegate;

    /**
     * Creates a new {@code SecureRandomSource} using the JVM's default
     * {@link SecureRandom} algorithm.
     */
    public SecureRandomSource() {
        this.delegate = new SecureRandom();
        log.debug("SecureRandomSource initialised; algorithm={}", delegate.getAlgorithm());
    }

    /**
     * Returns a cryptographically secure uniformly distributed random
     * {@code int} in {@code [0, bound)}.
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

