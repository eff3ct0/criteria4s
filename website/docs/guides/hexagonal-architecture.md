---
sidebar_position: 6
title: Real-World Example
---

# Real-World Example: Hexagonal Architecture

One of the most compelling uses of criteria4s is in applications that follow a **hexagonal architecture** (also called ports-and-adapters). This style separates the core domain from its infrastructure, and criteria4s fits naturally into that separation: your domain defines the filter contracts using a polymorphic type parameter, and the infrastructure layer decides which backend to use by simply fixing that type.

This page shows a realistic Scala 3 HTTP API that starts with PostgreSQL and switches to MongoDB by changing a single type — no domain logic changes, no interface changes.

![Hexagonal architecture with criteria4s — Domain, Application, and Infrastructure layers](/img/diagram-hexagonal.svg)

## Application Structure

Following screaming architecture, each feature lives in its own package and the structure names what the application does, not the frameworks it uses:

```
src/
└── main/
    └── scala/
        └── com/example/userapi/
            ├── domain/
            │   ├── User.scala              -- Domain entity
            │   ├── UserFilter.scala        -- Filter definition (polymorphic)
            │   └── UserRepository.scala    -- Port (abstract interface)
            ├── application/
            │   └── UserService.scala       -- Use cases
            ├── infrastructure/
            │   ├── postgres/
            │   │   └── PostgresUserRepo.scala  -- PostgreSQL adapter
            │   └── mongodb/
            │       └── MongoUserRepo.scala     -- MongoDB adapter
            └── entrypoint/
                └── HttpApi.scala           -- http4s routes
```

## The Domain Layer

The domain has no knowledge of any database. The `UserFilter` is a polymorphic function that constructs a criteria expression for whatever dialect `T` supports the required predicates:

```scala
// domain/User.scala
package com.example.userapi.domain

case class User(
  id: Long,
  name: String,
  email: String,
  age: Int,
  plan: String,
  active: Boolean
)
```

```scala
// domain/UserFilter.scala
package com.example.userapi.domain

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

/** A collection of filter builders that work across any backend.
  * The type parameter T is fixed at the infrastructure layer.
  */
object UserFilter:

  /** Active users of a certain minimum age. */
  def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using
      Show[Column, T],
      Show[Boolean, T],
      Show[Int, T]
  ): Criteria[T] =
    F.col[T]("age").geq(F.lit(18)) and
    (F.col[T]("active") === F.lit(true))

  /** Users matching a name prefix, on a given plan. */
  def byNameAndPlan[T <: CriteriaTag: LIKE: EQ: AND: OR](
      namePrefix: String,
      plan: String
  )(using
      Show[Column, T],
      Show[String, T]
  ): Criteria[T] =
    F.col[T]("name").startsWith(F.lit(s"$namePrefix%")) and
    (F.col[T]("plan") === F.lit(plan))

  /** Premium users — either on the premium plan or high spenders. */
  def premiumUsers[T <: CriteriaTag: EQ: GT: AND: OR](using
      Show[Column, T],
      Show[String, T],
      Show[Int, T]
  ): Criteria[T] =
    (F.col[T]("plan") === F.lit("premium")) or
    (F.col[T]("lifetime_spend") :> F.lit(5000))
```

The `UserRepository` port is the contract that the domain layer uses to access users. Notice it works with the already-rendered `Criteria[T]`, so the application layer computes the criteria expression and passes it to the repository:

```scala
// domain/UserRepository.scala
package com.example.userapi.domain

import com.eff3ct.criteria4s.core.*

trait UserRepository[F[_], T <: CriteriaTag]:
  def find(criteria: Criteria[T]): F[List[User]]
  def findOne(criteria: Criteria[T]): F[Option[User]]
  def count(criteria: Criteria[T]): F[Long]
```

## The Application Layer

The use cases live in the application layer. They call `UserFilter` to build criteria and pass them to the repository. The type `T` is still abstract here — it flows through from the infrastructure:

```scala
// application/UserService.scala
package com.example.userapi.application

import cats.Monad
import cats.syntax.all.*
import com.eff3ct.criteria4s.core.*
import com.example.userapi.domain.*

class UserService[F[_]: Monad, T <: CriteriaTag](
    repo: UserRepository[F, T]
)(using
    GEQ[T], EQ[T], AND[T], OR[T], GT[T], LIKE[T],
    Show[Column, T], Show[Boolean, T], Show[Int, T], Show[String, T]
):

  def getActiveAdults: F[List[User]] =
    repo.find(UserFilter.activeAdults[T])

  def searchByNameAndPlan(prefix: String, plan: String): F[List[User]] =
    repo.find(UserFilter.byNameAndPlan[T](prefix, plan))

  def getPremiumUsers: F[List[User]] =
    repo.find(UserFilter.premiumUsers[T])

  def countPremiumUsers: F[Long] =
    repo.count(UserFilter.premiumUsers[T])
```

## The Infrastructure Layer

This is the only layer that knows about a specific database. Each adapter is a concrete implementation of `UserRepository` for a specific dialect.

### PostgreSQL Adapter

```scala
// infrastructure/postgres/PostgresUserRepo.scala
package com.example.userapi.infrastructure.postgres

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.eff3ct.criteria4s.dialect.sql.jdbc.given
import com.example.userapi.domain.*

class PostgresUserRepo(xa: Transactor[IO])
    extends UserRepository[IO, PostgreSQL]:

  def find(criteria: Criteria[PostgreSQL]): IO[List[User]] =
    val sql = criteria.appendTo("SELECT id, name, email, age, plan, active FROM users")
    Fragment.const(sql).query[User].to[List].transact(xa)

  def findOne(criteria: Criteria[PostgreSQL]): IO[Option[User]] =
    val sql = criteria.appendTo("SELECT id, name, email, age, plan, active FROM users")
    Fragment.const(sql).query[User].option.transact(xa)

  def count(criteria: Criteria[PostgreSQL]): IO[Long] =
    val sql = criteria.appendTo("SELECT COUNT(*) FROM users")
    Fragment.const(sql).query[Long].unique.transact(xa)
```

