# Architecture

## Purpose & Scope

This document describes the structure of **CardKit** — a
reusable Java domain library for card games. It is deliberately **not** a
deployable service: there is no REST API, web layer, persistence, or messaging.
It is consumed as a JAR by other projects.

The design goal that shapes everything below is **generalization**: hand
representation and evaluation must work across poker variants (5-card draw,
Texas Hold'em, Omaha Hi-Lo) and Blackjack, rather than hard-coding 5-card
logic.

The departure from the global default stack (Spring Boot + Angular + Docker
Compose + Swagger) is intentional and recorded in
[ADR-0001](adr/0001-standalone-java-library-not-spring-service.md).

## Consumer Context

```
+-----------------------------+
|   Consuming application(s)   |   e.g. a future betting/pot phase, a CLI,
|   (out of scope here)        |        a service wrapper — all separate projects
+--------------+--------------+
               | depends on (Maven)
               v
+-----------------------------+
|      cardkit (this lib)   |   published JAR artifact(s)
+-----------------------------+
```

Consumers depend on the library through Maven coordinates and drive it purely
through Java method calls. The library holds no global mutable state and owns no
I/O — a consumer instantiates the types, drives a workflow, and reads the
results.

## Component Overview

Two Maven modules. `games` depends on `core`; `core` depends on nothing beyond
the JDK and a logging facade.

```
core (card primitives, no game rules)
 ├── card        Card, Rank, Suit
 ├── deck        Deck, Shoe
 ├── random      RandomSource, SecureRandomSource, SeededRandomSource
 ├── hand        Hand (generic card collection + invariants)
 └── participant Participant, Player (bankroll/chip stack + hand)

games (game-specific rules, depends on core)
 ├── poker       PokerVariant, PokerHand, HandCategory, HandRank,
 │               HandEvaluator (+ per-variant evaluators), HighLowResult, dealing
 ├── blackjack   BlackjackHand, BlackjackRound,
 │               RoundOutcome (WIN/LOSE/PUSH)
 ├── participant Dealer, AiPlayer, DecisionStrategy (stub-able)
 └── session     GameSession (participants + round state, owns board)
```

## Package & Maven Module Structure

```
cardkit/                      parent POM (packaging: pom)  groupId: org.rezatron
 ├── pom.xml
 ├── core/
 │    ├── pom.xml                (packaging: jar)  artifactId: cardkit-core
 │    └── src/main/java/.../core/{card,deck,random,hand,participant}
 └── games/
      ├── pom.xml                (packaging: jar)  artifactId: cardkit-games
      └── src/main/java/.../games/{poker,blackjack,participant,session}
```

- Base package: `org.rezatron.cardkit` (`org.rezatron.cardkit.core.*`, `org.rezatron.cardkit.games.*`).
- Each module publishes an independent JAR. A consumer that only needs cards and
  shuffling can depend on `core` alone; a consumer that needs game rules depends
  on `games`, which brings `core` transitively.
- Public vs. internal surface is enforced by package boundaries (and,
  optionally, `module-info.java`); see [conventions.md](conventions.md) and
  [api-overview.md](api-overview.md).

## Key Abstractions & Boundaries

- **`Card` / `Rank` / `Suit`** — immutable value objects. A card is identity-free
  (two aces of spades are equal). These are the atomic currency of the library.
- **`Deck` / `Shoe`** — a `Deck` is one 52-card set; a `Shoe` is *N* decks
  combined for dealing (Blackjack, some poker games). Both draw from an injected
  `RandomSource` when shuffling — they never construct their own RNG.
- **`Hand`** — a generic ordered collection of cards with size/duplicate
  invariants. Game-specific hands (`PokerHand`, `BlackjackHand`) build on it.
- **`HandEvaluator`** — the core generalization point. It takes a player's cards,
  the shared community board (may be empty), and a `PokerVariant` rule, and
  returns a comparable result. The variant rule — not the caller — owns the
  card-selection constraint (see the Omaha Hi-Lo case below). Detailed in
  [ADR-0002](adr/0002-variant-aware-hand-evaluation.md).
- **Participants** — `Player` (bankroll + hand) lives in `core` as a data
  holder. Game-specific behavior — `Dealer` fixed rules, `AiPlayer`
  `DecisionStrategy` — lives in `games`.
- **`GameSession`** — owns round state and the **shared community
  board**. In Hold'em and Omaha the board (0–5 cards) is table state, not copied
  per player; each player holds only their hole cards.

## Extensibility Model

- **Add a poker variant:** introduce a new `PokerVariant` value plus a
  `HandEvaluator` strategy that encapsulates its selection rule. No changes to
  `Card`, `Deck`, or `Shoe`.
- **Add a new game:** add a package under `games` that composes `core`
  primitives (deck/shoe, hand, participants) with its own rules and session
  type. `core` never depends on `games`, so new games cannot destabilize the
  primitives.
- **Swap AI behavior:** provide a `DecisionStrategy` implementation; the stub
  ships in-phase (see the brief's non-goals).
- **Control randomness:** inject a `RandomSource` — `SecureRandomSource` in
  production, `SeededRandomSource` in tests. See the Randomness boundary below.

## Randomness & Shuffle Boundary

All non-determinism is funnelled through a single seam: the `RandomSource`
abstraction in `core.random`.

- Production shuffling uses `SecureRandomSource`, backed by
  `java.security.SecureRandom` (mandated by the brief; **never**
  `java.util.Random`).
- Tests inject `SeededRandomSource` for reproducible shuffles, so
  correctness tests stay deterministic and fully offline.
- `Deck`/`Shoe` accept a `RandomSource` and never instantiate an RNG directly.

Rationale and the security/testability trade-off are recorded in
[ADR-0003](adr/0003-injectable-secure-randomness.md).

## Build & Packaging

- **Language:** Java 25 (latest LTS at time of writing).
- **Build:** Maven multi-module (parent POM aggregating `core` + `games`), per
  the global multi-module convention.
- **Artifacts:** two library JARs (`cardkit-core`, `cardkit-games`).
  Nothing is a fat/executable JAR; there is no `main` entry point and no
  deployable unit.
- **"Deployment":** publishing the JARs to a Maven repository. There is no
  runtime host, container image, or Compose stack — see Deviations below.
- **Runtime dependencies:** JDK standard library only, plus the **SLF4J API**
  (logging facade — no bound implementation; see
  [nfrs.md](nfrs.md)). **Test** dependencies (JUnit 5, AssertJ, etc.) follow the
  global testing standards and are not restated here.

## Deviations from Global Standards

The global standards default every project to a Spring Boot backend + Angular
frontend + Docker Compose + Swagger UI, with observability infra
(Loki/Grafana) and a persistence layer. This project intentionally has **none**
of those, because it is a standalone reusable library. The rationale and the
downstream consequences (no REST/OpenAPI, no persistence/Flyway, no
metrics/tracing infrastructure, logging via facade only) are captured in
[ADR-0001](adr/0001-standalone-java-library-not-spring-service.md).

## Open Questions

- **`TBD — open question`:** numeric performance/scale targets are intentionally
  unset this phase (see [nfrs.md](nfrs.md)).
- **`TBD — open question`:** `AiPlayer` `DecisionStrategy` may ship as a stub
  this phase; the eventual decision model is deferred to the follow-on
  betting/pot phase.
