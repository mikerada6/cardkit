# 0014 — Blackjack hand & totals

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** 30-60%
**Depends on:** 0007

## Pre-warned notes
none

## Scope
Implement the blackjack hand per [data-model.md](../docs/data-model.md)
§`BlackjackHand`. Deliver in `games.blackjack`: `BlackjackHand` (building on the
generic `core.hand.Hand`) exposing derived state computed from its cards, not
stored independently: best **total** with aces counted as 1 or 11 ("soft" vs.
"hard"), `isBust` (> 21), and `isBlackjack` (natural 21 on the first two cards).
No round flow, dealer, or settlement here — only the hand and its derived
totals. Lands in `games.blackjack`.

## Out of scope
- `BlackjackRound`, `Dealer`, `Action`, `RoundOutcome`, settlement (story 0015).
- Poker hands/evaluators (stories 0009–0013).

## Acceptance Criteria
- [ ] `BlackjackHand` in `games.blackjack` computes the best total treating aces
      as 1 or 11 (soft vs. hard), and exposes `isBust` and `isBlackjack`.
- [ ] `isBlackjack` is true only for a natural 21 on the **first two** cards (not
      a 21 made by hitting).
- [ ] **Canonical vectors** cover: soft 17 (A+6) vs. hard 17, multi-ace totals
      (A+A = 12 or 2, A+A+9 soft/hard), a bust (> 21), and a natural blackjack.
- [ ] Derived state is computed from cards, not stored (verified by adding a card
      and re-reading the total).
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit vectors first).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for total computation if useful; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §BlackjackHand`, `docs/nfrs.md §Correctness (primary bar)`
- ADRs implemented or impacted: none

## Definition of Done — commit message
```
feat(games): add BlackjackHand with soft/hard totals

Add games.blackjack BlackjackHand computing best total (aces 1/11,
soft vs. hard), isBust (>21), and isBlackjack (natural 21 on first two
cards) from its cards, with soft/hard/bust/natural vectors, per
docs/data-model.md (BlackjackHand).
```
