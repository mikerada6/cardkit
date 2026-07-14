# 0012 — Poker evaluator: Omaha Hi-Lo

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0010, 0011

## Pre-warned notes
none

## Scope
Implement the Omaha Hi-Lo strategy per
[ADR-0002](../docs/adr/0002-variant-aware-hand-evaluation.md) (decision point 3,
Omaha case), [data-model.md](../docs/data-model.md) §Per-Variant Deal Shape and
§Evaluation Result Types (`HighLowResult`), and
[api-overview.md](../docs/api-overview.md) workflow 1. Deliver in `games.poker`:
the OMAHA_HI_LO `HandEvaluator` strategy and `evaluateHighLow(holeCards, board)`
returning a `HighLowResult(high: HandRank, low: Optional<LowHandRank>)`. The
strategy enumerates the `C(4,2) × C(5,3) = 60` legal combinations of **exactly 2
of the 4 hole cards + exactly 3 of the 5 board cards**, takes the best `HandRank`
for the high and, **independently**, the best qualifying `LowHandRank` for the
low (the two need not use the same hole cards). The low is present only if an
8-or-better low qualifies; otherwise `Optional.empty()`. The model stores 4 hole
/ up to 5 board cards; the exact-2+3 constraint is enforced here, not by what was
dealt. `HighLowResult` is a public contract type.

## Out of scope
- Session orchestration, dealing, and pot splitting/showdown (story 0013 —
  `settleShowdown` consumes `HighLowResult`).
- The high and low rankers themselves (stories 0009, 0010 — reused here).

## Acceptance Criteria
- [ ] OMAHA_HI_LO strategy and `evaluateHighLow(hole, board)` exist in
      `games.poker`, returning `HighLowResult(HandRank, Optional<LowHandRank>)`.
- [ ] Exactly the 60 legal 2-hole+3-board combinations are considered; high and
      low are selected **independently** across them.
- [ ] **Canonical vectors** cover: the "play only 2 hole cards" trap (a hand that
      looks like a flush using 3+ hole cards is not valid), a qualifying low
      present vs. a non-qualifying board (low absent), independent hi/low hole
      selection, and a scoop (same hand wins hi and lo) vs. split.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit vectors first).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for enumeration/selection detail; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Per-Variant Deal Shape`,
  `docs/data-model.md §Evaluation Result Types` (HighLowResult),
  `docs/api-overview.md §Primary Workflows` (workflow 1)
- ADRs implemented or impacted: `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
feat(games): add Omaha Hi-Lo evaluator with independent high/low

Add the OMAHA_HI_LO strategy and evaluateHighLow returning
HighLowResult(HandRank, Optional<LowHandRank>) over the 60 legal 2-hole+
3-board combinations, selecting high and 8-or-better low independently,
with 2-hole-only trap and scoop/split vectors, per ADR-0002 and
docs/data-model.md (Per-Variant Deal Shape, Evaluation Result Types).
```
