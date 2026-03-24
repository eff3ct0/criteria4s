---
sidebar_position: 5
title: MongoDB
---

# MongoDB Dialect

The MongoDB dialect renders criteria as JSON-like filter expressions using MongoDB's `$operator` syntax. Unlike the SQL family, MongoDB is **not** based on the SQL trait — it extends `CriteriaTag` directly and implements its own rendering for each predicate.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

MongoDB renders field names with double quotes, consistent with JSON:

```scala
val column = summon[Show[Column, MongoDB]]
// column: Show[Column, MongoDB] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@18da50cf
column.show(Column("user_name"))
// res0: String = "\"user_name\""
```

## Predicate Reference

### Equality and Comparison

```scala
// $eq
F.===[MongoDB, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "{\"age\": {$eq: 30}}"

// $ne
F.=!=[MongoDB, Column, Int](F.col("age"), F.lit(30)).value
// res2: String = "{\"age\": {$ne: 30}}"

// $gt
F.gt[MongoDB, Column, Int](F.col("age"), F.lit(18)).value
// res3: String = "{\"age\": {$gt: 18}}"

// $gte
F.geq[MongoDB, Column, Int](F.col("age"), F.lit(21)).value
// res4: String = "{\"age\": {$gte: 21}}"

// $lt
F.lt[MongoDB, Column, Int](F.col("age"), F.lit(65)).value
// res5: String = "{\"age\": {$lt: 65}}"

// $lte
F.leq[MongoDB, Column, Int](F.col("age"), F.lit(99)).value
// res6: String = "{\"age\": {$lte: 99}}"
```

### Set Operations

```scala
// $in
F.in[MongoDB, Column, Seq[Int]](F.col("id"), F.array[MongoDB, Int](1, 2, 3)).value
// res7: String = "{\"id\": {$in: [1, 2, 3]}}"

// $nin (NOT IN)
F.notIn[MongoDB, Column, Seq[Int]](F.col("id"), F.array[MongoDB, Int](4, 5)).value
// res8: String = "{\"id\": {$nin: [4, 5]}}"
```

### Null Checks

```scala
// IS NULL (field: null)
F.isNull[MongoDB, Column](F.col("email")).value
// res9: String = "{\"email\": null}"

// IS NOT NULL ($ne: null)
F.isNotNull[MongoDB, Column](F.col("email")).value
// res10: String = "{\"email\": {$ne: null}}"
```

### Boolean Checks

```scala
// true
F.isTrue[MongoDB, Column](F.col("active")).value
// res11: String = "{\"active\": true}"

// false
F.isFalse[MongoDB, Column](F.col("active")).value
// res12: String = "{\"active\": false}"
```

### Range Predicates (BETWEEN)

:::warning
MongoDB BETWEEN semantics differ from SQL. In SQL, `BETWEEN` is inclusive on both ends. In MongoDB, criteria4s renders it as `$gte` (inclusive left) and `$lt` (exclusive right). This follows MongoDB's conventional range query pattern.
:::

```scala
// BETWEEN: inclusive left ($gte), exclusive right ($lt)
F.between[MongoDB, Column, (Int, Int)](F.col("age"), F.range[MongoDB, Int](18, 65)).value
// res13: String = "{\"age\": { $gte: 18, $lt: 65 }}"

// NOT BETWEEN
F.notBetween[MongoDB, Column, (Int, Int)](F.col("age"), F.range[MongoDB, Int](0, 17)).value
// res14: String = "{\"age\": {$not: { $gte: 0, $lt: 17 }}}"
```

### Pattern Matching

Pattern predicates in MongoDB render as `$regex` queries:

```scala
// LIKE renders as $regex
F.like[MongoDB, Column, String](F.col("name"), F.lit("^John")).value
// res15: String = "{\"name\": {$regex: ^John}}"

// STARTSWITH renders as $regex
F.startsWith[MongoDB, Column, String](F.col("name"), F.lit("^A")).value
// res16: String = "{\"name\": {$regex: ^A}}"
```

### Logical Operators

Conjunctions in MongoDB use the `$and`, `$or` array syntax:

```scala
val left  = F.===[MongoDB, Column, Int](F.col("a"), F.lit(1))
// left: Criteria[MongoDB] = {"a": {$eq: 1}}
val right = F.===[MongoDB, Column, Int](F.col("b"), F.lit(2))
// right: Criteria[MongoDB] = {"b": {$eq: 2}}

// $and
F.and[MongoDB](left, right).value
// res17: String = "{$and: [{\"a\": {$eq: 1}}, {\"b\": {$eq: 2}}]}"

// $or
F.or[MongoDB](left, right).value
// res18: String = "{$or: [{\"a\": {$eq: 1}}, {\"b\": {$eq: 2}}]}"
```

## Practical Examples

### Find adult active users

```scala
val adults = F.col[MongoDB]("age")
  .geq(F.lit[MongoDB, Int](18))
  .and(F.col[MongoDB]("active").isTrue)
// adults: Criteria[MongoDB] = {$and: [{"age": {$gte: 18}}, {"active": true}]}

adults.value
// res19: String = "{$and: [{\"age\": {$gte: 18}}, {\"active\": true}]}"
```

### Search by status with null check

```scala
val search = F.col[MongoDB]("status")
  .in(F.array[MongoDB, String]("active", "pending"))
  .and(F.col[MongoDB]("deleted_at").isNull)
// search: Criteria[MongoDB] = {$and: [{"status": {$in: [active, pending]}}, {"deleted_at": null}]}

search.value
// res20: String = "{$and: [{\"status\": {$in: [active, pending]}}, {\"deleted_at\": null}]}"
```

### Price range filter

```scala
val priceFilter = F.col[MongoDB]("price")
  .between(F.range[MongoDB, Int](100, 500))
  .and(F.col[MongoDB]("category") === F.lit[MongoDB, String]("electronics"))
// priceFilter: Criteria[MongoDB] = {$and: [{"price": { $gte: 100, $lt: 500 }}, {"category": {$eq: electronics}}]}

priceFilter.value
// res21: String = "{$and: [{\"price\": { $gte: 100, $lt: 500 }}, {\"category\": {$eq: electronics}}]}"
```
