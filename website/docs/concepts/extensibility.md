---
sidebar_position: 4
---

# Extensibility

criteria4s is designed to be extended in two directions: adding new dialects and adding new predicates. This page walks through the concrete steps for each.

## Creating a Custom Dialect from Scratch

Suppose you need to target a custom data store -- say, a proprietary query language called "QQL" that uses `WHERE field EQUALS value` syntax. Here is how to build a complete dialect.

### Step 1: Define the Tag Type

```scala
import com.eff3ct.criteria4s.core.CriteriaTag

trait QQL extends CriteriaTag
```

### Step 2: Provide Show Instances

The `Show` type class tells criteria4s how to render column names and other types in your dialect:

```scala
import com.eff3ct.criteria4s.core.{Column, Show}

object QQL:
  given showColumn: Show[Column, QQL] =
    Show.create(col => col.colName)

  given showString: Show[String, QQL] =
    Show.create(s => s"\"$s\"")

  given showSeq[V](using show: Show[V, QQL]): Show[Seq[V], QQL] =
    Show.create(_.map(show.show).mkString("{", ", ", "}"))
```

### Step 3: Provide Predicate and Conjunction Instances

Use the `build` helper from `com.eff3ct.criteria4s.instances` to construct type class instances from formatting functions:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

object QQL:
  // ... Show instances from above ...

  trait QQLExpr[T <: QQL]:
    given eqPred: EQ[T]  = build[T, EQ]((l, r) => s"$l EQUALS $r")
    given neqPred: NEQ[T] = build[T, NEQ]((l, r) => s"$l NOT_EQUALS $r")
    given gtPred: GT[T]  = build[T, GT]((l, r) => s"$l GREATER $r")
    given ltPred: LT[T]  = build[T, LT]((l, r) => s"$l LESS $r")

    given andConj: AND[T] = build[T, AND]((l, r) => s"($l) ALL ($r)")
    given orConj: OR[T]   = build[T, OR]((l, r) => s"($l) ANY ($r)")
    given notConj: NOT[T] = build[T, NOT](e => s"NEGATE ($e)")

    // Add ISNULL, ISNOTNULL, etc. as needed
    given isnullPred: ISNULL[T] = build[T, ISNULL](v => s"$v IS_EMPTY")
```

### Step 4: Export via a Package Object

The standard pattern is to create a package object that extends your expression trait, making all `given` instances available through a single import:

```scala
package com.example.criteria4s.dialect

package object qql extends QQL.QQLExpr[QQL]
```

Now users can write:

```scala
import com.example.criteria4s.dialect.qql.{*, given}
import com.eff3ct.criteria4s.functions as F

val filter = F.===(F.col[QQL]("name"), F.lit[QQL, String]("Alice"))
// filter.value == """name EQUALS "Alice""""
```

## Extending an Existing SQL Dialect

criteria4s supports dialect inheritance. PostgreSQL, MySQL, and Spark SQL all extend the base `SQL` dialect and inherit its type class instances. This is the recommended approach when your target shares most of its syntax with standard SQL.

### How PostgreSQL Extends SQL

Look at how the PostgreSQL dialect is built:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.SQL
```

The `PostgreSQL` tag extends `SQL`:

```scala
// PostgreSQL.scala
trait PostgreSQL extends SQL
```

The companion object extends `SQL.SQLExpr[PostgreSQL]`, inheriting all predicate, conjunction, and transform instances. It then overrides only the `Show` instances that differ -- in PostgreSQL's case, column names are rendered with double quotes:

```scala
object PostgreSQL extends SQL.SQLExpr[PostgreSQL]:
  // Override column rendering to use double-quoted identifiers
  given showColumn: Show[Column, PostgreSQL] =
    Show.create(col => s""""${col.colName}"""")

  given showSeq[V](using show: Show[V, PostgreSQL]): Show[Seq[V], PostgreSQL] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  given showTuple[V](using show: Show[V, PostgreSQL]): Show[(V, V), PostgreSQL] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }
```

The package object exports everything:

```scala
package object postgresql extends SQL.SQLExpr[PostgreSQL]
```

The result is that PostgreSQL gets all SQL predicates (EQ, GT, AND, ...) for free, with customized column name rendering.

### Building Your Own SQL Variant

Follow the same pattern to create, say, a ClickHouse dialect:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.SQL

trait ClickHouse extends SQL

object ClickHouse extends SQL.SQLExpr[ClickHouse]:
  // ClickHouse uses backtick-quoted identifiers
  given showColumn: Show[Column, ClickHouse] =
    Show.create(col => s"`${col.colName}`")

  given showSeq[V](using show: Show[V, ClickHouse]): Show[Seq[V], ClickHouse] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  given showTuple[V](using show: Show[V, ClickHouse]): Show[(V, V), ClickHouse] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }
```

You can also override individual predicate instances if a specific operation has non-standard syntax in your database.

## Customizing Individual Predicates

Sometimes you need to override just one predicate for an existing dialect. Since Scala 3's `given` resolution picks the most specific instance, you can provide a more specific instance that shadows the inherited one.

For example, suppose your custom SQL dialect uses `<>` instead of `!=` for not-equal:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*
import com.eff3ct.criteria4s.dialect.sql.SQL

trait CustomSQL extends SQL

object CustomSQL extends SQL.SQLExpr[CustomSQL]:
  // Override just NEQ to use <> syntax
  override given neqPred: NEQ[CustomSQL] =
    build[CustomSQL, NEQ]((l, r) => s"$l <> $r")

  given showColumn: Show[Column, CustomSQL] =
    Show.create(col => col.colName)

  given showSeq[V](using show: Show[V, CustomSQL]): Show[Seq[V], CustomSQL] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  given showTuple[V](using show: Show[V, CustomSQL]): Show[(V, V), CustomSQL] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }
```

All other predicates (EQ, GT, AND, etc.) continue to use the standard SQL rendering.

## The Package Object Pattern

Every built-in dialect follows the same export pattern:

```scala
// sql/package.scala
package object sql extends SQL.SQLExpr[SQL]

// mongodb/package.scala
package object mongodb extends MongoDB.MongoDBExpr[MongoDB]

// elasticsearch/package.scala
package object elasticsearch extends Elasticsearch.ElasticsearchExpr[Elasticsearch]

// postgresql/package.scala
package object postgresql extends SQL.SQLExpr[PostgreSQL]
```

Extending the expression trait from a package object accomplishes two things:

1. All `given` instances defined in the trait become available through the package import (`import com.eff3ct.criteria4s.dialect.sql.{*, given}`).
2. The `Show` instances defined in the companion object (which are not inside the trait) are resolved through the companion scope of the tag type.

This is why a single import line brings in everything needed to use a dialect:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*

// One import gives you all SQL type class instances
import com.eff3ct.criteria4s.dialect.sql.{*, given}
val result = (F.col[SQL]("age") :> F.lit(18)) and (F.col[SQL]("name") === F.lit("Alice"))
// result: Criteria[SQL] = (age > 18) AND (name = 'Alice')
result.value
// res0: String = "(age > 18) AND (name = 'Alice')"
```

## Summary

| Task                           | Steps                                                              |
|--------------------------------|--------------------------------------------------------------------|
| New dialect from scratch       | Define tag trait, Show instances, predicate/conjunction instances, package object |
| Extend an SQL-like dialect     | Extend `SQL` tag, extend `SQL.SQLExpr[YourTag]`, override Show instances        |
| Override a single predicate    | Provide a more specific `given` in your dialect's expression trait              |
| Export instances               | Package object extends the expression trait                                    |
