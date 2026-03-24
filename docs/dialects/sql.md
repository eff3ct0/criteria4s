---
sidebar_position: 1
title: SQL (Base)
---

# SQL (Base Dialect)

The base SQL dialect renders criteria as standard SQL expressions. It serves as the foundation for all SQL-based dialects -- PostgreSQL, MySQL, and SparkSQL extend it.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-sql" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

The base SQL dialect uses **unquoted** column identifiers. Column names are rendered as-is:

```scala mdoc
val column = summon[Show[Column, SQL]]
column.show(Column("user_name"))
```

String values are wrapped in single quotes, with internal single quotes escaped by doubling:

```scala mdoc
val str = summon[Show[String, SQL]]
str.show("O'Brien")
```

## Predicate Reference

### Comparison Predicates

```scala mdoc
// Equal
F.===[SQL, Column, Int](F.col("age"), F.lit(30)).value

// Not equal
F.=!=[SQL, Column, Int](F.col("age"), F.lit(30)).value

// Greater than
F.gt[SQL, Column, Int](F.col("age"), F.lit(18)).value

// Greater than or equal
F.geq[SQL, Column, Int](F.col("age"), F.lit(21)).value

// Less than
F.lt[SQL, Column, Int](F.col("age"), F.lit(65)).value

// Less than or equal
F.leq[SQL, Column, Int](F.col("age"), F.lit(99)).value
```

### Pattern Matching

```scala mdoc
// LIKE
F.like[SQL, Column, String](F.col("name"), F.lit("%John%")).value

// STARTSWITH (renders as LIKE)
F.startsWith[SQL, Column, String](F.col("name"), F.lit("A%")).value

// ENDSWITH (renders as LIKE)
F.endsWith[SQL, Column, String](F.col("name"), F.lit("%z")).value

// CONTAINS (renders as LIKE)
F.contains[SQL, Column, String](F.col("name"), F.lit("%mid%")).value
```

### Set Predicates

```scala mdoc
// IN
F.in[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](1, 2, 3)).value

// NOT IN
F.notIn[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](4, 5)).value
```

### Null Checks

```scala mdoc
// IS NULL
F.isNull[SQL, Column](F.col("email")).value

// IS NOT NULL
F.isNotNull[SQL, Column](F.col("email")).value
```

### Boolean Checks

```scala mdoc
// IS TRUE
F.isTrue[SQL, Column](F.col("active")).value

// IS FALSE
F.isFalse[SQL, Column](F.col("active")).value
```

### Range Predicates

```scala mdoc
// BETWEEN (inclusive both ends)
F.between[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](18, 65)).value

// NOT BETWEEN
F.notBetween[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](0, 17)).value
```

### Conjunctions

```scala mdoc
val left  = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
val right = F.===[SQL, Column, Int](F.col("b"), F.lit(2))

// AND
F.and[SQL](left, right).value

// OR
F.or[SQL](left, right).value

// NOT
F.not[SQL](left).value
```

### Transform Functions

```scala mdoc
// UPPER
(F.upper[SQL, Column](F.col("name")) === F.lit[SQL, String]("JOHN")).value

// LOWER
(F.lower[SQL, Column](F.col("name")) === F.lit[SQL, String]("john")).value

// TRIM
(F.trim[SQL, Column](F.col("name")) === F.lit[SQL, String]("John")).value

// COALESCE
(F.coalesce[SQL, Column](F.col("nickname"), F.col("name")) === F.lit[SQL, String]("John")).value

// CONCAT
(F.concat[SQL, String](F.lit("Hello"), F.lit(" World")) === F.lit[SQL, String]("Hello World")).value
```

## Practical Examples

### Filtering active adult users

```scala mdoc
val activeAdults = F.col[SQL]("age")
  .geq(F.lit[SQL, Int](18))
  .and(F.col[SQL]("active") === F.lit[SQL, Boolean](true))

activeAdults.value
```

### Complex WHERE clause with OR

```scala mdoc
val filter = F.col[SQL]("age")
  .gt(F.lit[SQL, Int](18))
  .and(F.col[SQL]("name").like(F.lit[SQL, String]("A%")))
  .or(F.col[SQL]("active") === F.lit[SQL, Boolean](true))

filter.value
```

### Search with IN and null check

```scala mdoc
val search = F.col[SQL]("status")
  .in(F.array[SQL, String]("active", "pending"))
  .and(F.col[SQL]("deleted_at").isNull)

search.value
```

:::note
The base SQL dialect is rarely used directly. In practice you will use one of the specialized dialects (PostgreSQL, MySQL, SparkSQL) which inherit all of these operations and add dialect-specific column quoting.
:::
