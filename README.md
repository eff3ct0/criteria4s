[![CI](https://github.com/eff3ct0/criteria4s/actions/workflows/ci.yml/badge.svg)](https://github.com/eff3ct0/criteria4s/actions/workflows/ci.yml) [![Release](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml/badge.svg)](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Criteria4s

Type-safe, data-store-agnostic filter expressions for Scala 3.

Write a predicate once and evaluate it against SQL, MongoDB, Elasticsearch, or any custom backend — with full compiler enforcement and zero runtime dispatch.

**[Documentation](https://eff3ct0.github.io/criteria4s/)**

## Quick Start

```scala
// build.sbt
libraryDependencies += "com.eff3ct" %% "criteria4s-core"       % "<version>"
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql"  % "<version>" // or any dialect
```

Define a filter once:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*

def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using Show[Column, T]): Criteria[T] =
  F.col[T]("age").geq(F.lit(18)) and (F.col[T]("active") === F.lit(true))
```

Evaluate against any backend:

```scala
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.dialect.mongodb.{given, *}

activeAdults[SQL].value
// (age >= 18) AND (active = true)

activeAdults[MongoDB].value
// {$and: [{"age": {$gte: 18}}, {"active": {$eq: true}}]}
```

## Dialects

| Module                      | Backend       |
|-----------------------------|---------------|
| `criteria4s-sql`            | SQL (base)    |
| `criteria4s-postgresql`     | PostgreSQL    |
| `criteria4s-mysql`          | MySQL         |
| `criteria4s-sparksql`       | Spark SQL     |
| `criteria4s-duckdb`         | DuckDB        |
| `criteria4s-clickhouse`     | ClickHouse    |
| `criteria4s-mongodb`        | MongoDB       |
| `criteria4s-elasticsearch`  | Elasticsearch |

## Client Integrations

| Module                              | Client                        |
|-------------------------------------|-------------------------------|
| `criteria4s-sql-jdbc`               | JDBC (all SQL dialects)       |
| `criteria4s-mongodb-driver`         | MongoDB Java Driver           |
| `criteria4s-elasticsearch-client`   | Elasticsearch Java Client     |
| `criteria4s-clickhouse-client`      | ClickHouse Native Client (v2) |

For the full API reference, guides, and integration examples see the **[documentation site](https://criteria4s.rafaelfernandez.dev/)**.

## License

[MIT](./LICENSE) — Copyright © 2024-2026 Rafael Fernandez
