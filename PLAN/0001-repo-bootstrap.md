# 0001 — Repo bootstrap

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** none

## Pre-warned notes
none

## Scope
Stand up the Maven multi-module skeleton for the cardkit library per
[architecture.md](../docs/architecture.md) §Package & Maven Module Structure
and §Build & Packaging. Create the parent POM (`packaging=pom`) aggregating two
child modules, `core` (artifactId `cardkit-core`) and `games` (artifactId
`cardkit-games`, depends on `core`), both `packaging=jar`, base package
`org.rezatron.cardkit`. Set Java 25, add `slf4j-api` as the only runtime dependency
and JUnit 5 + AssertJ as test dependencies via parent `dependencyManagement`,
add the Maven wrapper (`./mvnw`), a README with the build/test command matrix
and module map, and initialise a gitflow `develop` branch. Modules may be empty
(a package-info or placeholder) — this story proves the build aggregates and
`./mvnw verify` succeeds on empty modules. Lands the `cardkit/` parent and
`core/`, `games/` module areas from `PLAN/structure.md`.

## Out of scope
- CI lane wiring / profiles (story 0002).
- Any domain types, logging config, or randomness seam (stories 0003+).
- `module-info.java` exports and Maven publishing config (story 0018).
- Any `docker-compose.yml`, persistence, or web layer — N/A per ADR-0001.

## Acceptance Criteria
- [x] Parent `pom.xml` has `packaging=pom` and `<modules>core</modules>` +
      `<modules>games</modules>`; `games` declares a dependency on `core`.
- [x] Java release set to 25; `slf4j-api` is the sole runtime dependency;
      JUnit 5 + AssertJ managed as test-scoped.
- [x] Maven wrapper committed; `./mvnw -v` reports the pinned Maven version.
- [x] README documents the command matrix (build, fast test, integration test)
      and the `core` vs `games` module map.
- [x] `develop` branch exists (gitflow).
- [x] `./mvnw verify` exits 0 (fast/offline lane — no Docker, empty modules OK).
- [x] Tests added/updated (a trivial smoke test per module is acceptable to
      keep the reactor honest; unit-first).
- [x] **Best-in-class Observability (library-appropriate):** no business logic
      yet, so no loggers/metrics required. Record in the README/POM comment that
      logging is SLF4J-facade-only with no bound implementation, and that
      Micrometer/Prometheus/OTel are **N/A per ADR-0001** (library imposes no
      observability backend). Full logging conventions land in story 0003.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Files touched
- `pom.xml` — parent aggregator POM (packaging=pom), Java 25, dependencyManagement (slf4j-api, JUnit5, AssertJ), aggregates core+games.
- `core/pom.xml` — cardkit-core module POM (packaging=jar).
- `games/pom.xml` — cardkit-games module POM (packaging=jar), depends on core.
- `core/src/main/java/org/rezatron/cardkit/core/package-info.java` — placeholder to keep the module non-empty and compiling.
- `games/src/main/java/org/rezatron/cardkit/games/package-info.java` — placeholder to keep the module non-empty and compiling.
- `core/src/test/java/org/rezatron/cardkit/core/CoreSmokeTest.java` — trivial smoke test keeping the reactor honest.
- `games/src/test/java/org/rezatron/cardkit/games/GamesSmokeTest.java` — trivial smoke test keeping the reactor honest.
- `README.md` — build/test command matrix, module map, observability/N-A note.
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` — Maven wrapper (pinned Maven version).
- `.gitignore` — ignore `verify.log` produced by the verification command.

## Traceability
- Docs implemented: `docs/architecture.md §Package & Maven Module Structure`,
  `docs/architecture.md §Build & Packaging`, `docs/conventions.md §Java Language Level & Features`
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`

## Definition of Done — commit message
```
build(core,games): bootstrap Maven multi-module library skeleton

Add parent POM (packaging=pom) aggregating cardkit-core and
cardkit-games, Java 25, slf4j-api runtime + JUnit5/AssertJ test deps,
Maven wrapper, README command matrix, and gitflow develop branch, per
docs/architecture.md (Module Structure, Build & Packaging) and ADR-0001
(standalone library, no Spring/Docker).
```
