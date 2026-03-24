---
sidebar_position: 3
---

# Architecture

This page describes the full type hierarchy of criteria4s, from the foundational tag type through predicates, conjunctions, transforms, and clause-level constructs.

## Overview Diagram

```
CriteriaTag (phantom type)
 |
 +-- Criteria[T]              -- A rendered predicate expression
 |
 +-- Ref[T, V]                -- A typed reference to a value
 |    +-- Value[T, V]         -- Literal values (int, string, ...)
 |    +-- Col[T]              -- Column references (Ref[T, Column])
 |    +-- Collection[T, V]    -- Sequences (Ref[T, Seq[V]])
 |    +-- Range[T, V]         -- Pairs for BETWEEN (Ref[T, (V, V)])
 |    +-- Bool[T]             -- Boolean literals (also a Criteria[T])
 |
 +-- Show[V, D]               -- Type class: render V as a string in dialect D
 |
 +-- Predicate layer
 |    +-- PredicateBinary[T]  -- Two-argument predicates
 |    |    +-- EQ, NEQ, GT, LT, GEQ, LEQ
 |    |    +-- LIKE, IN, NOTIN
 |    |    +-- BETWEEN, NOTBETWEEN
 |    |    +-- STARTSWITH, ENDSWITH, CONTAINS
 |    +-- PredicateUnary[T]   -- One-argument predicates
 |         +-- ISNULL, ISNOTNULL
 |         +-- ISTRUE, ISFALSE
 |
 +-- Conjunction layer
 |    +-- ConjunctionBinary[T]  -- Two-argument connectives
 |    |    +-- AND, OR
 |    +-- ConjunctionUnary[T]   -- One-argument connectives
 |         +-- NOT
 |
 +-- Transform layer
 |    +-- TransformUnary[T]   -- Single-value transforms
 |    |    +-- UPPER, LOWER, TRIM
 |    +-- TransformBinary[T]  -- Two-value transforms
 |         +-- COALESCE, CONCAT
 |
 +-- Clause layer
 |    +-- Order[T]            -- Ordering expressions (ASC, DESC)
 |    +-- OrderAsc[T]         -- Type class for ASC rendering
 |    +-- OrderDesc[T]        -- Type class for DESC rendering
 |    +-- LimitExpr[T]        -- LIMIT clause
 |    +-- LimitBuilder[T]     -- Type class for LIMIT rendering
 |    +-- OffsetExpr[T]       -- OFFSET clause
 |    +-- OffsetBuilder[T]    -- Type class for OFFSET rendering
 |    +-- CaseExpr            -- CASE WHEN ... THEN ... ELSE ... END
 |    +-- CaseBuilder[T]      -- Type class for CASE rendering
 |
 +-- Builder layer
      +-- BuilderBinary[H]   -- Constructs H[T] from (String, String) => String
      +-- BuilderUnary[H]    -- Constructs H[T] from String => String
```

## Core Types

### CriteriaTag

The phantom type that identifies a dialect. Every dialect defines a trait that extends `CriteriaTag`. Since it carries no data, it exists purely to parameterize other types so the compiler can enforce dialect separation:

```scala
trait CriteriaTag

// Dialects
trait SQL           extends CriteriaTag
trait MongoDB       extends CriteriaTag
trait Elasticsearch extends CriteriaTag
trait PostgreSQL    extends SQL    // inherits all SQL instances
```

### Criteria[T]

The result of evaluating a predicate or conjunction. It wraps a rendered `String` value:

```scala
trait Criteria[T <: CriteriaTag]:
  def value: String
```

Instances are created internally by predicate and conjunction type classes. The `Criteria.pure` constructor is package-private to prevent arbitrary string injection — criteria should only be built through the DSL.

### Ref[T, V]

A typed reference to a value within a dialect. `Ref` is a sealed trait with several subtypes:

```scala
sealed trait Ref[D <: CriteriaTag, V]:
  def asString(using Show[V, D]): String
```

