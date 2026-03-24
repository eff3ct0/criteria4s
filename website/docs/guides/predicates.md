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

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Comparison Predicates

### EQ (equals)

Tests whether two values are equal.

**Function-style** -- use `F.===` or its alphabetic alias `F.eqv`:

```scala
val eqFunc = F.===[SQL, Column, Int](F.col("age"), F.lit(30))
// eqFunc: Criteria[SQL] = age = 30
eqFunc.value
// res0: String = "age = 30"
// age = 30

val eqvFunc = F.eqv[SQL, Column, Int](F.col("age"), F.lit(30))
// eqvFunc: Criteria[SQL] = age = 30
eqvFunc.value
// res1: String = "age = 30"
```

**Extension-style** -- use `.===` or `.eqv` as infix operators:

```scala
val eqExt = F.col[SQL]("age") === F.lit[SQL, Int](30)
// eqExt: Criteria[SQL] = age = 30
eqExt.value
// res2: String = "age = 30"

val eqvExt = F.col[SQL]("age") eqv F.lit[SQL, Int](30)
// eqvExt: Criteria[SQL] = age = 30
eqvExt.value
// res3: String = "age = 30"
```

Renders to SQL: `age = 30`

### NEQ (not equals)

Tests whether two values are different.

**Function-style** -- use `F.=!=` or `F.neq`:

```scala
val neqFunc = F.=!=[SQL, Column, String](F.col("status"), F.lit("inactive"))
// neqFunc: Criteria[SQL] = status != 'inactive'
neqFunc.value
// res4: String = "status != 'inactive'"

val neqAlias = F.neq[SQL, Column, String](F.col("status"), F.lit("inactive"))
// neqAlias: Criteria[SQL] = status != 'inactive'
neqAlias.value
// res5: String = "status != 'inactive'"
```

**Extension-style** -- use `.=!=`:

```scala
val neqExt = F.col[SQL]("status") =!= F.lit[SQL, String]("inactive")
// neqExt: Criteria[SQL] = status != 'inactive'
neqExt.value
// res6: String = "status != 'inactive'"
```

Renders to SQL: `status != 'inactive'`

### GT (greater than)

```scala
// Function-style
val gtFunc = F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
// gtFunc: Criteria[SQL] = age > 18
gtFunc.value
// res7: String = "age > 18"

// Extension-style
val gtExt = F.col[SQL]("age") gt F.lit[SQL, Int](18)
// gtExt: Criteria[SQL] = age > 18
gtExt.value
// res8: String = "age > 18"
```

Renders to SQL: `age > 18`

### LT (less than)

```scala
// Function-style
val ltFunc = F.lt[SQL, Column, Int](F.col("age"), F.lit(65))
// ltFunc: Criteria[SQL] = age < 65
ltFunc.value
// res9: String = "age < 65"

// Extension-style
val ltExt = F.col[SQL]("age") lt F.lit[SQL, Int](65)
// ltExt: Criteria[SQL] = age < 65
ltExt.value
// res10: String = "age < 65"
```

Renders to SQL: `age < 65`

### GEQ (greater than or equal)

```scala
// Function-style
val geqFunc = F.geq[SQL, Column, Int](F.col("salary"), F.lit(50000))
// geqFunc: Criteria[SQL] = salary >= 50000
geqFunc.value
// res11: String = "salary >= 50000"

// Extension-style
val geqExt = F.col[SQL]("salary") geq F.lit[SQL, Int](50000)
// geqExt: Criteria[SQL] = salary >= 50000
geqExt.value
// res12: String = "salary >= 50000"
```

Renders to SQL: `salary >= 50000`

### LEQ (less than or equal)

```scala
// Function-style
val leqFunc = F.leq[SQL, Column, Int](F.col("rating"), F.lit(5))
// leqFunc: Criteria[SQL] = rating <= 5
leqFunc.value
// res13: String = "rating <= 5"

// Extension-style
val leqExt = F.col[SQL]("rating") leq F.lit[SQL, Int](5)
// leqExt: Criteria[SQL] = rating <= 5
leqExt.value
// res14: String = "rating <= 5"
```

Renders to SQL: `rating <= 5`

### Symbol Aliases for Comparison

Extension-style also provides symbolic operators that may feel more natural:

