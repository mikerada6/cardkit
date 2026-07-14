# 0013 — Poker session (GameSession)

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0006, 0008, 0012

## Pre-warned notes
none

## Scope
Implement poker round orchestration per [data-model.md](../docs/data-model.md)
§Session State: `GameSession` and §Relationships & Ownership, and
[api-overview.md](../docs/api-overview.md) §Primary Workflows (workflow 1).
Deliver in `games.session`: `GameSession` — created for a `PokerVariant` and a
dealing source (`Deck`/`Shoe`); `seat(Player)`; `dealHoleCards()` (variant-aware
count: 5 for draw, 2 for Hold'em, 4 for Omaha); `dealBoard()` (flop/turn/river,
up to 5 **shared** board cards — the board is owned by the session, never copied
into player hands); `players()` / `board()` accessors; and `settleShowdown(evaluator)`
which evaluates each player's hole cards against the shared board and returns the
winner(s), splitting a hi/lo pot for Omaha Hi-Lo. Acting out of phase (e.g.
showdown before dealing) throws a domain exception. Owns the only cross-participant
state.

## Out of scope
- Betting/pot sizing and chip movement — **out of scope this phase** (brief);
  `settleShowdown` reports winners/splits, it does not move chips.
- The evaluators/rankers themselves (stories 0009–0012, reused here).
- Blackjack (stories 0014, 0015).

## Open questions
- **`Showdown` result type is undeclared in `docs/data-model.md`.**
  `api-overview.md` workflow 1 names `Showdown result = session.settleShowdown(evaluator)`,
  but `data-model.md`'s type catalog never defines a `Showdown` type (docs audit
  finding **F-004**, resolution "Skipped by user"). This story must define the
  concrete shape of the settlement result (winner(s) + hi/lo split) during
  implementation and record it as a **`## Design drift`** pointer so
  `revise-design` can reconcile `data-model.md`. Do not silently invent it into
  the docs.

## Acceptance Criteria
- [ ] `GameSession` in `games.session` supports `seat`, variant-aware
      `dealHoleCards`, `dealBoard` (0–5 shared board cards owned by the session),
      `players()`, `board()`, and `settleShowdown(evaluator)`.
- [ ] The community board is shared table state — a player's evaluable hand is
      their hole cards + the shared board, combined at evaluation time (never
      copied per player); verified by test.
- [ ] `settleShowdown` returns correct winner(s) for a high-only variant and a
      correct hi/lo split (including scoop) for OMAHA_HI_LO, using the evaluators.
- [ ] Out-of-phase actions throw the documented domain exception.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit-first; all deals use `SeededRandomSource`).
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE for phase transitions/deals; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.
- [ ] Settlement result (`Showdown`) shape recorded under `## Design drift` for
      `revise-design` (see Open questions).

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Session State: GameSession`,
  `docs/data-model.md §Relationships & Ownership`, `docs/api-overview.md §Primary Workflows`
- ADRs implemented or impacted: `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
feat(games): add GameSession poker round orchestration

Add games.session GameSession: seat, variant-aware dealHoleCards, shared
dealBoard, and settleShowdown returning winners with hi/lo split for Omaha,
owning the community board, per docs/data-model.md (Session State) and
docs/api-overview.md. Settlement result shape flagged for revise-design.
```
