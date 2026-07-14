# 0010 — Poker low ranker (ace-to-five, 8-or-better)

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** 30-60%
**Depends on:** 0009

## Pre-warned notes
none

## Scope
Implement the ace-to-five low ranker per
[ADR-0002](../docs/adr/0002-variant-aware-hand-evaluation.md) (decision point 2)
and [data-model.md](../docs/data-model.md) §Evaluation Result Types (`LowHandRank`).
Deliver in `games.poker`: `LowHandRank` (immutable, `Comparable`) and the
function that turns a 5-card hand into a low rank under **ace-to-five** rules —
aces low, straights and flushes ignored, with the **8-or-better** qualifier: the
hand qualifies only if it has five distinct ranks all ≤ 8. The best possible low
is `5-4-3-2-A` (the "wheel"). Non-qualifying hands are modelled as absence
(surfaced as `Optional`/no-low by the caller in story 0012), not exceptions.
Impl may live under `games.poker.internal`; `LowHandRank` is a public contract
type. Builds directly on the card strength model from story 0005 (ace-low context).

## Out of scope
- Combining high + low into `HighLowResult` and the Omaha 2+3 enumeration
  (story 0012) — this story ranks a single concrete 5-card low only.
- The high 5-card ranker (story 0009).

## Acceptance Criteria
- [ ] `LowHandRank` (Comparable) and the ace-to-five ranking function exist in
      `games.poker`; straights/flushes are ignored; aces count low.
- [ ] The **8-or-better** qualifier is enforced: a hand qualifies only with five
      distinct ranks all ≤ 8; otherwise it is reported as non-qualifying (no low).
- [ ] **Canonical low vectors** cover: the `5-4-3-2-A` wheel as best low,
      qualifying vs. non-qualifying (e.g. a pair disqualifies, a 9-high fails the
      8-or-better), and correct ordering between two qualifying lows (compare
      from the highest card down).
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit vectors first).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for low-ranking detail; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Evaluation Result Types` (LowHandRank),
  `docs/nfrs.md §Correctness (primary bar)`
- ADRs implemented or impacted: `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
feat(games): add ace-to-five low ranker (8-or-better)

Add games.poker Comparable LowHandRank and the ace-to-five ranking
function (aces low, straights/flushes ignored, 8-or-better qualifier,
5-4-3-2-A wheel as best low), with qualifying/non-qualifying/wheel
vectors, per ADR-0002 and docs/data-model.md (Evaluation Result Types).
```
