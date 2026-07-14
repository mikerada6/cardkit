# 3. Injectable, cryptographically secure randomness for shuffling

Date: 2026-07-14

## Status

Accepted

## Context

The brief mandates that shuffling use a **cryptographically secure random
source** (`java.security.SecureRandom`), explicitly **not** `java.util.Random`.
At the same time, correctness is the primary bar and tests must be **fully
offline and deterministic** — many evaluation and round tests need a reproducible
shuffle to assert specific deals. `SecureRandom` is (correctly) not reproducible,
so using it directly inside `Deck`/`Shoe` would make deterministic testing
impossible and would scatter RNG construction across the code.

## Decision

Introduce a single randomness **seam**: a `RandomSource` abstraction in
`core.random`.

- `Deck` and `Shoe` accept an injected `RandomSource` and **never** construct
  their own RNG. Shuffling is an explicit **Fisher–Yates** over the
  `RandomSource`.
- **`SecureRandomSource`** — backed by `java.security.SecureRandom` — is the
  **production** implementation and the default consumers use.
- **`SeededRandomSource`** — a deterministic PRNG created with a fixed seed — is
  used **only in tests** to make shuffles reproducible.

Direct use of `java.util.Random`, `Math.random()`, or `Collections.shuffle`
(which uses an internal `Random`) is prohibited by convention (see
`conventions.md`).

## Consequences

**Positive**

- Production shuffles are cryptographically secure, satisfying the brief.
- Tests are deterministic and offline via `SeededRandomSource`.
- One auditable seam for all non-determinism; easy to verify no insecure RNG is
  used in production paths.

**Negative / trade-offs**

- `SeededRandomSource` must never reach a production code path; this is guarded
  by convention and code review rather than the type system.
- `SecureRandom` can incur first-use seeding cost on some platforms; negligible
  for this library's usage pattern.

**Alternatives considered**

- *Use `SecureRandom` directly in `Deck`/`Shoe`* — rejected: prevents
  deterministic tests and duplicates RNG wiring.
- *A test-only boolean/flag to switch RNG* — rejected: an injected abstraction is
  cleaner, avoids production branches, and makes the insecure path
  unrepresentable in production wiring.
