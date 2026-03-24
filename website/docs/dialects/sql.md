---
sidebar_position: 1
title: SQL (Base)
---

# SQL (Base Dialect)

The base SQL dialect renders criteria as standard SQL expressions. It serves as the foundation for all SQL-based dialects -- PostgreSQL, MySQL, and SparkSQL extend it.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-sql" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

The base SQL dialect uses **unquoted** column identifiers. Column names are rendered as-is:

```scala
val column = summon[Show[Column, SQL]]
// column: Show[Column, SQL] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9288778de0@778e4093
column.show(Column("user_name"))
// res0: String = "user_name"
```

String values are wrapped in single quotes, with internal single quotes escaped by doubling:

```scala
val str = summon[Show[String, SQL]]
// str: Show[String, SQL] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9288778de0@3f41145
str.show("O'Brien")
// res1: String = "'O''Brien'"
```

## Predicate Reference

### Comparison Predicates

```scala
// Equal
F.===[SQL, Column, Int](F.col("age"), F.lit(30)).value
// res2: String = "age = 30"

// Not equal
F.=!=[SQL, Column, Int](F.col("age"), F.lit(30)).value
// res3: String = "age != 30"

// Greater than
F.gt[SQL, Column, Int](F.col("age"), F.lit(18)).value
// res4: String = "age > 18"

// Greater than or equal
F.geq[SQL, Column, Int](F.col("age"), F.lit(21)).value
// res5: String = "age >= 21"

// Less than
F.lt[SQL, Column, Int](F.col("age"), F.lit(65)).value
// res6: String = "age < 65"

// Less than or equal
F.leq[SQL, Column, Int](F.col("age"), F.lit(99)).value
// res7: String = "age <= 99"
```

### Pattern Matching

```scala
// LIKE
F.like[SQL, Column, String](F.col("name"), F.lit("%John%")).value
// res8: String = "name LIKE '%John%'"

// STARTSWITH (renders as LIKE)
F.startsWith[SQL, Column, String](F.col("name"), F.lit("A%")).value
// res9: String = "name LIKE 'A%'"

// ENDSWITH (renders as LIKE)
F.endsWith[SQL, Column, String](F.col("name"), F.lit("%z")).value
// res10: String = "name LIKE '%z'"

// CONTAINS (renders as LIKE)
F.contains[SQL, Column, String](F.col("name"), F.lit("%mid%")).value
// res11: String = "name LIKE '%mid%'"
```

### Set Predicates

```scala
// IN
F.in[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](1, 2, 3)).value
// res12: String = "id IN (1, 2, 3)"

// NOT IN
F.notIn[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](4, 5)).value
// res13: String = "id NOT IN (4, 5)"
```

### Null Checks

```scala
// IS NULL
F.isNull[SQL, Column](F.col("email")).value
// res14: String = "email IS NULL"

// IS NOT NULL
F.isNotNull[SQL, Column](F.col("email")).value
// res15: String = "email IS NOT NULL"
```

### Boolean Checks

```scala
// IS TRUE
F.isTrue[SQL, Column](F.col("active")).value
// res16: String = "active IS TRUE"

// IS FALSE
F.isFalse[SQL, Column](F.col("active")).value
// res17: String = "active IS FALSE"
```

### Range Predicates

```scala
// BETWEEN (inclusive both ends)
F.between[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](18, 65)).value
// res18: String = "age BETWEEN 18 AND 65"

// NOT BETWEEN
F.notBetween[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](0, 17)).value
// res19: String = "age NOT BETWEEN 0 AND 17"
```

### Conjunctions

```scala
val left  = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
// left: Criteria[SQL] = a = 1
val right = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
// right: Criteria[SQL] = b = 2

// AND
F.and[SQL](left, right).value
// res20: String = "(a = 1) AND (b = 2)"

// OR
F.or[SQL](left, right).value
// res21: String = "(a = 1) OR (b = 2)"

// NOT
F.not[SQL](left).value
// res22: String = "NOT (a = 1)"
```

### Transform Functions

```scala
// UPPER
(F.upper[SQL, Column](F.col("name")) === F.lit[SQL, String]("JOHN")).value
// res23: String = "UPPER(name) = 'JOHN'"

// LOWER
(F.lower[SQL, Column](F.col("name")) === F.lit[SQL, String]("john")).value
// res24: String = "LOWER(name) = 'john'"

// TRIM
(F.trim[SQL, Column](F.col("name")) === F.lit[SQL, String]("John")).value
// res25: String = "TRIM(name) = 'John'"

// COALESCE
(F.coalesce[SQL, Column](F.col("nickname"), F.col("name")) === F.lit[SQL, String]("John")).value
// res26: String = "COALESCE(nickname, name) = 'John'"

// CONCAT
(F.concat[SQL, String](F.lit("Hello"), F.lit(" World")) === F.lit[SQL, String]("Hello World")).value
// res27: String = "CONCAT('Hello', ' World') = 'Hello World'"
```

## Practical Examples

### Filtering active adult users

```scala
val activeAdults = F.col[SQL]("age")
  .geq(F.lit[SQL, Int](18))
  .and(F.col[SQL]("active") === F.lit[SQL, Boolean](true))
// activeAdults: Criteria[SQL] = (age >= 18) AND (active = true)

activeAdults.value
// res28: String = "(age >= 18) AND (active = true)"
```

### Complex WHERE clause with OR

```scala
val filter = F.col[SQL]("age")
  .gt(F.lit[SQL, Int](18))
  .and(F.col[SQL]("name").like(F.lit[SQL, String]("A%")))
  .or(F.col[SQL]("active") === F.lit[SQL, Boolean](true))
// filter: Criteria[SQL] = ((age > 18) AND (name LIKE 'A%')) OR (active = true)

filter.value
// res29: String = "((age > 18) AND (name LIKE 'A%')) OR (active = true)"
```

### Search with IN and null check

```scala
val search = F.col[SQL]("status")
  .in(F.array[SQL, String]("active", "pending"))
  .and(F.col[SQL]("deleted_at").isNull)
// search: Criteria[SQL] = (status IN ('active', 'pending')) AND (deleted_at IS NULL)

search.value
// res30: String = "(status IN ('active', 'pending')) AND (deleted_at IS NULL)"
```

:::note
The base SQL dialect is rarely used directly. In practice you will use one of the specialized dialects (PostgreSQL, MySQL, SparkSQL) which inherit all of these operations and add dialect-specific column quoting.
:::
