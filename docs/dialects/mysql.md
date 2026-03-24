---
sidebar_position: 3
title: MySQL
---

# MySQL Dialect

The MySQL dialect extends the base SQL dialect with **backtick-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only difference from the base SQL dialect is how column names are rendered.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-mysql" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mysql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

MySQL uses backtick-quoted identifiers, which you can see reflected in every predicate output:

```scala mdoc
val column = summon[Show[Column, MySQL]]
column.show(Column("user_name"))
```

```scala mdoc
F.===[MySQL, Column, Int](F.col("age"), F.lit(30)).value
```

## Inherited Operations

MySQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala mdoc
// Comparison
F.gt[MySQL, Column, Int](F.col("age"), F.lit(18)).value
F.leq[MySQL, Column, Int](F.col("score"), F.lit(100)).value

// Pattern matching
F.like[MySQL, Column, String](F.col("name"), F.lit("%John%")).value

// Set membership
F.in[MySQL, Column, Seq[Int]](F.col("id"), F.array[MySQL, Int](1, 2, 3)).value

// Null checks
F.isNull[MySQL, Column](F.col("deleted_at")).value

// Range
F.between[MySQL, Column, (Int, Int)](
  F.col("price"), F.range[MySQL, Int](10, 100)
).value

// Boolean
F.isTrue[MySQL, Column](F.col("active")).value
```

## Practical Examples

### Product search with price range

```scala mdoc
val productSearch = F.col[MySQL]("price")
  .between(F.range[MySQL, Int](50, 200))
  .and(F.col[MySQL]("category") === F.lit[MySQL, String]("electronics"))
  .and(F.col[MySQL]("in_stock").isTrue)

productSearch.value
```

### User lookup by email domain

```scala mdoc
val gmailUsers = F.col[MySQL]("email")
  .endsWith(F.lit[MySQL, String]("%@gmail.com"))
  .and(F.col[MySQL]("active") === F.lit[MySQL, Boolean](true))

gmailUsers.value
```

### Exclusion filter

```scala mdoc
val excluded = F.col[MySQL]("status")
  .notIn(F.array[MySQL, String]("banned", "suspended"))
  .and(F.col[MySQL]("role") =!= F.lit[MySQL, String]("guest"))

excluded.value
```
