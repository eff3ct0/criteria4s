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
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql"      % "<version>" // Spark SQL
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb"       % "<version>" // MongoDB
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "<version>" // Elasticsearch
```

**Requires Scala 3.6+**

## Supported Dialects

| Dialect         | Module                    | Column Quoting | Extends    |
|-----------------|---------------------------|----------------|------------|
| SQL (base)      | `criteria4s-sql`          | `'col'`        | —          |
| PostgreSQL      | `criteria4s-postgresql`   | `"col"`        | SQL        |
| Spark SQL       | `criteria4s-sparksql`     | `` `col` ``    | SQL        |
| MongoDB         | `criteria4s-mongodb`      | `"col"`        | CriteriaTag |
| Elasticsearch   | `criteria4s-elasticsearch`| `"col"`        | CriteriaTag |

## Supported Operations

| Predicate | Function | Extension | Symbol |
|-----------|----------|-----------|--------|
| EQ        | `===`    | `.===`, `.eqv` | — |
| NEQ       | `=!=`, `neq` | `.=!=`, `.neq` | — |
| GT        | `gt`     | `.gt`     | `:>`   |
| LT        | `lt`     | `.lt`     | `:<`   |
| GEQ       | `geq`    | `.geq`    | `:>=`  |
| LEQ       | `leq`    | `.leq`    | `:<=`  |
| LIKE      | `like`   | `.like`   | —      |
| IN        | `in`     | `.in`     | —      |
| NOT IN    | `notIn`  | `.notIn`  | —      |
| IS NULL   | `isNull` | `.isNull` | —      |
| IS NOT NULL | `isNotNull` | `.isNotNull` | — |
| BETWEEN   | `between`| `.between`| —      |
| NOT BETWEEN | `notBetween` | `.notBetween` | — |
| STARTS WITH | `startsWith` | `.startsWith` | — |
| ENDS WITH | `endsWith` | `.endsWith` | — |
| CONTAINS  | `contains` | `.contains` | — |
| IS TRUE   | `isTrue` | `.isTrue` | —      |
| IS FALSE  | `isFalse`| `.isFalse`| —      |
| AND       | `and`    | `.and`    | `&&`, `:&` |
| OR        | `or`     | `.or`     | `\|\|`, `:\|` |
| NOT       | `not`, `!!` | `.not` | —      |

## Quick Example

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions.*

// Define a polymorphic criteria expression
def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](implicit
    sc: Show[Column, T]
): Criteria[T] =
  (col[T]("age") geq lit(18)) and (col[T]("active") === lit(true))
```

Evaluate against different backends:

```scala
import com.eff3ct.criteria4s.dialect.sql.*
import com.eff3ct.criteria4s.dialect.mongodb.*
import com.eff3ct.criteria4s.dialect.elasticsearch.*

activeAdults[SQL]
// ('age' >= 18) AND ('active' = true)

activeAdults[MongoDB]
// {$and: [{"age": {$gte: 18}}, {"active": {$eq: true}}]}

activeAdults[Elasticsearch]
// {"bool": {"must": [{"range": {"age": {"gte": 18}}}, {"term": {"active": true}}]}}
```

### Function-Style API

```scala
val filter = and(
  gt(col("age"), lit(18)),
  or(
    isNull(col("email")),
    like(col("name"), lit("%Smith%"))
  )
)
```

### Extension / Infix Style

```scala
val filter =
  (col[SQL]("age") :> lit(18))
    .and(col[SQL]("email").isNull)
    .or(col[SQL]("name") like lit("%Smith%"))
```

## Creating a Custom Dialect

Extend `CriteriaTag` and provide implicit instances for each predicate:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

trait MyDialect extends CriteriaTag

object MyDialect {
  implicit val showColumn: Show[Column, MyDialect] =
    Show.create(col => s"[${col.colName}]")

  implicit val eqPred: EQ[MyDialect] =
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

| Module                  | Published | Dependencies |
|-------------------------|-----------|--------------|
| `criteria4s-core`       | Yes       | —            |
| `criteria4s-sql`        | Yes       | core         |
| `criteria4s-postgresql`  | Yes       | sql          |
| `criteria4s-sparksql`   | Yes       | sql          |
| `criteria4s-mongodb`    | Yes       | core         |
| `criteria4s-elasticsearch` | Yes    | core         |
| `criteria4s-examples`   | No        | all          |

## License

[MIT License](./LICENSE)
