---
sidebar_position: 2
title: PostgreSQL
---

# PostgreSQL Dialect

The PostgreSQL dialect extends the base SQL dialect with **double-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

PostgreSQL uses double-quoted identifiers, which allows reserved words and special characters in column names. This means every column reference in an expression will be wrapped in double quotes:

```scala mdoc
val column = summon[Show[Column, PostgreSQL]]
column.show(Column("user_name"))
```

```scala mdoc
F.===[PostgreSQL, Column, Int](F.col("age"), F.lit(30)).value
```

## Inherited Operations

PostgreSQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala mdoc
// Comparison
F.gt[PostgreSQL, Column, Int](F.col("age"), F.lit(18)).value
F.leq[PostgreSQL, Column, Int](F.col("score"), F.lit(100)).value

// Pattern matching
F.like[PostgreSQL, Column, String](F.col("email"), F.lit("%@gmail.com")).value

// Set membership
F.in[PostgreSQL, Column, Seq[String]](
  F.col("status"), F.array[PostgreSQL, String]("active", "pending")
).value

// Null checks
F.isNull[PostgreSQL, Column](F.col("deleted_at")).value
F.isNotNull[PostgreSQL, Column](F.col("email")).value

// Range
F.between[PostgreSQL, Column, (Int, Int)](
  F.col("age"), F.range[PostgreSQL, Int](18, 65)
).value

// Boolean
F.isTrue[PostgreSQL, Column](F.col("verified")).value
```

## Practical Examples

### User search with pagination-ready WHERE clause

```scala mdoc
val userSearch = F.col[PostgreSQL]("age")
  .geq(F.lit[PostgreSQL, Int](21))
  .and(F.col[PostgreSQL]("country") === F.lit[PostgreSQL, String]("US"))
  .and(F.col[PostgreSQL]("deleted_at").isNull)

userSearch.value
```

### Case-insensitive search using UPPER

```scala mdoc
val ciSearch = F.upper[PostgreSQL, Column](F.col("email"))
  .===(F.lit[PostgreSQL, String]("ADMIN@EXAMPLE.COM"))

ciSearch.value
```

### Active users or recent signups

```scala mdoc
val filter = F.col[PostgreSQL]("active")
  .isTrue
  .or(F.col[PostgreSQL]("created_at") :> F.lit[PostgreSQL, String]("2025-01-01"))

filter.value
```
