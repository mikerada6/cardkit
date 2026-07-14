# Conventions

## Purpose & Scope

Project-specific conventions that **extend or refine** the global engineering
standards in `~/.claude/CLAUDE.md`. The global standards are the base and are
not restated here; this file records only what is specific to this library, or
where the library deviates (with a pointer to the governing ADR).

## Java Language Level & Features

- **Java 25 (latest LTS).** Prefer modern language features where they sharpen
  the domain model:
  - `record` for immutable value objects (`Card`, evaluation results).
  - `enum` / `sealed` types for closed sets: `Suit`, `Rank`, `HandCategory`,
    `PokerVariant`, `RoundOutcome`, `Action`.
  - `switch` pattern matching for variant-aware evaluation dispatch.

## Immutability & Null Policy

- Value objects are immutable; mutable domain objects (`Deck`, `Shoe`, `Hand`,
  session/round types) confine their mutation to well-defined lifecycle methods.
- **No `null` across the public boundary.** Return `Optional<T>` for
  "absent" (e.g. an unqualified Omaha low); never return `null` collections
  (return empty). Validate arguments with `Objects.requireNonNull`, and validate
  domain constraints (hand/board sizes, duplicate cards) at entry.

## Package Naming & Organization; Public vs. Internal

- Base package `org.rezatron.cardkit`; `org.rezatron.cardkit.core.*` and
  `org.rezatron.cardkit.games.*` per [architecture.md](architecture.md).
- Implementation detail lives in `.internal` subpackages and, where
  `module-info.java` is used, is **not** `exports`ed. Only supported packages
  form the API contract ([api-overview.md](api-overview.md)).

## Domain Naming

Use consistent domain vocabulary throughout code, tests, and docs: `rank`,
`suit`, `holeCards`, `board` / `community`, `handCategory`, `handRank`,
`highLow` / `lowHandRank`, `shoe`, `bankroll`, `push`. Avoid synonyms
(`community` vs. `board` should not both appear as public names for the same
concept — pick one per surface and stay consistent).

## Randomness Usage Rule

- **Never** call `new java.util.Random()`, `Math.random()`, or
  `Collections.shuffle(list)` (which uses an internal `Random`). All randomness
  is obtained from an injected `RandomSource`; production uses
  `SecureRandomSource`. Shuffling is an explicit Fisher–Yates over the
  `RandomSource`. See [ADR-0003](adr/0003-injectable-secure-randomness.md).
- `SeededRandomSource` is **test-only**; it must never be wired into a
  production code path.

## Chip / Bankroll Representation

- Bankroll is a whole-number **chip count** stored as a primitive `long` (chip
  units, non-negative). There are no fractional chips and no betting behavior
  this phase, so a primitive suffices. If money semantics grow in the follow-on
  betting/pot phase, introduce a dedicated `Chips` value object then — do not
  pre-build it now.

## Testing Conventions

- Test method naming: `unitOfWork_condition_expectedResult`.
- **Hand-evaluation test vectors** are first-class fixtures: maintain named
  vectors for each poker category and tie-break, Omaha 2+3 enumeration cases,
  Hi-Lo qualifying/non-qualifying and wheel lows, and blackjack soft/hard/bust/
  natural/dealer-edge cases (see [nfrs.md](nfrs.md)).
- Shuffle-dependent tests inject a fixed-seed `SeededRandomSource` for
  determinism; never assert specific orderings against `SecureRandom`.

## Logging Conventions

- Log through the **SLF4J API** only; one logger per class
  (`LoggerFactory.getLogger(Xxx.class)`). The library **binds no logging
  implementation** — the consumer supplies one ([nfrs.md](nfrs.md)).
- The library stays quiet by default: internal detail at DEBUG/TRACE; no
  INFO-level output during normal operation.
- **Log the interesting paths, stay silent on the hot happy path.** Mutable/
  behavioural types (`Deck`, `Shoe`, `RandomSource` impls) log lifecycle events
  at DEBUG and per-card deals at TRACE. Boundary/precondition helpers (`Require`)
  log only on the **rejection** branch (DEBUG, naming the failed parameter)
  before throwing — so the happy path allocates and logs nothing.
- **Do not log in value objects or exception constructors.** Immutable value
  types (`Card`, `Rank`, `Suit`) sit on hot construction/evaluation paths and
  carry no logger. Domain exceptions (`core.error`) never self-log; the throwing
  call site (or the consumer's handler) owns that, avoiding double-logging.

## Exceptions & Error Types

- **Game outcomes are return values**, not exceptions (`RoundOutcome`,
  `HighLowResult`, `Optional`).
- **Rule/programming violations throw unchecked exceptions** (e.g. dealing from
  an exhausted source, acting out of phase, invalid hand construction). Prefer a
  small set of descriptive domain exception types over generic
  `RuntimeException`. Each public method documents what it throws.

## ADR / Deviations Index

- [ADR-0001](adr/0001-standalone-java-library-not-spring-service.md) — standalone
  library instead of a Spring Boot/Angular service (no REST/OpenAPI, no
  persistence, observability via facade only).
- [ADR-0002](adr/0002-variant-aware-hand-evaluation.md) — variant-aware hand
  evaluation strategy.
- [ADR-0003](adr/0003-injectable-secure-randomness.md) — injectable,
  cryptographically secure randomness for shuffling.