```scala
val ltSymbol  = F.col[SQL]("x") :< F.lit[SQL, Int](10)
// ltSymbol: Criteria[SQL] = x < 10
ltSymbol.value
// res15: String = "x < 10"

val gtSymbol  = F.col[SQL]("x") :> F.lit[SQL, Int](10)
// gtSymbol: Criteria[SQL] = x > 10
gtSymbol.value
// res16: String = "x > 10"

val leqSymbol = F.col[SQL]("x") :<= F.lit[SQL, Int](10)
// leqSymbol: Criteria[SQL] = x <= 10
leqSymbol.value
// res17: String = "x <= 10"

val geqSymbol = F.col[SQL]("x") :>= F.lit[SQL, Int](10)
// geqSymbol: Criteria[SQL] = x >= 10
geqSymbol.value
// res18: String = "x >= 10"
```

## Pattern Predicates

### LIKE

Matches a column against a SQL LIKE pattern.

```scala
// Function-style
val likeFunc = F.like[SQL, Column, String](F.col("name"), F.lit("%John%"))
// likeFunc: Criteria[SQL] = name LIKE '%John%'
likeFunc.value
// res19: String = "name LIKE '%John%'"

// Extension-style
val likeExt = F.col[SQL]("name") like F.lit[SQL, String]("%John%")
// likeExt: Criteria[SQL] = name LIKE '%John%'
likeExt.value
// res20: String = "name LIKE '%John%'"
```

Renders to SQL: `name LIKE '%John%'`

### STARTSWITH

A semantic alias for LIKE that signals prefix matching intent. In the SQL dialect this renders to `LIKE`.

```scala
// Function-style
val swFunc = F.startsWith[SQL, Column, String](F.col("email"), F.lit("admin%"))
// swFunc: Criteria[SQL] = email LIKE 'admin%'
swFunc.value
// res21: String = "email LIKE 'admin%'"

// Extension-style
val swExt = F.col[SQL]("email") startsWith F.lit[SQL, String]("admin%")
// swExt: Criteria[SQL] = email LIKE 'admin%'
swExt.value
// res22: String = "email LIKE 'admin%'"
```

Renders to SQL: `email LIKE 'admin%'`

### ENDSWITH

A semantic alias for LIKE that signals suffix matching intent.

```scala
// Function-style
val ewFunc = F.endsWith[SQL, Column, String](F.col("filename"), F.lit("%.pdf"))
// ewFunc: Criteria[SQL] = filename LIKE '%.pdf'
ewFunc.value
// res23: String = "filename LIKE '%.pdf'"

// Extension-style
val ewExt = F.col[SQL]("filename") endsWith F.lit[SQL, String]("%.pdf")
// ewExt: Criteria[SQL] = filename LIKE '%.pdf'
ewExt.value
// res24: String = "filename LIKE '%.pdf'"
```

Renders to SQL: `filename LIKE '%.pdf'`

### CONTAINS

A semantic alias for LIKE that signals substring matching intent.

```scala
// Function-style
val cFunc = F.contains[SQL, Column, String](F.col("bio"), F.lit("%scala%"))
// cFunc: Criteria[SQL] = bio LIKE '%scala%'
cFunc.value
// res25: String = "bio LIKE '%scala%'"

// Extension-style
val cExt = F.col[SQL]("bio") contains F.lit[SQL, String]("%scala%")
// cExt: Criteria[SQL] = bio LIKE '%scala%'
cExt.value
// res26: String = "bio LIKE '%scala%'"
```

Renders to SQL: `bio LIKE '%scala%'`

## Membership Predicates

### IN

Tests whether a column value is in a given collection.

```scala
// Function-style
val inFunc = F.in[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](1, 2, 3))
// inFunc: Criteria[SQL] = id IN (1, 2, 3)
inFunc.value
// res27: String = "id IN (1, 2, 3)"

// Extension-style
val inExt = F.col[SQL]("id") in F.array[SQL, Int](1, 2, 3)
// inExt: Criteria[SQL] = id IN (1, 2, 3)
inExt.value
// res28: String = "id IN (1, 2, 3)"
```

Renders to SQL: `id IN (1, 2, 3)`

You can also use string collections:

```scala
val inStr = F.col[SQL]("role") in F.array[SQL, String]("admin", "editor", "viewer")
// inStr: Criteria[SQL] = role IN ('admin', 'editor', 'viewer')
inStr.value
// res29: String = "role IN ('admin', 'editor', 'viewer')"
```

### NOTIN

The negation of `IN`.

