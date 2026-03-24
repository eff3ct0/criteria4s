---
sidebar_position: 5
title: DuckDB
---

# DuckDB Dialect

The DuckDB dialect extends the base SQL dialect with **double-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

[DuckDB](https://duckdb.org/) is an in-process analytical database designed for fast OLAP queries. It uses standard SQL with double-quoted identifiers, making its dialect nearly identical to PostgreSQL in terms of expression syntax.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-duckdb" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.duckdb.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

DuckDB uses double-quoted identifiers, following the SQL standard. Every column reference in an expression will be wrapped in double quotes:

```scala mdoc
val column = summon[Show[Column, DuckDB]]
column.show(Column("user_name"))
```

```scala mdoc
F.===[DuckDB, Column, Int](F.col("age"), F.lit(30)).value
```

## Inherited Operations

DuckDB inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala mdoc
// Comparison
F.gt[DuckDB, Column, Int](F.col("age"), F.lit(18)).value
F.leq[DuckDB, Column, Int](F.col("score"), F.lit(100)).value

// Pattern matching
F.like[DuckDB, Column, String](F.col("email"), F.lit("%@gmail.com")).value

// Set membership
F.in[DuckDB, Column, Seq[String]](
  F.col("status"), F.array[DuckDB, String]("active", "pending")
).value

// Null checks
F.isNull[DuckDB, Column](F.col("deleted_at")).value
F.isNotNull[DuckDB, Column](F.col("email")).value

// Range
F.between[DuckDB, Column, (Int, Int)](
  F.col("age"), F.range[DuckDB, Int](18, 65)
).value

// Boolean
F.isTrue[DuckDB, Column](F.col("verified")).value
```

## Practical Examples

### Analytical query filter for OLAP workloads

```scala mdoc
val olap = F.col[DuckDB]("revenue")
  .geq(F.lit[DuckDB, Int](1000))
  .and(F.col[DuckDB]("region") === F.lit[DuckDB, String]("EMEA"))
  .and(F.col[DuckDB]("quarter").isNotNull)

olap.value
```

### Case-insensitive search using UPPER

```scala mdoc
val ciSearch = F.upper[DuckDB, Column](F.col("category"))
  .===(F.lit[DuckDB, String]("ELECTRONICS"))

ciSearch.value
```

### Filter with set membership and range

```scala mdoc
val filter = F.col[DuckDB]("status")
  .in(F.array[DuckDB, String]("shipped", "delivered"))
  .and(F.col[DuckDB]("amount").between(F.range[DuckDB, Int](100, 5000)))

filter.value
```
