# 0003 — Logging & error-model conventions

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0001

## Pre-warned notes
none

## Scope
Establish the cross-cutting logging and error conventions every later Java story
builds on, per [conventions.md](../docs/conventions.md) §Logging Conventions and
§Exceptions & Error Types, [nfrs.md](../docs/nfrs.md) §Observability, and
[api-overview.md](../docs/api-overview.md) §Error-Handling Model. Deliver: (1) a
small, descriptive domain exception hierarchy in `core.error` for rule/
programming violations (e.g. dealing from an exhausted source, acting out of
phase, invalid hand construction) — a lightweight base plus a couple of concrete
types, not a generic `RuntimeException`; (2) documented null-policy helpers/usage
(`Objects.requireNonNull` at boundaries; `Optional`/empty collections for
absence); (3) the SLF4J-facade logging convention (one `LoggerFactory.getLogger`
per class, no bound implementation, quiet-by-default DEBUG/TRACE). Lands in
`core.error` plus a short conventions note; no game logic here. Game-outcome
values (`RoundOutcome`, `HighLowResult`) are modelled in their own feature
stories, not here.

## Out of scope
- Any concrete throwing call sites (they live in the feature stories that
  perform the operation).
- Metrics/tracing wiring — **N/A per ADR-0001**.
- Card/deck/hand types (stories 0005+).

## Acceptance Criteria
- [ ] `core.error` exposes a small descriptive exception set for rule/
      programming violations (unchecked), with a clear base type; no bare
      `RuntimeException` throwing intended for domain use.
- [ ] Null policy is exercised by tests: `requireNonNull` rejects nulls at a
      representative boundary; a helper/pattern for it is available to feature stories.
- [ ] Unit tests cover exception construction/messaging and the null-rejection helper.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (unit-first).
- [ ] **Best-in-class Observability (library-appropriate):**
      - Structured logging via SLF4J: one class-scoped logger per type
        (`LoggerFactory.getLogger(Xxx.class)`); levels per the global log-level
        table (this library stays at DEBUG/TRACE, no INFO chatter). No MDC/trace
        IDs — the library owns no request context.
      - Metrics via Micrometer / custom Prometheus counters: **N/A per ADR-0001**
        (a library must not impose an observability backend on its host).

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/conventions.md §Logging Conventions`,
  `docs/conventions.md §Exceptions & Error Types`, `docs/api-overview.md §Error-Handling Model`,
  `docs/nfrs.md §Observability (library-appropriate)`
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`

## Definition of Done — commit message
```
feat(core): add domain exception model and SLF4J logging conventions

Introduce a small descriptive core.error exception hierarchy for
rule/programming violations, null-policy helpers (requireNonNull,
Optional/empty for absence), and the SLF4J-facade logging convention
(class loggers, DEBUG/TRACE, no bound impl), per docs/conventions.md
(Logging, Exceptions), docs/api-overview.md (Error-Handling) and ADR-0001.
```
