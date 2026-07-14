# 0004 — Randomness seam

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0001, 0003

## Pre-warned notes
none

## Scope
Introduce the single randomness seam all shuffling flows through, per
[ADR-0003](../docs/adr/0003-injectable-secure-randomness.md),
[architecture.md](../docs/architecture.md) §Randomness & Shuffle Boundary, and
[conventions.md](../docs/conventions.md) §Randomness Usage Rule. Deliver, in
`core.random`: the `RandomSource` abstraction; `SecureRandomSource` (production,
backed by `java.security.SecureRandom`); and `SeededRandomSource` (test-only,
deterministic PRNG from a fixed seed). Define the minimal `RandomSource`
operations shuffling needs (e.g. bounded integer generation supporting a
Fisher–Yates swap). The actual Fisher–Yates shuffle lives with `Deck`/`Shoe`
(story 0006); this story provides only the source. `java.util.Random`,
`Math.random()`, and `Collections.shuffle` must not appear. Lands in
`core.random`.

## Out of scope
- `Deck`/`Shoe` and the Fisher–Yates shuffle itself (story 0006).
- Any card types (story 0005).

## Acceptance Criteria
- [x] `RandomSource` interface plus `SecureRandomSource` and
      `SeededRandomSource` implementations exist in `core.random`.
- [x] `SecureRandomSource` is backed by `java.security.SecureRandom`; no use of
      `java.util.Random` / `Math.random()` / `Collections.shuffle` anywhere.
- [x] `SeededRandomSource` with a fixed seed produces a reproducible sequence
      (asserted deterministically in tests).
- [x] An ArchUnit or test guard flags any reference to the prohibited RNG APIs
      in `main` sources (may be added here or extend the 0002 scaffold).
- [x] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [x] Tests added/updated (unit-first; determinism test uses `SeededRandomSource`).
- [x] **Best-in-class Observability (library-appropriate):**
      - SLF4J class loggers where useful (DEBUG/TRACE only; quiet by default).
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/architecture.md §Randomness & Shuffle Boundary`,
  `docs/conventions.md §Randomness Usage Rule`, `docs/nfrs.md §Security & Randomness`
- ADRs implemented or impacted: `docs/adr/0003-injectable-secure-randomness.md`

## Definition of Done — commit message
```
feat(core): add injectable RandomSource seam for shuffling

Add core.random RandomSource with SecureRandomSource (production,
java.security.SecureRandom) and test-only SeededRandomSource for
reproducible shuffles, forbidding java.util.Random/Math.random/
Collections.shuffle, per ADR-0003 and docs/conventions.md (Randomness).
```
