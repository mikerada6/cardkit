# 0019 — Require rejection-path logging

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0003

## Pre-warned notes
none

## Scope
Close a gap left by story 0003's logging conventions, per
[conventions.md](../docs/conventions.md) §Logging Conventions: `Require`
(`core.util`) had no logger, so precondition rejections were silent. Add a
class-scoped SLF4J logger to `Require.nonNull` / `Require.nonNullElements`
that emits a single DEBUG line naming the failed parameter on the
**rejection** path only, before the corresponding exception is thrown — the
happy path stays silent and allocation-free. Document the resulting rule in
`conventions.md` §Logging Conventions: mutable/behavioural types log
lifecycle and per-item detail at DEBUG/TRACE; boundary/precondition helpers
log only on rejection; value objects and exception constructors never
self-log. Add a test-scope `slf4j-simple` binding (`core` and `games`
modules) so unit tests exercise a real logging provider instead of the NOP
logger, and bump the `archunit` test dependency to the version this pipeline
already validates against. Bundled incidental fix: tick the already-satisfied
Acceptance Criteria checkboxes on `PLAN/0005-card-primitives.md`, which
shipped `Status: DONE` in story 0005 but left the boxes unchecked.

## Out of scope
- Any new exception types or null-policy behavior change — this only adds
  logging to the existing `Require` helpers (story 0003 scope).
- Logging in `Deck`/`Shoe` (already covered by story 0006) or any other
  domain type.

## Acceptance Criteria
- [x] `Require.nonNull` and `Require.nonNullElements` each emit one DEBUG log
      line naming the failed parameter (and, for collection elements, the
      index) on the rejection path only; the happy path logs nothing
      (asserted).
- [x] `docs/conventions.md` §Logging Conventions documents the
      lifecycle/rejection-only/never-self-log split across behavioural types,
      boundary helpers, and value objects/exceptions.
- [x] `core` and `games` modules carry a test-scope `slf4j-simple` binding so
      tests run against a real SLF4J provider rather than the NOP logger.
- [x] `PLAN/0005-card-primitives.md` Acceptance Criteria checkboxes are ticked
      to match its `Status: DONE`.
- [x] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [x] Tests added/updated (unit-first): rejection-path log assertions for both
      `Require` methods; happy-path silence asserted.
- [x] **Best-in-class Observability (library-appropriate):**
      - SLF4J DEBUG on rejection only, per `conventions.md` §Logging Conventions.
      - Metrics/tracing: **N/A per ADR-0001**.

## Files touched
- `core/src/main/java/org/rezatron/cardkit/core/util/Require.java` — add a
  class SLF4J logger; emit one DEBUG line naming the failed parameter (and the
  index, for elements) on each rejection branch before throwing.
- `core/src/test/java/org/rezatron/cardkit/core/util/RequireTest.java` — add
  rejection-path log assertions for both methods and happy-path silence
  assertions (unique per-test parameter names → order-independent).
- `core/src/test/resources/simplelogger.properties` — test-only slf4j-simple
  config: DEBUG for `Require`, output sunk to `target/require-test.log` so the
  test reads it via `java.nio.file.Files` and never touches `PrintStream`
  (keeps it clean under the `LoggingConventionTest` ArchUnit guard).
- `docs/conventions.md` — §Logging Conventions: document the
  lifecycle / rejection-only / never-self-log split.
- `pom.xml` — add test-scoped `slf4j-simple` to `dependencyManagement`; bump
  `archunit` 1.3.0 → 1.4.1 (**required**: the project compiles to Java 25
  bytecode = class-file major version 69, which archunit 1.3.0's bundled ASM
  cannot parse — it skips every project class and the guard rules pass
  vacuously via `allowEmptyShould(true)`; 1.4.1 parses v69 so the guards
  actually enforce).
- `core/pom.xml` — add test-scoped `slf4j-simple` dependency.
- `games/pom.xml` — add test-scoped `slf4j-simple` dependency.
- `PLAN/0005-card-primitives.md` — tick the already-satisfied AC checkboxes
  (bundled incidental fix per Scope).

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/conventions.md §Logging Conventions`
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`

## Definition of Done — commit message
```
feat(core): add rejection-path logging to Require and test-only SLF4J binding

Add a DEBUG log line on the rejection path of Require.nonNull and
Require.nonNullElements (silent on the happy path), document the
lifecycle/rejection-only/never-self-log logging split in
docs/conventions.md, and add a test-scope slf4j-simple binding so
unit tests exercise a real SLF4J provider, per docs/conventions.md
(Logging Conventions) and ADR-0001. Also ticks the already-satisfied
Acceptance Criteria checkboxes on PLAN/0005-card-primitives.md.
```
