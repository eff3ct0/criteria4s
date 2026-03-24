---
sidebar_position: 1
slug: /
---

# criteria4s

**A type-safe, extensible DSL for building filter and predicate expressions in Scala 3.**

## The Problem

Almost every application needs to build filter expressions: WHERE clauses for SQL databases, query documents for MongoDB, query DSL objects for Elasticsearch. The typical approach is to write these expressions directly against a specific database client library. This creates tight coupling: your business logic becomes inseparable from your storage layer, and switching databases (or supporting multiple databases) means rewriting every filter.

Even when you abstract things behind a repository pattern, the criteria themselves -- "age greater than 18 AND status equals active" -- still get expressed in a database-specific way deep inside each implementation.

## The Insight

criteria4s takes a different approach. It observes that filter expressions share a common structure across all data stores: they all have equality checks, comparisons, boolean connectives, and so on. What differs is the **syntax** -- SQL writes `age > 18`, MongoDB writes `{"age": {"$gt": 18}}`, and Elasticsearch writes `{"range": {"age": {"gt": 18}}}`.

By encoding predicate operations as **type classes** parameterized by a phantom tag type, criteria4s lets you write filter expressions once and evaluate them against any supported backend. The compiler ensures you never accidentally mix SQL and MongoDB expressions, and adding a new backend requires zero changes to existing code.

## A Quick Taste

Define a filter once, evaluate it against three different backends:

![One Criteria[T] definition evaluating to SQL, MongoDB, and Elasticsearch](/img/diagram-evaluation.svg)

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using
    Show[Column, T]
): Criteria[T] =
  (F.col[T]("age") geq F.lit(18)) and (F.col[T]("active") === F.lit(true))
```

Now evaluate it:

```scala
import com.eff3ct.criteria4s.dialect.sql.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}

// SQL
activeAdults[SQL].value
// res0: String = "(age >= 18) AND (active = true)"

// MongoDB
activeAdults[MongoDB].value
// res1: String = "{$and: [{\"age\": {$gte: 18}}, {\"active\": {$eq: true}}]}"

// Elasticsearch
activeAdults[Elasticsearch].value
// res2: String = "{\"bool\": {\"must\": [{\"range\": {\"age\": {\"gte\": 18}}}, {\"term\": {\"active\": true}}]}}"
```

One definition. Three backends. Full type safety. Zero runtime dispatch.

## Benefits

- **Type safety** -- The compiler prevents mixing expressions from different dialects. A `Criteria[SQL]` cannot be combined with a `Criteria[MongoDB]`.
- **Composability** -- Expressions compose naturally with `and`, `or`, and `not`. Build complex filters from simple building blocks.
- **Extensibility** -- Adding a new dialect means providing `given` instances for the predicate type classes. No existing code changes. Adding a new predicate means defining a new type class trait and implementing it per dialect.
- **Zero runtime dependencies** -- The core module has no external dependencies. Dialect modules only depend on the core.
- **Two API styles** -- Use function-style (`F.===`, `F.and`) or extension-style (`.===`, `.and`) depending on your preference.

## Next Steps

Head over to the [Getting Started](getting-started.md) guide to set up criteria4s in your project and write your first expressions.
