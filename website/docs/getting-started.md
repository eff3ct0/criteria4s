---
sidebar_position: 2
---

# Getting Started

## Installation

![criteria4s module dependency graph](/img/diagram-modules.svg)

Add the core library and the dialect modules you need to your `build.sbt`:

```scala
// Core DSL (always required)
libraryDependencies += "com.eff3ct" %% "criteria4s-core" % "1.0.0"

// Dialect modules (pick the ones you need)
libraryDependencies += "com.eff3ct" %% "criteria4s-sql"           % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb"       % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql"    % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-mysql"         % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql"      % "1.0.0"

// Integration modules (for driver-level interop)
libraryDependencies += "com.eff3ct" %% "criteria4s-sql-jdbc"             % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb-driver"       % "1.0.0"
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch-client" % "1.0.0"
```

criteria4s targets **Scala 3.6.4 LTS** and above.

## Your First Expression

Let's build a simple SQL filter: `age > 18`.

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.dialect.sql.{*, given}
```

```scala
val criteria: Criteria[SQL] = F.gt(F.col[SQL]("age"), F.lit[SQL, Int](18))
// criteria: Criteria[SQL] = age > 18
criteria.value
// res0: String = "age > 18"
```

That's it. `F.col` creates a column reference, `F.lit` creates a literal value, and `F.gt` produces a `Criteria[SQL]` tagged with the SQL dialect. Calling `.value` renders the expression string.

## The Two API Styles

criteria4s provides two ways to build expressions: **function-style** and **extension-style**. They produce identical results -- choose whichever reads best in your context.

### Function-style with `functions as F`

The function-style API uses top-level functions from the `functions` package:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val expr = F.and(
  F.===(F.col[SQL]("status"), F.lit[SQL, String]("active")),
  F.gt(F.col[SQL]("age"), F.lit[SQL, Int](18))
)
// expr: Criteria[SQL] = (status = 'active') AND (age > 18)
expr.value
// res1: String = "(status = 'active') AND (age > 18)"
```

### Extension-style with `extensions.*`

The extension-style API adds methods directly to `Ref` and `Criteria` values:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val expr = (F.col[SQL]("status") === F.lit("active")) and
           (F.col[SQL]("age") :> F.lit(18))
// expr: Criteria[SQL] = (status = 'active') AND (age > 18)
expr.value
// res2: String = "(status = 'active') AND (age > 18)"
```

The extension style reads more naturally for chained expressions. Note the symbolic aliases: `:>` for `gt`, `:<` for `lt`, `:>=` for `geq`, `:<=` for `leq`, `:&` for `and`, and `:|` for `or`.

### Why `functions as F` Instead of `functions.*`

You may wonder why the examples import `functions as F` rather than using a wildcard import. The reason is that the `functions` package defines names like `and`, `or`, `not`, `in`, and `lt` which are common enough to collide with other identifiers in your code. Using a qualified import via `as F` keeps everything under a clear namespace (`F.and`, `F.or`, `F.gt`) without polluting your scope. If you prefer the wildcard import, it works fine -- just be aware of the potential for name clashes.

## Composing Expressions

Expressions compose naturally. Every predicate returns a `Criteria[T]`, and conjunctions combine two `Criteria[T]` values into a new `Criteria[T]`:

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val isAdult   = F.col[SQL]("age") :>= F.lit(18)
// isAdult: Criteria[SQL] = age >= 18
val isActive  = F.col[SQL]("status") === F.lit("active")
// isActive: Criteria[SQL] = status = 'active'
val hasEmail  = F.col[SQL]("email").isNotNull
// hasEmail: Criteria[SQL] = email IS NOT NULL

val filter = (isAdult and isActive) or hasEmail
// filter: Criteria[SQL] = ((age >= 18) AND (status = 'active')) OR (email IS NOT NULL)
filter.value
// res3: String = "((age >= 18) AND (status = 'active')) OR (email IS NOT NULL)"
```

## What's Next

- [Type Classes](concepts/type-classes.md) -- Understand why criteria4s uses type classes and how this solves the Expression Problem.
- [Tagless Final](concepts/tagless-final.md) -- Learn how phantom types and polymorphic functions let you write backend-agnostic criteria.
- [Architecture](concepts/architecture.md) -- Explore the full type hierarchy from `CriteriaTag` to `CaseExpr`.
