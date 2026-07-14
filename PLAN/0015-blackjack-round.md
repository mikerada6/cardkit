# 0015 — Blackjack round & resolution

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0006, 0008, 0014

## Pre-warned notes
none

## Scope
Implement blackjack round orchestration per [data-model.md](../docs/data-model.md)
§Participants (`Dealer`) and [api-overview.md](../docs/api-overview.md) §Primary
Workflows (workflow 2). Deliver `games.blackjack.BlackjackRound` —
`start(shoe, players)`, `dealInitial()` (two cards per player + dealer),
`decide(player)` / `hit(player)` (bust ends the turn) / `stand(player)`,
`resolveDealer()` (dealer hits ≤16, stands ≥17), and `settle()` returning
`Map<Player, RoundOutcome>`; plus `RoundOutcome` (enum WIN/LOSE/PUSH), the
`Action` enum (e.g. HIT/STAND) returned by `decide`, and `Dealer`
(`games.participant`) encapsulating the fixed house rules. Out-of-phase actions
throw a domain exception. Uses `BlackjackHand` (story 0014) for totals.

## Out of scope
- Betting/payout amounts and chip movement — **out of scope this phase**;
  `settle` reports WIN/LOSE/PUSH per player, it does not move chips.
- `AiPlayer` decision behavior (story 0016) — a human/consumer drives `decide`
  here; `AiPlayer` merely delegates to a `DecisionStrategy` later.

## Open questions
- **`Action` enum is not modelled in `docs/data-model.md`.** It is named in
  `docs/conventions.md` (closed-set enums) and the `api-overview.md` workflow
  sketch (`round.decide(p) == Action.HIT`) but absent from `data-model.md`'s type
  catalog (docs audit **F-004**, "Skipped by user"). Define the concrete `Action`
  values during implementation and record a **`## Design drift`** pointer for
  `revise-design`; do not silently patch the docs.
- **Blackjack soft-17 rule is deferred** (`data-model.md` §Open Questions): dealer
  stands on all 17 vs. hits soft 17. **Default to stand-on-all-17 and expose it
  as a rule flag** on `Dealer`/`BlackjackRound`; the definitive choice is
  deferred. Record the flag under `## Design drift` for `revise-design`.

## Acceptance Criteria
- [ ] `BlackjackRound`, `RoundOutcome` (WIN/LOSE/PUSH), `Action` enum, and
      `Dealer` exist; the workflow `start → dealInitial → decide/hit/stand →
      resolveDealer → settle` runs end to end.
- [ ] `resolveDealer` implements hit-≤16 / stand-≥17; the soft-17 behavior is
      controlled by a flag defaulting to **stand on all 17**.
- [ ] `settle` returns the correct `RoundOutcome` per player, including PUSH ties,
      player bust = LOSE, dealer bust = WIN, and natural-blackjack handling.
- [ ] **Dealer-edge vectors** cover hit-≤16/stand-≥17 boundaries and (via the
      flag) both soft-17 behaviors.
- [ ] Out-of-phase actions throw the documented domain exception.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit-first; all deals use `SeededRandomSource`).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for phase/deal/dealer-resolution detail; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.
- [ ] `Action` values and the soft-17 flag recorded under `## Design drift` for
      `revise-design` (see Open questions).

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Participants` (Dealer),
  `docs/api-overview.md §Primary Workflows` (workflow 2), `docs/nfrs.md §Correctness (primary bar)`
- ADRs implemented or impacted: none

## Definition of Done — commit message
```
feat(games): add BlackjackRound with dealer rules and settlement

Add games.blackjack BlackjackRound (dealInitial/decide/hit/stand/
resolveDealer/settle), RoundOutcome (WIN/LOSE/PUSH), Action enum, and
Dealer (hit<=16/stand>=17, soft-17 flag defaulting to stand-on-all-17),
per docs/data-model.md (Participants) and docs/api-overview.md. Action
shape and soft-17 flag flagged for revise-design.
```
