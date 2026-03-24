---
sidebar_position: 5
---

# Extensions vs Functions

criteria4s offers two API styles for building expressions. Both produce the same `Criteria[T]` values
and render identically -- the difference is purely ergonomic.

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
```

## Function-Style API

Import the `functions` object (typically aliased as `F`) and call methods as prefixed functions:

```scala
import com.eff3ct.criteria4s.functions as F
```

```scala
val funcExpr = F.and[SQL](
  F.gt[SQL, Column, Int](F.col("age"), F.lit(18)),
  F.like[SQL, Column, String](F.col("name"), F.lit("%John%"))
)
// funcExpr: Criteria[SQL] = (age > 18) AND (name LIKE '%John%')
funcExpr.value
// res0: String = "(age > 18) AND (name LIKE '%John%')"
```

Key characteristics:

- Every call starts with `F.` -- reads like a builder or factory.
- Type parameters are explicit: `F.gt[SQL, Column, Int](...)`.
- `F.col`, `F.lit`, `F.array`, and `F.range` create typed references with a `CriteriaTag` parameter.
- Conjunctions are standalone calls: `F.and(a, b)`, `F.or(a, b)`, `F.not(a)`.

## Extension-Style API

Import `extensions.*` to get Scala 3 extension methods on `Ref` and `Criteria` values:

```scala
import com.eff3ct.criteria4s.extensions.*
```

```scala
val extExpr =
  (F.col[SQL]("age") gt F.lit[SQL, Int](18)) and
  (F.col[SQL]("name") like F.lit[SQL, String]("%John%"))
// extExpr: Criteria[SQL] = (age > 18) AND (name LIKE '%John%')
extExpr.value
// res1: String = "(age > 18) AND (name LIKE '%John%')"
```

Key characteristics:

- Predicates are infix methods on `Ref`: `col gt lit`, `col === lit`, `col like lit`.
- Conjunctions are infix methods on `Criteria`: `a and b`, `a or b`, `a.not`.
- Symbolic aliases available: `:<`, `:>`, `:<=`, `:>=`, `:&`, `:|`, `&&`, `||`.
- Transforms are postfix: `col.upper`, `col.lower`, `col.trim`.
- You still need `F.col` and `F.lit` to create the initial references.

## Why You Cannot Import Both with Wildcards

The extension-style API defines methods like `lt`, `gt`, `like`, `in`, and so on as Scala 3 extension
methods on `Ref`. The function-style API defines top-level methods with the same names (`F.lt`, `F.gt`,
etc.). If you import both with wildcards:

```scala
// DO NOT do this -- name collisions
import com.eff3ct.criteria4s.functions.*
import com.eff3ct.criteria4s.extensions.*
```

The compiler will see ambiguous references for names like `lt`, `gt`, `like`, `in`, etc.
The extension methods and the top-level functions share identical names but have different
signatures, leading to confusing errors.

**Safe patterns:**

```scala
// Pattern 1: function-style with qualified F prefix
import com.eff3ct.criteria4s.functions as F
// Use F.gt(...), F.lt(...), etc.

// Pattern 2: extension-style with extensions wildcard + F for constructors
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
// Use F.col(...), F.lit(...) for constructors
// Use col gt lit, col === lit for predicates
// Use a and b, a or b for conjunctions

// Pattern 3: function-style wildcard (no extensions)
import com.eff3ct.criteria4s.functions.*
// Use gt(col("age"), lit(18)) without F. prefix
// No extension methods available
```

## When to Use Which

### Function-style is better for:

- **Polymorphic / generic code** -- when you write methods parameterized by `T <: CriteriaTag`,
  explicit type parameters make the compiler's job easier:

```scala
def activeFilter[T <: CriteriaTag](using
  eq: EQ[T],
  sc: Show[Column, T],
  sb: Show[Boolean, T]
): Criteria[T] =
  F.===[T, Column, Boolean](F.col("active"), F.lit(true))

activeFilter[SQL].value
// res2: String = "active = true"
```

- **Nested expressions** -- deeply composed criteria can be clearer with function nesting:

```scala
val deeplyNested = F.or[SQL](
  F.and[SQL](
    F.gt[SQL, Column, Int](F.col("age"), F.lit(18)),
    F.isNotNull[SQL, Column](F.col("email"))
  ),
  F.not[SQL](
    F.===[SQL, Column, String](F.col("status"), F.lit("banned"))
  )
)
// deeplyNested: Criteria[SQL] = ((age > 18) AND (email IS NOT NULL)) OR (NOT (status = 'banned'))
deeplyNested.value
// res3: String = "((age > 18) AND (email IS NOT NULL)) OR (NOT (status = 'banned'))"
```

### Extension-style is better for:

- **Concrete DSL code** -- when you are working with a known dialect and want readable,
  SQL-like syntax:

```scala
val readableFilter =
  (F.col[SQL]("age") geq F.lit[SQL, Int](18))
    .and(F.col[SQL]("status") === F.lit[SQL, String]("active"))
    .and(F.col[SQL]("email").isNotNull)
    .or(F.col[SQL]("role") === F.lit[SQL, String]("admin"))
// readableFilter: Criteria[SQL] = (((age >= 18) AND (status = 'active')) AND (email IS NOT NULL)) OR (role = 'admin')

readableFilter.value
// res4: String = "(((age >= 18) AND (status = 'active')) AND (email IS NOT NULL)) OR (role = 'admin')"
```

- **Quick prototyping** -- less ceremony, more natural reading order.

## Side-by-Side Comparison

Here is the same complex expression written in both styles.

**Requirement:** Find users who are either (active adults with verified email) or (admins),
excluding banned users.

### Function-style

```scala
val funcFilter = F.and[SQL](
  F.or[SQL](
    F.and[SQL](
      F.and[SQL](
        F.geq[SQL, Column, Int](F.col("age"), F.lit(18)),
        F.===[SQL, Column, Boolean](F.col("active"), F.lit(true))
      ),
      F.isNotNull[SQL, Column](F.col("verified_at"))
    ),
    F.===[SQL, Column, String](F.col("role"), F.lit("admin"))
  ),
  F.not[SQL](
    F.===[SQL, Column, String](F.col("status"), F.lit("banned"))
  )
)
// funcFilter: Criteria[SQL] = ((((age >= 18) AND (active = true)) AND (verified_at IS NOT NULL)) OR (role = 'admin')) AND (NOT (status = 'banned'))
funcFilter.value
// res5: String = "((((age >= 18) AND (active = true)) AND (verified_at IS NOT NULL)) OR (role = 'admin')) AND (NOT (status = 'banned'))"
```

### Extension-style

```scala
val extFilter =
  (
    (F.col[SQL]("age") geq F.lit[SQL, Int](18))
      .and(F.col[SQL]("active") === F.lit[SQL, Boolean](true))
      .and(F.col[SQL]("verified_at").isNotNull)
      .or(F.col[SQL]("role") === F.lit[SQL, String]("admin"))
  ).and(
    (F.col[SQL]("status") === F.lit[SQL, String]("banned")).not
  )
// extFilter: Criteria[SQL] = ((((age >= 18) AND (active = true)) AND (verified_at IS NOT NULL)) OR (role = 'admin')) AND (NOT (status = 'banned'))

extFilter.value
// res6: String = "((((age >= 18) AND (active = true)) AND (verified_at IS NOT NULL)) OR (role = 'admin')) AND (NOT (status = 'banned'))"
```

Both produce the same output. Choose the style that fits your codebase and team preferences.
