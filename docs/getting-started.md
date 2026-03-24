---
sidebar_position: 2
---

# Getting Started

## Installation

Add the core library and the dialect modules you need to your `build.sbt`:

```scala
// Core DSL (always required)
libraryDependencies += "com.eff3ct" %% "criteria4s-core" % "@VERSION@"

// Dialect modules (pick the ones you need)
libraryDependencies += "com.eff3ct" %% "criteria4s-sql"           % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb"       % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch" % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-postgresql"    % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-mysql"         % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql"      % "@VERSION@"

// Integration modules (for driver-level interop)
libraryDependencies += "com.eff3ct" %% "criteria4s-sql-jdbc"             % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-mongodb-driver"       % "@VERSION@"
libraryDependencies += "com.eff3ct" %% "criteria4s-elasticsearch-client" % "@VERSION@"
```

criteria4s targets **Scala 3.6.4 LTS** and above.

## Your First Expression

Let's build a simple SQL filter: `age > 18`.

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.dialect.sql.{*, given}
```

```scala mdoc
val criteria: Criteria[SQL] = F.gt(F.col[SQL]("age"), F.lit[SQL, Int](18))
criteria.value
```

That's it. `F.col` creates a column reference, `F.lit` creates a literal value, and `F.gt` produces a `Criteria[SQL]` tagged with the SQL dialect. Calling `.value` renders the expression string.

## The Two API Styles

criteria4s provides two ways to build expressions: **function-style** and **extension-style**. They produce identical results -- choose whichever reads best in your context.

### Function-style with `functions as F`

The function-style API uses top-level functions from the `functions` package:

```scala mdoc:nest
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val expr = F.and(
  F.===(F.col[SQL]("status"), F.lit[SQL, String]("active")),
  F.gt(F.col[SQL]("age"), F.lit[SQL, Int](18))
)
expr.value
```

### Extension-style with `extensions.*`

The extension-style API adds methods directly to `Ref` and `Criteria` values:

```scala mdoc:nest
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val expr = (F.col[SQL]("status") === F.lit("active")) and
           (F.col[SQL]("age") :> F.lit(18))
expr.value
```

The extension style reads more naturally for chained expressions. Note the symbolic aliases: `:>` for `gt`, `:<` for `lt`, `:>=` for `geq`, `:<=` for `leq`, `:&` for `and`, and `:|` for `or`.

### Why `functions as F` Instead of `functions.*`

You may wonder why the examples import `functions as F` rather than using a wildcard import. The reason is that the `functions` package defines names like `and`, `or`, `not`, `in`, and `lt` which are common enough to collide with other identifiers in your code. Using a qualified import via `as F` keeps everything under a clear namespace (`F.and`, `F.or`, `F.gt`) without polluting your scope. If you prefer the wildcard import, it works fine -- just be aware of the potential for name clashes.

## Composing Expressions

Expressions compose naturally. Every predicate returns a `Criteria[T]`, and conjunctions combine two `Criteria[T]` values into a new `Criteria[T]`:

```scala mdoc:nest
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.dialect.sql.{*, given}

val isAdult   = F.col[SQL]("age") :>= F.lit(18)
val isActive  = F.col[SQL]("status") === F.lit("active")
val hasEmail  = F.col[SQL]("email").isNotNull

val filter = (isAdult and isActive) or hasEmail
filter.value
```

## What's Next

- [Type Classes](concepts/type-classes.md) -- Understand why criteria4s uses type classes and how this solves the Expression Problem.
- [Tagless Final](concepts/tagless-final.md) -- Learn how phantom types and polymorphic functions let you write backend-agnostic criteria.
- [Architecture](concepts/architecture.md) -- Explore the full type hierarchy from `CriteriaTag` to `CaseExpr`.
