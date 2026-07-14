# 2. Variant-aware poker hand evaluation

Date: 2026-07-14

## Status

Accepted

## Context

The library must evaluate and rank poker hands across multiple variants without
hard-coding 5-card logic (an explicit brief goal). The variants differ mainly in
**which cards may form the scored 5-card hand**:

- **5-card draw** — the player's 5 cards are the hand.
- **Texas Hold'em** — best 5 of the 7 available (2 hole + up to 5 community), any
  mix, including "playing the board".
- **Omaha Hi-Lo** — a made hand must use **exactly 2 of the 4 hole cards and
  exactly 3 of the 5 community cards**. It is also a **split** game: the pot is
  shared between the best **high** hand and the best **8-or-better low** hand,
  and the two need not use the same hole cards.

A single hard-coded 5-card evaluator cannot express Hold'em's best-of-7 selection
or Omaha's exact-2+3 constraint and independent high/low selection. The model
must also **store the extra cards** (Omaha's 4 hole, the up-to-5 board) while
enforcing the selection rule only at evaluation time.

## Decision

Model evaluation as a **strategy selected by `PokerVariant`**, layered on a
shared 5-card ranker:

1. A **5-card ranker** turns any concrete 5-card hand into a comparable
   `HandRank` (`HandCategory` + ordered tie-break kickers). This is variant
   independent.
2. A **low ranker** turns a 5-card hand into a `LowHandRank` under ace-to-five
   rules with the **8-or-better** qualifier (five distinct ranks ≤ 8; aces low;
   straights/flushes ignored; best low is `5-4-3-2-A`).
3. A **`HandEvaluator` strategy per variant** owns the card-selection rule and
   feeds candidate 5-card sets to the rankers:
   - draw → the 5 held cards, ranked directly;
   - Hold'em → best `HandRank` over the `C(7,5) = 21` five-card subsets of
     {hole ∪ board};
   - Omaha Hi-Lo → enumerate the `C(4,2) × C(5,3) = 60` legal 2-hole+3-board
     combinations; take the best `HandRank` for the high and, **independently**,
     the best qualifying `LowHandRank` for the low, returning a
     `HighLowResult(high, Optional<low>)`.

`evaluate(hole, board)` returns a `HandRank` for high-only variants;
`evaluateHighLow(hole, board)` returns a `HighLowResult` for Hi-Lo.

## Consequences

**Positive**

- Adding a variant is additive: a new `PokerVariant` + strategy, with no change
  to `core` primitives or the shared rankers.
- Omaha's exact-2+3 rule is enforced centrally in one strategy; storing 4 hole /
  5 board cards is fully supported, and the constraint cannot be bypassed by
  callers.
- Independent high/low selection falls out naturally from evaluating both rankers
  over the same 60 combinations.

**Negative / trade-offs**

- Combinatorial enumeration (21 for Hold'em, 60 for Omaha per player). This is
  negligible and acceptable given the correctness-first, no-performance-target
  scope; see `nfrs.md`.
- The `HandRank`/`LowHandRank` tie-break ordering must be exactly correct and
  stable; this is pinned by canonical test vectors.

**Alternatives considered**

- *Single hard-coded 5-card evaluator* — rejected: cannot express Hold'em or
  Omaha selection rules.
- *Precomputed lookup tables / perfect-hash 7-card evaluators* — rejected as
  premature optimization; there is no performance target and enumeration is
  cheap. Revisit only if real targets appear.
