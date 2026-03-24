---
sidebar_position: 4
title: Spark SQL
---

# Spark SQL Dialect

The Spark SQL dialect extends the base SQL dialect with **backtick-quoted** column identifiers, matching Apache Spark's SQL syntax.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql" % "@VERSION@"
```

:::note
Apache Spark itself currently requires Scala 2.13, but the criteria4s-sparksql module compiles with Scala 3. It generates SQL expression strings that you can pass to Spark's `where()` / `filter()` methods. The criteria4s expression module does not depend on Spark at runtime — it only produces strings.
:::

## Import Pattern

```scala mdoc:silent
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sparksql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

Spark SQL uses backtick-quoted identifiers, identical to MySQL:

```scala mdoc
val column = summon[Show[Column, SparkSQL]]
column.show(Column("user_name"))
```

All predicates render with backtick-quoted column names:

```scala mdoc
F.===[SparkSQL, Column, Int](F.col("age"), F.lit(30)).value
```

## Inherited Operations

SparkSQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala mdoc
// Comparison
F.gt[SparkSQL, Column, Int](F.col("age"), F.lit(18)).value

// Pattern matching
F.like[SparkSQL, Column, String](F.col("name"), F.lit("%John%")).value

// Set membership
F.in[SparkSQL, Column, Seq[String]](
  F.col("status"), F.array[SparkSQL, String]("active", "pending")
).value

// Null checks
F.isNull[SparkSQL, Column](F.col("partition_date")).value

// Range
F.between[SparkSQL, Column, (Int, Int)](
  F.col("score"), F.range[SparkSQL, Int](70, 100)
).value
```

## Practical Examples

### DataFrame filter expression

criteria4s generates the filter string, which you then pass to Spark's string-based filter API:

```scala mdoc
val sparkFilter = F.col[SparkSQL]("age")
  .geq(F.lit[SparkSQL, Int](18))
  .and(F.col[SparkSQL]("country") === F.lit[SparkSQL, String]("US"))

sparkFilter.value
```

In your Spark 2.13 project, you would use it like this:

```scala
// In your Spark 2.13 project:
val filterStr: String = sparkFilter.value
df.where(filterStr)
```

### ETL partition filter

```scala mdoc
val partitionFilter = F.col[SparkSQL]("partition_date")
  .between(F.range[SparkSQL, String]("2025-01-01", "2025-03-31"))
  .and(F.col[SparkSQL]("region").in(F.array[SparkSQL, String]("us-east-1", "eu-west-1")))

partitionFilter.value
```

### Data quality check

```scala mdoc
val qualityCheck = F.col[SparkSQL]("email").isNotNull
  .and(F.col[SparkSQL]("email").like(F.lit[SparkSQL, String]("%@%")))
  .and(F.col[SparkSQL]("age") :> F.lit[SparkSQL, Int](0))

qualityCheck.value
```
