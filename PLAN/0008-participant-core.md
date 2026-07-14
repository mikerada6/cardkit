# 0008 — Participant (core)

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0007

## Pre-warned notes
none

## Scope
Model the seated participant data holder per [data-model.md](../docs/data-model.md)
§Participants and §Relationships & Ownership, and
[conventions.md](../docs/conventions.md) §Chip / Bankroll Representation.
Deliver in `core.participant`: `Player` — an identifier/name, a **bankroll /
chip stack stored as a primitive `long`** (chip units, non-negative), and a
current `Hand`. The bankroll is a passive attribute this phase: no debits/
credits, no betting behavior (that is the follow-on phase) — but the
non-negative invariant is enforced at construction. If a shared `Participant`
abstraction is warranted (per architecture.md's `core.participant` listing),
introduce it minimally; `Dealer`/`AiPlayer` are `games` types added later.
Lands in `core.participant`.

## Out of scope
- Betting/pot mechanics, chip debits/credits — **out of scope this phase** (brief).
- `Dealer` / `AiPlayer` / `DecisionStrategy` — those are `games` types
  (stories 0015, 0016).
- A dedicated `Chips` value object — deferred to the betting phase per conventions.md.

## Acceptance Criteria
- [ ] `Player` in `core.participant` holds name/id, a `long` bankroll (non-negative,
      enforced), and a current `Hand`.
- [ ] Constructing a `Player` with a negative bankroll or null name/hand is
      rejected (documented domain exception / `requireNonNull`).
- [ ] No betting behavior is present (bankroll is read-only-ish attribute this
      phase); verified by review — no debit/credit API.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit-first): construction, invariants, hand association.
- [ ] **Best-in-class Observability (library-appropriate):**
      - SLF4J class logger at DEBUG/TRACE if useful; quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Participants`,
  `docs/data-model.md §Relationships & Ownership`, `docs/conventions.md §Chip / Bankroll Representation`
- ADRs implemented or impacted: none

## Definition of Done — commit message
```
feat(core): add Player participant with bankroll and hand

Add core.participant Player: name/id, non-negative long chip bankroll
(passive attribute this phase), and a current Hand, per
docs/data-model.md (Participants) and docs/conventions.md (Chip/Bankroll
Representation). No betting behavior yet.
```
