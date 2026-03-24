/*
 * MIT License
 *
 * Copyright (c) 2024 Rafael Fernandez
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
import com.eff3ct.criteria4s.functions.*

class SQLExprSpec extends munit.FunSuite {

  // -- Predicates (function-style API) --

  test("SQL EQ renders correctly") {
    val result = ===[SQL, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "age = 30")
  }

  test("SQL NEQ renders correctly") {
    val result = =!=[SQL, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "age != 30")
  }

  test("SQL GT renders correctly") {
    val result = gt[SQL, Column, Int](col("age"), lit(18))
    assertEquals(result.value, "age > 18")
  }

  test("SQL LT renders correctly") {
    val result = lt[SQL, Column, Int](col("age"), lit(65))
    assertEquals(result.value, "age < 65")
  }

  test("SQL GEQ renders correctly") {
    val result = geq[SQL, Column, Int](col("age"), lit(21))
    assertEquals(result.value, "age >= 21")
  }

  test("SQL LEQ renders correctly") {
    val result = leq[SQL, Column, Int](col("age"), lit(99))
    assertEquals(result.value, "age <= 99")
  }

  test("SQL LIKE renders correctly") {
    val result = like[SQL, Column, String](col("name"), lit("%John%"))
    assertEquals(result.value, "name LIKE %John%")
  }

  test("SQL IN renders correctly") {
    val result = in[SQL, Column, Seq[Int]](col("id"), array[SQL, Int](1, 2, 3))
    assertEquals(result.value, "id IN (1, 2, 3)")
  }

  test("SQL NOTIN renders correctly") {
    val result = notIn[SQL, Column, Seq[Int]](col("id"), array[SQL, Int](4, 5))
    assertEquals(result.value, "id NOT IN (4, 5)")
  }

  test("SQL ISNULL renders correctly") {
    val result = isNull[SQL, Column](col("email"))
    assertEquals(result.value, "email IS NULL")
  }

  test("SQL ISNOTNULL renders correctly") {
    val result = isNotNull[SQL, Column](col("email"))
    assertEquals(result.value, "email IS NOT NULL")
  }

  test("SQL BETWEEN renders correctly") {
    val result = between[SQL, Column, (Int, Int)](col("age"), range[SQL, Int](18, 65))
    assertEquals(result.value, "age BETWEEN 18 AND 65")
  }

  test("SQL NOTBETWEEN renders correctly") {
    val result = notBetween[SQL, Column, (Int, Int)](col("age"), range[SQL, Int](0, 17))
    assertEquals(result.value, "age NOT BETWEEN 0 AND 17")
  }

  // -- Conjunctions --

  test("SQL AND renders correctly") {
    val left   = ===[SQL, Column, Int](col("a"), lit(1))
    val right  = ===[SQL, Column, Int](col("b"), lit(2))
    val result = and[SQL](left, right)
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("SQL OR renders correctly") {
    val left   = ===[SQL, Column, Int](col("a"), lit(1))
    val right  = ===[SQL, Column, Int](col("b"), lit(2))
    val result = or[SQL](left, right)
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  test("SQL NOT renders correctly") {
    val expr   = ===[SQL, Column, Int](col("a"), lit(1))
    val result = not[SQL](expr)
    assertEquals(result.value, "NOT (a = 1)")
  }

  // -- Extension syntax --

  test("extension .=== delegates to EQ") {
    val result = col[SQL]("x").===(lit[SQL, Int](5))
    assertEquals(result.value, "x = 5")
  }

  test("extension .and delegates to AND") {
    val a      = ===[SQL, Column, Int](col("a"), lit(1))
    val b      = ===[SQL, Column, Int](col("b"), lit(2))
    val result = a.and(b)
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("extension .or delegates to OR") {
    val a      = ===[SQL, Column, Int](col("a"), lit(1))
    val b      = ===[SQL, Column, Int](col("b"), lit(2))
    val result = a.or(b)
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  test("extension .not delegates to NOT") {
    val expr   = ===[SQL, Column, Int](col("a"), lit(1))
    val result = expr.not
    assertEquals(result.value, "NOT (a = 1)")
  }

  test("eqv() alias works the same as ===") {
    val result = eqv[SQL, Column, Int](col("x"), lit(1))
    assertEquals(result.value, "x = 1")
  }

  test("extension .eqv delegates to EQ") {
    val result = col[SQL]("x").eqv(lit[SQL, Int](1))
    assertEquals(result.value, "x = 1")
  }

  // -- Show instances --

  test("Show[Column, SQL] renders unquoted identifier") {
    val show = implicitly[Show[Column, SQL]]
    assertEquals(show.show(Column("name")), "name")
  }

  test("Show[Seq[Int], SQL] renders as parenthesized list") {
    val show = implicitly[Show[Seq[Int], SQL]]
    assertEquals(show.show(Seq(1, 2, 3)), "(1, 2, 3)")
  }

  test("Show[(Int,Int), SQL] renders with AND separator") {
    val show = implicitly[Show[(Int, Int), SQL]]
    assertEquals(show.show((10, 20)), "10 AND 20")
  }

  // -- Bool --

  test("bool creates a criteria with string value") {
    val b = bool[SQL](true)
    assertEquals(b.value, "true")
  }

  // -- Symbol extensions --

  test("extension .:< delegates to LT") {
    val result = col[SQL]("x") :< lit[SQL, Int](5)
    assertEquals(result.value, "x < 5")
  }

  test("extension .:> delegates to GT") {
    val result = col[SQL]("x") :> lit[SQL, Int](5)
    assertEquals(result.value, "x > 5")
  }

  test("extension .:<= delegates to LEQ") {
    val result = col[SQL]("x") :<= lit[SQL, Int](5)
    assertEquals(result.value, "x <= 5")
  }

  test("extension .:>= delegates to GEQ") {
    val result = col[SQL]("x") :>= lit[SQL, Int](5)
    assertEquals(result.value, "x >= 5")
  }

  test("extension .:& delegates to AND") {
    val a      = ===[SQL, Column, Int](col("a"), lit(1))
    val b      = ===[SQL, Column, Int](col("b"), lit(2))
    val result = a :& b
    assertEquals(result.value, "(a = 1) AND (b = 2)")
  }

  test("extension .:| delegates to OR") {
    val a      = ===[SQL, Column, Int](col("a"), lit(1))
    val b      = ===[SQL, Column, Int](col("b"), lit(2))
    val result = a :| b
    assertEquals(result.value, "(a = 1) OR (b = 2)")
  }

  // -- New predicates (issue #7) --

  test("SQL STARTSWITH renders as LIKE") {
    val result = startsWith[SQL, Column, String](col("name"), lit("A%"))
    assertEquals(result.value, "name LIKE A%")
  }

  test("SQL ENDSWITH renders as LIKE") {
    val result = endsWith[SQL, Column, String](col("name"), lit("%z"))
    assertEquals(result.value, "name LIKE %z")
  }

  test("SQL CONTAINS renders as LIKE") {
    val result = contains[SQL, Column, String](col("name"), lit("%mid%"))
    assertEquals(result.value, "name LIKE %mid%")
  }

  test("SQL ISTRUE renders correctly") {
    val result = isTrue[SQL, Column](col("active"))
    assertEquals(result.value, "active IS TRUE")
  }

  test("SQL ISFALSE renders correctly") {
    val result = isFalse[SQL, Column](col("active"))
    assertEquals(result.value, "active IS FALSE")
  }

  test("extension .startsWith delegates to STARTSWITH") {
    val result = col[SQL]("name").startsWith(lit[SQL, String]("A%"))
    assertEquals(result.value, "name LIKE A%")
  }

  test("extension .isTrue delegates to ISTRUE") {
    val result = col[SQL]("active").isTrue
    assertEquals(result.value, "active IS TRUE")
  }

  test("extension .isFalse delegates to ISFALSE") {
    val result = col[SQL]("active").isFalse
    assertEquals(result.value, "active IS FALSE")
  }

  // -- Transform functions --

  test("upper() wraps ref in UPPER function") {
    val result = upper[SQL, Column](col("name")) === lit[SQL, String]("JOHN")
    assertEquals(result.value, "UPPER(name) = 'JOHN'")
  }

  test("lower() wraps ref in LOWER function") {
    val result = lower[SQL, Column](col("name")) === lit[SQL, String]("john")
    assertEquals(result.value, "LOWER(name) = 'john'")
  }

  test("trim() wraps ref in TRIM function") {
    val ref    = trim[SQL, Column](col("name"))
    val result = ref === lit[SQL, String]("John")
    assertEquals(result.value, "TRIM(name) = 'John'")
  }

  test("coalesce() wraps two refs in COALESCE function") {
    val ref    = coalesce[SQL, Column](col("nickname"), col("name"))
    val result = ref === lit[SQL, String]("John")
    assertEquals(result.value, "COALESCE(nickname, name) = 'John'")
  }

  test("concat() wraps two refs in CONCAT function") {
    val ref    = concat[SQL, String](lit("Hello"), lit(" World"))
    val result = ref === lit[SQL, String]("Hello World")
    assertEquals(result.value, "CONCAT('Hello', ' World') = 'Hello World'")
  }

  test("extension .upper delegates to UPPER") {
    val result = col[SQL]("name").upper === lit[SQL, String]("JOHN")
    assertEquals(result.value, "UPPER(name) = 'JOHN'")
  }

  // -- Composed expressions --

  test("complex composed expression renders correctly") {
    val expr = col[SQL]("age")
      .gt(lit[SQL, Int](18))
      .and(col[SQL]("name").like(lit[SQL, String]("A%")))
      .or(col[SQL]("active").===(lit[SQL, Boolean](true)))
    assertEquals(
      expr.value,
      "((age > 18) AND (name LIKE A%)) OR (active = true)"
    )
  }
}
