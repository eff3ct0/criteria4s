---
sidebar_position: 6
title: ClickHouse
---

# ClickHouse Dialect

The ClickHouse dialect extends the base SQL dialect with **backtick-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

[ClickHouse](https://clickhouse.com/) is a column-oriented OLAP database designed for real-time analytics on large datasets. It uses backtick-quoted identifiers (similar to MySQL), making it ideal for high-throughput analytical pipelines.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-clickhouse" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.clickhouse.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

ClickHouse uses backtick-quoted identifiers. Every column reference in an expression will be wrapped in backticks:

```scala mdoc
val column = summon[Show[Column, ClickHouse]]
column.show(Column("user_name"))
```

```scala mdoc
F.===[ClickHouse, Column, Int](F.col("age"), F.lit(30)).value
```

## Inherited Operations

ClickHouse inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala mdoc
// Comparison
F.gt[ClickHouse, Column, Int](F.col("age"), F.lit(18)).value
F.leq[ClickHouse, Column, Int](F.col("score"), F.lit(100)).value

// Pattern matching
F.like[ClickHouse, Column, String](F.col("email"), F.lit("%@gmail.com")).value

// Set membership
F.in[ClickHouse, Column, Seq[String]](
  F.col("status"), F.array[ClickHouse, String]("active", "pending")
).value

// Null checks
F.isNull[ClickHouse, Column](F.col("deleted_at")).value
F.isNotNull[ClickHouse, Column](F.col("email")).value

// Range
F.between[ClickHouse, Column, (Int, Int)](
  F.col("age"), F.range[ClickHouse, Int](18, 65)
).value

// Boolean
F.isTrue[ClickHouse, Column](F.col("verified")).value
```

## Practical Examples

### Real-time analytics filter

```scala mdoc
val analytics = F.col[ClickHouse]("event_count")
  .geq(F.lit[ClickHouse, Int](100))
  .and(F.col[ClickHouse]("event_type") === F.lit[ClickHouse, String]("page_view"))
  .and(F.col[ClickHouse]("timestamp").isNotNull)

analytics.value
```

### Case-insensitive search using LOWER

```scala mdoc
val ciSearch = F.lower[ClickHouse, Column](F.col("hostname"))
  .===(F.lit[ClickHouse, String]("api.example.com"))

ciSearch.value
```

### High-throughput event filtering

```scala mdoc
val filter = F.col[ClickHouse]("status_code")
  .in(F.array[ClickHouse, Int](200, 201, 204))
  .and(F.col[ClickHouse]("response_time") :< F.lit[ClickHouse, Int](500))
  .and(F.not(F.col[ClickHouse]("is_bot").isTrue))

filter.value
```

:::note
ClickHouse supports most standard SQL syntax. If you need ClickHouse-specific functions (like `toDateTime`, `arrayJoin`, or `dictGet`), you can use raw expressions via `F.raw()`.
:::