```scala
// Function-style
val notInFunc = F.notIn[SQL, Column, Seq[String]](F.col("status"), F.array[SQL, String]("banned", "deleted"))
// notInFunc: Criteria[SQL] = status NOT IN ('banned', 'deleted')
notInFunc.value
// res30: String = "status NOT IN ('banned', 'deleted')"

// Extension-style
val notInExt = F.col[SQL]("status") notIn F.array[SQL, String]("banned", "deleted")
// notInExt: Criteria[SQL] = status NOT IN ('banned', 'deleted')
notInExt.value
// res31: String = "status NOT IN ('banned', 'deleted')"
```

Renders to SQL: `status NOT IN ('banned', 'deleted')`

## Range Predicates

### BETWEEN

Tests whether a column value falls within a range (inclusive).

```scala
// Function-style
val betweenFunc = F.between[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](18, 65))
// betweenFunc: Criteria[SQL] = age BETWEEN 18 AND 65
betweenFunc.value
// res32: String = "age BETWEEN 18 AND 65"

// Extension-style
val betweenExt = F.col[SQL]("age") between F.range[SQL, Int](18, 65)
// betweenExt: Criteria[SQL] = age BETWEEN 18 AND 65
betweenExt.value
// res33: String = "age BETWEEN 18 AND 65"
```

Renders to SQL: `age BETWEEN 18 AND 65`

### NOTBETWEEN

The negation of `BETWEEN`.

```scala
// Function-style
val notBetweenFunc = F.notBetween[SQL, Column, (Int, Int)](F.col("score"), F.range[SQL, Int](0, 49))
// notBetweenFunc: Criteria[SQL] = score NOT BETWEEN 0 AND 49
notBetweenFunc.value
// res34: String = "score NOT BETWEEN 0 AND 49"

// Extension-style
val notBetweenExt = F.col[SQL]("score") notBetween F.range[SQL, Int](0, 49)
// notBetweenExt: Criteria[SQL] = score NOT BETWEEN 0 AND 49
notBetweenExt.value
// res35: String = "score NOT BETWEEN 0 AND 49"
```

Renders to SQL: `score NOT BETWEEN 0 AND 49`

## Null Predicates

### ISNULL

Tests whether a column value is null.

```scala
// Function-style
val isNullFunc = F.isNull[SQL, Column](F.col("email"))
// isNullFunc: Criteria[SQL] = email IS NULL
isNullFunc.value
// res36: String = "email IS NULL"

// Extension-style
val isNullExt = F.col[SQL]("email").isNull
// isNullExt: Criteria[SQL] = email IS NULL
isNullExt.value
// res37: String = "email IS NULL"
```

Renders to SQL: `email IS NULL`

### ISNOTNULL

Tests whether a column value is not null.

```scala
// Function-style
val isNotNullFunc = F.isNotNull[SQL, Column](F.col("phone"))
// isNotNullFunc: Criteria[SQL] = phone IS NOT NULL
isNotNullFunc.value
// res38: String = "phone IS NOT NULL"

// Extension-style
val isNotNullExt = F.col[SQL]("phone").isNotNull
// isNotNullExt: Criteria[SQL] = phone IS NOT NULL
isNotNullExt.value
// res39: String = "phone IS NOT NULL"
```

Renders to SQL: `phone IS NOT NULL`

## Boolean Predicates

### ISTRUE

Tests whether a boolean column is true.

```scala
// Function-style
val isTrueFunc = F.isTrue[SQL, Column](F.col("active"))
// isTrueFunc: Criteria[SQL] = active IS TRUE
isTrueFunc.value
// res40: String = "active IS TRUE"

// Extension-style
val isTrueExt = F.col[SQL]("active").isTrue
// isTrueExt: Criteria[SQL] = active IS TRUE
isTrueExt.value
// res41: String = "active IS TRUE"
```

Renders to SQL: `active IS TRUE`

### ISFALSE

Tests whether a boolean column is false.

```scala
// Function-style
val isFalseFunc = F.isFalse[SQL, Column](F.col("archived"))
// isFalseFunc: Criteria[SQL] = archived IS FALSE
isFalseFunc.value
// res42: String = "archived IS FALSE"

// Extension-style
val isFalseExt = F.col[SQL]("archived").isFalse
// isFalseExt: Criteria[SQL] = archived IS FALSE
isFalseExt.value
// res43: String = "archived IS FALSE"
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
