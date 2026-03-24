---
sidebar_position: 6
title: Elasticsearch
---

# Elasticsearch Dialect

The Elasticsearch dialect renders criteria as Elasticsearch Query DSL JSON. Like MongoDB, it extends `CriteriaTag` directly rather than the SQL trait.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "1.0.0"
```

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

Elasticsearch renders field names with double quotes, consistent with JSON:

```scala
val column = summon[Show[Column, Elasticsearch]]
// column: Show[Column, Elasticsearch] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9288778de0@679af10d
column.show(Column("user_name"))
// res0: String = "\"user_name\""
```

## Predicate Reference

### Term Queries (Equality)

```scala
// term query
F.===[Elasticsearch, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "{\"term\": {\"age\": 30}}"

// negated term (bool must_not)
F.=!=[Elasticsearch, Column, Int](F.col("age"), F.lit(30)).value
// res2: String = "{\"bool\": {\"must_not\": [{\"term\": {\"age\": 30}}]}}"
```

### Range Queries

```scala
// gt
F.gt[Elasticsearch, Column, Int](F.col("age"), F.lit(18)).value
// res3: String = "{\"range\": {\"age\": {\"gt\": 18}}}"

// gte
F.geq[Elasticsearch, Column, Int](F.col("age"), F.lit(21)).value
// res4: String = "{\"range\": {\"age\": {\"gte\": 21}}}"

// lt
F.lt[Elasticsearch, Column, Int](F.col("age"), F.lit(65)).value
// res5: String = "{\"range\": {\"age\": {\"lt\": 65}}}"

// lte
F.leq[Elasticsearch, Column, Int](F.col("age"), F.lit(99)).value
// res6: String = "{\"range\": {\"age\": {\"lte\": 99}}}"
```

### Wildcard Queries (Pattern Matching)

```scala
// wildcard (LIKE equivalent)
F.like[Elasticsearch, Column, String](F.col("name"), F.lit("Joh*")).value
// res7: String = "{\"wildcard\": {\"name\": {\"value\": Joh*}}}"

// startsWith, endsWith, contains also render as wildcard
F.startsWith[Elasticsearch, Column, String](F.col("name"), F.lit("A*")).value
// res8: String = "{\"wildcard\": {\"name\": {\"value\": A*}}}"
```

### Terms Queries (Set Membership)

```scala
// terms query (IN)
F.in[Elasticsearch, Column, Seq[Int]](
  F.col("id"), F.array[Elasticsearch, Int](1, 2, 3)
).value
// res9: String = "{\"terms\": {\"id\": [1, 2, 3]}}"

// negated terms (NOT IN)
F.notIn[Elasticsearch, Column, Seq[Int]](
  F.col("id"), F.array[Elasticsearch, Int](4, 5)
).value
// res10: String = "{\"bool\": {\"must_not\": [{\"terms\": {\"id\": [4, 5]}}]}}"
```

### Exists Queries (Null Checks)

```scala
// must_not exists (IS NULL)
F.isNull[Elasticsearch, Column](F.col("email")).value
// res11: String = "{\"bool\": {\"must_not\": [{\"exists\": {\"field\": \"email\"}}]}}"

// exists (IS NOT NULL)
F.isNotNull[Elasticsearch, Column](F.col("email")).value
// res12: String = "{\"exists\": {\"field\": \"email\"}}"
```

### Boolean Term Queries

```scala
// term: true
F.isTrue[Elasticsearch, Column](F.col("active")).value
// res13: String = "{\"term\": {\"active\": true}}"

// term: false
F.isFalse[Elasticsearch, Column](F.col("active")).value
// res14: String = "{\"term\": {\"active\": false}}"
```

### Range Queries (BETWEEN)

```scala
// BETWEEN: gte (inclusive) and lt (exclusive right)
F.between[Elasticsearch, Column, (Int, Int)](
  F.col("age"), F.range[Elasticsearch, Int](18, 65)
).value
// res15: String = "{\"range\": {\"age\": {\"gte\": 18, \"lt\": 65}}}"

// NOT BETWEEN (bool must_not range)
F.notBetween[Elasticsearch, Column, (Int, Int)](
  F.col("age"), F.range[Elasticsearch, Int](0, 17)
).value
// res16: String = "{\"bool\": {\"must_not\": [{\"range\": {\"age\": {\"gte\": 0, \"lt\": 17}}}]}}"
```

:::warning
Like MongoDB, Elasticsearch BETWEEN uses `gte` (inclusive) and `lt` (exclusive right), which differs from SQL's fully inclusive `BETWEEN`.
:::

### Bool Queries (Conjunctions)

The Elasticsearch dialect maps logical operators to the `bool` query structure:

```scala
val left  = F.===[Elasticsearch, Column, Int](F.col("a"), F.lit(1))
// left: Criteria[Elasticsearch] = {"term": {"a": 1}}
val right = F.===[Elasticsearch, Column, Int](F.col("b"), F.lit(2))
// right: Criteria[Elasticsearch] = {"term": {"b": 2}}

// bool must (AND)
F.and[Elasticsearch](left, right).value
// res17: String = "{\"bool\": {\"must\": [{\"term\": {\"a\": 1}}, {\"term\": {\"b\": 2}}]}}"

// bool should (OR)
F.or[Elasticsearch](left, right).value
// res18: String = "{\"bool\": {\"should\": [{\"term\": {\"a\": 1}}, {\"term\": {\"b\": 2}}]}}"

// bool must_not (NOT)
F.not[Elasticsearch](left).value
// res19: String = "{\"bool\": {\"must_not\": [{\"term\": {\"a\": 1}}]}}"
```

Note the nested JSON structure: each conjunction wraps its operands inside a `bool` query with the appropriate clause (`must`, `should`, or `must_not`).

## Practical Examples

### Search for active adult users

```scala
val adults = F.col[Elasticsearch]("age")
  .geq(F.lit[Elasticsearch, Int](18))
  .and(F.col[Elasticsearch]("active").isTrue)
// adults: Criteria[Elasticsearch] = {"bool": {"must": [{"range": {"age": {"gte": 18}}}, {"term": {"active": true}}]}}

adults.value
// res20: String = "{\"bool\": {\"must\": [{\"range\": {\"age\": {\"gte\": 18}}}, {\"term\": {\"active\": true}}]}}"
```

### Product catalog filter

```scala
val products = F.col[Elasticsearch]("price")
  .between(F.range[Elasticsearch, Int](100, 500))
  .and(F.col[Elasticsearch]("category") === F.lit[Elasticsearch, String]("electronics"))
// products: Criteria[Elasticsearch] = {"bool": {"must": [{"range": {"price": {"gte": 100, "lt": 500}}}, {"term": {"category": electronics}}]}}

products.value
// res21: String = "{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": 100, \"lt\": 500}}}, {\"term\": {\"category\": electronics}}]}}"
```

### Find documents with missing fields

```scala
val incomplete = F.col[Elasticsearch]("email").isNull
  .or(F.col[Elasticsearch]("phone").isNull)
// incomplete: Criteria[Elasticsearch] = {"bool": {"should": [{"bool": {"must_not": [{"exists": {"field": "email"}}]}}, {"bool": {"must_not": [{"exists": {"field": "phone"}}]}}]}}

incomplete.value
// res22: String = "{\"bool\": {\"should\": [{\"bool\": {\"must_not\": [{\"exists\": {\"field\": \"email\"}}]}}, {\"bool\": {\"must_not\": [{\"exists\": {\"field\": \"phone\"}}]}}]}}"
```
