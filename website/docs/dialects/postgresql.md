---
sidebar_position: 2
title: PostgreSQL
---

# PostgreSQL Dialect

The PostgreSQL dialect extends the base SQL dialect with **double-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only thing that differs from the base SQL dialect is how column names are rendered.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

PostgreSQL uses double-quoted identifiers, which allows reserved words and special characters in column names. This means every column reference in an expression will be wrapped in double quotes:

```scala
val column = summon[Show[Column, PostgreSQL]]
// column: Show[Column, PostgreSQL] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@74d1a8
column.show(Column("user_name"))
// res0: String = "\"user_name\""
```

```scala
F.===[PostgreSQL, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "\"age\" = 30"
```

## Inherited Operations

PostgreSQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala
// Comparison
F.gt[PostgreSQL, Column, Int](F.col("age"), F.lit(18)).value
// res2: String = "\"age\" > 18"
F.leq[PostgreSQL, Column, Int](F.col("score"), F.lit(100)).value
// res3: String = "\"score\" <= 100"

// Pattern matching
F.like[PostgreSQL, Column, String](F.col("email"), F.lit("%@gmail.com")).value
// res4: String = "\"email\" LIKE %@gmail.com"

// Set membership
F.in[PostgreSQL, Column, Seq[String]](
  F.col("status"), F.array[PostgreSQL, String]("active", "pending")
).value
// res5: String = "\"status\" IN (active, pending)"

// Null checks
F.isNull[PostgreSQL, Column](F.col("deleted_at")).value
// res6: String = "\"deleted_at\" IS NULL"
F.isNotNull[PostgreSQL, Column](F.col("email")).value
// res7: String = "\"email\" IS NOT NULL"

// Range
F.between[PostgreSQL, Column, (Int, Int)](
  F.col("age"), F.range[PostgreSQL, Int](18, 65)
).value
// res8: String = "\"age\" BETWEEN 18 AND 65"

// Boolean
F.isTrue[PostgreSQL, Column](F.col("verified")).value
// res9: String = "\"verified\" IS TRUE"
```

## Practical Examples

### User search with pagination-ready WHERE clause

```scala
val userSearch = F.col[PostgreSQL]("age")
  .geq(F.lit[PostgreSQL, Int](21))
  .and(F.col[PostgreSQL]("country") === F.lit[PostgreSQL, String]("US"))
  .and(F.col[PostgreSQL]("deleted_at").isNull)
// userSearch: Criteria[PostgreSQL] = (("age" >= 21) AND ("country" = US)) AND ("deleted_at" IS NULL)

userSearch.value
// res10: String = "((\"age\" >= 21) AND (\"country\" = US)) AND (\"deleted_at\" IS NULL)"
```

### Case-insensitive search using UPPER

```scala
val ciSearch = F.upper[PostgreSQL, Column](F.col("email"))
  .===(F.lit[PostgreSQL, String]("ADMIN@EXAMPLE.COM"))
// ciSearch: Criteria[PostgreSQL] = UPPER("email") = ADMIN@EXAMPLE.COM

ciSearch.value
// res11: String = "UPPER(\"email\") = ADMIN@EXAMPLE.COM"
```

### Active users or recent signups

```scala
val filter = F.col[PostgreSQL]("active")
  .isTrue
  .or(F.col[PostgreSQL]("created_at") :> F.lit[PostgreSQL, String]("2025-01-01"))
// filter: Criteria[PostgreSQL] = ("active" IS TRUE) OR ("created_at" > 2025-01-01)

filter.value
// res12: String = "(\"active\" IS TRUE) OR (\"created_at\" > 2025-01-01)"
```
