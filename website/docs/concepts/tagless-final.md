---
sidebar_position: 2
---

# Tagless Final Pattern

## What Tagless Final Means Here

The term "tagless final" comes from the programming language theory world, where it describes a technique for embedding DSLs using type class constraints instead of algebraic data types. In criteria4s, it means:

1. **Tagless** -- There is no sealed ADT (no "tags" in a sum type). Expressions are not data you pattern match on; they are produced by calling type class methods.
2. **Final** -- Expressions are interpreted immediately when constructed. There is no intermediate AST that gets transformed later. When you call `F.col[SQL]("age") :> F.lit(18)`, the result is already the string `"age > 18"` wrapped in a `Criteria[SQL]`.

The practical benefit is that a single function definition, parameterized by a tag type `T`, can produce results for any dialect that provides the required type class instances.

## The CriteriaTag Phantom Type

At the center of the design is `CriteriaTag`, a trait that serves as a phantom type -- it exists only at the type level and carries no runtime data:

```scala
// From the core library
trait Criteria[T <: CriteriaTag]:
  def value: String
```

Each dialect defines its own tag that extends `CriteriaTag`:

```scala
// In the sql module
trait SQL extends CriteriaTag

// In the mongodb module
trait MongoDB extends CriteriaTag

// In the elasticsearch module
trait Elasticsearch extends CriteriaTag
```

The tag flows through the type system to prevent mixing:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
```

```scala
// This compiles: both sides are SQL
val sqlExpr = (F.col[SQL]("a") === F.lit[SQL, Int](1)) and (F.col[SQL]("b") === F.lit[SQL, Int](2))
// sqlExpr: Criteria[SQL] = (a = 1) AND (b = 2)
sqlExpr.value
// res0: String = "(a = 1) AND (b = 2)"
```

But trying to combine a `Criteria[SQL]` with a `Criteria[MongoDB]` will not compile. The `and` conjunction requires both arguments to share the same tag type `T`, and `SQL` is not `MongoDB`.

```scala
// This will NOT compile:
// val mixed = sqlCriteria and mongoCriteria
// Error: type mismatch; found Criteria[MongoDB], required Criteria[SQL]
```

This compile-time guarantee eliminates an entire class of bugs where dialect-specific expressions leak across boundaries.

## How `Criteria[T]` Carries the Tag

Every value in the DSL is tagged with the dialect:

- `Ref[T, V]` -- A reference (column, literal, collection) tagged with dialect `T` and value type `V`
- `Criteria[T]` -- A complete predicate expression tagged with dialect `T`
- `Order[T]` -- An ordering expression tagged with dialect `T`

When you write `F.col[SQL]("age")`, you get a `Ref.Col[SQL]`. When you compare it with `F.lit[SQL, Int](18)`, the `gt` type class instance for `SQL` produces a `Criteria[SQL]`. The tag never disappears -- it propagates through every operation.

## Writing Polymorphic Functions

The power of the tagless final pattern emerges when you write functions that are **polymorphic in the tag type**. Instead of fixing a specific dialect, you constrain `T` with the type classes your function needs:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using
    Show[Column, T]
): Criteria[T] =
  (F.col[T]("age") geq F.lit(18)) and (F.col[T]("active") === F.lit(true))
```

Breaking down the signature:

- `T <: CriteriaTag` -- `T` must be some dialect tag
- `: GEQ` -- There must be a `given GEQ[T]` in scope (for the `>=` comparison)
- `: EQ` -- There must be a `given EQ[T]` in scope (for the `=` comparison)
- `: AND` -- There must be a `given AND[T]` in scope (for boolean conjunction)
- `Show[Column, T]` -- There must be a way to render column names in dialect `T`

This function compiles against any `T` that satisfies these constraints. The caller decides which dialect to use by supplying a concrete type argument:

```scala
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}

activeAdults[SQL].value
// res1: String = "(age >= 18) AND (active = true)"
activeAdults[MongoDB].value
// res2: String = "{$and: [{\"age\": {$gte: 18}}, {\"active\": {$eq: true}}]}"
activeAdults[Elasticsearch].value
// res3: String = "{\"bool\": {\"must\": [{\"range\": {\"age\": {\"gte\": 18}}}, {\"term\": {\"active\": true}}]}}"
```

## A Worked Example: Define Once, Evaluate Against Three Backends

Here is a more realistic polymorphic filter that combines multiple predicates:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

def premiumUsers[T <: CriteriaTag: EQ: GT: ISNOTNULL: AND: OR](using
    Show[Column, T]
): Criteria[T] =
  val isPremium   = F.col[T]("plan") === F.lit("premium")
  val highSpender = F.col[T]("total_spent") :> F.lit(1000)
  val hasEmail    = F.col[T]("email").isNotNull
  (isPremium or highSpender) and hasEmail
```

```scala
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}

premiumUsers[SQL].value
// res4: String = "((plan = premium) OR (total_spent > 1000)) AND (email IS NOT NULL)"
premiumUsers[MongoDB].value
// res5: String = "{$and: [{$or: [{\"plan\": {$eq: premium}}, {\"total_spent\": {$gt: 1000}}]}, {\"email\": {$ne: null}}]}"
premiumUsers[Elasticsearch].value
// res6: String = "{\"bool\": {\"must\": [{\"bool\": {\"should\": [{\"term\": {\"plan\": premium}}, {\"range\": {\"total_spent\": {\"gt\": 1000}}}]}}, {\"exists\": {\"field\": \"email\"}}]}}"
```

The function `premiumUsers` has no dependency on any specific database library. It compiles to the correct syntax for each backend purely through type class resolution at compile time.

## When to Use Polymorphic Definitions

Not every expression needs to be polymorphic. If you know your application only targets PostgreSQL, write your criteria directly with `[PostgreSQL]` and keep things simple. The polymorphic style shines when:

- You need to support **multiple backends** (e.g., SQL for reporting, MongoDB for real-time queries).
- You are building a **library or shared module** that downstream consumers should be able to target to their own data store.
- You want to **test** your filter logic by evaluating against a simple dialect (like SQL) and asserting on the string output, while running the same filter in production against a different backend.

In all these cases, the tagless final approach lets you write the filter logic exactly once and defer the dialect choice to the call site.
