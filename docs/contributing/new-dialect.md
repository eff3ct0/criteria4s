---
sidebar_position: 2
title: Adding a New Dialect
---

# Adding a New Dialect

This guide walks through creating a new criteria4s dialect step by step. We will use a fictional "CouchDB" dialect as an example.

## Step 1: Create the Module Directory

Create a new directory at the project root:

```
couchdb/
└── src/
    ├── main/scala/com/eff3ct/criteria4s/dialect/couchdb/
    └── test/scala/com/eff3ct/criteria4s/dialect/couchdb/
```

## Step 2: Define the Dialect Trait

Create a trait that extends `CriteriaTag`. If your dialect is SQL-based, extend `SQL` instead.

```scala
// couchdb/src/main/scala/com/eff3ct/criteria4s/dialect/couchdb/CouchDB.scala
package com.eff3ct.criteria4s.dialect.couchdb

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

// For a non-SQL dialect, extend CriteriaTag directly:
trait CouchDB extends CriteriaTag

// For a SQL-based dialect, extend SQL instead:
// trait CouchDB extends com.eff3ct.criteria4s.dialect.sql.SQL
```

## Step 3: Create the Companion Object with Given Instances

Define `Show` instances for your dialect (how columns, strings, sequences, and tuples are rendered) and an inner trait with all the predicate/conjunction `given` instances.

```scala
object CouchDB {

  // How column names are rendered
  given showColumn: Show[Column, CouchDB] =
    Show.create(col => s""""${col.colName}"""")

  // How sequences are rendered (for IN/NOT IN)
  given showSeq[V](using show: Show[V, CouchDB]): Show[Seq[V], CouchDB] =
    Show.create(_.map(show.show).mkString("[", ", ", "]"))

  // How range tuples are rendered (for BETWEEN)
  given showTuple[V](using show: Show[V, CouchDB]): Show[(V, V), CouchDB] =
    Show.create { case (l, r) =>
      s"""{"$$gte": ${show.show(l)}, "$$lt": ${show.show(r)}}"""
    }

  // Inner trait with all predicate given instances
  trait CouchDBExpr[T <: CouchDB] {

    // Define how each predicate renders for your dialect.
    // Use `build[T, PredicateType](renderFunction)` from the instances package.

    given eqPred: EQ[T] = build[T, EQ] { (field, value) =>
      s"""{"selector": {$field: {"$$eq": $value}}}"""
    }

    given neqPred: NEQ[T] = build[T, NEQ] { (field, value) =>
      s"""{"selector": {$field: {"$$ne": $value}}}"""
    }

    given gtPred: GT[T] = build[T, GT] { (field, value) =>
      s"""{"selector": {$field: {"$$gt": $value}}}"""
    }

    // ... define all other predicates: LT, GEQ, LEQ, IN, NOTIN,
    //     LIKE, ISNULL, ISNOTNULL, BETWEEN, NOTBETWEEN,
    //     STARTSWITH, ENDSWITH, CONTAINS, ISTRUE, ISFALSE

    given andConj: AND[T] = build[T, AND] { (left, right) =>
      s"""{"$$and": [$left, $right]}"""
    }

    given orConj: OR[T] = build[T, OR] { (left, right) =>
      s"""{"$$or": [$left, $right]}"""
    }

    given notConj: NOT[T] = build[T, NOT] { expr =>
      s"""{"$$not": $expr}"""
    }
  }
}
```

The `build` function comes from `com.eff3ct.criteria4s.instances.*` and uses the `BuilderBinary`/`BuilderUnary` type class mechanism to construct predicate instances from render functions.

## Step 4: Create the Package Object

The package object extends your inner `Expr` trait to export all `given` instances:

```scala
// couchdb/src/main/scala/com/eff3ct/criteria4s/dialect/couchdb/package.scala
package com.eff3ct.criteria4s.dialect

package object couchdb extends CouchDB.CouchDBExpr[CouchDB]
```

