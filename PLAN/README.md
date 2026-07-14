# PLAN

_Generated: 2026-07-14T00:00:00Z_
_Last updated: 2026-07-14T00:00:00Z — story 0019 DONE_

_Scope: MVP_

Backlog for **CardKit** — a standalone Java domain library
([ADR-0001](../docs/adr/0001-standalone-java-library-not-spring-service.md)),
**not** a Spring Boot/Angular service. The six web-service foundational stories
from the standard template (observability infra, API plumbing, OpenAPI contract,
auth, frontend, database) are intentionally **absent** — they describe
infrastructure ADR-0001 excludes. See [structure.md](structure.md) for the
proposed layout and [notes.md](notes.md) for the agent handover log.

| # | Title | Status | Size | Depends on |
|---|---|---|---|---|
| 0001 | repo-bootstrap | DONE | M | none |
| 0002 | ci-lanes | DONE | S | 0001 |
| 0003 | logging-and-error-model | DONE | S | 0001 |
| 0004 | randomness-seam | DONE | S | 0001, 0003 |
| 0005 | card-primitives | DONE | S | 0003 |
| 0006 | deck-and-shoe | DONE | M | 0004, 0005 |
| 0007 | hand | TODO | S | 0005 |
| 0008 | participant-core | TODO | S | 0007 |
| 0009 | poker-hand-ranker | TODO | M | 0005, 0007, 0002 |
| 0010 | poker-low-ranker | TODO | S | 0009 |
| 0011 | poker-evaluator-draw-holdem | TODO | M | 0009 |
| 0012 | poker-evaluator-omaha-hilo | TODO | M | 0010, 0011 |
| 0013 | poker-session | TODO | M | 0006, 0008, 0012 |
| 0014 | blackjack-hand | TODO | S | 0007 |
| 0015 | blackjack-round | TODO | M | 0006, 0008, 0014 |
| 0016 | ai-player-stub | TODO | S | 0008 |
| 0017 | e2e-workflow-acceptance | TODO | M | 0013, 0015, 0016 |
| 0018 | release-readiness | TODO | S | 0009, 0017 |
| 0019 | require-rejection-logging | DONE | S | 0003 |

## Open questions (surfaced during seed; not blocking)

- **0013** — `Showdown` settlement result type is used in `api-overview.md`
  sketches but not modelled in `data-model.md` (docs audit F-004, skipped by
  user). Its concrete shape is defined at implementation time and flagged for
  `revise-design`.
- **0015** — `Action` enum is named in `conventions.md`/sketches but not modelled
  in `data-model.md` (F-004). Blackjack **soft-17** rule is deferred
  (`data-model.md` open question): default stand-on-all-17 behind a flag; flagged
  for `revise-design`.

## Deferred / Post-MVP

Titles only — no story files this phase (per the brief's non-goals and the docs'
open questions):

- Betting / pot mechanics (chip debits/credits, side pots)
- Real `AiPlayer` decision model (beyond the stub `DecisionStrategy`)
- Additional poker variants beyond 5-card draw / Texas Hold'em / Omaha Hi-Lo
- Performance optimization (lookup tables / perfect-hash evaluators)
- Definitive blackjack soft-17 policy decision
- Structured, machine-readable event output beyond SLF4J logging
- A network/service wrapper (would be a separate consuming project, per ADR-0001)
