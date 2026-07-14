# 0018 — Release readiness

**Status:** TODO   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0009, 0017

## Pre-warned notes
none

## Scope
Make the two JARs publishable and pin the public API surface, per
[api-overview.md](../docs/api-overview.md) §Package Layout & Public vs. Internal
Surface and §Versioning & Compatibility, and
[architecture.md](../docs/architecture.md) §Build & Packaging. Deliver: Maven
publishing configuration for the two library JARs (`cardkit-core`,
`cardkit-games`) including source and javadoc jars and `distributionManagement`
/ publish setup for a Maven repository; **Semantic Versioning** documented; the
public-vs-internal boundary enforced by `module-info.java` `exports` (only
supported packages exported; `.internal` not exported) and/or a tightened
ArchUnit rule; README consumer section (coordinates, module choice, that the
consumer supplies the SLF4J binding); and a final ADR sweep confirming the three
ADRs still match the built code. Standard-stack production items (graceful
shutdown, container image, secrets/vault, Compose) are **N/A per ADR-0001** —
this is a library published to a Maven repo, not a deployed service.

## Out of scope
- Any new domain behavior.
- CI publishing pipeline beyond the Maven configuration that makes
  `deploy`/`install` produce the artifacts (hosted-CI release automation is not
  required this phase).

## Acceptance Criteria
- [ ] Both modules build library JARs plus `-sources` and `-javadoc` jars;
      publishing config (`distributionManagement`/publish plugin) is present and
      `./mvnw -DskipTests install` produces all artifacts locally.
- [ ] Public vs. internal surface is enforced: only supported packages are
      exported (`module-info.java` `exports`, and/or ArchUnit forbids external
      use of `.internal`); a test/verification proves `.internal` is not on the
      exported contract.
- [ ] README documents SemVer, Maven coordinates, module selection (core-only vs.
      games), and that the consumer supplies the logging binding.
- [ ] Final ADR sweep: a short note (README or an ADR addendum) confirms
      ADR-0001/0002/0003 still describe the built code; any divergence is routed
      to `revise-design`, not silently changed.
- [ ] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [ ] Tests added/updated (export-boundary check; publish is verified by the
      install command below).
- [ ] **Best-in-class Observability (library-appropriate):** no new runtime logic;
      confirm the library still binds no logging implementation and imposes no
      metrics backend. Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
./mvnw -q -DskipTests install > install.log 2>&1 && tail -n5 install.log || { cat install.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/api-overview.md §Package Layout & Public vs. Internal Surface`,
  `docs/api-overview.md §Versioning & Compatibility`, `docs/architecture.md §Build & Packaging`
- ADRs implemented or impacted: `docs/adr/0001-standalone-java-library-not-spring-service.md`

## Definition of Done — commit message
```
build(release): make library JARs publishable and pin public API

Add Maven publish config (JARs + sources/javadoc, distributionManagement),
document SemVer and consumer coordinates, enforce public-vs-.internal
surface via module-info exports/ArchUnit, and sweep ADR-0001/0002/0003
against the built code, per docs/api-overview.md (Package Layout,
Versioning) and docs/architecture.md (Build & Packaging).
```
