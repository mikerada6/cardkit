# 0006 — Deck & Shoe dealing sources

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** M
**Context budget:** 30-60%
**Depends on:** 0004, 0005

## Pre-warned notes
none

## Scope
Implement the dealing sources per [data-model.md](../docs/data-model.md) §`Deck`,
§`Shoe`, and §Invariants, and [architecture.md](../docs/architecture.md)
§Randomness & Shuffle Boundary. Deliver in `core.deck`: `Deck` — one standard
52-card set (all 52 distinct `(Rank, Suit)`), with mutable draw state (shuffle
reorders; dealing removes the top card; a dealt card is not re-dealt until
reset), shuffled via an **explicit Fisher–Yates over an injected `RandomSource`**
(never its own RNG); and `Shoe` — `Shoe.ofDecks(n, randomSource)` combining
`N ≥ 1` decks (`N × 52` cards; the same card may appear up to `N` times, which is
legal). Dealing from an exhausted source throws a `core.error` domain exception
(from story 0003). Lands in `core.deck`.

## Out of scope
- `Hand` (story 0007) and any participant/session types.
- Game-specific dealing choreography (hole cards / board) — that is
  `GameSession`/`BlackjackRound` (stories 0013, 0015).

## Acceptance Criteria
- [x] `Deck` constructs with exactly 52 distinct cards (all `(Rank, Suit)`);
      no duplicates within a single deck (asserted).
- [x] Shuffling is Fisher–Yates over the injected `RandomSource`; `Deck`/`Shoe`
      never instantiate an RNG. With a `SeededRandomSource`, a shuffle is a
      reproducible **permutation** — the multiset of cards is preserved (asserted).
- [x] `Shoe.ofDecks(n, rng)` yields exactly `N × 52` cards; the same card may
      appear up to `N` times across the shoe (asserted legal, not a violation).
- [x] Dealing removes cards so the dealt multiset is disjoint from the remaining
      source; dealing from an exhausted source throws the documented domain
      exception.
- [x] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [x] Tests added/updated (unit-first; all shuffle tests inject `SeededRandomSource`).
- [x] **Best-in-class Observability (library-appropriate):**
      - SLF4J class loggers at DEBUG/TRACE (e.g. shuffle/deal detail); quiet by default.
      - Metrics/tracing: **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Files touched
- `core/src/main/java/org/rezatron/cardkit/core/deck/Deck.java` — new: 52-card deck, Fisher–Yates over injected `RandomSource`, mutable draw state, exhaustion guard.
- `core/src/main/java/org/rezatron/cardkit/core/deck/Shoe.java` — new: `Shoe.ofDecks(n, rng)`, N×52 cards, up-to-N duplicates legal, exhaustion guard.
- `core/src/main/java/org/rezatron/cardkit/core/deck/package-info.java` — new: `core.deck` package doc (design constraints, ADR-0003 pointer).
- `core/src/test/java/org/rezatron/cardkit/core/deck/DeckTest.java` — new: construction, shuffle-permutation (seeded), deal/exhaustion, reset unit tests.
- `core/src/test/java/org/rezatron/cardkit/core/deck/ShoeTest.java` — new: `ofDecks` invariants, N-duplicate legality, shuffle-permutation, deal/exhaustion, reset unit tests.
- `PLAN/0006-deck-and-shoe.md` — this story (bookkeeping).
- `PLAN/README.md` — status row 0006 → DONE (bookkeeping).
- `PLAN/structure.md` — mark `core.deck` landed (bookkeeping).

## Traceability
- Docs implemented: `docs/data-model.md §Deck`, `docs/data-model.md §Shoe`,
  `docs/data-model.md §Invariants`, `docs/architecture.md §Randomness & Shuffle Boundary`
- ADRs implemented or impacted: `docs/adr/0003-injectable-secure-randomness.md`

## Definition of Done — commit message
```
feat(core): add Deck and Shoe dealing sources

Add core.deck Deck (52 distinct cards, Fisher-Yates over injected
RandomSource, draw state) and Shoe.ofDecks(n, rng) (N x 52, up to N
duplicates allowed), with exhausted-source and invariant checks, per
docs/data-model.md (Deck, Shoe, Invariants) and ADR-0003.
```
