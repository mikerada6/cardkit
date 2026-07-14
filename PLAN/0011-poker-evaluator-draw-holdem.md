# 0011 — Poker evaluator: draw + Hold'em

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0009

## Pre-warned notes
none

## Scope
Implement the variant-selection layer for the high-only variants per
[ADR-0002](../docs/adr/0002-variant-aware-hand-evaluation.md) (decision point 3)
and [api-overview.md](../docs/api-overview.md) §Key Public Types and §Primary
Workflows. Deliver in `games.poker`: `PokerVariant` (enum with at least
FIVE_CARD_DRAW, TEXAS_HOLDEM, OMAHA_HI_LO — the Omaha *strategy* lands in story
0012); `HandEvaluator` with `forVariant(PokerVariant)` and `evaluate(holeCards,
board)` returning a single `HandRank`; and the strategies for the two high-only
variants: FIVE_CARD_DRAW ranks the 5 held cards directly; TEXAS_HOLDEM returns
the best `HandRank` over the `C(7,5) = 21` five-card subsets of {hole ∪ board},
any mix (including "playing the board"). The variant strategy — not the caller —
owns the selection rule. `HandEvaluator`/`DecisionStrategy` are expected to be
**stateless and reusable** (api-overview §Threading). Also introduce the
`PokerHand` hole-card container (data-model.md §`PokerHand`) if needed to carry
hole cards; it never scores itself.

## Out of scope
- Omaha Hi-Lo strategy, `evaluateHighLow`, and `HighLowResult` (story 0012).
- Session orchestration / dealing / showdown (story 0013).

## Acceptance Criteria
- [ ] `PokerVariant` enum and `HandEvaluator.forVariant(...)` /
      `evaluate(hole, board)` exist in `games.poker`; the evaluator is stateless
      and reusable across calls.
- [ ] FIVE_CARD_DRAW evaluates the 5 held cards to the correct `HandRank`.
- [ ] TEXAS_HOLDEM returns the best of the 21 five-card subsets of {2 hole ∪ up
      to 5 board}; vectors include "play the board" (both hole cards unused) and
      cases where the best hand mixes hole+board.
- [ ] The selection rule lives in the strategy, not the caller (verified by review).
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit vectors first) covering both variants and edge selections.
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for selection/evaluation detail; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/api-overview.md §Key Public Types & Entry Points`,
  `docs/api-overview.md §Primary Workflows`, `docs/data-model.md §Per-Variant Deal Shape`
- ADRs implemented or impacted: `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
feat(games): add PokerVariant + HandEvaluator for draw and Hold'em

Add games.poker PokerVariant enum and stateless HandEvaluator.forVariant/
evaluate: FIVE_CARD_DRAW (5 held cards) and TEXAS_HOLDEM (best of C(7,5)=21
subsets, incl. playing the board), with selection owned by the strategy,
per ADR-0002 and docs/api-overview.md.
```