This is what users import to get all dialect-specific instances into scope.

## Step 5: Add to build.sbt

Add the module to the root `build.sbt`:

```scala
lazy val couchdb: Project =
  (project in file("couchdb"))
    .settings(
      name                := "criteria4s-couchdb",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(core)  // or .dependsOn(sql) for SQL-based dialects
```

Add it to the root aggregate:

```scala
lazy val criteria4s: Project =
  project
    .in(file("."))
    .aggregate(core, sql, mongodb, postgresql, mysql, sparksql,
               elasticsearch, couchdb, /* ... */)
```

If you want the docs project to compile mdoc blocks for this dialect, also add it to the `docs` project's `.dependsOn(...)`.

## Step 6: Write Tests

Create a test suite that verifies every predicate renders correctly:

```scala
// couchdb/src/test/scala/com/eff3ct/criteria4s/dialect/couchdb/CouchDBExprSpec.scala
package com.eff3ct.criteria4s.dialect.couchdb

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F

class CouchDBExprSpec extends munit.FunSuite {

  test("CouchDB EQ renders correctly") {
    val result = F.===[CouchDB, Column, Int](F.col("age"), F.lit(30))
    assertEquals(result.value, """{"selector": {"age": {"$eq": 30}}}""")
  }

  test("CouchDB AND renders correctly") {
    val left  = F.===[CouchDB, Column, Int](F.col("a"), F.lit(1))
    val right = F.===[CouchDB, Column, Int](F.col("b"), F.lit(2))
    val result = F.and[CouchDB](left, right)
    assertEquals(result.value, """{"$and": [...]}""")
  }

  // Test ALL predicates: EQ, NEQ, GT, LT, GEQ, LEQ, IN, NOTIN,
  // LIKE, ISNULL, ISNOTNULL, BETWEEN, NOTBETWEEN,
  // STARTSWITH, ENDSWITH, CONTAINS, ISTRUE, ISFALSE
  // AND, OR, NOT
}
```

## Step 7: For SQL-Based Dialects

If your dialect is SQL-based, the process is simpler. You only need to override the `Show[Column, T]` instance and optionally the `Show[Seq[V], T]` and `Show[(V, V), T]` instances:

```scala
trait MyNewSQL extends SQL

object MyNewSQL extends SQL.SQLExpr[MyNewSQL] {
  // Override column quoting
  given showColumn: Show[Column, MyNewSQL] =
    Show.create(col => s"[${col.colName}]")  // e.g., SQL Server bracket quoting

  given showSeq[V](using show: Show[V, MyNewSQL]): Show[Seq[V], MyNewSQL] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  given showTuple[V](using show: Show[V, MyNewSQL]): Show[(V, V), MyNewSQL] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }
}
```

All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN are inherited from `SQL.SQLExpr`.

## Complete Minimal Example

Here is the minimal set of files needed for a new SQL-based dialect with bracket quoting:

**`newsql/src/main/scala/.../NewSQL.scala`**:
```scala
package com.eff3ct.criteria4s.dialect.newsql

import com.eff3ct.criteria4s.core.{Column, Show}
import com.eff3ct.criteria4s.dialect.sql.SQL

trait NewSQL extends SQL

object NewSQL extends SQL.SQLExpr[NewSQL] {
  given showColumn: Show[Column, NewSQL] =
    Show.create(col => s"[${col.colName}]")

  given showSeq[V](using show: Show[V, NewSQL]): Show[Seq[V], NewSQL] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  given showTuple[V](using show: Show[V, NewSQL]): Show[(V, V), NewSQL] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }
}
```

**`newsql/src/main/scala/.../package.scala`**:
```scala
package com.eff3ct.criteria4s.dialect

package object newsql extends sql.SQL.SQLExpr[NewSQL]
```

That is all you need -- the SQL base provides every predicate, conjunction, and transform automatically.
