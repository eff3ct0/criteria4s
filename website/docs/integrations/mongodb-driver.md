---
sidebar_position: 2
title: MongoDB Driver
---

# MongoDB Driver Integration

The `criteria4s-mongodb-driver` module bridges criteria4s with the official MongoDB Java/Scala driver. It provides extension methods and implicit conversions that turn a `Criteria[MongoDB]` directly into a `Bson` filter document, so you can pass your type-safe criteria straight into the driver's `find()` or aggregation methods.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb-driver" % "1.0.0"
```

This module transitively depends on `criteria4s-mongodb` and the MongoDB driver sync (`org.mongodb:mongodb-driver-sync`).

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.driver.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

The key import is `com.eff3ct.criteria4s.dialect.mongodb.driver.given`, which brings in `.toBson`, `.toBsonDocument`, and the implicit `Conversion[Criteria[MongoDB], Bson]`.

## API

### `.toBson`

Converts a criteria to a `Bson` filter document:

```scala
val filter: Criteria[MongoDB] = F.col[MongoDB]("age").geq(F.lit[MongoDB, Int](18))
val bson: Bson = filter.toBson
```

### `.toBsonDocument`

Converts a criteria to a `BsonDocument` (a more specific type):

```scala
val doc: BsonDocument = filter.toBsonDocument
```

### Implicit Conversion to `Bson`

The driver package provides a `given Conversion[Criteria[MongoDB], Bson]`, so you can pass criteria directly wherever `Bson` is expected — no explicit conversion needed:

```scala
import com.eff3ct.criteria4s.dialect.mongodb.driver.given

val filter: Criteria[MongoDB] = F.col[MongoDB]("age").geq(F.lit[MongoDB, Int](18))
collection.find(filter) // Criteria[MongoDB] converts to Bson automatically
```

## JSON Normalization

The MongoDB expression dialect renders `$`-prefixed operators without surrounding quotes for readability (e.g., `$eq`, `$gte`). The driver integration automatically normalizes these to valid Extended JSON before parsing, quoting all `$`-prefixed operators. For example:

- Input: `{"age": {$gte: 18}}`
- Normalized: `{"age": {"$gte": 18}}`

This normalization happens transparently when calling `.toBson`, `.toBsonDocument`, or using the implicit conversion.

## Example with MongoCollection

The following example shows how to use criteria4s with the MongoDB driver. This is a plain code example since the MongoDB driver is not available in the docs classpath.

```scala
import com.mongodb.client.{MongoClients, MongoCollection}
import org.bson.Document
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.driver.given
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

val client     = MongoClients.create("mongodb://localhost:27017")
val database   = client.getDatabase("mydb")
val collection = database.getCollection("users")

// Build a type-safe filter
val filter = F.col[MongoDB]("age")
  .geq(F.lit[MongoDB, Int](21))
  .and(F.col[MongoDB]("status") === F.lit[MongoDB, String]("active"))
  .and(F.col[MongoDB]("deleted_at").isNull)

// Use directly with find() -- implicit conversion to Bson
val results = collection.find(filter)

results.forEach { doc =>
  println(s"${doc.getString("name")}: ${doc.getInteger("age")}")
}

// Or use .toBson explicitly
val explicitBson = filter.toBson
val results2 = collection.find(explicitBson)

client.close()
```

## Combining with Aggregation Pipelines

You can also use criteria as a `$match` stage in aggregation pipelines by converting to `Bson` explicitly:

```scala
import com.mongodb.client.model.Aggregates

val matchStage = Aggregates.`match`(filter.toBson)
val pipeline   = java.util.List.of(matchStage)
val aggResults = collection.aggregate(pipeline)
```
