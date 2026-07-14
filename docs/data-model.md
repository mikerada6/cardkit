# Domain Model

## Purpose & Scope

This document defines the **in-memory domain model** of the library: the core
types, their relationships, ownership, and invariants. There is **no
persistence** in this phase — no database, no Flyway migrations, no JPA
entities/DTOs. The rationale is recorded in
[ADR-0001](adr/0001-standalone-java-library-not-spring-service.md). "Entity"
here means a domain concept, not a persisted row.

Types live in the two modules described in [architecture.md](architecture.md):
primitives in `core`, game-specific types in `games`.

## Value Objects vs. Entities; Immutability Policy

- **Immutable value objects** (no identity, equal-by-value; Java `record`s):
  `Card`, `Rank`, `Suit`, and every evaluation result (`HandCategory`,
  `HandRank`, `LowHandRank`, `HighLowResult`, `RoundOutcome`). Sharing them
  freely is safe.
- **Mutable domain objects** (identity + lifecycle): `Deck`, `Shoe`, `Hand`
  (cards are added/removed as play proceeds), `Player`/`Dealer`/`AiPlayer`
  (hold a changing hand and, for players, a bankroll), and
  `GameSession` (round state advances).
- **Null policy:** public APIs never return `null` collections or results;
  "no result" is modelled explicitly (e.g. an empty `Optional`, or a
  `HighLowResult` whose low is absent). See [conventions.md](conventions.md).

## Core Types

### `Card`, `Rank`, `Suit`

- **`Suit`** — enum: `CLUBS`, `DIAMONDS`, `HEARTS`, `SPADES`.
- **`Rank`** — enum: `TWO`…`TEN`, `JACK`, `QUEEN`, `KING`, `ACE`, each carrying
  ordinal strength. **Ace is high for poker ranking and low for the ace-to-five
  low hand and for `A-2-3-4-5` "wheel" straights** — rank comparison is
  context-supplied by the evaluator, not baked into a single natural order.
- **`Card`** — immutable `record(Rank, Suit)`. Equality is by value: two aces of
  spades are equal. A `Card` has no notion of "which deck it came from".

### `Deck`

- One standard 52-card set: the 52 distinct `(Rank, Suit)` combinations.
- **Mutable draw state:** shuffling reorders; dealing removes the top card.
  A dealt card cannot be dealt again from the same deck instance until reset.
- Shuffles via an injected `RandomSource` (never its own RNG) —
  [ADR-0003](adr/0003-injectable-secure-randomness.md).

### `Shoe`

- *N* combined decks (`N ≥ 1`) drawn from as a single source; used by Blackjack
  and multi-deck poker games.
- A shoe of *N* decks legitimately contains up to *N* copies of the same card —
  the "no duplicate cards" invariant is a **per-deck** rule, not a per-shoe rule
  (see Invariants).

## Hand Types

### `PokerHand`

Holds a player's **hole cards**; the count depends on the variant (see the deal
shape table). It does **not** own the community board — that is shared table
state (see Ownership). Evaluation is delegated to a `HandEvaluator`; a
`PokerHand` never scores itself.

### `BlackjackHand`

Holds the cards dealt to one participant and exposes derived state used by the
round: best total (aces counted as 1 or 11 — "soft" vs. "hard"), `isBust`
(> 21), and `isBlackjack` (natural 21 on the first two cards). These are
computed from the cards, not stored independently.

## Evaluation Result Types

- **`HandCategory`** — enum ordered weakest→strongest: `HIGH_CARD`, `PAIR`,
  `TWO_PAIR`, `THREE_OF_A_KIND`, `STRAIGHT`, `FLUSH`, `FULL_HOUSE`,
  `FOUR_OF_A_KIND`, `STRAIGHT_FLUSH` (a royal flush is the ace-high case of
  `STRAIGHT_FLUSH`).
- **`HandRank`** — immutable, `Comparable`. Encodes the winning 5-card hand as a
  `HandCategory` plus ordered tie-break kickers, so two hands of the same
  category compare correctly (e.g. two flushes decided by highest card down).
- **`LowHandRank`** — immutable, `Comparable`. Ace-to-five low (aces low;
  straights and flushes ignored) with the **8-or-better** qualifier: the hand
  qualifies only if it has five distinct ranks all ≤ 8. The best possible low is
  `5-4-3-2-A` (the "wheel").