### MongoDB Adapter

```scala
// infrastructure/mongodb/MongoUserRepo.scala
package com.example.userapi.infrastructure.mongodb

import cats.effect.IO
import com.mongodb.client.MongoCollection
import org.bson.Document
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.dialect.mongodb.driver.given
import com.example.userapi.domain.*

class MongoUserRepo(collection: MongoCollection[Document])
    extends UserRepository[IO, MongoDB]:

  def find(criteria: Criteria[MongoDB]): IO[List[User]] = IO.blocking {
    // Criteria[MongoDB] converts to Bson implicitly
    collection.find(criteria)
      .map(doc => User(
        id    = doc.getLong("id"),
        name  = doc.getString("name"),
        email = doc.getString("email"),
        age   = doc.getInteger("age"),
        plan  = doc.getString("plan"),
        active = doc.getBoolean("active")
      ))
      .into(new java.util.ArrayList[User])
      .asScala.toList
  }

  def findOne(criteria: Criteria[MongoDB]): IO[Option[User]] =
    find(criteria).map(_.headOption)

  def count(criteria: Criteria[MongoDB]): IO[Long] = IO.blocking {
    collection.countDocuments(criteria) // implicit Criteria[MongoDB] => Bson
  }
```

## The Entrypoint

The HTTP routes use `UserService`, which is already fully constructed with the correct dialect. The routes don't care whether `T` is `PostgreSQL` or `MongoDB`:

```scala
// entrypoint/HttpApi.scala
package com.example.userapi.entrypoint

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*
import com.eff3ct.criteria4s.core.*
import com.example.userapi.application.UserService
import com.example.userapi.domain.User

def userRoutes[T <: CriteriaTag](service: UserService[IO, T]): HttpRoutes[IO] =
  HttpRoutes.of[IO] {

    case GET -> Root / "users" / "active" =>
      service.getActiveAdults.flatMap(Ok(_))

    case GET -> Root / "users" / "premium" =>
      service.getPremiumUsers.flatMap(Ok(_))

    case GET -> Root / "users" / "search" / namePrefix / plan =>
      service.searchByNameAndPlan(namePrefix, plan).flatMap(Ok(_))

    case GET -> Root / "users" / "premium" / "count" =>
      service.countPremiumUsers.flatMap(n => Ok(s"""{"count": $n}"""))
  }
```

## The Wiring — Where the Type Gets Fixed

The only place where `T` gets resolved to a concrete type is in the application's main entry point. To switch databases, you change exactly one line:

```scala
// Main.scala
import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import com.example.userapi.entrypoint.userRoutes

// ─── Using PostgreSQL ──────────────────────────────────────────────────────
import com.eff3ct.criteria4s.dialect.postgresql.{*, given}
import com.example.userapi.infrastructure.postgres.PostgresUserRepo

object MainPostgres extends IOApp.Simple:
  def run: IO[Unit] =
    for
      xa   <- buildTransactor()
      repo  = PostgresUserRepo(xa)
      svc   = UserService[IO, PostgreSQL](repo)
      _    <- EmberServerBuilder.default[IO]
                .withHttpApp(userRoutes(svc).orNotFound)
                .build
                .useForever
    yield ()
```

```scala
// ─── Switching to MongoDB — one line changes ───────────────────────────────
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.example.userapi.infrastructure.mongodb.MongoUserRepo

object MainMongo extends IOApp.Simple:
  def run: IO[Unit] =
    for
      collection <- buildMongoCollection()
      repo        = MongoUserRepo(collection)
      svc         = UserService[IO, MongoDB](repo)     // <-- only this line changes
      _          <- EmberServerBuilder.default[IO]
                      .withHttpApp(userRoutes(svc).orNotFound)
                      .build
                      .useForever
    yield ()
```

## What Actually Changes When You Switch Databases

| Layer          | PostgreSQL                          | MongoDB                            |
|----------------|-------------------------------------|------------------------------------|
| Domain         | `UserFilter.activeAdults[T]`        | `UserFilter.activeAdults[T]`       |
| Application    | `UserService[IO, T]`                | `UserService[IO, T]`               |
| HTTP routes    | `userRoutes(svc)`                   | `userRoutes(svc)`                  |
| Repository     | `PostgresUserRepo(xa)`              | `MongoUserRepo(collection)`        |
| **Type fixed** | `UserService[IO, **PostgreSQL**]`   | `UserService[IO, **MongoDB**]`     |

Everything above the infrastructure layer is completely unchanged. The compiler verifies that the repository and the service agree on the same dialect — if you accidentally pass a `MongoUserRepo` to `UserService[IO, PostgreSQL]`, you get a compile error immediately.

## Filter Output Comparison

Here is what the same filter produces for each backend:

```
UserFilter.activeAdults[PostgreSQL].value
// → ("age" >= 18) AND ("active" = true)

UserFilter.activeAdults[MongoDB].value
// → {$and: [{"age": {$gte: 18}}, {"active": true}]}

UserFilter.premiumUsers[PostgreSQL].value
// → (plan = 'premium') OR (lifetime_spend > 5000)

UserFilter.premiumUsers[MongoDB].value
// → {$or: [{"plan": {$eq: "premium"}}, {"lifetime_spend": {$gt: 5000}}]}
```

The filter logic is defined once, in the domain layer, and the correct output for each database is derived automatically at compile time through type class resolution.
