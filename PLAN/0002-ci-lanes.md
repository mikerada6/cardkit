# 0002 — CI lane setup

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0001

## Pre-warned notes
none

## Scope
Wire the two CI test lanes described in `PLAN/structure.md` §CI lanes. The
**fast lane** (`./mvnw verify`) runs unit tests plus ArchUnit rules, fully
offline with no Docker. The **integration lane** (`./mvnw verify -P it`)
activates a Maven profile that runs cross-module acceptance tests; it must be
wired and green while empty (populated later by stories 0009 and 0017). Because
this is a library with no I/O or infrastructure (ADR-0001), the integration
lane uses **no Testcontainers/Docker** — it is offline cross-module testing.
Add an ArchUnit test scaffold in the `games` module asserting the module
dependency direction (`games → core`, never the reverse) and that `.internal`
packages are not referenced across module boundaries; the rules may start
permissive and tighten as types land. Lands in `core`/`games` test sources and
the parent/child POMs (surefire vs failsafe / `it` profile).

## Out of scope
- Actual acceptance test content (stories 0009, 0017).
- Any hosted-CI YAML unless trivially needed to run the two lanes; the lanes
  are Maven-driven and must work from the command line first.
- Domain code.

## Acceptance Criteria
- [ ] `./mvnw verify` runs unit tests + ArchUnit and exits 0 (offline, no Docker).
- [ ] `./mvnw verify -P it` activates the integration profile, runs the
      (currently empty) cross-module acceptance suite, and exits 0.
- [ ] ArchUnit scaffold present: a rule asserting `games` may depend on `core`
      but `core` must not depend on `games`, and `.internal` is not used across
      modules. Passes on the current (near-empty) codebase.
- [ ] Naming split is correct: unit tests bind to the fast lane (surefire),
      acceptance tests to the `it` profile (failsafe) — no acceptance test runs
      in the fast lane.
- [ ] Tests added/updated (the ArchUnit rule counts as the first real test).
- [ ] **Best-in-class Observability (library-appropriate):** no business logic;
      no loggers/metrics required. Micrometer/Prometheus/OTel **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > fast.log 2>&1 && tail -n5 fast.log || { cat fast.log; exit 1; }
./mvnw -q verify -P it > it.log 2>&1 && tail -n5 it.log || { cat it.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/nfrs.md §Testing`, `docs/architecture.md §Component Overview` (module direction)
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`

## Definition of Done — commit message
```
build(ci): wire fast (unit+ArchUnit) and integration (-P it) test lanes

Add offline fast lane and an empty-but-green integration profile for
cross-module acceptance tests (no Docker/Testcontainers — library has no
infra), plus an ArchUnit scaffold enforcing games -> core direction and
.internal non-export, per docs/nfrs.md (Testing) and ADR-0001.
```
