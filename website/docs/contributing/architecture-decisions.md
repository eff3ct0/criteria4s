---
sidebar_position: 3
title: Architecture Decisions
---

# Architecture Decisions

This page documents the key design decisions behind criteria4s and the reasoning for each.

## Why Type Classes Over ADT Pattern Matching

criteria4s uses type classes (`given` instances) to define how each predicate renders in each dialect, rather than an algebraic data type (ADT) with pattern matching.

**Rationale**: An ADT approach would require a central `render` function that matches on every dialect, meaning adding a new dialect requires modifying the core module. The type class approach allows each dialect module to provide its own rendering instances without touching core code. This follows the expression problem solution -- new data types (dialects) and new operations (predicates) can be added independently.

In practice, adding a new dialect means creating a new module with `given` instances. No existing code needs to change.

## Why String Rendering (`Criteria.value` is `String`)

`Criteria[T]` wraps a single `String` value. Predicates and conjunctions render directly to strings rather than building an intermediate AST.

**Rationale**: criteria4s is a **criteria rendering library**, not a query builder. Its job is to produce dialect-specific expression strings. An AST would add complexity without benefit since there is no query optimization, transformation, or validation pass. The string approach keeps the library simple and predictable: what you build is what you get.

This also makes integration straightforward. The JDBC module can just read `.value` to get a SQL fragment. The MongoDB driver module parses `.value` as JSON. There is no serialization step to worry about.

The tradeoff is that there is no structural validation. If a dialect has a bug in its render function, it produces a bad string. This is mitigated by comprehensive tests for every predicate in every dialect.

## Why `given` in Scala 3 (and the `final` Limitation)

All dialect instances are defined as `given` values inside traits (like `SQLExpr[T <: SQL]`). This uses Scala 3's context parameter mechanism.

**Rationale**: Scala 3's `given`/`using` pattern is the idiomatic way to provide type class instances. By putting them in a trait, SQL-based dialects can inherit all instances simply by extending the trait. The package object for each dialect extends the appropriate `Expr` trait, which exports all `given` instances.

**The `final` limitation**: Scala 3 does not allow `given` definitions in traits to be marked `final`. This means a subtype dialect could technically shadow a parent given. In practice, this is not a problem because SQL-based dialects (PostgreSQL, MySQL, SparkSQL) only override `Show` instances for column quoting -- they inherit all predicate/conjunction givens unchanged.

## Why `import functions as F`

The idiomatic import pattern uses `import com.eff3ct.criteria4s.functions as F`, giving all functions a short `F.` prefix.

**Rationale**: criteria4s provides many function names that could conflict with user code: `col`, `lit`, `and`, `or`, `not`, `in`, `gt`, `lt`, etc. Using a namespace prefix avoids collisions while keeping expressions readable:

```scala
F.col[SQL]("age") :> F.lit[SQL, Int](18)
```

The `F.` prefix also makes it immediately clear which functions come from criteria4s versus application code.

The extension method style (`col.geq(lit(...))`) does not need the `F.` prefix because extension methods are scoped through the `extensions.*` import and apply only to `Ref[T, V]` and `Criteria[T]` types.

## Why BETWEEN Semantics Differ Across Dialects

SQL `BETWEEN` is inclusive on both ends (`>= AND <=`). MongoDB and Elasticsearch BETWEEN uses `$gte`/`gte` (inclusive left) and `$lt`/`lt` (exclusive right).

**Rationale**: This follows the conventional range query pattern for each platform:

- **SQL**: The `BETWEEN` keyword is defined by the SQL standard as inclusive on both ends. Deviating would surprise SQL developers.
- **MongoDB**: Range queries conventionally use `$gte`/`$lt` (half-open intervals), which avoids off-by-one errors with pagination and is the pattern recommended in MongoDB documentation.
- **Elasticsearch**: Same rationale as MongoDB. Half-open intervals (`gte`/`lt`) are the conventional pattern for range queries in Elasticsearch.

The `Show[(V, V), T]` instance for each dialect controls this rendering:

- SQL: `Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }` -- the `BETWEEN` keyword handles inclusivity
- MongoDB: `Show.create { case (l, r) => s"{ $$gte: ${show.show(l)}, $$lt: ${show.show(r)} }" }`
- Elasticsearch: `Show.create { case (l, r) => s"""{"gte": ${show.show(l)}, "lt": ${show.show(r)}}""" }`

If you need different boundary semantics, you can compose individual `geq`/`lt`/`leq`/`gt` predicates with `and`.

## Why `Criteria.pure` is Sealed

`Criteria.pure` is `private[criteria4s]`, meaning only code within the criteria4s packages can create `Criteria` values directly from strings.

**Rationale**: This prevents arbitrary string injection from user code. If `pure` were public, users could write `Criteria.pure[SQL]("1=1; DROP TABLE users")` and bypass the entire type-safe DSL.

By restricting construction to the library internals, all `Criteria` values are guaranteed to have been built through the predicate and conjunction type classes. The only way to create a `Criteria` is by using the DSL functions (`F.===`, `F.gt`, `F.and`, etc.) or extensions (`.geq`, `.and`, etc.), which go through the proper rendering pipeline.

The `CriteriaTag` trait is similarly restricted (`private[core]`), preventing users from creating arbitrary tag types that might not have proper instances.
