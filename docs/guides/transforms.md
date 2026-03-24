---
sidebar_position: 3
---

# Transforms

Transforms wrap column references (or literal values) in SQL functions before they are used in predicates.
A transform takes a `Ref[T, V]` and returns a new `Ref[T, V]`, so you can compose them freely with
any predicate.

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Unary Transforms

These transforms wrap a single reference.

### UPPER

Converts a column or value to uppercase.

```scala mdoc
// Function-style
val upperFunc = F.upper[SQL, Column](F.col("name"))

// Extension-style
val upperExt = F.col[SQL]("name").upper
```

### LOWER

Converts a column or value to lowercase.

```scala mdoc
// Function-style
val lowerFunc = F.lower[SQL, Column](F.col("name"))

// Extension-style
val lowerExt = F.col[SQL]("name").lower
```

### TRIM

Removes leading and trailing whitespace from a column or value.

```scala mdoc
// Function-style
val trimFunc = F.trim[SQL, Column](F.col("name"))

// Extension-style
val trimExt = F.col[SQL]("name").trim
```

## Binary Transforms

These transforms combine two references.

### COALESCE

Returns the first non-null value from two references. Useful for providing fallback columns.

```scala mdoc
// Function-style
val coalesceFunc = F.coalesce[SQL, Column](F.col("nickname"), F.col("name"))
```

Use it in a predicate to test the coalesced value:

```scala mdoc
val coalesceExpr = F.coalesce[SQL, Column](F.col("nickname"), F.col("name")) === F.lit[SQL, String]("John")
coalesceExpr.value
```

Renders to SQL: `COALESCE(nickname, name) = 'John'`

### CONCAT

Concatenates two values together.

```scala mdoc
// Function-style -- concatenating two literals
val concatLiterals = F.concat[SQL, String](F.lit("Hello"), F.lit(" World"))

// Function-style -- concatenating two columns
val concatCols = F.concat[SQL, Column](F.col("first_name"), F.col("last_name"))
```

## Composing Transforms with Predicates

The real power of transforms is that they return `Ref` values, so they plug directly into any
predicate. This lets you write case-insensitive comparisons, trimmed matches, and more.

### Case-insensitive equality

```scala mdoc
val caseInsensitive = F.upper[SQL, Column](F.col("name")) === F.lit[SQL, String]("JOHN")
caseInsensitive.value
```

Renders to SQL: `UPPER(name) = 'JOHN'`

### Trimmed comparison

```scala mdoc
val trimmedMatch = F.trim[SQL, Column](F.col("code")) === F.lit[SQL, String]("ABC")
trimmedMatch.value
```

Renders to SQL: `TRIM(code) = 'ABC'`

### Lower-case LIKE search

```scala mdoc
val lowerLike = F.lower[SQL, Column](F.col("email")) like F.lit[SQL, String]("%@example.com")
lowerLike.value
```

Renders to SQL: `LOWER(email) LIKE '%@example.com'`

### Extension syntax composition

With extension-style, transforms chain naturally before predicates:

```scala mdoc
val extComposed = F.col[SQL]("name").upper === F.lit[SQL, String]("ALICE")
extComposed.value

val extLower = F.col[SQL]("email").lower like F.lit[SQL, String]("%@company.com")
extLower.value

val extTrim = F.col[SQL]("code").trim === F.lit[SQL, String]("XYZ")
extTrim.value
```

### Combining transforms in a filter

```scala mdoc
val searchFilter =
  (F.col[SQL]("name").upper === F.lit[SQL, String]("JOHN"))
    .and(F.col[SQL]("email").lower like F.lit[SQL, String]("%@example.com"))
    .and(F.col[SQL]("deleted_at").isNull)

searchFilter.value
```

## Quick Reference

| Transform | Function-style                     | Extension-style       | SQL output              |
|-----------|------------------------------------|-----------------------|-------------------------|
| UPPER     | `F.upper(ref)`                     | `ref.upper`           | `UPPER(col)`            |
| LOWER     | `F.lower(ref)`                     | `ref.lower`           | `LOWER(col)`            |
| TRIM      | `F.trim(ref)`                      | `ref.trim`            | `TRIM(col)`             |
| COALESCE  | `F.coalesce(ref1, ref2)`           | --                    | `COALESCE(a, b)`        |
| CONCAT    | `F.concat(ref1, ref2)`             | --                    | `CONCAT(a, b)`          |
