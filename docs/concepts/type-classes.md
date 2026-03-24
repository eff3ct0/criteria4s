---
sidebar_position: 1
---

# Type Classes and the Expression Problem

## The Challenge

Suppose you want to represent filter expressions that can target multiple databases. A natural first attempt uses an algebraic data type (ADT) to model the expression tree, then pattern matches over it to render each dialect.

### The Pattern Matching Approach

```scala
// A sealed ADT for expressions
enum Expr:
  case Eq(column: String, value: Any)
  case Gt(column: String, value: Any)
  case And(left: Expr, right: Expr)

// Render to SQL
def toSQL(expr: Expr): String = expr match
  case Expr.Eq(col, v)    => s"$col = $v"
  case Expr.Gt(col, v)    => s"$col > $v"
  case Expr.And(l, r)     => s"(${toSQL(l)}) AND (${toSQL(r)})"

// Render to MongoDB
def toMongo(expr: Expr): String = expr match
  case Expr.Eq(col, v)    => s"""{"$col": {"$$eq": $v}}"""
  case Expr.Gt(col, v)    => s"""{"$col": {"$$gt": $v}}"""
  case Expr.And(l, r)     => s"""{"$$and": [${toMongo(l)}, ${toMongo(r)}]}"""
```

This works for a small, fixed set of operations and dialects. But it has two scaling problems:

1. **Adding a new dialect** (e.g., Elasticsearch) requires writing a new function that pattern matches on *every* variant of `Expr`. If `Expr` has 20 variants, that is 20 new cases.
2. **Adding a new operation** (e.g., `LIKE`, `BETWEEN`) requires modifying the `Expr` enum *and* updating *every* rendering function.

This is the classic **Expression Problem**: you cannot extend both the set of operations and the set of data variants without modifying existing code.

### The Type Class Approach

criteria4s solves this with type classes. Instead of a single sealed ADT, each operation is defined as an independent trait parameterized by a dialect tag. Each dialect then provides `given` instances for the operations it supports:

```scala
// Each predicate is a trait parameterized by a tag type
trait EQ[T]:
  def eval(left: String, right: String): String

trait GT[T]:
  def eval(left: String, right: String): String

trait AND[T]:
  def eval(left: String, right: String): String
```

```scala
// SQL instances
object SQLInstances:
  given EQ[SQL] with
    def eval(l: String, r: String) = s"$l = $r"

  given GT[SQL] with
    def eval(l: String, r: String) = s"$l > $r"

  given AND[SQL] with
    def eval(l: String, r: String) = s"($l) AND ($r)"

// MongoDB instances
object MongoDBInstances:
  given EQ[MongoDB] with
    def eval(l: String, r: String) = s"""{$l: {$$eq: $r}}"""

  given GT[MongoDB] with
    def eval(l: String, r: String) = s"""{$l: {$$gt: $r}}"""

  given AND[MongoDB] with
    def eval(l: String, r: String) = s"""{$$and: [$l, $r]}"""
```

Now both dimensions are open for extension:

- **New dialect?** Write a new set of `given` instances. No existing code changes.
- **New predicate?** Define a new trait (e.g., `trait LIKE[T]`) and add `given` instances for each dialect. No existing trait or instance is modified.

## How criteria4s Uses This

In the actual criteria4s codebase, the pattern is slightly more sophisticated. The predicate traits are parameterized by `CriteriaTag` and operate on typed `Ref` values rather than raw strings:

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
```

The core type classes follow a consistent structure. Here is a simplified view of how `EQ` works:

```scala
// From the core library (simplified)
trait PredicateBinary[T <: CriteriaTag]:
  def eval[L, R](left: Ref[T, L], right: Ref[T, R])(using
      Show[L, T], Show[R, T]
  ): Criteria[T]

// EQ extends PredicateBinary
trait EQ[T <: CriteriaTag] extends PredicateBinary[T]
```

A dialect like SQL provides a `given EQ[SQL]` that renders `left = right`. MongoDB provides a `given EQ[MongoDB]` that renders `{left: {$eq: right}}`. The key point is that **the `EQ` trait itself never changes** — only new instances are added.

## Concrete Comparison

To make the difference tangible, here is the same filter built both ways.

### Pattern Matching Version

```scala
// Must modify this enum to add LIKE, BETWEEN, etc.
enum Expr:
  case Eq(col: String, value: Any)
  case And(left: Expr, right: Expr)

// Must modify this function to add Elasticsearch, etc.
def toSQL(e: Expr): String = e match
  case Expr.Eq(c, v)  => s"$c = $v"
  case Expr.And(l, r) => s"(${toSQL(l)}) AND (${toSQL(r)})"
```

### Type Class Version (criteria4s)

```scala mdoc:nest
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}

// Define once, parameterized by tag
def usersFilter[T <: CriteriaTag: EQ: AND](using Show[Column, T]): Criteria[T] =
  (F.col[T]("name") === F.lit("Alice")) and (F.col[T]("role") === F.lit("admin"))

// Evaluate against any backend
usersFilter[SQL].value
usersFilter[MongoDB].value
```

The type class version required zero changes to the core library to work with both SQL and MongoDB. And if you add Elasticsearch tomorrow, you only need to import its given instances — `usersFilter` works unchanged.

## Summary

| Dimension              | Pattern Matching                          | Type Classes (criteria4s)                 |
|------------------------|-------------------------------------------|-------------------------------------------|
| Add a new dialect      | Modify/add a rendering function           | Provide new `given` instances             |
| Add a new predicate    | Modify the ADT + all rendering functions  | Add a new trait + instances per dialect    |
| Type safety            | Runtime `Any` values                      | Compile-time checked `Ref[T, V]`          |
| Mixing dialects        | Nothing prevents it                       | Compiler error (type mismatch on `T`)     |

The type class approach trades a small amount of boilerplate (one `given` per predicate per dialect) for **open extensibility in both dimensions** and **compile-time safety** that prevents invalid cross-dialect combinations.
