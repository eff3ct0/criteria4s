---
sidebar_position: 1
title: JDBC
---

# JDBC Integration

The `criteria4s-sql-jdbc` module provides extension methods and implicit conversions for using criteria with JDBC `PreparedStatement` and raw SQL strings.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-sql-jdbc" % "@VERSION@"
```

This module depends on `criteria4s-sql` and works with any SQL-based dialect (`SQL`, `PostgreSQL`, `MySQL`, `SparkSQL`, `DuckDB`, `ClickHouse`).

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.dialect.sql.jdbc.{given, *}
import com.eff3ct.criteria4s.functions as F
```

The key import is `com.eff3ct.criteria4s.dialect.sql.jdbc.*`, which brings in the extension methods and the `given Conversion[Criteria[T], String]`.

## API

### `.toWhereClause`

Returns the criteria as a full `WHERE` clause, including the `WHERE` keyword:

```scala mdoc
val filter = F.and[SQL](
  F.geq[SQL, Column, Int](F.col("age"), F.lit(18)),
  F.===[SQL, Column, Boolean](F.col("active"), F.lit(true))
)

filter.toWhereClause
```

### `.toSqlFragment`

Returns the criteria as a SQL fragment without the `WHERE` keyword. Useful when you need to compose the clause yourself:

```scala mdoc
filter.toSqlFragment
```

### `.appendTo`

Appends a `WHERE` clause to an existing SQL string. This is the most convenient method for building complete query strings:

```scala mdoc
filter.appendTo("SELECT * FROM users")
```

### Implicit Conversion to `String`

The JDBC package provides a `given Conversion[Criteria[T <: SQL], String]` so you can use criteria anywhere a `String` is expected:

```scala mdoc
val sqlString: String = filter
sqlString
```

## Example with PreparedStatement

The following example shows how you might use criteria4s with JDBC in practice. Note that this is a plain code example since JDBC is not available in the docs classpath.

```scala
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.eff3ct.criteria4s.dialect.postgresql.PostgreSQL.given
import com.eff3ct.criteria4s.dialect.sql.jdbc.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

val connection: Connection = DriverManager.getConnection(
  "jdbc:postgresql://localhost:5432/mydb", "user", "password"
)

// Build a type-safe filter
val criteria = F.col[PostgreSQL]("age")
  .geq(F.lit[PostgreSQL, Int](21))
  .and(F.col[PostgreSQL]("country") === F.lit[PostgreSQL, String]("US"))
  .and(F.col[PostgreSQL]("deleted_at").isNull)

// Append to base query
val fullSql = criteria.appendTo("SELECT id, name, email FROM users")
// "SELECT id, name, email FROM users WHERE \"age\" >= 21 AND ..."

val stmt: PreparedStatement = connection.prepareStatement(fullSql)
val rs: ResultSet = stmt.executeQuery()

while (rs.next()) {
  println(s"${rs.getInt("id")}: ${rs.getString("name")}")
}

rs.close()
stmt.close()
connection.close()
```

## Using with Different SQL Dialects

The JDBC integration works with any `T <: SQL` dialect. When using a specific dialect, remember to import both the dialect givens and the JDBC givens:

```scala
// PostgreSQL
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.eff3ct.criteria4s.dialect.postgresql.PostgreSQL.given
import com.eff3ct.criteria4s.dialect.sql.jdbc.given

// MySQL
import com.eff3ct.criteria4s.dialect.mysql.{*, given}
import com.eff3ct.criteria4s.dialect.mysql.MySQL.given
import com.eff3ct.criteria4s.dialect.sql.jdbc.given

// DuckDB
import com.eff3ct.criteria4s.dialect.duckdb.{*, given}
import com.eff3ct.criteria4s.dialect.duckdb.DuckDB.given
import com.eff3ct.criteria4s.dialect.sql.jdbc.given

// ClickHouse (via JDBC)
import com.eff3ct.criteria4s.dialect.clickhouse.{*, given}
import com.eff3ct.criteria4s.dialect.clickhouse.ClickHouse.given
import com.eff3ct.criteria4s.dialect.sql.jdbc.given
```

:::tip
For ClickHouse, you can also use the dedicated [ClickHouse Client](clickhouse-client.md) integration which bridges with the official native Java client (`com.clickhouse:client-v2`).
:::
