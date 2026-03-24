---
sidebar_position: 5
title: DuckDB
---

# DuckDB Dialect

The DuckDB dialect extends the base SQL dialect with **double-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

[DuckDB](https://duckdb.org/) is an in-process analytical database designed for fast OLAP queries. It uses standard SQL with double-quoted identifiers, making its dialect nearly identical to PostgreSQL in terms of expression syntax.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-duckdb" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.duckdb.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

DuckDB uses double-quoted identifiers, following the SQL standard. Every column reference in an expression will be wrapped in double quotes:

```scala
val column = summon[Show[Column, DuckDB]]
// column: Show[Column, DuckDB] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@5fc11122
column.show(Column("user_name"))
// res0: String = "\"user_name\""
```

```scala
F.===[DuckDB, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "\"age\" = 30"
```

## Inherited Operations

DuckDB inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala
// Comparison
F.gt[DuckDB, Column, Int](F.col("age"), F.lit(18)).value
// res2: String = "\"age\" > 18"
F.leq[DuckDB, Column, Int](F.col("score"), F.lit(100)).value
// res3: String = "\"score\" <= 100"

// Pattern matching
F.like[DuckDB, Column, String](F.col("email"), F.lit("%@gmail.com")).value
// res4: String = "\"email\" LIKE %@gmail.com"

// Set membership
F.in[DuckDB, Column, Seq[String]](
  F.col("status"), F.array[DuckDB, String]("active", "pending")
).value
// res5: String = "\"status\" IN (active, pending)"

// Null checks
F.isNull[DuckDB, Column](F.col("deleted_at")).value
// res6: String = "\"deleted_at\" IS NULL"
F.isNotNull[DuckDB, Column](F.col("email")).value
// res7: String = "\"email\" IS NOT NULL"

// Range
F.between[DuckDB, Column, (Int, Int)](
  F.col("age"), F.range[DuckDB, Int](18, 65)
).value
// res8: String = "\"age\" BETWEEN 18 AND 65"

// Boolean
F.isTrue[DuckDB, Column](F.col("verified")).value
// res9: String = "\"verified\" IS TRUE"
```

## Practical Examples

### Analytical query filter for OLAP workloads

```scala
val olap = F.col[DuckDB]("revenue")
  .geq(F.lit[DuckDB, Int](1000))
  .and(F.col[DuckDB]("region") === F.lit[DuckDB, String]("EMEA"))
  .and(F.col[DuckDB]("quarter").isNotNull)
// olap: Criteria[DuckDB] = (("revenue" >= 1000) AND ("region" = EMEA)) AND ("quarter" IS NOT NULL)

olap.value
// res10: String = "((\"revenue\" >= 1000) AND (\"region\" = EMEA)) AND (\"quarter\" IS NOT NULL)"
```

### Case-insensitive search using UPPER

```scala
val ciSearch = F.upper[DuckDB, Column](F.col("category"))
  .===(F.lit[DuckDB, String]("ELECTRONICS"))
// ciSearch: Criteria[DuckDB] = UPPER("category") = ELECTRONICS

ciSearch.value
// res11: String = "UPPER(\"category\") = ELECTRONICS"
```

### Filter with set membership and range

```scala
val filter = F.col[DuckDB]("status")
  .in(F.array[DuckDB, String]("shipped", "delivered"))
  .and(F.col[DuckDB]("amount").between(F.range[DuckDB, Int](100, 5000)))
// filter: Criteria[DuckDB] = ("status" IN (shipped, delivered)) AND ("amount" BETWEEN 100 AND 5000)

filter.value
// res12: String = "(\"status\" IN (shipped, delivered)) AND (\"amount\" BETWEEN 100 AND 5000)"
```
