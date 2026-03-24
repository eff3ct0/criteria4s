---
sidebar_position: 4
title: ClickHouse Client
---

# ClickHouse Client Integration

The `criteria4s-clickhouse-client` module bridges criteria4s with the official [ClickHouse Java client](https://github.com/ClickHouse/clickhouse-java) (`client-v2`). It provides extension methods and implicit conversions that let you use `Criteria[ClickHouse]` to build and execute queries directly against a ClickHouse native client — without going through JDBC.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-clickhouse-client" % "@VERSION@"
```

This module transitively depends on `criteria4s-clickhouse` and the ClickHouse Java client (`com.clickhouse:client-v2`).

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.clickhouse.{*, given}
import com.eff3ct.criteria4s.dialect.clickhouse.client.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

The key import is `com.eff3ct.criteria4s.dialect.clickhouse.client.given`, which brings in the extension methods and the `given Conversion[Criteria[ClickHouse], String]`.

## API

### `.toWhereClause`

Returns the criteria as a full `WHERE` clause, including the `WHERE` keyword:

```scala
val filter = F.and[ClickHouse](
  F.geq[ClickHouse, Column, Int](F.col("age"), F.lit(18)),
  F.===[ClickHouse, Column, Boolean](F.col("active"), F.lit(true))
)

filter.toWhereClause
// "WHERE (`age` >= 18) AND (`active` = true)"
```

### `.toSqlFragment`

Returns the criteria as a SQL fragment without the `WHERE` keyword:

```scala
filter.toSqlFragment
// "(`age` >= 18) AND (`active` = true)"
```

### `.appendTo`

Appends a `WHERE` clause to an existing SQL string:

```scala
filter.appendTo("SELECT * FROM events")
// "SELECT * FROM events WHERE (`age` >= 18) AND (`active` = true)"
```

### `.queryWith`

Executes an async query against the ClickHouse native client. Returns a `CompletableFuture[QueryResponse]`:

```scala
val future = filter.queryWith(client, "SELECT * FROM events")
val response = future.get() // blocks until complete
```

### `.queryAllWith`

Executes a synchronous query and loads the full result set into memory as a `List[GenericRecord]`:

```scala
val records = filter.queryAllWith(client, "SELECT * FROM events")
records.forEach { record =>
  println(s"${record.getString("name")}: ${record.getInteger("age")}")
}
```

### Implicit Conversion to `String`

The client package provides a `given Conversion[Criteria[ClickHouse], String]` so you can use criteria anywhere a `String` is expected:

```scala
val sqlString: String = filter
```

## Example with ClickHouse Client

The following example shows how to use criteria4s with the ClickHouse native client. This is a plain code example since the ClickHouse client is not available in the docs classpath.

```scala
import com.clickhouse.client.api.Client
import com.clickhouse.client.api.enums.Protocol
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.clickhouse.{*, given}
import com.eff3ct.criteria4s.dialect.clickhouse.ClickHouse.given
import com.eff3ct.criteria4s.dialect.clickhouse.client.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

// Create the ClickHouse client
val client = new Client.Builder()
  .addEndpoint(Protocol.HTTP, "localhost", 8123)
  .setUsername("default")
  .setPassword("")
  .build()

// Build a type-safe filter
val criteria = F.col[ClickHouse]("event_type")
  .===(F.lit[ClickHouse, String]("page_view"))
  .and(F.col[ClickHouse]("timestamp").isNotNull)
  .and(F.col[ClickHouse]("response_time") :< F.lit[ClickHouse, Int](500))

// Synchronous query — loads all records into memory
val records = criteria.queryAllWith(client, "SELECT * FROM events")

records.forEach { record =>
  println(s"${record.getString("event_type")}: ${record.getInteger("response_time")}ms")
}

// Async query — returns CompletableFuture
val future = criteria.queryWith(client, "SELECT count() FROM events")
val response = future.get()
response.close()

client.close()
```

## When to Use JDBC vs. Native Client

| Feature | JDBC (`sql-jdbc`) | Native Client (`clickhouse-client`) |
|---------|-------------------|-------------------------------------|
| Protocol | JDBC over HTTP | HTTP (native) |
| Async support | No | Yes (`CompletableFuture`) |
| Connection pooling | Driver-dependent | Built-in |
| Compression | Driver-dependent | LZ4 built-in |
| Bulk inserts | Standard JDBC batching | Optimized insert API |
| Compatibility | Any SQL dialect | ClickHouse only |

:::tip
If you're already using JDBC in your project, the generic [`sql-jdbc`](jdbc.md) module works with ClickHouse too — just bring the ClickHouse JDBC driver (`com.clickhouse:clickhouse-jdbc`). The native client integration is better suited for high-throughput analytical workloads that benefit from async queries and built-in compression.
:::
