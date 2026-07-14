# API Overview

## Purpose & Scope

This is the **public Java API surface** of the library — the types a consuming
project programs against — not an HTTP/REST API. There is **no** web service,
`/api/v1` namespace, response envelope, or OpenAPI contract in this project; the
reasons are in [ADR-0001](adr/0001-standalone-java-library-not-spring-service.md).
The contract described here is a **binary/source API** governed by Semantic
Versioning (see below).

## Package Layout & Public vs. Internal Surface

- Public, supported packages:
  `org.rezatron.cardkit.core.{card,deck,random,hand,participant}` and
  `org.rezatron.cardkit.games.{poker,blackjack,participant,session}`.
- Anything under a `.internal` subpackage is **not** part of the contract and
  may change without a major version bump. Where `module-info.java` is used,
  only supported packages are `exports`ed.
- Value objects (`Card`, `Rank`, `Suit`, `HandCategory`, `HandRank`,
  `LowHandRank`, `HighLowResult`, `RoundOutcome`) are immutable and safe to
  share.

## Key Public Types & Entry Points

| Type | Module / package | Role |
|------|------------------|------|
| `RandomSource`, `SecureRandomSource`, `SeededRandomSource` | `core.random` | Injectable randomness seam for shuffling |
| `Deck`, `Shoe` | `core.deck` | Dealing sources; `Shoe.ofDecks(n, randomSource)` |
| `Player` | `core.participant` | Bankroll + hand holder |
| `PokerVariant` | `games.poker` | `FIVE_CARD_DRAW`, `TEXAS_HOLDEM`, `OMAHA_HI_LO` |
| `HandEvaluator` | `games.poker` | `forVariant(...)`; `evaluate(...)` / `evaluateHighLow(...)` |
| `GameSession` | `games.session` | Poker round orchestration; owns the board |
| `BlackjackRound` | `games.blackjack` | Blackjack round orchestration |
| `Dealer`, `AiPlayer`, `DecisionStrategy` | `games.participant` | House rules & pluggable bot decisions |

## Primary Workflows as API Usage

These sketches are illustrative of the intended surface, not final signatures.

### 1 — Deal + evaluate a poker hand (Omaha Hi-Lo)

```java
RandomSource rng = new SecureRandomSource();          // production randomness
Shoe shoe = Shoe.ofDecks(1, rng);
shoe.shuffle();                                       // Fisher–Yates over rng

GameSession session = GameSession.poker(PokerVariant.OMAHA_HI_LO, shoe);
session.seat(new Player("Alice", 1_000));             // bankroll attribute
session.seat(new Player("Bob",   1_000));

session.dealHoleCards();      // 4 hole cards each (Omaha)
session.dealBoard();          // flop, turn, river -> up to 5 shared board cards

HandEvaluator evaluator = HandEvaluator.forVariant(PokerVariant.OMAHA_HI_LO);
for (Player p : session.players()) {
    HighLowResult r = evaluator.evaluateHighLow(p.holeCards(), session.board());
    HandRank high = r.high();                 // best of the 60 legal 2+3 combos
    Optional<LowHandRank> low = r.low();      // present only if 8-or-better qualifies
}

Showdown result = session.settleShowdown(evaluator);  // winner(s); splits hi/lo pot
```

For `FIVE_CARD_DRAW` / `TEXAS_HOLDEM`, `evaluator.evaluate(holeCards, board)`
returns a single `HandRank` (no low half).

### 2 — Play a full blackjack round

```java
RandomSource rng = new SecureRandomSource();
Shoe shoe = Shoe.ofDecks(6, rng);
shoe.shuffle();

BlackjackRound round = BlackjackRound.start(shoe, List.of(new Player("Alice", 500)));
round.dealInitial();                          // two cards per player + dealer

for (Player p : round.players()) {
    // A human consumer decides; an AiPlayer delegates to its DecisionStrategy.
    while (round.decide(p) == Action.HIT) {
        round.hit(p);                         // bust ends the player's turn
    }
    round.stand(p);
}

round.resolveDealer();                        // dealer hits <=16, stands >=17
Map<Player, RoundOutcome> outcomes = round.settle();  // WIN / LOSE / PUSH per player
```

## Extension Points

- **New poker variant:** add a `PokerVariant` and a `HandEvaluator` strategy —
  see [ADR-0002](adr/0002-variant-aware-hand-evaluation.md). No changes to
  `core`.
- **New game:** add a package under `games` composing `core` primitives with a
  new session/round type.
- **Bot behavior:** implement `DecisionStrategy` (stub-able this phase) and wire
  it into an `AiPlayer`.
- **Randomness:** inject any `RandomSource`
  ([ADR-0003](adr/0003-injectable-secure-randomness.md)).

## Error-Handling Model

No HTTP status codes, no `{data, meta}` envelope (there is no wire protocol).
Instead:

- **Domain outcomes are return values**, modelled explicitly: `RoundOutcome`
  (WIN/LOSE/PUSH), `HighLowResult` (with optional low), `Optional<...>` for
  "no result". Callers branch on values, not exceptions.
- **Rule / programming violations throw unchecked exceptions**: e.g. dealing
  from an exhausted source, acting out of phase, or constructing an invalid
  hand. Each public method documents the exceptions it can throw. Nulls are
  rejected at the boundary (`NullPointerException` via `requireNonNull`).

## Versioning & Compatibility

- **Semantic Versioning** on the two JAR artifacts. Breaking changes to any
  supported (exported) package require a major bump; additive changes are minor.
- Internal packages and non-`exports`ed types carry no compatibility guarantee.
- The evaluation result types are part of the contract — their comparison
  semantics (tie-break ordering) are covered by the test vectors in
  [nfrs.md](nfrs.md) and must remain stable within a major version.

## Threading & Instance-Reuse

- `Deck`, `Shoe`, `Hand`, `GameSession`, and `BlackjackRound` are **mutable and
  not thread-safe**; a round is driven from a single thread.
- Value objects (`Card`, results, enums) are immutable and freely shareable.
- `HandEvaluator` and `DecisionStrategy` implementations are expected to be
  **stateless** and reusable across rounds and threads.

## Open Questions

- **`TBD — open question`:** exact final method signatures (e.g. whether
  `evaluate`/`evaluateHighLow` unify behind one result type) are settled during
  implementation; the shapes above fix the intended surface, not the syntax.
