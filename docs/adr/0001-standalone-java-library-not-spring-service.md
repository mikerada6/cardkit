# 1. Standalone Java library instead of a Spring Boot / Angular service

Date: 2026-07-14

## Status

Accepted

## Context

The global engineering standards default **every** project to a Spring Boot
backend + Angular frontend, a Flyway/JPA persistence layer, a Docker Compose
local stack, Swagger UI (springdoc-openapi), and first-class
metrics/tracing/logging infrastructure (Loki/Grafana).

The brief scopes this project as a **reusable Java domain library** for card
games, consumed as a JAR by other projects. It explicitly excludes a REST API,
web service, UI, persistence/database, betting/pot mechanics, and multiplayer
networking. The brief further states there is **no planned transition** to a
service wrapping this library — if that ever happens it would be a new consuming
project, not a rearchitecture of this one — and asks that this deviation be
recorded as an ADR.

## Decision

Build the project as a **standalone Maven multi-module Java library** (`core` +
`games`), packaged as library JARs, with **no** Spring Boot/web layer, **no**
persistence, and **no** Docker Compose stack.

Consequences of that decision for the other standard elements:

- **No REST/HTTP API and therefore no OpenAPI contract.** The `design-docs`
  OpenAPI artifact is intentionally omitted; the "API" documented for this
  project is the **public Java API surface** (see `api-overview.md`).
- **No persistence.** No database, Flyway migrations, or JPA entities; the
  domain model is in-memory only (see `data-model.md`).
- **Observability via the SLF4J API facade only.** The library binds no logging
  implementation and ships no metrics/tracing infrastructure; consumers
  instrument at their level (see `nfrs.md`).
- **Java 25 (latest LTS), Maven multi-module**, published to a Maven repository
  rather than deployed as a service.

A future service that wants these capabilities is a **separate consuming
project** that depends on this JAR.

## Consequences

**Positive**

- Minimal dependency surface (JDK + SLF4J API); trivially embeddable.
- Fast, fully offline unit tests; no infrastructure to stand up locally.
- Keeps the phase focused on its primary bar: rule/evaluation correctness.

**Negative / trade-offs**

- Forgoes the out-of-the-box API docs, observability, and local-parity tooling
  the standard stack provides; consumers must supply logging binding and any
  instrumentation.
- Departs from several global defaults at once; contributors must consult this
  ADR rather than assume the standard stack.

**Revisit if**

- The project is ever asked to expose a network API or persist state. Per the
  brief, that should spawn a new consuming project — at which point its own
  design docs (including an OpenAPI contract with the global `{data, meta}`
  envelope) would apply.
