---
sidebar_position: 6
title: ClickHouse
---

# ClickHouse Dialect

The ClickHouse dialect extends the base SQL dialect with **backtick-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

[ClickHouse](https://clickhouse.com/) is a column-oriented OLAP database designed for real-time analytics on large datasets. It uses backtick-quoted identifiers (similar to MySQL), making it ideal for high-throughput analytical pipelines.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-clickhouse" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.clickhouse.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

ClickHouse uses backtick-quoted identifiers. Every column reference in an expression will be wrapped in backticks:

```scala
val column = summon[Show[Column, ClickHouse]]
// column: Show[Column, ClickHouse] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@556be7d6
column.show(Column("user_name"))
// res0: String = "`user_name`"
```

```scala
F.===[ClickHouse, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "`age` = 30"
```

## Inherited Operations

ClickHouse inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala
// Comparison
F.gt[ClickHouse, Column, Int](F.col("age"), F.lit(18)).value
// res2: String = "`age` > 18"
F.leq[ClickHouse, Column, Int](F.col("score"), F.lit(100)).value
// res3: String = "`score` <= 100"

// Pattern matching
F.like[ClickHouse, Column, String](F.col("email"), F.lit("%@gmail.com")).value
// res4: String = "`email` LIKE %@gmail.com"

// Set membership
F.in[ClickHouse, Column, Seq[String]](
  F.col("status"), F.array[ClickHouse, String]("active", "pending")
).value
// res5: String = "`status` IN (active, pending)"

// Null checks
F.isNull[ClickHouse, Column](F.col("deleted_at")).value
// res6: String = "`deleted_at` IS NULL"
F.isNotNull[ClickHouse, Column](F.col("email")).value
// res7: String = "`email` IS NOT NULL"

// Range
F.between[ClickHouse, Column, (Int, Int)](
  F.col("age"), F.range[ClickHouse, Int](18, 65)
).value
// res8: String = "`age` BETWEEN 18 AND 65"

// Boolean
F.isTrue[ClickHouse, Column](F.col("verified")).value
// res9: String = "`verified` IS TRUE"
```

## Practical Examples

### Real-time analytics filter

```scala
val analytics = F.col[ClickHouse]("event_count")
  .geq(F.lit[ClickHouse, Int](100))
  .and(F.col[ClickHouse]("event_type") === F.lit[ClickHouse, String]("page_view"))
  .and(F.col[ClickHouse]("timestamp").isNotNull)
// analytics: Criteria[ClickHouse] = ((`event_count` >= 100) AND (`event_type` = page_view)) AND (`timestamp` IS NOT NULL)

analytics.value
// res10: String = "((`event_count` >= 100) AND (`event_type` = page_view)) AND (`timestamp` IS NOT NULL)"
```

### Case-insensitive search using LOWER

```scala
val ciSearch = F.lower[ClickHouse, Column](F.col("hostname"))
  .===(F.lit[ClickHouse, String]("api.example.com"))
// ciSearch: Criteria[ClickHouse] = LOWER(`hostname`) = api.example.com

ciSearch.value
// res11: String = "LOWER(`hostname`) = api.example.com"
```

### High-throughput event filtering

```scala
val filter = F.col[ClickHouse]("status_code")
  .in(F.array[ClickHouse, Int](200, 201, 204))
  .and(F.col[ClickHouse]("response_time") :< F.lit[ClickHouse, Int](500))
  .and(F.not(F.col[ClickHouse]("is_bot").isTrue))
// filter: Criteria[ClickHouse] = ((`status_code` IN (200, 201, 204)) AND (`response_time` < 500)) AND (NOT (`is_bot` IS TRUE))

filter.value
// res12: String = "((`status_code` IN (200, 201, 204)) AND (`response_time` < 500)) AND (NOT (`is_bot` IS TRUE))"
```

:::note
ClickHouse supports most standard SQL syntax. If you need ClickHouse-specific functions (like `toDateTime`, `arrayJoin`, or `dictGet`), you can use raw expressions via `F.raw()`.
:::