- **`HighLowResult`** — for Omaha Hi-Lo: the best `HandRank` (high) plus an
  `Optional<LowHandRank>` (present only if a qualifying low exists). This is the
  unit the session uses to settle a possibly-split pot.

## Participants

- **`Player`** (`core`) — an identifier/name, a **bankroll / chip stack**
  (integer chip units — a bankroll *attribute*, since betting/pot mechanics are
  out of scope this phase), and a current `Hand`.
- **`Dealer`** (`games`) — the house participant: owns its own hand and
  encapsulates **fixed rule-bound behavior**. For Blackjack: hit while total
  ≤ 16, stand at ≥ 17. (Soft-17 handling is called out under Open Questions.)
- **`AiPlayer`** (`games`) — a computer-controlled `Player` that delegates
  actions (bet/fold/hit/stand) to a pluggable `DecisionStrategy`. The strategy
  may be a **stub** this phase (per the brief).

## Session State: `GameSession`

Tracks a round: the participants, the dealing source (`Deck`/`Shoe`), the
**shared community board** (0–5 cards for board games), and the current phase
(e.g. pre-deal → deal → player actions → dealer resolves → settle). It is the
only type that owns cross-participant state.

## Relationships & Ownership

```
GameSession
 ├── 1  Shoe (or Deck)                 dealing source
 ├── 0..5  community board : Card      SHARED (board games only), owned here
 ├── 1  Dealer                         house participant
 └── 1..*  Player / AiPlayer           seated participants
             ├── 1  bankroll (chips)   Player/AiPlayer only
             └── 1  Hand
                     └── *  Card        (hole cards; value objects)
```

- The **community board is owned by the session**, never copied into each
  player's hand. A player's evaluable poker hand = *their hole cards* + *the
  shared board*, combined at evaluation time.
- `Card` value objects may appear by value in multiple result objects, but a
  given physical card dealt from a `Deck`/`Shoe` pass is dealt to exactly one
  place (see Invariants).

## Per-Variant Deal Shape

| Variant           | Hole cards / player | Community board | Evaluation selection rule |
|-------------------|---------------------|-----------------|---------------------------|
| 5-card draw       | 5                   | none            | evaluate the 5 held cards |
| Texas Hold'em     | 2                   | up to 5         | best 5 of {2 hole ∪ board}, any mix (may play the board) |
| Omaha Hi-Lo       | **4**               | up to 5         | **exactly 2 hole + exactly 3 board** (60 combos); high and 8-or-better low chosen independently |
| Blackjack         | starts at 2, grows on hit | none      | total ≤ 21, aces 1/11      |

The model **stores the extra cards** (Omaha's 4 hole, the up-to-5 board); the
"exactly 2 + 3" constraint is enforced by the evaluator, not by what is dealt —
see [ADR-0002](adr/0002-variant-aware-hand-evaluation.md).

## Invariants

- **Deck:** exactly 52 cards at construction, all 52 `(Rank, Suit)` combinations
  distinct — no duplicates *within a single deck*.
- **Shoe:** exactly `N × 52` cards for *N* decks; the same card may appear up to
  *N* times across the shoe (multiple decks) — this is legal, not a violation.
- **Dealing:** a card removed from a `Deck`/`Shoe` in a given pass is not dealt
  again until that source is reset/reshuffled; the multiset of dealt cards is
  disjoint from the cards remaining in the source.
- **Omaha Hi-Lo:** hole-card count per player == 4; community board size ∈ 0..5;
  a made hand uses exactly 2 hole + exactly 3 community.
- **Board games:** community board size ∈ 0..5 and is shared by all seated
  participants.
- **Bankroll:** chip stack is non-negative (it is a passive attribute this
  phase; no debits/credits occur without betting mechanics).

## Open Questions

- **`TBD — open question`:** Blackjack **soft-17** rule — dealer stands on all 17
  vs. hits soft 17. The brief says "stand on 17"; default to stand-on-all-17 and
  expose it as a rule flag, but the definitive choice is deferred.
- **`TBD — open question`:** chip-stack representation (`long` chip units vs. a
  dedicated `Chips`/money value object) is finalized in `conventions.md`; it has
  no betting behavior attached this phase.
