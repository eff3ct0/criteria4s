---
sidebar_position: 1
---

# Predicates

Predicates are the fundamental building blocks of criteria4s. Each predicate compares a column reference
against a value (or another column) and produces a `Criteria[T]` that renders to your target dialect.

Every predicate is available in two styles:

- **Function-style** -- prefix calls through the `F` namespace, with explicit type parameters.
- **Extension-style** -- infix / postfix methods on `Ref` values, using Scala 3 extension methods.

All examples on this page use the generic **SQL** dialect.

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Comparison Predicates

### EQ (equals)

Tests whether two values are equal.

**Function-style** -- use `F.===` or its alphabetic alias `F.eqv`:

```scala mdoc
val eqFunc = F.===[SQL, Column, Int](F.col("age"), F.lit(30))
eqFunc.value
// age = 30

val eqvFunc = F.eqv[SQL, Column, Int](F.col("age"), F.lit(30))
eqvFunc.value
```

**Extension-style** -- use `.===` or `.eqv` as infix operators:

```scala mdoc
val eqExt = F.col[SQL]("age") === F.lit[SQL, Int](30)
eqExt.value

val eqvExt = F.col[SQL]("age") eqv F.lit[SQL, Int](30)
eqvExt.value
```

Renders to SQL: `age = 30`

### NEQ (not equals)

Tests whether two values are different.

**Function-style** -- use `F.=!=` or `F.neq`:

```scala mdoc
val neqFunc = F.=!=[SQL, Column, String](F.col("status"), F.lit("inactive"))
neqFunc.value

val neqAlias = F.neq[SQL, Column, String](F.col("status"), F.lit("inactive"))
neqAlias.value
```

**Extension-style** -- use `.=!=`:

```scala mdoc
val neqExt = F.col[SQL]("status") =!= F.lit[SQL, String]("inactive")
neqExt.value
```

Renders to SQL: `status != 'inactive'`

### GT (greater than)

```scala mdoc
// Function-style
val gtFunc = F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
gtFunc.value

// Extension-style
val gtExt = F.col[SQL]("age") gt F.lit[SQL, Int](18)
gtExt.value
```

Renders to SQL: `age > 18`

### LT (less than)

```scala mdoc
// Function-style
val ltFunc = F.lt[SQL, Column, Int](F.col("age"), F.lit(65))
ltFunc.value

// Extension-style
val ltExt = F.col[SQL]("age") lt F.lit[SQL, Int](65)
ltExt.value
```

Renders to SQL: `age < 65`

### GEQ (greater than or equal)

```scala mdoc
// Function-style
val geqFunc = F.geq[SQL, Column, Int](F.col("salary"), F.lit(50000))
geqFunc.value

// Extension-style
val geqExt = F.col[SQL]("salary") geq F.lit[SQL, Int](50000)
geqExt.value
```

Renders to SQL: `salary >= 50000`

### LEQ (less than or equal)

```scala mdoc
// Function-style
val leqFunc = F.leq[SQL, Column, Int](F.col("rating"), F.lit(5))
leqFunc.value

// Extension-style
val leqExt = F.col[SQL]("rating") leq F.lit[SQL, Int](5)
leqExt.value
```

Renders to SQL: `rating <= 5`

### Symbol Aliases for Comparison

Extension-style also provides symbolic operators that may feel more natural:

```scala mdoc
val ltSymbol  = F.col[SQL]("x") :< F.lit[SQL, Int](10)
ltSymbol.value

val gtSymbol  = F.col[SQL]("x") :> F.lit[SQL, Int](10)
gtSymbol.value

val leqSymbol = F.col[SQL]("x") :<= F.lit[SQL, Int](10)
leqSymbol.value

val geqSymbol = F.col[SQL]("x") :>= F.lit[SQL, Int](10)
geqSymbol.value
```

## Pattern Predicates

### LIKE

Matches a column against a SQL LIKE pattern.

```scala mdoc
// Function-style
val likeFunc = F.like[SQL, Column, String](F.col("name"), F.lit("%John%"))
likeFunc.value

// Extension-style
val likeExt = F.col[SQL]("name") like F.lit[SQL, String]("%John%")
likeExt.value
```

Renders to SQL: `name LIKE '%John%'`

### STARTSWITH

A semantic alias for LIKE that signals prefix matching intent. In the SQL dialect this renders to `LIKE`.

```scala mdoc
// Function-style
val swFunc = F.startsWith[SQL, Column, String](F.col("email"), F.lit("admin%"))
swFunc.value

// Extension-style
val swExt = F.col[SQL]("email") startsWith F.lit[SQL, String]("admin%")
swExt.value
```

Renders to SQL: `email LIKE 'admin%'`

### ENDSWITH

A semantic alias for LIKE that signals suffix matching intent.

```scala mdoc
// Function-style
val ewFunc = F.endsWith[SQL, Column, String](F.col("filename"), F.lit("%.pdf"))
ewFunc.value

// Extension-style
val ewExt = F.col[SQL]("filename") endsWith F.lit[SQL, String]("%.pdf")
ewExt.value
```

Renders to SQL: `filename LIKE '%.pdf'`

### CONTAINS

A semantic alias for LIKE that signals substring matching intent.

```scala mdoc
// Function-style
val cFunc = F.contains[SQL, Column, String](F.col("bio"), F.lit("%scala%"))
cFunc.value

// Extension-style
val cExt = F.col[SQL]("bio") contains F.lit[SQL, String]("%scala%")
cExt.value
```

Renders to SQL: `bio LIKE '%scala%'`

