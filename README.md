[![Release](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml/badge.svg)](https://github.com/eff3ct0/criteria4s/actions/workflows/releases.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Criteria4s

Criteria4s is a straightforward domain-specific language (DSL) designed to define criteria and predicate expressions for any data store using Scala's type class mechanisms in a type-safe manner. Its core is designed to be agnostic to any specific data store, making it extensible and capable of supporting a variety of data storage systems.

## Getting Started

To utilize the Criteria4s DSL in your project, add the following dependencies to your build configuration:

### Sonatype Repository

- **Core library**: [criteria4s-core](https://central.sonatype.com/artifact/com.eff3ct/criteria4s-core_2.13)
- **SQL implementation**: [criteria4s-sql](https://central.sonatype.com/artifact/com.eff3ct/criteria4s-sql_2.13)
- **MongoDB implementation**: [criteria4s-mongodb](https://central.sonatype.com/artifact/com.eff3ct/criteria4s-mongodb_2.13)

### SBT

Add these to your `build.sbt`:

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-core" % "<version>" // Core library
libraryDependencies += "com.eff3ct" %% "criteria4s-sql" % "<version>" // SQL implementation
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb" % "<version>" // MongoDB implementation
```

> [!IMPORTANT]  
> Criteria4s is currently a work in progress and not ready for production use. It is available exclusively for Scala 2.13.

## Dialects Supported

Criteria4s is extensible, supporting various types of data stores. Presently, it supports the following dialects:

| Dialect     | Package                                                                                    |
|-------------|:-------------------------------------------------------------------------------------------|
| SQL         | [sql](./sql/src/main/scala/io/github/rafafrdz/criteria4s/dialect/sql)                      | 
| MongoDB     | [mongodb](./mongodb/src/main/scala/io/github/rafafrdz/criteria4s/dialect/mongodb)          | 
| PostgresSQL | [postgresql](./postgresql/src/main/scala/io/github/rafafrdz/criteria4s/dialect/postgresql) |

## Examples

Here are examples demonstrating how to use the Criteria DSL. More samples are available in the [`criteria4s-examples`](./examples/src/main/scala/io/github/rafafrdz/criteria4s/examples) module.

### Imports

Start by importing the Criteria4s DSL and the SQL dialect:

```scala
import com.eff3ct.criteria4s.core._
import com.eff3ct.criteria4s.examples.datastores._
import com.eff3ct.criteria4s.extensions._
import com.eff3ct.criteria4s.functions._
```

### Defining Criteria Expressions

Define criteria expressions in a polymorphic way using the Criteria DSL. Additional examples are provided in the [`Defining Criteria Expressions`](./doc/defining-criteria-expressions.md) document.

```scala
def expr[T <: CriteriaTag : LEQ : EQ : AND : OR : Show[Column, *]]: Criteria[T] =
  (col[T]("field1") leq lit(3)) and (col[T]("field2") leq lit(4)) or (col[T]("field3") === lit("c"))
```

#### Use Cases

The following code snippets demonstrate how to use the defined criteria expressions:

```scala
def ageCriteria[T <: CriteriaTag : GT : LT : AND : Show[Column, *]]: Criteria[T] =
  (col[T]("age") gt lit(18)) and (col[T]("age") lt lit(65))

def refCriteria[T <: CriteriaTag : EQ : Show[Column, *]](fieldName: String, id: UUID): Criteria[T] =
  col[T](fieldName) === lit(id.toString)
```

### Evaluating Criteria Expressions

Utilize these expressions to generate criteria for different data stores by importing the corresponding dialects. Evaluation examples for various dialects are available in the following documents:

- [PostgreSQL Dialect](./doc/postgresql-dialect-evaluating.md): Evaluate criteria expressions for PostgreSQL data stores.
- [MongoDB Dialect](./doc/mongodb-dialect-evaluating.md): Evaluate criteria expressions for MongoDB data stores.
- [MySQL Dialect](./doc/mysql-dialect-evaluating.md): Evaluate criteria expressions for MySQL data stores.
- [Custom Dialect](./doc/custom-dialect-evaluating.md): Evaluate criteria expressions for custom data stores.

### Inline Criteria DSL

Leverage the Criteria DSL in an inline manner:

```scala
(col[PostgreSQL]("field1") leq lit(3)) and (col[PostgreSQL]("field2") leq lit(4)) or (col[PostgreSQL]("field3") === lit("c"))
// res: (('field1' <= 3) AND ('field2' <= 4)) OR ('field3' = c)
```

```scala
(col[MongoDB]("field1") leq lit(3)) and (col[MongoDB]("field2") leq lit(4)) or (col[MongoDB]("field3") === lit("c"))
// res: {$or: [{$and: [{"field1": {$lte: 3}}, {"field2": {$lte: 4}}]}, {"field3": {$eq: c}}]}
```

```scala
(col[WeirdDatastore]("field1") leq lit(3)) and (col[WeirdDatastore]("field2") leq lit(4)) or (col[WeirdDatastore]("field3") === lit("c"))
// res: {left: {left: {left: field1, opt: <=, right: 3 }, opt: AND, right: {left: field2, opt: <=, right: 4 } }, opt: OR, right: {left: field3, opt: =, right: c } }
```
