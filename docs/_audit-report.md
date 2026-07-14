# Docs Audit Report
_Generated: 2026-07-14T00:00:00Z_
_Files audited: 8_

## Summary
- Contradictions: 3
- Best-practice gaps: 0
- Global-standard deviations: 0
- Style nits: 1

## Findings

### F-001 — Blackjack house participant is named/located two different ways
- **Category:** Contradiction
- **Severity:** Medium
- **Files:** `docs/architecture.md`, `docs/data-model.md`, `docs/api-overview.md`
- **Problem:** `architecture.md`'s component overview introduces a `BlackjackDealer` type in the `games.blackjack` package *and* a separate `Dealer` in `games.participant`. Every other doc knows only `Dealer` (in `games.participant`) and attaches the fixed blackjack rules to that single `Dealer`. It is unclear whether the blackjack house participant is `BlackjackDealer` or `Dealer`, and which package owns its rule-bound behavior. Implementers reading `architecture.md` vs. `data-model.md`/`api-overview.md` would build two different type hierarchies.
- **Evidence:** `architecture.md:54` — `blackjack   BlackjackHand, BlackjackDealer (fixed rules), BlackjackRound,` and `architecture.md:56` — `participant Dealer, AiPlayer, DecisionStrategy (stub-able)`; contrast `data-model.md:95-96` — "**`Dealer`** (`games`) — the house participant … For Blackjack: hit while total ≤ 16, stand at ≥ 17" and `api-overview.md:34` — `| `Dealer`, `AiPlayer`, `DecisionStrategy` | `games.participant` | House rules & pluggable bot decisions |`.
- **Recommended fix:** Pick one model. Simplest: drop `BlackjackDealer` from `architecture.md:54` and keep a single `Dealer` in `games.participant` carrying the fixed blackjack rules (matching `data-model.md`/`api-overview.md`). If a distinct `BlackjackDealer` is genuinely intended, add it to `data-model.md` and `api-overview.md` and state its relationship to `Dealer`.
- **Resolution:** Applied — removed `BlackjackDealer` from the `games.blackjack` listing in `architecture.md`; the single `Dealer` in `games.participant` now carries the blackjack rules.

### F-002 — `LowHandRank` and `HandCategory` omitted from the public value-object enumerations
- **Category:** Contradiction
- **Severity:** Low
- **Files:** `docs/api-overview.md`, `docs/data-model.md`
- **Problem:** `LowHandRank` and `HandCategory` are defined in `data-model.md` as immutable evaluation-result types, and `LowHandRank` appears directly in the documented public Omaha workflow (`Optional<LowHandRank> low = r.low()`). Yet both the `api-overview.md` "immutable and safe to share" value-object list and the `data-model.md` value-object summary enumerate only `HandRank`, `HighLowResult`, `RoundOutcome` — omitting `LowHandRank` and `HandCategory`. A reader taking those lists as the set of shareable value objects would miss two contract types.
- **Evidence:** `api-overview.md:20` — "Value objects (`Card`, `Rank`, `Suit`, `HandRank`, `HighLowResult`, `RoundOutcome`) are immutable and safe to share." and `api-overview.md:58` — `Optional<LowHandRank> low = r.low();`; `data-model.md:19` — "every evaluation result (`HandRank`, `HighLowResult`, `RoundOutcome`)" vs. `data-model.md:81` — "**`LowHandRank`** — immutable, `Comparable`."
- **Recommended fix:** Add `LowHandRank` and `HandCategory` to the value-object lists at `api-overview.md:20` and `data-model.md:19` (or reword the parentheticals to "e.g." so they read as examples rather than the complete set).
- **Resolution:** Applied — added `HandCategory` and `LowHandRank` to the immutable value-object enumerations in both `api-overview.md` and `data-model.md`.

### F-003 — Persistent dual naming `GameSession / Table` violates the project's own naming rule
- **Category:** Contradiction
- **Severity:** Medium
- **Files:** `docs/conventions.md`, `docs/architecture.md`, `docs/data-model.md`
- **Problem:** `conventions.md` explicitly forbids exposing two public names for one concept ("pick one per surface and stay consistent"). Both `architecture.md` and `data-model.md` refer to the session type throughout as "`GameSession` / `Table`", never resolving whether `Table` is a distinct public type or an alias for `GameSession`. `api-overview.md` uses only `GameSession`, so the double name is unresolved and contradicts the stated convention.
- **Evidence:** `conventions.md:41-42` — "(`community` vs. `board` should not both appear as public names for the same concept — pick one per surface and stay consistent)"; `architecture.md:57` — `session     GameSession / Table (participants + round state, owns board)`; `data-model.md:101` — "## Session State: `GameSession` / `Table`"; contrast `api-overview.md:32` which lists only `GameSession`.
- **Recommended fix:** Commit to one public name (`GameSession`, matching `api-overview.md`) across `architecture.md` and `data-model.md`. If `Table` must remain, state once that it is a documentation synonym / not a separate public type, rather than pairing both names everywhere.
- **Resolution:** Applied — replaced all `GameSession / Table` dual references with `GameSession` in `architecture.md` (2) and `data-model.md` (4), matching `api-overview.md`.

### F-004 — API workflow sketches reference undeclared public types (`Showdown`, `Action`)
- **Category:** Style
- **Severity:** Low
- **Files:** `docs/api-overview.md`, `docs/data-model.md`
- **Problem:** The illustrative workflows in `api-overview.md` name a `Showdown` result type (returned by `settleShowdown`) and an `Action` enum, but neither appears in `data-model.md`'s type catalog. `data-model.md` describes settlement as producing/using `HighLowResult` and never introduces a `Showdown` type; `Action` is listed as an enum in `conventions.md` but not modeled in `data-model.md`. Sketches are marked "illustrative, not final signatures," so this is a low-priority completeness nit rather than a contract error.
- **Evidence:** `api-overview.md:61` — `Showdown result = session.settleShowdown(evaluator);  // winner(s); splits hi/lo pot`; `api-overview.md:79` — `while (round.decide(p) == Action.HIT) {`; neither `Showdown` nor `Action` appears in `data-model.md` (cf. its "Evaluation Result Types" section, `data-model.md:72-87`).
- **Recommended fix:** Either add `Showdown` (poker settlement result) and `Action` (blackjack player action enum) to `data-model.md`'s type catalog, or add a one-line note near the sketches that settlement/action result types are finalized during implementation (consistent with the existing Open Question at `api-overview.md:133-135`).
- **Resolution:** Skipped by user.
