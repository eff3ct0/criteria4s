/*
 * MIT License
 *
 * Copyright (c) 2024-2026 Rafael Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.eff3ct.criteria4s.dialect.sql

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F

class SQLExprSpec extends munit.FunSuite {

  // -- Predicates (function-style API) --

  test("SQL EQ renders correctly") {
    val result = F.===[SQL, Column, Int](F.col("age"), F.lit(30))
    assertEquals(result.value, "age = 30")
  }

  test("SQL NEQ renders correctly") {
    val result = F.=!=[SQL, Column, Int](F.col("age"), F.lit(30))
    assertEquals(result.value, "age != 30")
  }

  test("SQL GT renders correctly") {
    val result = F.gt[SQL, Column, Int](F.col("age"), F.lit(18))
    assertEquals(result.value, "age > 18")
  }

  test("SQL LT renders correctly") {
    val result = F.lt[SQL, Column, Int](F.col("age"), F.lit(65))
    assertEquals(result.value, "age < 65")
  }

  test("SQL GEQ renders correctly") {
    val result = F.geq[SQL, Column, Int](F.col("age"), F.lit(21))
    assertEquals(result.value, "age >= 21")
  }

  test("SQL LEQ renders correctly") {
    val result = F.leq[SQL, Column, Int](F.col("age"), F.lit(99))
    assertEquals(result.value, "age <= 99")
  }

  test("SQL LIKE renders correctly") {
    val result = F.like[SQL, Column, String](F.col("name"), F.lit("%John%"))
    assertEquals(result.value, "name LIKE '%John%'")
  }

  test("SQL IN renders correctly") {
    val result = F.in[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](1, 2, 3))
    assertEquals(result.value, "id IN (1, 2, 3)")
  }

  test("SQL NOTIN renders correctly") {
    val result = F.notIn[SQL, Column, Seq[Int]](F.col("id"), F.array[SQL, Int](4, 5))
    assertEquals(result.value, "id NOT IN (4, 5)")
  }

  test("SQL ISNULL renders correctly") {
    val result = F.isNull[SQL, Column](F.col("email"))
    assertEquals(result.value, "email IS NULL")
  }

  test("SQL ISNOTNULL renders correctly") {
    val result = F.isNotNull[SQL, Column](F.col("email"))
    assertEquals(result.value, "email IS NOT NULL")
  }

  test("SQL BETWEEN renders correctly") {
    val result = F.between[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](18, 65))
    assertEquals(result.value, "age BETWEEN 18 AND 65")
  }

  test("SQL NOTBETWEEN renders correctly") {
    val result = F.notBetween[SQL, Column, (Int, Int)](F.col("age"), F.range[SQL, Int](0, 17))
    assertEquals(result.value, "age NOT BETWEEN 0 AND 17")
  }

  // -- Conjunctions --

  test("SQL AND renders correctly") {
    val left   = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val right  = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = F.and[SQL](left, right)
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("SQL OR renders correctly") {
    val left   = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val right  = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = F.or[SQL](left, right)
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  test("SQL NOT renders correctly") {
    val expr   = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val result = F.not[SQL](expr)
    assertEquals(result.value, "NOT (a = 1)")
  }

  // -- Extension syntax --

  test("extension .=== delegates to EQ") {
    val result = F.col[SQL]("x").===(F.lit[SQL, Int](5))
    assertEquals(result.value, "x = 5")
  }

  test("extension .and delegates to AND") {
    val a      = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val b      = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = a.and(b)
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("extension .or delegates to OR") {
    val a      = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val b      = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = a.or(b)
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  test("extension .not delegates to NOT") {
    val expr   = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val result = expr.not
    assertEquals(result.value, "NOT (a = 1)")
  }

  test("eqv() alias works the same as ===") {
    val result = F.eqv[SQL, Column, Int](F.col("x"), F.lit(1))
    assertEquals(result.value, "x = 1")
  }

  test("extension .eqv delegates to EQ") {
    val result = F.col[SQL]("x").eqv(F.lit[SQL, Int](1))
    assertEquals(result.value, "x = 1")
  }

  // -- Show instances --

  test("Show[Column, SQL] renders unquoted identifier") {
    val show = summon[Show[Column, SQL]]
    assertEquals(show.show(Column("name")), "name")
  }

  test("Show[String, SQL] escapes single quotes") {
    val show = summon[Show[String, SQL]]
    assertEquals(show.show("O'Brien"), "'O''Brien'")
  }

  test("Show[Seq[Int], SQL] renders as parenthesized list") {
    val show = summon[Show[Seq[Int], SQL]]
    assertEquals(show.show(Seq(1, 2, 3)), "(1, 2, 3)")
  }

  test("Show[(Int,Int), SQL] renders with AND separator") {
    val show = summon[Show[(Int, Int), SQL]]
    assertEquals(show.show((10, 20)), "10 AND 20")
  }

  // -- Bool --

  test("bool creates a criteria with string value") {
    val b = F.bool[SQL](true)
    assertEquals(b.value, "true")
  }

  // -- Symbol extensions --

  test("extension .:< delegates to LT") {
    val result = F.col[SQL]("x") :< F.lit[SQL, Int](5)
    assertEquals(result.value, "x < 5")
  }

  test("extension .:> delegates to GT") {
    val result = F.col[SQL]("x") :> F.lit[SQL, Int](5)
    assertEquals(result.value, "x > 5")
  }

  test("extension .:<= delegates to LEQ") {
    val result = F.col[SQL]("x") :<= F.lit[SQL, Int](5)
    assertEquals(result.value, "x <= 5")
  }

  test("extension .:>= delegates to GEQ") {
    val result = F.col[SQL]("x") :>= F.lit[SQL, Int](5)
    assertEquals(result.value, "x >= 5")
  }

  test("extension .:& delegates to AND") {
    val a      = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val b      = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = a :& b
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("extension .:| delegates to OR") {
    val a      = F.===[SQL, Column, Int](F.col("a"), F.lit(1))
    val b      = F.===[SQL, Column, Int](F.col("b"), F.lit(2))
    val result = a :| b
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  // -- New predicates (issue #7) --

  test("SQL STARTSWITH renders as LIKE") {
    val result = F.startsWith[SQL, Column, String](F.col("name"), F.lit("A%"))
    assertEquals(result.value, "name LIKE 'A%'")
  }

  test("SQL ENDSWITH renders as LIKE") {
    val result = F.endsWith[SQL, Column, String](F.col("name"), F.lit("%z"))
    assertEquals(result.value, "name LIKE '%z'")
  }

  test("SQL CONTAINS renders as LIKE") {
    val result = F.contains[SQL, Column, String](F.col("name"), F.lit("%mid%"))
    assertEquals(result.value, "name LIKE '%mid%'")
  }

  test("SQL ISTRUE renders correctly") {
    val result = F.isTrue[SQL, Column](F.col("active"))
    assertEquals(result.value, "active IS TRUE")
  }

  test("SQL ISFALSE renders correctly") {
    val result = F.isFalse[SQL, Column](F.col("active"))
    assertEquals(result.value, "active IS FALSE")
  }

  test("extension .startsWith delegates to STARTSWITH") {
    val result = F.col[SQL]("name").startsWith(F.lit[SQL, String]("A%"))
    assertEquals(result.value, "name LIKE 'A%'")
  }

  test("extension .isTrue delegates to ISTRUE") {
    val result = F.col[SQL]("active").isTrue
    assertEquals(result.value, "active IS TRUE")
  }

  test("extension .isFalse delegates to ISFALSE") {
    val result = F.col[SQL]("active").isFalse
    assertEquals(result.value, "active IS FALSE")
  }

  // -- Transform functions --

  test("upper() wraps ref in UPPER function") {
    val result = F.upper[SQL, Column](F.col("name")) === F.lit[SQL, String]("JOHN")
    assertEquals(result.value, "UPPER(name) = 'JOHN'")
  }

  test("lower() wraps ref in LOWER function") {
    val result = F.lower[SQL, Column](F.col("name")) === F.lit[SQL, String]("john")
    assertEquals(result.value, "LOWER(name) = 'john'")
  }

  test("trim() wraps ref in TRIM function") {
    val ref    = F.trim[SQL, Column](F.col("name"))
    val result = ref === F.lit[SQL, String]("John")
    assertEquals(result.value, "TRIM(name) = 'John'")
  }

  test("coalesce() wraps two refs in COALESCE function") {
    val ref    = F.coalesce[SQL, Column](F.col("nickname"), F.col("name"))
    val result = ref === F.lit[SQL, String]("John")
    assertEquals(result.value, "COALESCE(nickname, name) = 'John'")
  }

  test("concat() wraps two refs in CONCAT function") {
    val ref    = F.concat[SQL, String](F.lit("Hello"), F.lit(" World"))
    val result = ref === F.lit[SQL, String]("Hello World")
    assertEquals(result.value, "CONCAT('Hello', ' World') = 'Hello World'")
  }

  test("extension .upper delegates to UPPER") {
    val result = F.col[SQL]("name").upper === F.lit[SQL, String]("JOHN")
    assertEquals(result.value, "UPPER(name) = 'JOHN'")
  }

  // -- Ordering --

  test("asc() renders ASC ordering") {
    val result = F.asc[SQL, Column](F.col("name"))
    assertEquals(result.value, "name ASC")
  }

  test("desc() renders DESC ordering") {
    val result = F.desc[SQL, Column](F.col("age"))
    assertEquals(result.value, "age DESC")
  }

  test("extension .asc delegates to OrderAsc") {
    val result = F.col[SQL]("name").asc
    assertEquals(result.value, "name ASC")
  }

  test("extension .desc delegates to OrderDesc") {
    val result = F.col[SQL]("age").desc
    assertEquals(result.value, "age DESC")
  }

  // -- LIMIT / OFFSET --

  test("limit() renders LIMIT clause") {
    val result = F.limit[SQL](10)
    assertEquals(result.value, "LIMIT 10")
  }

  test("offset() renders OFFSET clause") {
    val result = F.offset[SQL](20)
    assertEquals(result.value, "OFFSET 20")
  }

  // -- CASE WHEN --

  test("CASE WHEN with single branch") {
    val result = F
      .caseWhen[SQL, Int](
        F.col[SQL]("status") === F.lit[SQL, String]("active"),
        F.lit[SQL, Int](1)
      )
      .otherwise(F.lit[SQL, Int](0))
    assertEquals(result.asString, "CASE WHEN status = 'active' THEN 1 ELSE 0 END")
  }

  test("CASE WHEN with multiple branches") {
    val result = F
      .caseWhen[SQL, String](
        F.col[SQL]("score") gt F.lit[SQL, Int](90),
        F.lit[SQL, String]("A")
      )
      .when(
        F.col[SQL]("score") gt F.lit[SQL, Int](80),
        F.lit[SQL, String]("B")
      )
      .otherwise(F.lit[SQL, String]("C"))
    assertEquals(
      result.asString,
      "CASE WHEN score > 90 THEN 'A' WHEN score > 80 THEN 'B' ELSE 'C' END"
    )
  }

  // -- Composed expressions --

  test("complex composed expression renders correctly") {
    val expr = F
      .col[SQL]("age")
      .gt(F.lit[SQL, Int](18))
      .and(F.col[SQL]("name").like(F.lit[SQL, String]("A%")))
      .or(F.col[SQL]("active").===(F.lit[SQL, Boolean](true)))
    assertEquals(
      expr.value,
      "((age > 18) AND (name LIKE 'A%')) OR (active = true)"
    )
  }
}
