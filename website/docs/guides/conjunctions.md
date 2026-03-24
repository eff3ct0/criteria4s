---
sidebar_position: 2
---

# Conjunctions

Conjunctions combine individual `Criteria[T]` values into compound boolean expressions.
criteria4s provides three conjunctions: **AND**, **OR**, and **NOT**.

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## AND

Combines two criteria so both must be true.

**Function-style** -- `F.and` or its symbolic alias `F.&&`:

```scala
val left  = F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
// left: Criteria[SQL] = age > 18
val right = F.===[SQL, Column, String](F.col("status"), F.lit("active"))
// right: Criteria[SQL] = status = 'active'

val andFunc = F.and[SQL](left, right)
// andFunc: Criteria[SQL] = (age > 18) AND (status = 'active')
andFunc.value
// res0: String = "(age > 18) AND (status = 'active')"

val andSymbol = F.&&[SQL](left, right)
// andSymbol: Criteria[SQL] = (age > 18) AND (status = 'active')
andSymbol.value
// res1: String = "(age > 18) AND (status = 'active')"
```

**Extension-style** -- `.and`, `.&&`, or `:&`:

```scala
val andExt = left and right
// andExt: Criteria[SQL] = (age > 18) AND (status = 'active')
andExt.value
// res2: String = "(age > 18) AND (status = 'active')"

val andOp = left && right
// andOp: Criteria[SQL] = (age > 18) AND (status = 'active')
andOp.value
// res3: String = "(age > 18) AND (status = 'active')"

val andColon = left :& right
// andColon: Criteria[SQL] = (age > 18) AND (status = 'active')
andColon.value
// res4: String = "(age > 18) AND (status = 'active')"
```

Renders to SQL: `(age > 18) AND (status = 'active')`

## OR

Combines two criteria so at least one must be true.

**Function-style** -- `F.or` or its symbolic alias `F.||`:

```scala
val orFunc = F.or[SQL](left, right)
// orFunc: Criteria[SQL] = (age > 18) OR (status = 'active')
orFunc.value
// res5: String = "(age > 18) OR (status = 'active')"

val orSymbol = F.||[SQL](left, right)
// orSymbol: Criteria[SQL] = (age > 18) OR (status = 'active')
orSymbol.value
// res6: String = "(age > 18) OR (status = 'active')"
```

**Extension-style** -- `.or`, `.||`, or `:|`:

```scala
val orExt = left or right
// orExt: Criteria[SQL] = (age > 18) OR (status = 'active')
orExt.value
// res7: String = "(age > 18) OR (status = 'active')"

val orOp = left || right
// orOp: Criteria[SQL] = (age > 18) OR (status = 'active')
orOp.value
// res8: String = "(age > 18) OR (status = 'active')"

val orColon = left :| right
// orColon: Criteria[SQL] = (age > 18) OR (status = 'active')
orColon.value
// res9: String = "(age > 18) OR (status = 'active')"
```

Renders to SQL: `(age > 18) OR (status = 'active')`

## NOT

Negates a single criteria expression.

**Function-style** -- `F.not` or its symbolic alias `F.!!`:

```scala
val notFunc = F.not[SQL](left)
// notFunc: Criteria[SQL] = NOT (age > 18)
notFunc.value
// res10: String = "NOT (age > 18)"

val notSymbol = F.!![SQL](left)
// notSymbol: Criteria[SQL] = NOT (age > 18)
notSymbol.value
// res11: String = "NOT (age > 18)"
```

**Extension-style** -- `.not`:

```scala
val notExt = left.not
// notExt: Criteria[SQL] = NOT (age > 18)
notExt.value
// res12: String = "NOT (age > 18)"
```

Renders to SQL: `NOT (age > 18)`

## Composing Complex Expressions

Conjunctions return a `Criteria[T]`, so you can chain them to build arbitrarily complex filters.
Parenthesization is handled automatically -- each conjunction wraps its operands in parentheses.

### Chaining with dot syntax

```scala
val complexChain =
  (F.col[SQL]("age") geq F.lit[SQL, Int](18))
    .and(F.col[SQL]("role") === F.lit[SQL, String]("admin"))
    .or(F.col[SQL]("superuser") === F.lit[SQL, Boolean](true))
// complexChain: Criteria[SQL] = ((age >= 18) AND (role = 'admin')) OR (superuser = true)

complexChain.value
// res13: String = "((age >= 18) AND (role = 'admin')) OR (superuser = true)"
```

This produces: `((age >= 18) AND (role = 'admin')) OR (superuser = true)`

Notice how the first `.and` produces `(age >= 18) AND (role = 'admin')`, and then `.or` wraps
that entire result as the left operand.

### Nested function calls

```scala
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
// nested: Criteria[SQL] = ((name LIKE '%John%') AND (age > 18)) OR ((email IS NULL) AND (status != 'inactive'))
nested.value
// res14: String = "((name LIKE '%John%') AND (age > 18)) OR ((email IS NULL) AND (status != 'inactive'))"
```

### Controlling precedence with explicit grouping

Because `and` and `or` have the same structural precedence in criteria4s (they are just method calls),
you control evaluation order with parentheses in your Scala code:

```scala
val a = F.col[SQL]("x") === F.lit[SQL, Int](1)
// a: Criteria[SQL] = x = 1
val b = F.col[SQL]("y") === F.lit[SQL, Int](2)
// b: Criteria[SQL] = y = 2
val c = F.col[SQL]("z") === F.lit[SQL, Int](3)
// c: Criteria[SQL] = z = 3

// (a AND b) OR c
val leftGrouped = (a and b) or c
// leftGrouped: Criteria[SQL] = ((x = 1) AND (y = 2)) OR (z = 3)
leftGrouped.value
// res15: String = "((x = 1) AND (y = 2)) OR (z = 3)"

// a AND (b OR c)
val rightGrouped = a and (b or c)
// rightGrouped: Criteria[SQL] = (x = 1) AND ((y = 2) OR (z = 3))
rightGrouped.value
// res16: String = "(x = 1) AND ((y = 2) OR (z = 3))"
```

## Real-World Example: User Search Filter

Here is a realistic filter combining multiple predicates and conjunctions:

```scala
val userFilter =
  (F.col[SQL]("age") between F.range[SQL, Int](18, 65))
    .and(F.col[SQL]("status") in F.array[SQL, String]("active", "pending"))
    .and(F.col[SQL]("email").isNotNull)
    .and(
      (F.col[SQL]("role") === F.lit[SQL, String]("admin"))
        .or(F.col[SQL]("department") === F.lit[SQL, String]("engineering"))
    )
// userFilter: Criteria[SQL] = (((age BETWEEN 18 AND 65) AND (status IN ('active', 'pending'))) AND (email IS NOT NULL)) AND ((role = 'admin') OR (department = 'engineering'))

userFilter.value
// res17: String = "(((age BETWEEN 18 AND 65) AND (status IN ('active', 'pending'))) AND (email IS NOT NULL)) AND ((role = 'admin') OR (department = 'engineering'))"
```

## Quick Reference

| Conjunction | Function-style      | Extension-style          |
|-------------|--------------------|-----------------------------|
| AND         | `F.and(a, b)` / `F.&&(a, b)` | `a and b` / `a && b` / `a :& b` |
| OR          | `F.or(a, b)` / `F.\|\|(a, b)` | `a or b` / `a \|\| b` / `a :\| b` |
| NOT         | `F.not(a)` / `F.!!(a)` | `a.not`                    |
