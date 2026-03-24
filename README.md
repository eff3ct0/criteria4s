[![CI](https://github.com/eff3ct0/criteria4s/actions/workflows/ci.yml/badge.svg)](https://github.com/eff3ct0/criteria4s/actions/workflows/ci.yml) [![Release](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml/badge.svg)](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Criteria4s

Criteria4s is a type-safe, data-store-agnostic DSL for defining criteria and predicate expressions in Scala 3. It uses a tagless-final / type-class pattern so that a single polymorphic criteria definition can be evaluated against multiple backends.

## Getting Started

Add the dependencies you need to your `build.sbt`:

```scala
// Core library (required)
libraryDependencies += "com.eff3ct" %% "criteria4s-core" % "<version>"

// Pick one or more dialect implementations
libraryDependencies += "com.eff3ct" %% "criteria4s-sql"           % "<version>" // Base SQL
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql"    % "<version>" // PostgreSQL
libraryDependencies += "com.eff3ct" %% "criteria4s-mysql"         % "<version>" // MySQL
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql"      % "<version>" // Spark SQL
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb"       % "<version>" // MongoDB
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "<version>" // Elasticsearch
```

**Requires Scala 3.6+**

## Supported Dialects

| Dialect         | Module                    | Column Quoting | Extends    |
|-----------------|---------------------------|----------------|------------|
| SQL (base)      | `criteria4s-sql`          | `col`          | —          |
| PostgreSQL      | `criteria4s-postgresql`   | `"col"`        | SQL        |
| MySQL           | `criteria4s-mysql`        | `` `col` ``    | SQL        |
| Spark SQL       | `criteria4s-sparksql`     | `` `col` ``    | SQL        |
| MongoDB         | `criteria4s-mongodb`      | `"col"`        | CriteriaTag |
| Elasticsearch   | `criteria4s-elasticsearch`| `"col"`        | CriteriaTag |

## Supported Operations

### Predicates

| Predicate     | Function     | Extension      | Symbol |
|---------------|--------------|----------------|--------|
| EQ            | `===`, `eqv` | `.===`, `.eqv` | —      |
| NEQ           | `=!=`, `neq` | `.=!=`         | —      |
| GT            | `gt`         | `.gt`          | `:>`   |
| LT            | `lt`         | `.lt`          | `:<`   |
| GEQ           | `geq`        | `.geq`         | `:>=`  |
| LEQ           | `leq`        | `.leq`         | `:<=`  |
| LIKE          | `like`       | `.like`        | —      |
| IN            | `in`         | `.in`          | —      |
| NOT IN        | `notIn`      | `.notIn`       | —      |
| IS NULL       | `isNull`     | `.isNull`      | —      |
| IS NOT NULL   | `isNotNull`  | `.isNotNull`   | —      |
| BETWEEN       | `between`    | `.between`     | —      |
| NOT BETWEEN   | `notBetween` | `.notBetween`  | —      |
| STARTS WITH   | `startsWith` | `.startsWith`  | —      |
| ENDS WITH     | `endsWith`   | `.endsWith`    | —      |
| CONTAINS      | `contains`   | `.contains`    | —      |
| IS TRUE       | `isTrue`     | `.isTrue`      | —      |
| IS FALSE      | `isFalse`    | `.isFalse`     | —      |

### Conjunctions

| Conjunction | Function    | Extension  | Symbol     |
|-------------|-------------|------------|------------|
| AND         | `and`       | `.and`     | `&&`, `:&` |
| OR          | `or`        | `.or`      | `\|\|`, `:\|` |
| NOT         | `not`, `!!` | `.not`     | —          |

### Transform Functions

| Transform  | Function      | Extension  |
|------------|---------------|------------|
| UPPER      | `upper(ref)`  | `.upper`   |
| LOWER      | `lower(ref)`  | `.lower`   |
| TRIM       | `trim(ref)`   | `.trim`    |
| COALESCE   | `coalesce(a, b)` | —       |
| CONCAT     | `concat(a, b)`   | —       |

### Clauses

| Clause     | Function        | Extension  |
|------------|-----------------|------------|
| ASC        | `asc(ref)`      | `.asc`     |
| DESC       | `desc(ref)`     | `.desc`    |
| LIMIT      | `limit(n)`      | —          |
| OFFSET     | `offset(n)`     | —          |
| CASE WHEN  | `caseWhen(cond, result).when(...).otherwise(...)` | — |

## Quick Example

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F

// Define a polymorphic criteria expression
def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using
    sc: Show[Column, T]
): Criteria[T] =
  F.and(
    F.geq(F.col("age"), F.lit(18)),
    F.===(F.col("active"), F.lit(true))
  )
```

Evaluate against different backends:

```scala
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.dialect.mongodb.{given, *}
import com.eff3ct.criteria4s.dialect.elasticsearch.{given, *}

activeAdults[SQL]
// (age >= 18) AND (active = true)

activeAdults[MongoDB]
// {$and: [{"age": {$gte: 18}}, {"active": {$eq: true}}]}

activeAdults[Elasticsearch]
// {"bool": {"must": [{"range": {"age": {"gte": 18}}}, {"term": {"active": true}}]}}
```

### Function-Style API

```scala
import com.eff3ct.criteria4s.functions as F

val filter = F.and(
  F.gt(F.col("age"), F.lit(18)),
  F.or(
    F.isNull(F.col("email")),
    F.like(F.col("name"), F.lit("%Smith%"))
  )
)
```

### Extension / Infix Style

```scala
import com.eff3ct.criteria4s.extensions.*

val filter =
  (col[SQL]("age") :> lit(18))
    .and(col[SQL]("email").isNull)
    .or(col[SQL]("name") like lit("%Smith%"))
```

### Transform Functions

```scala
import com.eff3ct.criteria4s.functions as F

// UPPER(name) = 'JOHN'
F.upper(F.col("name")) === F.lit("JOHN")

// COALESCE(nickname, name) = 'John'
F.coalesce(F.col("nickname"), F.col("name")) === F.lit("John")
```

### Ordering and Pagination

```scala
import com.eff3ct.criteria4s.functions as F

F.asc(F.col("name"))   // name ASC
F.desc(F.col("age"))   // age DESC
F.limit(10)             // LIMIT 10
F.offset(20)            // OFFSET 20
```

### CASE WHEN Expressions

```scala
import com.eff3ct.criteria4s.functions as F

val gradeRef = F.caseWhen(
  F.col[SQL]("score") gt F.lit(90), F.lit("A")
).when(
  F.col[SQL]("score") gt F.lit(80), F.lit("B")
).otherwise(F.lit("C"))
// CASE WHEN score > 90 THEN 'A' WHEN score > 80 THEN 'B' ELSE 'C' END
```

## Creating a Custom Dialect

Extend `CriteriaTag` and provide `given` instances for each predicate:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

trait MyDialect extends CriteriaTag

object MyDialect {
  given showColumn: Show[Column, MyDialect] =
    Show.create(col => s"[${col.colName}]")

  given eqPred: EQ[MyDialect] =
    build[MyDialect, EQ]((l, r) => s"$l equals $r")

  // ... add more predicates as needed
}
```

## Examples

See the [`criteria4s-examples`](./examples/src/main/scala/com/eff3ct/criteria4s/examples) module for complete examples:

- [`FunctionStyleExample`](./examples/src/main/scala/com/eff3ct/criteria4s/examples/FunctionStyleExample.scala) — all predicates via function API
- [`ExtensionStyleExample`](./examples/src/main/scala/com/eff3ct/criteria4s/examples/ExtensionStyleExample.scala) — all predicates via infix syntax
- [`PolymorphicExample`](./examples/src/main/scala/com/eff3ct/criteria4s/examples/PolymorphicExample.scala) — tagless-final across all backends

## Module Structure

| Module                     | Published | Dependencies |
|----------------------------|-----------|--------------|
| `criteria4s-core`          | Yes       | —            |
| `criteria4s-sql`           | Yes       | core         |
| `criteria4s-postgresql`    | Yes       | sql          |
| `criteria4s-mysql`         | Yes       | sql          |
| `criteria4s-sparksql`      | Yes       | sql          |
| `criteria4s-mongodb`       | Yes       | core         |
| `criteria4s-elasticsearch` | Yes       | core         |
| `criteria4s-examples`      | No        | all          |

## License

[MIT License](./LICENSE) — Copyright (c) 2024-2026 Rafael Fernandez