| Subtype            | Purpose                                     | Example                              |
|--------------------|---------------------------------------------|--------------------------------------|
| `Value[T, V]`     | A literal value (int, string, boolean, ...) | `F.lit[SQL, Int](42)`                |
| `Col[T]`          | A column reference                          | `F.col[SQL]("age")`                  |
| `Collection[T, V]`| A sequence of values                        | `F.array[SQL, Int](1, 2, 3)`        |
| `Range[T, V]`     | A pair of values for BETWEEN                | `F.range[SQL, Int](10, 20)`         |
| `Bool[T]`         | A boolean literal (also extends `Criteria[T]`) | `F.bool[SQL](true)`             |

### Show[V, D]

The type class that renders a value of type `V` as a string in dialect `D`:

```scala
trait Show[-V, D <: CriteriaTag]:
  def show(v: V): String
```

Each dialect provides `Show` instances for the types it supports. For example, SQL renders strings with single-quote escaping (`'O''Brien'`), while MongoDB renders column names with double quotes (`"age"`). Default instances for `String` and `AnyVal` types are provided in the `Show` companion object, so basic types work out of the box.

## Predicate Layer

Predicates are the core comparison operations. They come in two arities.

### PredicateBinary[T]

Two-argument predicates that compare a left `Ref` to a right `Ref`:

```scala
trait PredicateBinary[T <: CriteriaTag]:
  def eval[L, R](left: Ref[T, L], right: Ref[T, R])(using
      Show[L, T], Show[R, T]
  ): Criteria[T]
```

Concrete subtypes:

| Type class    | SQL rendering        | MongoDB rendering                  |
|---------------|----------------------|------------------------------------|
| `EQ[T]`       | `col = value`        | `{col: {$eq: value}}`             |
| `NEQ[T]`      | `col != value`       | `{col: {$ne: value}}`             |
| `GT[T]`       | `col > value`        | `{col: {$gt: value}}`             |
| `LT[T]`       | `col < value`        | `{col: {$lt: value}}`             |
| `GEQ[T]`      | `col >= value`       | `{col: {$gte: value}}`            |
| `LEQ[T]`      | `col <= value`       | `{col: {$lte: value}}`            |
| `LIKE[T]`     | `col LIKE pattern`   | `{col: {$regex: pattern}}`         |
| `IN[T]`       | `col IN (a, b, c)`   | `{col: {$in: [a, b, c]}}`         |
| `NOTIN[T]`    | `col NOT IN (a, b)`  | `{col: {$nin: [a, b]}}`           |
| `BETWEEN[T]`  | `col BETWEEN a AND b`| `{col: {$gte: a, $lt: b}}`        |
| `NOTBETWEEN[T]`| `col NOT BETWEEN a AND b` | `{col: {$not: {$gte: a, $lt: b}}}` |
| `STARTSWITH[T]`| `col LIKE pattern`  | `{col: {$regex: pattern}}`         |
| `ENDSWITH[T]` | `col LIKE pattern`   | `{col: {$regex: pattern}}`         |
| `CONTAINS[T]` | `col LIKE pattern`   | `{col: {$regex: pattern}}`         |

### PredicateUnary[T]

Single-argument predicates that test a property of one `Ref`:

```scala
trait PredicateUnary[T <: CriteriaTag]:
  def eval[V](ref: Ref[T, V])(using Show[V, T]): Criteria[T]
```

| Type class      | SQL rendering        | MongoDB rendering            |
|-----------------|----------------------|------------------------------|
| `ISNULL[T]`     | `col IS NULL`        | `{col: null}`                |
| `ISNOTNULL[T]`  | `col IS NOT NULL`    | `{col: {$ne: null}}`        |
| `ISTRUE[T]`     | `col IS TRUE`        | `{col: true}`               |
| `ISFALSE[T]`    | `col IS FALSE`       | `{col: false}`              |

## Conjunction Layer

Conjunctions combine or negate `Criteria[T]` values.

### ConjunctionBinary[T]

```scala
trait ConjunctionBinary[T <: CriteriaTag]:
  def eval(left: Criteria[T], right: Criteria[T]): Criteria[T]
```

