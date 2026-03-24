---
sidebar_position: 4
---

# Clauses

Beyond predicates and conjunctions, criteria4s provides non-predicate expressions for ordering,
pagination, and conditional values. These produce their own types (`Order[T]`, `LimitExpr[T]`,
`OffsetExpr[T]`, and `Ref[T, V]` for CASE) rather than `Criteria[T]`.

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Ordering

Order clauses specify how results should be sorted. They produce an `Order[T]` value.

### ASC (ascending)

```scala
// Function-style
val ascFunc = F.asc[SQL, Column](F.col("name"))
// ascFunc: Order[SQL] = name ASC
ascFunc.value
// res0: String = "name ASC"

// Extension-style
val ascExt = F.col[SQL]("name").asc
// ascExt: Order[SQL] = name ASC
ascExt.value
// res1: String = "name ASC"
```

Renders to SQL: `name ASC`

### DESC (descending)

```scala
// Function-style
val descFunc = F.desc[SQL, Column](F.col("age"))
// descFunc: Order[SQL] = age DESC
descFunc.value
// res2: String = "age DESC"

// Extension-style
val descExt = F.col[SQL]("age").desc
// descExt: Order[SQL] = age DESC
descExt.value
// res3: String = "age DESC"
```

Renders to SQL: `age DESC`

### Multiple ordering columns

You can collect several `Order[T]` values into a list for multi-column sorting:

```scala
val ordering = List(
  F.col[SQL]("last_name").asc,
  F.col[SQL]("first_name").asc,
  F.col[SQL]("created_at").desc
)
// ordering: List[Order[SQL]] = List(
//   last_name ASC,
//   first_name ASC,
//   created_at DESC
// )

ordering.map(_.value)
// res4: List[String] = List(
//   "last_name ASC",
//   "first_name ASC",
//   "created_at DESC"
// )
```

## Pagination

Pagination clauses control how many rows are returned and where reading begins.

### LIMIT

Produces a `LimitExpr[T]` that restricts the number of rows returned.

```scala
val limitExpr = F.limit[SQL](10)
// limitExpr: LimitExpr[SQL] = LIMIT 10
limitExpr.value
// res5: String = "LIMIT 10"
```

Renders to SQL: `LIMIT 10`

### OFFSET

Produces an `OffsetExpr[T]` that skips a number of rows before returning results.

```scala
val offsetExpr = F.offset[SQL](20)
// offsetExpr: OffsetExpr[SQL] = OFFSET 20
offsetExpr.value
// res6: String = "OFFSET 20"
```

Renders to SQL: `OFFSET 20`

### Combining LIMIT and OFFSET

In practice you use both together for pagination. Since they produce separate values, you
combine them however your query builder expects:

```scala
val page     = 3
// page: Int = 3
val pageSize = 25
// pageSize: Int = 25

val paginationLimit  = F.limit[SQL](pageSize)
// paginationLimit: LimitExpr[SQL] = LIMIT 25
val paginationOffset = F.offset[SQL]((page - 1) * pageSize)
// paginationOffset: OffsetExpr[SQL] = OFFSET 50

paginationLimit.value
// res7: String = "LIMIT 25"
paginationOffset.value
// res8: String = "OFFSET 50"
```

## CASE WHEN

The CASE expression lets you produce conditional values. It builds through a fluent API:

1. Start with `F.caseWhen(condition, result)` to create the first branch.
2. Add more branches with `.when(condition, result)`.
3. Finish with `.otherwise(default)` to produce a `Ref[T, V]`.

### Single branch

```scala
val singleBranch = F
  .caseWhen[SQL, Int](
    F.col[SQL]("status") === F.lit[SQL, String]("active"),
    F.lit[SQL, Int](1)
  )
  .otherwise(F.lit[SQL, Int](0))
// singleBranch: Ref[SQL, String] = com.eff3ct.criteria4s.core.Ref$$anon$7@14bbc681

singleBranch.asString
// res9: String = "CASE WHEN status = 'active' THEN 1 ELSE 0 END"
```

Renders to SQL: `CASE WHEN status = 'active' THEN 1 ELSE 0 END`

### Multiple branches

```scala
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
// gradeLabel: Ref[SQL, String] = com.eff3ct.criteria4s.core.Ref$$anon$7@39d2e9eb

gradeLabel.asString
// res10: String = "CASE WHEN score > 90 THEN 'A' WHEN score > 80 THEN 'B' WHEN score > 70 THEN 'C' ELSE 'F' END"
```

Renders to SQL: `CASE WHEN score > 90 THEN 'A' WHEN score > 80 THEN 'B' WHEN score > 70 THEN 'C' ELSE 'F' END`

### Using CASE results in predicates

Since `otherwise` returns a `Ref[T, V]`, you can use the CASE result inside other predicates:

```scala
val tierRef = F
  .caseWhen[SQL, String](
    F.col[SQL]("amount") gt F.lit[SQL, Int](1000),
    F.lit[SQL, String]("premium")
  )
  .otherwise(F.lit[SQL, String]("standard"))
// tierRef: Ref[SQL, String] = com.eff3ct.criteria4s.core.Ref$$anon$7@10a2d9ce

val isPremium = tierRef === F.lit[SQL, String]("premium")
// isPremium: Criteria[SQL] = CASE WHEN amount > 1000 THEN 'premium' ELSE 'standard' END = 'premium'
isPremium.value
// res11: String = "CASE WHEN amount > 1000 THEN 'premium' ELSE 'standard' END = 'premium'"
```

## Complete Example

Here is a realistic scenario combining criteria, ordering, and pagination to model a
paginated user search:

```scala
// Filter: active users in engineering, aged 25-55
val filter =
  (F.col[SQL]("age") between F.range[SQL, Int](25, 55))
    .and(F.col[SQL]("department") === F.lit[SQL, String]("engineering"))
    .and(F.col[SQL]("active").isTrue)
// filter: Criteria[SQL] = ((age BETWEEN 25 AND 55) AND (department = 'engineering')) AND (active IS TRUE)

// Ordering: by last name ascending, then hire date descending
val orderClauses = List(
  F.col[SQL]("last_name").asc,
  F.col[SQL]("hire_date").desc
)
// orderClauses: List[Order[SQL]] = List(last_name ASC, hire_date DESC)

// Pagination: page 2, 20 results per page
val lim = F.limit[SQL](20)
// lim: LimitExpr[SQL] = LIMIT 20
val off = F.offset[SQL](20)
// off: OffsetExpr[SQL] = OFFSET 20

// Inspect the generated expressions
filter.value
// res12: String = "((age BETWEEN 25 AND 55) AND (department = 'engineering')) AND (active IS TRUE)"
orderClauses.map(_.value)
// res13: List[String] = List("last_name ASC", "hire_date DESC")
lim.value
// res14: String = "LIMIT 20"
off.value
// res15: String = "OFFSET 20"
```

## Quick Reference

| Clause    | Function-style              | Extension-style   | SQL output               |
|-----------|-----------------------------|-------------------|--------------------------|
| ASC       | `F.asc(ref)`                | `ref.asc`         | `col ASC`                |
| DESC      | `F.desc(ref)`               | `ref.desc`        | `col DESC`               |
| LIMIT     | `F.limit(n)`                | --                | `LIMIT n`                |
| OFFSET    | `F.offset(n)`               | --                | `OFFSET n`               |
| CASE WHEN | `F.caseWhen(cond, result).when(...).otherwise(...)` | -- | `CASE WHEN ... END` |
