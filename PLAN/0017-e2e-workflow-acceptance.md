# 0017 — End-to-end workflow acceptance tests

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0013, 0015, 0016

## Pre-warned notes
none

## Scope
Prove the two primary workflows end to end across both modules, per
[api-overview.md](../docs/api-overview.md) §Primary Workflows and
[nfrs.md](../docs/nfrs.md) §Correctness §Testing. Deliver cross-module
acceptance tests in the `games` module (`...games/acceptance/`) running under the
`-P it` lane (offline, deterministic via `SeededRandomSource` — **no
Testcontainers/Docker**, the library owns no infra): (1) **Poker** — deal +
evaluate an Omaha Hi-Lo hand through `GameSession` (seat players, deal 4 hole +
board, `evaluateHighLow`, `settleShowdown` producing the hi/lo split), asserting
the documented workflow-1 outcome; (2) **Blackjack** — a full round through
`BlackjackRound` (deal, player decisions incl. an `AiPlayer` delegating to its
stub strategy, dealer resolves, settle to `RoundOutcome`s), asserting workflow-2
outcomes. This is the library analogue of the closing smoke test. There is no
OpenAPI/HTTP schema to conform to (ADR-0001); the **substitute conformance check**
is that each workflow's public result validates against the shapes documented in
`api-overview.md` (the return types, `Optional` low presence/absence, WIN/LOSE/
PUSH per player). Cucumber is optional; plain JUnit acceptance tests under
failsafe are sufficient.

## Out of scope
- New domain behavior — this story only composes existing types into
  end-to-end scenarios and asserts outcomes.
- Performance/scale assertions — **no numeric targets this phase** (nfrs.md).

## Acceptance Criteria
- [ ] An Omaha Hi-Lo acceptance test drives `GameSession` end to end with a fixed
      `SeededRandomSource` and asserts a deterministic, correct hi/lo settlement
      (including a split or scoop case).
- [ ] A blackjack acceptance test drives `BlackjackRound` end to end (including an
      `AiPlayer` + stub `DecisionStrategy`) with a fixed seed and asserts the
      correct `RoundOutcome` per player.
- [ ] Each workflow's public result conforms to the shapes documented in
      `api-overview.md` (substitute for schema-conformance — no OpenAPI): correct
      result types, `Optional<LowHandRank>` presence/absence, per-player WIN/LOSE/PUSH.
- [ ] Both tests run under `-P it` (failsafe) and not in the fast lane; fully
      offline, no Docker.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] `./mvnw verify -P it` exits 0 (runs the acceptance suite).
- [ ] Tests added/updated (these acceptance tests are the deliverable).
- [ ] **Best-in-class Observability (library-appropriate):** the acceptance
      tests assert observable *outcomes*; no new production loggers required.
      Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > fast.log 2>&1 && tail -n5 fast.log || { cat fast.log; exit 1; }
./mvnw -q verify -P it > it.log 2>&1 && tail -n5 it.log || { cat it.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/api-overview.md §Primary Workflows`,
  `docs/nfrs.md §Correctness (primary bar)`, `docs/nfrs.md §Testing`
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`,
  `docs/adr/0002-variant-aware-hand-evaluation.md`

## Definition of Done — commit message
```
test(games): add end-to-end workflow acceptance tests

Add offline, deterministic cross-module acceptance tests under -P it for
the two documented workflows (Omaha Hi-Lo via GameSession; full blackjack
round via BlackjackRound + AiPlayer stub), asserting outcomes conform to
the api-overview.md public result shapes, per docs/api-overview.md
(Primary Workflows), docs/nfrs.md (Correctness, Testing) and ADR-0001.
```
