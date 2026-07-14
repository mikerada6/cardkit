# 0016 — AiPlayer + DecisionStrategy stub

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0008

## Pre-warned notes
none

## Scope
Provide the pluggable bot seam per [data-model.md](../docs/data-model.md)
§Participants (`AiPlayer`), [architecture.md](../docs/architecture.md)
§Extensibility Model, and [api-overview.md](../docs/api-overview.md) §Extension
Points. Deliver in `games.participant`: `DecisionStrategy` (interface for
choosing an action; stateless/reusable per api-overview §Threading), a **default
stub** implementation (a trivial deterministic policy — the real decision model
is explicitly a follow-on-phase concern), and `AiPlayer` (a computer-controlled
`Player` that delegates its action to a `DecisionStrategy`). This is the seam
only; no game rewires itself around AI decisions this phase. Lands in
`games.participant`.

## Out of scope
- Any non-trivial AI/decision logic — **stub only this phase** (brief non-goal;
  the eventual decision model is deferred to the betting/pot phase).
- Wiring `AiPlayer` into `BlackjackRound`/`GameSession` decision loops beyond
  what the stub demonstrates.

## Acceptance Criteria
- [ ] `DecisionStrategy` interface, a default stub implementation, and `AiPlayer`
      exist in `games.participant`; `AiPlayer` delegates its action to its strategy.
- [ ] `DecisionStrategy` implementations are stateless and reusable (verified by
      sharing one instance across two `AiPlayer`s in a test).
- [ ] The stub is deterministic (no RNG, or an injected `RandomSource` if any) so
      tests are reproducible.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit-first): delegation, stub determinism, reuse.
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for delegated decisions if useful; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Participants` (AiPlayer),
  `docs/architecture.md §Extensibility Model`, `docs/api-overview.md §Extension Points`
- ADRs implemented or impacted: none

## Definition of Done — commit message
```
feat(games): add AiPlayer and stub DecisionStrategy seam

Add games.participant DecisionStrategy interface, a deterministic default
stub, and AiPlayer delegating its action to the strategy, providing the
pluggable bot extension point (stub only this phase), per
docs/data-model.md (Participants) and docs/architecture.md (Extensibility).
```
