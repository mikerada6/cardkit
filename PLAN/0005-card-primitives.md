# 0005 — Card primitives

**Status:** DONE   _(allowed: TODO | IN PROGRESS | DONE | BLOCKED | STALE)_
**Size:** S
**Context budget:** <30%
**Depends on:** 0003

## Pre-warned notes
none

## Scope
Model the atomic value objects of the library per
[data-model.md](../docs/data-model.md) §Core Types and
[conventions.md](../docs/conventions.md) §Domain Naming. Deliver in `core.card`:
`Suit` (enum: CLUBS, DIAMONDS, HEARTS, SPADES); `Rank` (enum TWO…TEN, JACK,
QUEEN, KING, ACE carrying ordinal strength); and `Card` (immutable
`record(Rank, Suit)`, equal-by-value). Ace strength is **context-supplied by the
evaluator** — Rank must not bake a single natural order that assumes ace-high
(ace is high for poker ranking, low for ace-to-five and the wheel). Expose the
strength data Rank needs so later rankers can order in either direction, without
implementing any evaluation here. Lands in `core.card`.

## Out of scope
- Any ranking/evaluation logic (stories 0009+).
- `Deck`/`Shoe` (story 0006) and `Hand` (story 0007).

## Acceptance Criteria
- [x] `Suit` and `Rank` enums and the `Card` record exist in `core.card`;
      `Card` is an immutable value object equal-by-value (two aces of spades are
      equal) with no notion of source deck.
- [x] `Rank` carries strength data usable in both ace-high and ace-low contexts;
      it does not hard-code a single ace-high natural order that the low ranker
      would have to fight (verified by a test that reads strength in both senses).
- [x] The 52 distinct `(Rank, Suit)` combinations are enumerable for `Deck`
      construction (a helper or the enum values suffice).
- [x] `./mvnw verify` exits 0 (fast/offline lane — unit + ArchUnit, no Docker).
- [x] Tests added/updated (unit-first): equality/immutability, all 4 suits and
      13 ranks present, strength ordering in both contexts.
- [x] **Best-in-class Observability (library-appropriate):** pure value objects;
      no loggers needed. Metrics/tracing **N/A per ADR-0001**.

## Verification commands
```bash
./mvnw -q verify > verify.log 2>&1 && tail -n5 verify.log || { cat verify.log; exit 1; }
```

## Traceability
- Docs implemented: `docs/data-model.md §Core Types`,
  `docs/conventions.md §Domain Naming`, `docs/conventions.md §Immutability & Null Policy`
- ADRs implemented or impacted: none

## Definition of Done — commit message
```
feat(core): add Card, Rank, Suit value objects

Add immutable core.card value objects: Suit and Rank enums (Rank carrying
context-aware strength for ace-high and ace-low ordering) and the Card
record (equal-by-value), per docs/data-model.md (Core Types) and
docs/conventions.md (Domain Naming, Immutability).
```
