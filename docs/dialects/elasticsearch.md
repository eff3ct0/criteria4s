---
sidebar_position: 6
title: Elasticsearch
---

# Elasticsearch Dialect

The Elasticsearch dialect renders criteria as Elasticsearch Query DSL JSON. Like MongoDB, it extends `CriteriaTag` directly rather than the SQL trait, and each predicate maps to the appropriate Elasticsearch query type.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "@VERSION@"
```

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

Elasticsearch renders field names with double quotes, consistent with JSON:

```scala mdoc
val column = summon[Show[Column, Elasticsearch]]
column.show(Column("user_name"))
```

## Predicate Reference

### Term Queries (Equality)

Equality predicates map to Elasticsearch `term` queries:

```scala mdoc
// term query
F.===[Elasticsearch, Column, Int](F.col("age"), F.lit(30)).value

// negated term (bool must_not)
F.=!=[Elasticsearch, Column, Int](F.col("age"), F.lit(30)).value
```

### Range Queries

Comparison predicates map to Elasticsearch `range` queries:

```scala mdoc
// gt
F.gt[Elasticsearch, Column, Int](F.col("age"), F.lit(18)).value

// gte
F.geq[Elasticsearch, Column, Int](F.col("age"), F.lit(21)).value

// lt
F.lt[Elasticsearch, Column, Int](F.col("age"), F.lit(65)).value

// lte
F.leq[Elasticsearch, Column, Int](F.col("age"), F.lit(99)).value
```

### Wildcard Queries (Pattern Matching)

Pattern predicates map to Elasticsearch `wildcard` queries:

```scala mdoc
// wildcard (LIKE equivalent)
F.like[Elasticsearch, Column, String](F.col("name"), F.lit("Joh*")).value

// startsWith, endsWith, contains also render as wildcard
F.startsWith[Elasticsearch, Column, String](F.col("name"), F.lit("A*")).value
```

### Terms Queries (Set Membership)

```scala mdoc
// terms query (IN)
F.in[Elasticsearch, Column, Seq[Int]](
  F.col("id"), F.array[Elasticsearch, Int](1, 2, 3)
).value

// negated terms (NOT IN)
F.notIn[Elasticsearch, Column, Seq[Int]](
  F.col("id"), F.array[Elasticsearch, Int](4, 5)
).value
```

### Exists Queries (Null Checks)

```scala mdoc
// must_not exists (IS NULL)
F.isNull[Elasticsearch, Column](F.col("email")).value

// exists (IS NOT NULL)
F.isNotNull[Elasticsearch, Column](F.col("email")).value
```

### Boolean Term Queries

```scala mdoc
// term: true
F.isTrue[Elasticsearch, Column](F.col("active")).value

// term: false
F.isFalse[Elasticsearch, Column](F.col("active")).value
```

### Range Queries (BETWEEN)

:::warning
Like MongoDB, Elasticsearch BETWEEN uses `gte` (inclusive left) and `lt` (exclusive right), which differs from SQL's fully inclusive `BETWEEN`.
:::

```scala mdoc
// BETWEEN: gte (inclusive) and lt (exclusive right)
F.between[Elasticsearch, Column, (Int, Int)](
  F.col("age"), F.range[Elasticsearch, Int](18, 65)
).value

// NOT BETWEEN (bool must_not range)
F.notBetween[Elasticsearch, Column, (Int, Int)](
  F.col("age"), F.range[Elasticsearch, Int](0, 17)
).value
```

### Bool Queries (Conjunctions)

The Elasticsearch dialect maps logical operators to the `bool` query structure. Each conjunction wraps its operands inside a `bool` query with the appropriate clause:

```scala mdoc
val left  = F.===[Elasticsearch, Column, Int](F.col("a"), F.lit(1))
val right = F.===[Elasticsearch, Column, Int](F.col("b"), F.lit(2))

// bool must (AND)
F.and[Elasticsearch](left, right).value

// bool should (OR)
F.or[Elasticsearch](left, right).value

// bool must_not (NOT)
F.not[Elasticsearch](left).value
```

## Practical Examples

### Search for active adult users

```scala mdoc
val adults = F.col[Elasticsearch]("age")
  .geq(F.lit[Elasticsearch, Int](18))
  .and(F.col[Elasticsearch]("active").isTrue)

adults.value
```

### Product catalog filter

```scala mdoc
val products = F.col[Elasticsearch]("price")
  .between(F.range[Elasticsearch, Int](100, 500))
  .and(F.col[Elasticsearch]("category") === F.lit[Elasticsearch, String]("electronics"))

products.value
```

### Find documents with missing fields

```scala mdoc
val incomplete = F.col[Elasticsearch]("email").isNull
  .or(F.col[Elasticsearch]("phone").isNull)

incomplete.value
```
