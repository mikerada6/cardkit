# CardKit

A reusable **Java domain library for card games** — poker (5-card draw, Texas
Hold'em, Omaha Hi-Lo) and Blackjack — built around a generalized hand
representation and evaluation model.

CardKit is a **standalone library**, consumed as a JAR through Java method
calls. It is deliberately **not** a deployable service: there is no REST/web
layer, persistence, messaging, or Docker. This departure from the default
Spring Boot/Angular stack is recorded in
[ADR-0001](docs/adr/0001-standalone-java-library-not-spring-service.md). See
[docs/architecture.md](docs/architecture.md) for the full design.

## Requirements

- **Java 25** (sources compile with `--release 25`).
- Maven — use the bundled wrapper (`./mvnw`); no local Maven install required.

## Modules

| Module | artifactId | Contains | Depends on |
|---|---|---|---|
| `core` | `cardkit-core` | Card primitives — `card`, `deck`, `random`, `hand`, `participant`. No game rules. | JDK + SLF4J API only |
| `games` | `cardkit-games` | Game rules — `poker`, `blackjack`, `participant`, `session`. | `cardkit-core` (transitive) |

A consumer needing only cards and shuffling depends on `cardkit-core`; a
consumer needing game rules depends on `cardkit-games`, which brings `core`
transitively.

## Build & test command matrix

| Goal | Command | Notes |
|---|---|---|
| Build (compile + package both modules) | `./mvnw -q package` | Produces `cardkit-core` and `cardkit-games` JARs. |
| Fast test lane (default) | `./mvnw -q verify` | Unit tests, fully **offline**, no Docker. The default reactor lane. |
| Integration lane | `./mvnw -q verify -P it` | Cross-module acceptance tests — still offline (the library owns no I/O). Green-and-empty until stories 0009 and 0017 populate it. |
| Clean | `./mvnw -q clean` | Removes `target/`. |

There is no `main` entry point and no executable/fat JAR — the modules publish
as plain library JARs.

## Observability

CardKit is a library, not a service, so it imposes **no observability backend**:

- **Logging:** through the **SLF4J API** only. The library **binds no logging
  implementation** — the consuming application supplies one. It stays quiet by
  default (internal detail at DEBUG/TRACE; no INFO during normal operation).
  Full logging conventions land in story 0003.
- **Metrics / tracing:** Micrometer, Prometheus, and OpenTelemetry are **N/A
  per [ADR-0001](docs/adr/0001-standalone-java-library-not-spring-service.md)** —
  a consuming service that wants them wires them at its own boundary.

## Branching

Gitflow: `main` is release, `develop` is the integration branch, feature work
lands on `feature/*` branches merged into `develop`.
