---
sidebar_position: 2
---

# Conjunctions

Conjunctions combine individual `Criteria[T]` values into compound boolean expressions.
criteria4s provides three conjunctions: **AND**, **OR**, and **NOT**.

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## AND

Combines two criteria so both must be true.

**Function-style** with `F.and` or its symbolic alias `F.&&`:

```scala mdoc
val left  = F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
val right = F.===[SQL, Column, String](F.col("status"), F.lit("active"))

val andFunc = F.and[SQL](left, right)
andFunc.value

val andSymbol = F.&&[SQL](left, right)
andSymbol.value
```

**Extension-style** with `.and`, `.&&`, or `:&`:

```scala mdoc
val andExt = left and right
andExt.value

val andOp = left && right
andOp.value

val andColon = left :& right
andColon.value
```

Renders to SQL: `(age > 18) AND (status = 'active')`

## OR

Combines two criteria so at least one must be true.

**Function-style** with `F.or` or its symbolic alias `F.||`:

```scala mdoc
val orFunc = F.or[SQL](left, right)
orFunc.value

val orSymbol = F.||[SQL](left, right)
orSymbol.value
```

**Extension-style** with `.or`, `.||`, or `:|`:

```scala mdoc
val orExt = left or right
orExt.value

val orOp = left || right
orOp.value

val orColon = left :| right
orColon.value
```

Renders to SQL: `(age > 18) OR (status = 'active')`

## NOT

Negates a single criteria expression.

**Function-style** with `F.not` or its symbolic alias `F.!!`:

```scala mdoc
val notFunc = F.not[SQL](left)
notFunc.value

val notSymbol = F.!![SQL](left)
notSymbol.value
```

**Extension-style** with `.not`:

```scala mdoc
val notExt = left.not
notExt.value
```

Renders to SQL: `NOT (age > 18)`

## Composing Complex Expressions

Conjunctions return a `Criteria[T]`, so you can chain them to build arbitrarily complex filters.
Parenthesization is handled automatically — each conjunction wraps its operands in parentheses.

### Chaining with dot syntax

When you chain `.and` and `.or` calls, each step builds on the previous result. Notice how the first `.and` produces `(age >= 18) AND (role = 'admin')`, and then `.or` wraps that entire result as the left operand:

```scala mdoc
val complexChain =
  (F.col[SQL]("age") geq F.lit[SQL, Int](18))
    .and(F.col[SQL]("role") === F.lit[SQL, String]("admin"))
    .or(F.col[SQL]("superuser") === F.lit[SQL, Boolean](true))

complexChain.value
```

This produces: `((age >= 18) AND (role = 'admin')) OR (superuser = true)`

### Nested function calls

The function-style API makes deeply nested boolean logic read clearly, since the nesting is explicit in the code structure:

```scala mdoc
val nested = F.or[SQL](
  F.and[SQL](
    F.like[SQL, Column, String](F.col("name"), F.lit("%John%")),
    F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
  ),
  F.and[SQL](
    F.isNull[SQL, Column](F.col("email")),
    F.neq[SQL, Column, String](F.col("status"), F.lit("inactive"))
  )
)
nested.value
```

### Controlling precedence with explicit grouping

Because `and` and `or` have the same structural precedence in criteria4s (they are just method calls), you control evaluation order with parentheses in your Scala code:

```scala mdoc
val a = F.col[SQL]("x") === F.lit[SQL, Int](1)
val b = F.col[SQL]("y") === F.lit[SQL, Int](2)
val c = F.col[SQL]("z") === F.lit[SQL, Int](3)

// (a AND b) OR c
val leftGrouped = (a and b) or c
leftGrouped.value

// a AND (b OR c)
val rightGrouped = a and (b or c)
rightGrouped.value
```

## Real-World Example: User Search Filter

Here is a realistic filter combining multiple predicates and conjunctions. It finds users who are in a valid age range, have an allowed status, have a non-null email, and belong to either the admin role or the engineering department:

```scala mdoc
val userFilter =
  (F.col[SQL]("age") between F.range[SQL, Int](18, 65))
    .and(F.col[SQL]("status") in F.array[SQL, String]("active", "pending"))
    .and(F.col[SQL]("email").isNotNull)
    .and(
      (F.col[SQL]("role") === F.lit[SQL, String]("admin"))
        .or(F.col[SQL]("department") === F.lit[SQL, String]("engineering"))
    )

userFilter.value
```

## Quick Reference

| Conjunction | Function-style      | Extension-style          |
|-------------|--------------------|-----------------------------|
| AND         | `F.and(a, b)` / `F.&&(a, b)` | `a and b` / `a && b` / `a :& b` |
| OR          | `F.or(a, b)` / `F.\|\|(a, b)` | `a or b` / `a \|\| b` / `a :\| b` |
| NOT         | `F.not(a)` / `F.!!(a)` | `a.not`                    |
