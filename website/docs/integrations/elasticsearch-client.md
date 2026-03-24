---
sidebar_position: 3
title: Elasticsearch Client
---

# Elasticsearch Client Integration

The `criteria4s-elasticsearch-client` module bridges criteria4s with the official Elasticsearch Java client. It provides extension methods and implicit conversions that turn a `Criteria[Elasticsearch]` into an Elasticsearch `Query` object via the `WrapperQuery` mechanism, so you can use your type-safe criteria directly in search requests.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch-client" % "1.0.0"
```

This module transitively depends on `criteria4s-elasticsearch` and the Elasticsearch Java client (`co.elastic.clients:elasticsearch-java`).

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}
import com.eff3ct.criteria4s.dialect.elasticsearch.client.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

The key import is `com.eff3ct.criteria4s.dialect.elasticsearch.client.given`, which brings in `.toQuery` and the implicit `Conversion[Criteria[Elasticsearch], Query]`.

## API

### `.toQuery`

Converts a criteria to an Elasticsearch `Query`:

```scala
val filter: Criteria[Elasticsearch] = F.col[Elasticsearch]("age").geq(F.lit[Elasticsearch, Int](18))
val query: Query = filter.toQuery
```

### Implicit Conversion to `Query`

The client package provides a `given Conversion[Criteria[Elasticsearch], Query]`, so you can pass criteria directly to the search request builder:

```scala
import com.eff3ct.criteria4s.dialect.elasticsearch.client.given

val filter: Criteria[Elasticsearch] = F.col[Elasticsearch]("status")
  .===(F.lit[Elasticsearch, String]("active"))

// Use in search request builder -- implicit conversion to Query
client.search(s => s.index("users").query(filter), classOf[User])
```

## How It Works

The conversion uses Elasticsearch's `WrapperQuery`, which accepts a base64-encoded JSON query string. When you call `.toQuery` or trigger the implicit conversion, the steps are:

1. Take the `Criteria.value` string (a JSON Query DSL expression)
2. Encode it as UTF-8 bytes
3. Base64-encode those bytes
4. Wrap them in a `WrapperQuery`
5. Convert the `WrapperQuery` to a `Query`

This means the full Query DSL JSON produced by the Elasticsearch dialect is sent to Elasticsearch as-is, preserving the exact query structure.

## Example with SearchRequest

The following example shows how to use criteria4s with the Elasticsearch Java client. This is a plain code example since the ES client is not available in the docs classpath.

```scala
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}
import com.eff3ct.criteria4s.dialect.elasticsearch.client.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

// Assume `client` is an initialized ElasticsearchClient

// Build a type-safe filter
val filter = F.col[Elasticsearch]("age")
  .geq(F.lit[Elasticsearch, Int](21))
  .and(F.col[Elasticsearch]("status") === F.lit[Elasticsearch, String]("active"))

// Use with search -- implicit conversion to Query
val response: SearchResponse[java.util.Map[_, _]] = client.search(
  s => s.index("users").query(filter),
  classOf[java.util.Map[_, _]]
)

response.hits().hits().forEach { hit =>
  println(s"Score: ${hit.score()}, Source: ${hit.source()}")
}

// Or use .toQuery explicitly
val query: Query = filter.toQuery
val response2 = client.search(
  s => s.index("users").query(query),
  classOf[java.util.Map[_, _]]
)
```

## Combining Queries

You can compose criteria4s filters with other Elasticsearch queries by converting to `Query` first and then passing it into a `BoolQuery` builder:

```scala
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery

val criteria4sFilter: Query = filter.toQuery

val combinedQuery = BoolQuery.of { b =>
  b.must(criteria4sFilter)
   .filter(f => f.range(r => r.field("created_at").gte(co.elastic.clients.json.JsonData.of("2025-01-01"))))
}

client.search(
  s => s.index("users").query(q => q.bool(combinedQuery)),
  classOf[java.util.Map[_, _]]
)
```
