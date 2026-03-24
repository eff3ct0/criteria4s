---
sidebar_position: 4
---

# Clauses

Beyond predicates and conjunctions, criteria4s provides non-predicate expressions for ordering,
pagination, and conditional values. These produce their own types (`Order[T]`, `LimitExpr[T]`,
`OffsetExpr[T]`, and `Ref[T, V]` for CASE) rather than `Criteria[T]`.

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Ordering

Order clauses specify how results should be sorted. They produce an `Order[T]` value that you can inspect with `.value` or pass to your query builder.

### ASC (ascending)

```scala mdoc
// Function-style
val ascFunc = F.asc[SQL, Column](F.col("name"))
ascFunc.value

// Extension-style
val ascExt = F.col[SQL]("name").asc
ascExt.value
```

Renders to SQL: `name ASC`

### DESC (descending)

```scala mdoc
// Function-style
val descFunc = F.desc[SQL, Column](F.col("age"))
descFunc.value

// Extension-style
val descExt = F.col[SQL]("age").desc
descExt.value
```

Renders to SQL: `age DESC`

### Multiple ordering columns

You can collect several `Order[T]` values into a list for multi-column sorting:

```scala mdoc
val ordering = List(
  F.col[SQL]("last_name").asc,
  F.col[SQL]("first_name").asc,
  F.col[SQL]("created_at").desc
)

ordering.map(_.value)
```

## Pagination

Pagination clauses control how many rows are returned and where reading begins.

### LIMIT

Produces a `LimitExpr[T]` that restricts the number of rows returned:

```scala mdoc
val limitExpr = F.limit[SQL](10)
limitExpr.value
```

Renders to SQL: `LIMIT 10`

### OFFSET

Produces an `OffsetExpr[T]` that skips a number of rows before returning results:

```scala mdoc
val offsetExpr = F.offset[SQL](20)
offsetExpr.value
```

Renders to SQL: `OFFSET 20`

### Combining LIMIT and OFFSET

In practice you use both together for pagination. Since they produce separate values, you combine them however your query builder expects. Here is a typical page calculation:

```scala mdoc
val page     = 3
val pageSize = 25

val paginationLimit  = F.limit[SQL](pageSize)
val paginationOffset = F.offset[SQL]((page - 1) * pageSize)

paginationLimit.value
paginationOffset.value
```

## CASE WHEN

The CASE expression lets you produce conditional values inline. It builds through a fluent API:

1. Start with `F.caseWhen(condition, result)` to create the first branch.
2. Add more branches with `.when(condition, result)`.
3. Finish with `.otherwise(default)` to produce a `Ref[T, V]`.

### Single branch

```scala mdoc
val singleBranch = F
  .caseWhen[SQL, Int](
    F.col[SQL]("status") === F.lit[SQL, String]("active"),
    F.lit[SQL, Int](1)
  )
  .otherwise(F.lit[SQL, Int](0))

singleBranch.asString
```

Renders to SQL: `CASE WHEN status = 'active' THEN 1 ELSE 0 END`

### Multiple branches

You can add as many `.when` branches as you need before calling `.otherwise`:

```scala mdoc
val gradeLabel = F
  .caseWhen[SQL, String](
    F.col[SQL]("score") gt F.lit[SQL, Int](90),
    F.lit[SQL, String]("A")
  )
  .when(
    F.col[SQL]("score") gt F.lit[SQL, Int](80),
    F.lit[SQL, String]("B")
  )
  .when(
    F.col[SQL]("score") gt F.lit[SQL, Int](70),
    F.lit[SQL, String]("C")
  )
  .otherwise(F.lit[SQL, String]("F"))

gradeLabel.asString
```

Renders to SQL: `CASE WHEN score > 90 THEN 'A' WHEN score > 80 THEN 'B' WHEN score > 70 THEN 'C' ELSE 'F' END`

### Using CASE results in predicates

Since `.otherwise` returns a `Ref[T, V]`, you can use the CASE result inside other predicates:

```scala mdoc
val tierRef = F
  .caseWhen[SQL, String](
    F.col[SQL]("amount") gt F.lit[SQL, Int](1000),
    F.lit[SQL, String]("premium")
  )
  .otherwise(F.lit[SQL, String]("standard"))

val isPremium = tierRef === F.lit[SQL, String]("premium")
isPremium.value
```

## Complete Example

Here is a realistic scenario that puts criteria, ordering, and pagination together to model a paginated user search:

```scala mdoc
// Filter: active users in engineering, aged 25-55
val filter =
  (F.col[SQL]("age") between F.range[SQL, Int](25, 55))
    .and(F.col[SQL]("department") === F.lit[SQL, String]("engineering"))
    .and(F.col[SQL]("active").isTrue)

// Ordering: by last name ascending, then hire date descending
val orderClauses = List(
  F.col[SQL]("last_name").asc,
  F.col[SQL]("hire_date").desc
)

// Pagination: page 2, 20 results per page
val lim = F.limit[SQL](20)
val off = F.offset[SQL](20)

// Inspect the generated expressions
filter.value
orderClauses.map(_.value)
lim.value
off.value
```

## Quick Reference

| Clause    | Function-style              | Extension-style   | SQL output               |
|-----------|-----------------------------|-------------------|--------------------------|
| ASC       | `F.asc(ref)`                | `ref.asc`         | `col ASC`                |
| DESC      | `F.desc(ref)`               | `ref.desc`        | `col DESC`               |
| LIMIT     | `F.limit(n)`                | (none)            | `LIMIT n`                |
| OFFSET    | `F.offset(n)`               | (none)            | `OFFSET n`               |
| CASE WHEN | `F.caseWhen(cond, result).when(...).otherwise(...)` | (none) | `CASE WHEN ... END` |
