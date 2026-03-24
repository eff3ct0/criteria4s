---
sidebar_position: 3
title: MySQL
---

# MySQL Dialect

The MySQL dialect extends the base SQL dialect with **backtick-quoted** column identifiers. It inherits all SQL predicates, conjunctions, transforms, and clauses — the only difference from the base SQL dialect is how column names are rendered.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-mysql" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mysql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

MySQL uses backtick-quoted identifiers, which you can see reflected in every predicate output:

```scala
val column = summon[Show[Column, MySQL]]
// column: Show[Column, MySQL] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@60764419
column.show(Column("user_name"))
// res0: String = "`user_name`"
```

```scala
F.===[MySQL, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "`age` = 30"
```

## Inherited Operations

MySQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala
// Comparison
F.gt[MySQL, Column, Int](F.col("age"), F.lit(18)).value
// res2: String = "`age` > 18"
F.leq[MySQL, Column, Int](F.col("score"), F.lit(100)).value
// res3: String = "`score` <= 100"

// Pattern matching
F.like[MySQL, Column, String](F.col("name"), F.lit("%John%")).value
// res4: String = "`name` LIKE %John%"

// Set membership
F.in[MySQL, Column, Seq[Int]](F.col("id"), F.array[MySQL, Int](1, 2, 3)).value
// res5: String = "`id` IN (1, 2, 3)"

// Null checks
F.isNull[MySQL, Column](F.col("deleted_at")).value
// res6: String = "`deleted_at` IS NULL"

// Range
F.between[MySQL, Column, (Int, Int)](
  F.col("price"), F.range[MySQL, Int](10, 100)
).value
// res7: String = "`price` BETWEEN 10 AND 100"

// Boolean
F.isTrue[MySQL, Column](F.col("active")).value
// res8: String = "`active` IS TRUE"
```

## Practical Examples

### Product search with price range

```scala
val productSearch = F.col[MySQL]("price")
  .between(F.range[MySQL, Int](50, 200))
  .and(F.col[MySQL]("category") === F.lit[MySQL, String]("electronics"))
  .and(F.col[MySQL]("in_stock").isTrue)
// productSearch: Criteria[MySQL] = ((`price` BETWEEN 50 AND 200) AND (`category` = electronics)) AND (`in_stock` IS TRUE)

productSearch.value
// res9: String = "((`price` BETWEEN 50 AND 200) AND (`category` = electronics)) AND (`in_stock` IS TRUE)"
```

### User lookup by email domain

```scala
val gmailUsers = F.col[MySQL]("email")
  .endsWith(F.lit[MySQL, String]("%@gmail.com"))
  .and(F.col[MySQL]("active") === F.lit[MySQL, Boolean](true))
// gmailUsers: Criteria[MySQL] = (`email` LIKE %@gmail.com) AND (`active` = true)

gmailUsers.value
// res10: String = "(`email` LIKE %@gmail.com) AND (`active` = true)"
```

### Exclusion filter

```scala
val excluded = F.col[MySQL]("status")
  .notIn(F.array[MySQL, String]("banned", "suspended"))
  .and(F.col[MySQL]("role") =!= F.lit[MySQL, String]("guest"))
// excluded: Criteria[MySQL] = (`status` NOT IN (banned, suspended)) AND (`role` != guest)

excluded.value
// res11: String = "(`status` NOT IN (banned, suspended)) AND (`role` != guest)"
```
