---
sidebar_position: 4
title: Spark SQL
---

# Spark SQL Dialect

The Spark SQL dialect extends the base SQL dialect with **backtick-quoted** column identifiers, matching Apache Spark's SQL syntax.

## Dependency

```scala
libraryDependencies += "com.eff3ct" %% "criteria4s-sparksql" % "1.0.0"
```

:::note
Apache Spark itself currently requires Scala 2.13, but the criteria4s-sparksql module compiles with Scala 3. It generates SQL expression strings that you can pass to Spark's `where()` / `filter()` methods. The criteria4s expression module does not depend on Spark at runtime — it only produces strings.
:::

## Import Pattern

```scala
import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.sparksql.{*, given}
import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.extensions.*
```

## Column Quoting

Spark SQL uses backtick-quoted identifiers, identical to MySQL:

```scala
val column = summon[Show[Column, SparkSQL]]
// column: Show[Column, SparkSQL] = com.eff3ct.criteria4s.core.Show$$$Lambda$2369/0x00007f9b00778e10@4e5d5388
column.show(Column("user_name"))
// res0: String = "`user_name`"
```

All predicates render with backtick-quoted column names:

```scala
F.===[SparkSQL, Column, Int](F.col("age"), F.lit(30)).value
// res1: String = "`age` = 30"
```

## Inherited Operations

SparkSQL inherits every operation from the base SQL dialect. All predicates, conjunctions, transforms, ordering, LIMIT/OFFSET, and CASE WHEN work identically — only the column quoting differs.

## Predicate Examples

```scala
// Comparison
F.gt[SparkSQL, Column, Int](F.col("age"), F.lit(18)).value
// res2: String = "`age` > 18"

// Pattern matching
F.like[SparkSQL, Column, String](F.col("name"), F.lit("%John%")).value
// res3: String = "`name` LIKE %John%"

// Set membership
F.in[SparkSQL, Column, Seq[String]](
  F.col("status"), F.array[SparkSQL, String]("active", "pending")
).value
// res4: String = "`status` IN (active, pending)"

// Null checks
F.isNull[SparkSQL, Column](F.col("partition_date")).value
// res5: String = "`partition_date` IS NULL"

// Range
F.between[SparkSQL, Column, (Int, Int)](
  F.col("score"), F.range[SparkSQL, Int](70, 100)
).value
// res6: String = "`score` BETWEEN 70 AND 100"
```

## Practical Examples

### DataFrame filter expression

criteria4s generates the filter string, which you then pass to Spark's string-based filter API:

```scala
val sparkFilter = F.col[SparkSQL]("age")
  .geq(F.lit[SparkSQL, Int](18))
  .and(F.col[SparkSQL]("country") === F.lit[SparkSQL, String]("US"))
// sparkFilter: Criteria[SparkSQL] = (`age` >= 18) AND (`country` = US)

sparkFilter.value
// res7: String = "(`age` >= 18) AND (`country` = US)"
```

In your Spark 2.13 project, you would use it like this:

```scala
// In your Spark 2.13 project:
val filterStr: String = sparkFilter.value
df.where(filterStr)
```

### ETL partition filter

```scala
val partitionFilter = F.col[SparkSQL]("partition_date")
  .between(F.range[SparkSQL, String]("2025-01-01", "2025-03-31"))
  .and(F.col[SparkSQL]("region").in(F.array[SparkSQL, String]("us-east-1", "eu-west-1")))
// partitionFilter: Criteria[SparkSQL] = (`partition_date` BETWEEN 2025-01-01 AND 2025-03-31) AND (`region` IN (us-east-1, eu-west-1))

partitionFilter.value
// res8: String = "(`partition_date` BETWEEN 2025-01-01 AND 2025-03-31) AND (`region` IN (us-east-1, eu-west-1))"
```

### Data quality check

```scala
val qualityCheck = F.col[SparkSQL]("email").isNotNull
  .and(F.col[SparkSQL]("email").like(F.lit[SparkSQL, String]("%@%")))
  .and(F.col[SparkSQL]("age") :> F.lit[SparkSQL, Int](0))
// qualityCheck: Criteria[SparkSQL] = ((`email` IS NOT NULL) AND (`email` LIKE %@%)) AND (`age` > 0)

qualityCheck.value
// res9: String = "((`email` IS NOT NULL) AND (`email` LIKE %@%)) AND (`age` > 0)"
```
