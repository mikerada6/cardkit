# Agent Notes

_Async handover channel between agents. Read `## Active` before planning
any story; append at the moment of discovery. Point at artifacts by path —
never paste them. This file is coordination, not design truth: `docs/` owns
design, ADRs own decisions, story files own scope._
_Created: 2026-07-14T00:00:00Z_
_Last updated: 2026-07-14T00:00:00Z (story 0019 — added N-001: ArchUnit version ↔ bytecode version coupling)_

## Rules
1. Read `## Active` in full before planning a story. Never read
   `## Resolved` unless tracing history (grep it by `N-###` or story
   number).
2. Append at the moment of discovery, not at story end.
3. IDs `N-###` are sequential across the whole file and never reused.
   Entries move from Active to Resolved; they are never deleted.
4. A **BLOCKING** entry that applies to your story means stop and ask.
5. Cap: `## Active` holds at most 15 entries. At the cap, resolve or
   escalate before adding (run `/update-notes`).
6. A note never settles a design question. Mirror it as a DRIFT pointer
   and route the change through `revise-design`.

## Active

### N-001 — ArchUnit version is coupled to the compiled bytecode version
- **Type:** GOTCHA
- **Severity:** WARN
- **Raised:** 2026-07-14 by story 0019
- **Applies to:** any story adding/relying on ArchUnit rules (0009, 0018) or bumping `maven.compiler.release` / the JDK
- **Note:** The project compiles to Java 25 (class-file major version 69).
  ArchUnit's bundled ASM must support that version or `ClassFileImporter`
  silently skips every project class and rules pass **vacuously** (they use
  `allowEmptyShould(true)`). `archunit` 1.3.0 could NOT parse v69 (false green);
  1.4.1 can. If the compiler release or JDK is bumped later, re-verify ArchUnit
  actually imports classes (grep the verify log for "Couldn't import class …
  rezatron") before trusting a green run. Guard tests scope to main sources via
  `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS` (see `RandomnessConventionTest`);
  `LoggingConventionTest` does NOT and scans test classes too, so test code must
  avoid banned APIs like `java.io.PrintStream`.
- **Refs:** PLAN/0019-require-rejection-logging.md §Files touched, core/src/test/resources/simplelogger.properties, core/src/test/java/org/rezatron/cardkit/core/arch/LoggingConventionTest.java

<!-- Entry shape — copy verbatim, fill every field:
### N-001 — <one-line title>
- **Type:** BUG | GOTCHA | DRIFT | RISK | DECISION | REMINDER
- **Severity:** BLOCKING | WARN | FYI
- **Raised:** <ISO-8601> by <story NNNN | review NNNN rN | revise-design | manual>
- **Applies to:** <story numbers, module/package/area, or "all backend stories">
- **Note:** <2–4 sentences: what was discovered; what the next agent must
  do or avoid, concretely.>
- **Refs:** <paths only: PLAN/0007-*.md §Design drift, docs/api-overview.md §Users, PR URL>
-->

## Resolved

_Append-only archive, newest first. A resolved entry keeps its original
fields plus one line:_
_`- **Resolved:** <ISO-8601> by <story NNNN | revise-design | update-notes> — <one line>`_
