# 0009 — Poker 5-card hand ranker

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0005, 0007, 0002

## Pre-warned notes
none

## Scope
Implement the variant-independent 5-card ranker per
[ADR-0002](../docs/adr/0002-variant-aware-hand-evaluation.md) (decision point 1),
[data-model.md](../docs/data-model.md) §Evaluation Result Types, and
[nfrs.md](../docs/nfrs.md) §Correctness. Deliver in `games.poker`: `HandCategory`
(enum weakest→strongest: HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH); `HandRank` (immutable,
`Comparable` — encodes the winning 5-card hand as a `HandCategory` plus ordered
tie-break kickers so same-category hands compare correctly); and the ranking
function that turns any concrete 5-card hand into a `HandRank`. Handle the
`A-2-3-4-5` "wheel" straight (ace low) and ace-high straights/royal flush (the
ace-high `STRAIGHT_FLUSH` case). Ranker impl may live under `games.poker.internal`;
`HandCategory`/`HandRank` are public contract types. **This is the first `games`
story consuming `core`** — it must prove the `games → core` module seam with a
cross-module integration test (Card → ranker → HandRank) under `-P it` and the
ArchUnit direction rule from story 0002.

## Out of scope
- Ace-to-five **low** ranking / `LowHandRank` (story 0010).
- Variant selection (best-of-7, Omaha 2+3) and `HandEvaluator` (stories 0011, 0012).
- `PokerHand` container and session orchestration (stories 0012/0013).

## Acceptance Criteria
- [ ] `HandCategory`, `HandRank` (Comparable), and the 5-card ranking function
      exist in `games.poker`; the ranker returns the correct category and
      kicker ordering for any 5 cards.
- [ ] **Canonical test vectors** cover every category and representative
      tie-breaks: e.g. two flushes decided by highest card down, two pairs by
      the higher pair then kicker, the `A-2-3-4-5` wheel ranking below `2-3-4-5-6`,
      and royal vs. king-high straight flush. `HandRank` comparison is stable
      and total across the vectors.
- [ ] Cross-module seam proven: an integration test in the `games` module builds
      `Card`s from `core` and ranks them end-to-end under `-P it`; ArchUnit
      confirms `games → core` (never reverse).
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] `./mvnw verify -P it` exits 0 (adds the cross-module integration test).
- [ ] Tests added/updated (unit vectors first; one IT for the seam).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for ranking detail; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001** (correctness is proven by vectors,
        not runtime metrics).

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
./mvnw -q verify -P it > it.log 2>&1 && tail -n5 it.log || { cat it.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Evaluation Result Types`,
  `docs/nfrs.md §Correctness (primary bar)`
- ADRs implemented or impacted: `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
feat(games): add variant-independent 5-card poker ranker

Add games.poker HandCategory, Comparable HandRank (category + tie-break
kickers), and the 5-card ranking function handling wheel and ace-high
straights, backed by canonical category/tie-break vectors; prove the
games -> core seam via an integration test, per ADR-0002 and
docs/nfrs.md (Correctness).
```