## Membership Predicates

### IN

Tests whether a column value is in a given collection.

```scala mdoc
// Function-style
val inFunc = F.in[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](1, 2, 3))
inFunc.value

// Extension-style
val inExt = F.col[SQL]("id") in F.array[SQL, Int](1, 2, 3)
inExt.value
```

Renders to SQL: `id IN (1, 2, 3)`

You can also use string collections:

```scala mdoc
val inStr = F.col[SQL]("role") in F.array[SQL, String]("admin", "editor", "viewer")
inStr.value
```

### NOTIN

The negation of `IN`.

```scala mdoc
// Function-style
val notInFunc = F.notIn[SQL, Column, Seq[String]](F.col("status"), F.array[SQL, String]("banned", "deleted"))
notInFunc.value

// Extension-style
val notInExt = F.col[SQL]("status") notIn F.array[SQL, String]("banned", "deleted")
notInExt.value
```

Renders to SQL: `status NOT IN ('banned', 'deleted')`

## Range Predicates

### BETWEEN

Tests whether a column value falls within a range (inclusive).

```scala mdoc
// Function-style
val betweenFunc = F.between[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](18, 65))
betweenFunc.value

// Extension-style
val betweenExt = F.col[SQL]("age") between F.range[SQL, Int](18, 65)
betweenExt.value
```

Renders to SQL: `age BETWEEN 18 AND 65`

### NOTBETWEEN

The negation of `BETWEEN`.

```scala mdoc
// Function-style
val notBetweenFunc = F.notBetween[SQL, Column, (Int, Int)](F.col("score"), F.range[SQL, Int](0, 49))
notBetweenFunc.value

// Extension-style
val notBetweenExt = F.col[SQL]("score") notBetween F.range[SQL, Int](0, 49)
notBetweenExt.value
```

Renders to SQL: `score NOT BETWEEN 0 AND 49`

## Null Predicates

### ISNULL

Tests whether a column value is null.

```scala mdoc
// Function-style
val isNullFunc = F.isNull[SQL, Column](F.col("email"))
isNullFunc.value

// Extension-style
val isNullExt = F.col[SQL]("email").isNull
isNullExt.value
```

Renders to SQL: `email IS NULL`

### ISNOTNULL

Tests whether a column value is not null.

```scala mdoc
// Function-style
val isNotNullFunc = F.isNotNull[SQL, Column](F.col("phone"))
isNotNullFunc.value

// Extension-style
val isNotNullExt = F.col[SQL]("phone").isNotNull
isNotNullExt.value
```

Renders to SQL: `phone IS NOT NULL`

## Boolean Predicates

### ISTRUE

Tests whether a boolean column is true.

```scala mdoc
// Function-style
val isTrueFunc = F.isTrue[SQL, Column](F.col("active"))
isTrueFunc.value

// Extension-style
val isTrueExt = F.col[SQL]("active").isTrue
isTrueExt.value
```

Renders to SQL: `active IS TRUE`

### ISFALSE

Tests whether a boolean column is false.

```scala mdoc
// Function-style
val isFalseFunc = F.isFalse[SQL, Column](F.col("archived"))
isFalseFunc.value

// Extension-style
val isFalseExt = F.col[SQL]("archived").isFalse
isFalseExt.value
```

Renders to SQL: `archived IS FALSE`

## Quick Reference

| Predicate   | Function-style            | Extension-style              | SQL output example          |
|-------------|--------------------------|------------------------------|-----------------------------|
| EQ          | `F.===(c, v)` / `F.eqv`  | `c === v` / `c eqv v`       | `col = value`               |
| NEQ         | `F.=!=(c, v)` / `F.neq`  | `c =!= v`                   | `col != value`              |
| GT          | `F.gt(c, v)`             | `c gt v` / `c :> v`         | `col > value`               |
| LT          | `F.lt(c, v)`             | `c lt v` / `c :< v`         | `col < value`               |
| GEQ         | `F.geq(c, v)`            | `c geq v` / `c :>= v`      | `col >= value`              |
| LEQ         | `F.leq(c, v)`            | `c leq v` / `c :<= v`      | `col <= value`              |
| LIKE        | `F.like(c, v)`           | `c like v`                   | `col LIKE 'pattern'`        |
| STARTSWITH  | `F.startsWith(c, v)`     | `c startsWith v`             | `col LIKE 'prefix%'`        |
| ENDSWITH    | `F.endsWith(c, v)`       | `c endsWith v`               | `col LIKE '%suffix'`        |
| CONTAINS    | `F.contains(c, v)`       | `c contains v`               | `col LIKE '%substr%'`       |
| IN          | `F.in(c, arr)`           | `c in arr`                   | `col IN (1, 2, 3)`          |
| NOTIN       | `F.notIn(c, arr)`        | `c notIn arr`                | `col NOT IN (1, 2)`         |
| BETWEEN     | `F.between(c, rng)`      | `c between rng`              | `col BETWEEN a AND b`       |
| NOTBETWEEN  | `F.notBetween(c, rng)`   | `c notBetween rng`           | `col NOT BETWEEN a AND b`   |
| ISNULL      | `F.isNull(c)`            | `c.isNull`                   | `col IS NULL`               |
| ISNOTNULL   | `F.isNotNull(c)`         | `c.isNotNull`                | `col IS NOT NULL`           |
| ISTRUE      | `F.isTrue(c)`            | `c.isTrue`                   | `col IS TRUE`               |
| ISFALSE     | `F.isFalse(c)`           | `c.isFalse`                  | `col IS FALSE`              |
