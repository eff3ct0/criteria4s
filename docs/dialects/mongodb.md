---
sidebar_position: 5
title: MongoDB
---

# MongoDB Dialect

The MongoDB dialect renders criteria as JSON-like filter expressions using MongoDB's `$operator` syntax. Unlike the SQL family, MongoDB is **not** based on the SQL trait -- it extends `CriteriaTag` directly.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

MongoDB renders field names with double quotes:

```scala mdoc
val column = summon[Show[Column, MongoDB]]
column.show(Column("user_name"))
```

## Predicate Reference

### Equality and Comparison

```scala mdoc
// $eq
F.===[MongoDB, Column, Int](F.col("age"), F.lit(30)).value

// $ne
F.=!=[MongoDB, Column, Int](F.col("age"), F.lit(30)).value

// $gt
F.gt[MongoDB, Column, Int](F.col("age"), F.lit(18)).value

// $gte
F.geq[MongoDB, Column, Int](F.col("age"), F.lit(21)).value

// $lt
F.lt[MongoDB, Column, Int](F.col("age"), F.lit(65)).value

// $lte
F.leq[MongoDB, Column, Int](F.col("age"), F.lit(99)).value
```

### Set Operations

```scala mdoc
// $in
F.in[MongoDB, Column, Seq[Int]](F.col("id"), F.array[MongoDB, Int](1, 2, 3)).value

// $nin (NOT IN)
F.notIn[MongoDB, Column, Seq[Int]](F.col("id"), F.array[MongoDB, Int](4, 5)).value
```

### Null Checks

```scala mdoc
// IS NULL (field: null)
F.isNull[MongoDB, Column](F.col("email")).value

// IS NOT NULL ($ne: null)
F.isNotNull[MongoDB, Column](F.col("email")).value
```

### Boolean Checks

```scala mdoc
// true
F.isTrue[MongoDB, Column](F.col("active")).value

// false
F.isFalse[MongoDB, Column](F.col("active")).value
```

### Range Predicates (BETWEEN)

```scala mdoc
// BETWEEN: inclusive left ($gte), exclusive right ($lt)
F.between[MongoDB, Column, (Int, Int)](F.col("age"), F.range[MongoDB, Int](18, 65)).value

// NOT BETWEEN
F.notBetween[MongoDB, Column, (Int, Int)](F.col("age"), F.range[MongoDB, Int](0, 17)).value
```

:::warning
MongoDB BETWEEN semantics differ from SQL. In SQL, `BETWEEN` is inclusive on both ends. In MongoDB, criteria4s renders it as `$gte` (inclusive) and `$lt` (exclusive right boundary). This follows MongoDB's conventional range query pattern.
:::

### Pattern Matching

```scala mdoc
// LIKE renders as $regex
F.like[MongoDB, Column, String](F.col("name"), F.lit("^John")).value

// STARTSWITH renders as $regex
F.startsWith[MongoDB, Column, String](F.col("name"), F.lit("^A")).value
```

### Logical Operators

```scala mdoc
val left  = F.===[MongoDB, Column, Int](F.col("a"), F.lit(1))
val right = F.===[MongoDB, Column, Int](F.col("b"), F.lit(2))

// $and
F.and[MongoDB](left, right).value

// $or
F.or[MongoDB](left, right).value
```

## Practical Examples

### Find adult active users

```scala mdoc
val adults = F.col[MongoDB]("age")
  .geq(F.lit[MongoDB, Int](18))
  .and(F.col[MongoDB]("active").isTrue)

adults.value
```

### Search by status with null check

```scala mdoc
val search = F.col[MongoDB]("status")
  .in(F.array[MongoDB, String]("active", "pending"))
  .and(F.col[MongoDB]("deleted_at").isNull)

search.value
```

### Price range filter

```scala mdoc
val priceFilter = F.col[MongoDB]("price")
  .between(F.range[MongoDB, Int](100, 500))
  .and(F.col[MongoDB]("category") === F.lit[MongoDB, String]("electronics"))

priceFilter.value
```
