# Non-Functional Requirements

## Purpose & Scope

Quality targets for the library. The brief sets **correctness of game rules and
hand evaluation as the primary bar** and intentionally leaves numeric
performance/scale targets unset this phase. NFRs are expressed in a
library-appropriate way — the global standards' service-oriented targets
(availability SLOs, metrics/tracing infrastructure, capacity planning) largely
do not apply and are handled as deviations under
[ADR-0001](adr/0001-standalone-java-library-not-spring-service.md).

## Correctness (primary bar)

- Rule fidelity is the top priority: poker ranking across all variants,
  Omaha Hi-Lo high/low split (exactly 2+3, 8-or-better low), and blackjack
  round resolution must match published rules exactly.
- Backed by **canonical test vectors**: known 5-card rankings and tie-breaks;
  Hold'em best-5-of-7; Omaha 2+3 enumeration including "play only 2 hole cards"
  traps; Hi-Lo scenarios (qualifying vs. non-qualifying low, the `5-4-3-2-A`
  wheel, scoop vs. split); blackjack soft/hard totals, bust, natural blackjack,
  and dealer hit-≤16/stand-≥17 edges.
- Structural invariants asserted (see [data-model.md](data-model.md)): a fresh
  deck has 52 distinct cards; a shuffle is a permutation (multiset preserved); a
  shoe of *N* decks has `N × 52` cards; dealt cards are disjoint from the
  remaining source.

## Testing

- **Fully offline unit tests** — zero network, zero external infra — per the
  global testing standards (not restated here).
- **Determinism:** any test touching shuffling injects a `SeededRandomSource`
  ([ADR-0003](adr/0003-injectable-secure-randomness.md)) so results are
  reproducible; production `SecureRandom` is never exercised for assertions on
  specific orderings.
- Coverage expectation follows the global standard; evaluation and round logic
  are the highest-value targets and should be exhaustively vectored.

## Security & Randomness

- **Shuffling must use a cryptographically secure source** — `SecureRandom` via
  `SecureRandomSource`, never `java.util.Random` or `Math.random()`. Mandated by
  the brief; rationale in
  [ADR-0003](adr/0003-injectable-secure-randomness.md).
- No other security surface exists: no authentication (it is a library, not a
  multi-tenant service), no network, no persistence, no untrusted external
  input. The only inputs are consumer method calls, which are **validated at the
  public boundary** (null checks, hand/board size and duplicate-card checks).

## Observability (library-appropriate)

- **Logging via the SLF4J API facade only.** The library depends on
  `slf4j-api` and **binds no implementation** — the consuming application
  supplies logback/log4j2/etc. The library logs sparingly (DEBUG/TRACE for
  internal detail); it does not emit INFO-level chatter by default.
- **No metrics or tracing infrastructure** (no Micrometer, Loki, Grafana,
  OpenTelemetry wiring). A library must not impose an observability backend on
  its host; consumers instrument at their level. This is a deliberate deviation
  from the global observability defaults, recorded under
  [ADR-0001](adr/0001-standalone-java-library-not-spring-service.md).

## Performance & Scale

- **`TBD — open question`:** no numeric latency/throughput/scale targets this
  phase (per the brief).
- For context, evaluation cost is bounded and tiny: Hold'em best-5-of-7 is
  `C(7,5) = 21` candidate hands; Omaha is `C(4,2) × C(5,3) = 60`. No performance
  optimization (lookup tables, perfect-hash evaluators) is undertaken now; that
  is revisited only if real targets appear.

## Maintainability & Extensibility

- Two-module separation (`core` vs. `games`) keeps primitives stable while games
  evolve; new variants/games are additive (see [architecture.md](architecture.md)).
- Design decisions are traceable through the ADR log.

## Portability

- **Java 25 (latest LTS)** baseline; pure JVM, no native or platform-specific
  dependencies. Behavior is deterministic across platforms when a
  `SeededRandomSource` is supplied (production `SecureRandom` output is,
  correctly, not reproducible).

## Open Questions / TBD

- **`TBD — open question`:** numeric performance and capacity targets (deferred).
- **`TBD — open question`:** whether any structured, machine-readable event
  output (beyond SLF4J logging) is wanted by early consumers.
