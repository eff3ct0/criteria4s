---
sidebar_position: 3
---

# Transforms

Transforms wrap column references (or literal values) in SQL functions before they are used in predicates.
A transform takes a `Ref[T, V]` and returns a new `Ref[T, V]`, so you can compose them freely with
any predicate.

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sql.{given, *}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F
```

## Unary Transforms

These transforms wrap a single reference.

### UPPER

Converts a column or value to uppercase. You can use either the function or extension style:

```scala
// Function-style
val upperFunc = F.upper[SQL, Column](F.col("name"))
// upperFunc: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@24c74775

// Extension-style
val upperExt = F.col[SQL]("name").upper
// upperExt: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@364d9ab0
```

### LOWER

Converts a column or value to lowercase:

```scala
// Function-style
val lowerFunc = F.lower[SQL, Column](F.col("name"))
// lowerFunc: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@4e0c871b

// Extension-style
val lowerExt = F.col[SQL]("name").lower
// lowerExt: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@5b28a051
```

### TRIM

Removes leading and trailing whitespace from a column or value:

```scala
// Function-style
val trimFunc = F.trim[SQL, Column](F.col("name"))
// trimFunc: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@5508abf0

// Extension-style
val trimExt = F.col[SQL]("name").trim
// trimExt: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$8@1b904d48
```

## Binary Transforms

These transforms combine two references into a single `Ref` value.

### COALESCE

Returns the first non-null value from two references. Useful for providing a fallback column when one might be null:

```scala
// Function-style
val coalesceFunc = F.coalesce[SQL, Column](F.col("nickname"), F.col("name"))
// coalesceFunc: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$9@42723b0e
```

Once you have a coalesced reference, you use it in a predicate just like any other `Ref`:

```scala
val coalesceExpr = F.coalesce[SQL, Column](F.col("nickname"), F.col("name")) === F.lit[SQL, String]("John")
// coalesceExpr: Criteria[SQL] = COALESCE(nickname, name) = 'John'
coalesceExpr.value
// res0: String = "COALESCE(nickname, name) = 'John'"
```

Renders to SQL: `COALESCE(nickname, name) = 'John'`

### CONCAT

Concatenates two values together:

```scala
// Function-style -- concatenating two literals
val concatLiterals = F.concat[SQL, String](F.lit("Hello"), F.lit(" World"))
// concatLiterals: Ref[SQL, String] = com.eff3ct.criteria4s.core.Ref$$anon$9@57b685d1

// Function-style -- concatenating two columns
val concatCols = F.concat[SQL, Column](F.col("first_name"), F.col("last_name"))
// concatCols: Ref[SQL, Column] = com.eff3ct.criteria4s.core.Ref$$anon$9@518ce3c2
```

## Composing Transforms with Predicates

The real power of transforms is that they return `Ref` values, so they plug directly into any predicate. This lets you write case-insensitive comparisons, trimmed matches, and more without any special syntax.

### Case-insensitive equality

```scala
val caseInsensitive = F.upper[SQL, Column](F.col("name")) === F.lit[SQL, String]("JOHN")
// caseInsensitive: Criteria[SQL] = UPPER(name) = 'JOHN'
caseInsensitive.value
// res1: String = "UPPER(name) = 'JOHN'"
```

Renders to SQL: `UPPER(name) = 'JOHN'`

### Trimmed comparison

```scala
val trimmedMatch = F.trim[SQL, Column](F.col("code")) === F.lit[SQL, String]("ABC")
// trimmedMatch: Criteria[SQL] = TRIM(code) = 'ABC'
trimmedMatch.value
// res2: String = "TRIM(code) = 'ABC'"
```

Renders to SQL: `TRIM(code) = 'ABC'`

### Lower-case LIKE search

```scala
val lowerLike = F.lower[SQL, Column](F.col("email")) like F.lit[SQL, String]("%@example.com")
// lowerLike: Criteria[SQL] = LOWER(email) LIKE '%@example.com'
lowerLike.value
// res3: String = "LOWER(email) LIKE '%@example.com'"
```

Renders to SQL: `LOWER(email) LIKE '%@example.com'`

### Extension syntax composition

With extension-style, transforms chain naturally before predicates, so the code reads almost like plain prose:

```scala
val extComposed = F.col[SQL]("name").upper === F.lit[SQL, String]("ALICE")
// extComposed: Criteria[SQL] = UPPER(name) = 'ALICE'
extComposed.value
// res4: String = "UPPER(name) = 'ALICE'"

val extLower = F.col[SQL]("email").lower like F.lit[SQL, String]("%@company.com")
// extLower: Criteria[SQL] = LOWER(email) LIKE '%@company.com'
extLower.value
// res5: String = "LOWER(email) LIKE '%@company.com'"

val extTrim = F.col[SQL]("code").trim === F.lit[SQL, String]("XYZ")
// extTrim: Criteria[SQL] = TRIM(code) = 'XYZ'
extTrim.value
// res6: String = "TRIM(code) = 'XYZ'"
```

### Combining transforms in a filter

You can mix transforms with regular predicates in a single compound expression:

```scala
val searchFilter =
  (F.col[SQL]("name").upper === F.lit[SQL, String]("JOHN"))
    .and(F.col[SQL]("email").lower like F.lit[SQL, String]("%@example.com"))
    .and(F.col[SQL]("deleted_at").isNull)
// searchFilter: Criteria[SQL] = ((UPPER(name) = 'JOHN') AND (LOWER(email) LIKE '%@example.com')) AND (deleted_at IS NULL)

searchFilter.value
// res7: String = "((UPPER(name) = 'JOHN') AND (LOWER(email) LIKE '%@example.com')) AND (deleted_at IS NULL)"
```

## Quick Reference

| Transform | Function-style                     | Extension-style       | SQL output              |
|-----------|------------------------------------|-----------------------|-------------------------|
| UPPER     | `F.upper(ref)`                     | `ref.upper`           | `UPPER(col)`            |
| LOWER     | `F.lower(ref)`                     | `ref.lower`           | `LOWER(col)`            |
| TRIM      | `F.trim(ref)`                      | `ref.trim`            | `TRIM(col)`             |
| COALESCE  | `F.coalesce(ref1, ref2)`           | (no extension alias)  | `COALESCE(a, b)`        |
| CONCAT    | `F.concat(ref1, ref2)`             | (no extension alias)  | `CONCAT(a, b)`          |