| Type class | SQL rendering                    | MongoDB rendering                      |
|------------|----------------------------------|----------------------------------------|
| `AND[T]`   | `(left) AND (right)`            | `{$and: [left, right]}`               |
| `OR[T]`    | `(left) OR (right)`             | `{$or: [left, right]}`                |

### ConjunctionUnary[T]

```scala
trait ConjunctionUnary[T <: CriteriaTag]:
  def eval(expr: Criteria[T]): Criteria[T]
```

| Type class | SQL rendering     | MongoDB rendering          |
|------------|-------------------|----------------------------|
| `NOT[T]`   | `NOT (expr)`      | `{$not: expr}` (rewrites)  |

## Transform Layer

Transforms wrap `Ref` values with functions, producing a new `Ref` that can be used in predicates. Because transforms return `Ref` values, they chain freely with any predicate.

### TransformUnary[T]

```scala
trait TransformUnary[T <: CriteriaTag]:
  def apply(value: String): String
```

| Type class  | SQL rendering    |
|-------------|------------------|
| `UPPER[T]`  | `UPPER(value)`   |
| `LOWER[T]`  | `LOWER(value)`   |
| `TRIM[T]`   | `TRIM(value)`    |

### TransformBinary[T]

```scala
trait TransformBinary[T <: CriteriaTag]:
  def apply(left: String, right: String): String
```

| Type class     | SQL rendering             |
|----------------|---------------------------|
| `COALESCE[T]`  | `COALESCE(left, right)`   |
| `CONCAT[T]`    | `CONCAT(left, right)`     |

Here are two examples showing transforms composed with predicates:

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}
```

```scala mdoc
// Case-insensitive comparison using UPPER transform
val expr = F.upper(F.col[SQL]("name")) === F.lit[SQL, String]("ALICE")
expr.value

// COALESCE with fallback
val coalesced = F.coalesce(F.col[SQL]("nickname"), F.col[SQL]("name")) === F.lit[SQL, String]("Bob")
coalesced.value
```

## Clause Layer

Beyond predicates, criteria4s supports ordering, pagination, and conditional expressions.

### Order[T]

Order expressions are produced by the `OrderAsc[T]` and `OrderDesc[T]` type classes:

```scala mdoc
val ordering = F.asc[SQL, Column](F.col("name"))
ordering.value

val descOrder = F.col[SQL]("age").desc
descOrder.value
```

### LimitExpr[T] and OffsetExpr[T]

These pagination constructs produce `LimitExpr[T]` and `OffsetExpr[T]` values respectively:

```scala mdoc
F.limit[SQL](10).value
F.offset[SQL](20).value
```

### CaseExpr

A builder for CASE WHEN expressions. You start with `F.caseWhen`, add branches with `.when`, and finalize with `.otherwise`:

```scala mdoc
val grading = F.caseWhen[SQL, String](
    F.col[SQL]("score") :> F.lit[SQL, Int](90),
    F.lit[SQL, String]("A")
  )
  .when(F.col[SQL]("score") :> F.lit[SQL, Int](80), F.lit[SQL, String]("B"))
  .otherwise(F.lit[SQL, String]("C"))
grading.asString
```

## Builder Layer

The `BuilderBinary` and `BuilderUnary` traits are the mechanism that allows dialects to construct type class instances from simple string-formatting functions.

```scala
trait BuilderBinary[H[_ <: CriteriaTag]]:
  def build[T <: CriteriaTag](F: (String, String) => String): H[T]

trait BuilderUnary[H[_ <: CriteriaTag]]:
  def build[T <: CriteriaTag](F: String => String): H[T]
```

Each predicate/conjunction companion object provides a `given BuilderBinary` or `given BuilderUnary`. Dialects use the `build` helper function from the `instances` package to wire their string-formatting functions into the type class machinery:

```scala
import com.eff3ct.criteria4s.instances.*

// Inside a dialect's given definitions:
given eqPred: EQ[MyDialect] = build[MyDialect, EQ]((l, r) => s"$l = $r")
given notConj: NOT[MyDialect] = build[MyDialect, NOT](expr => s"NOT ($expr)")
```

This pattern means that creating a new dialect requires only writing the string-formatting logic — the builder infrastructure handles the rest.
