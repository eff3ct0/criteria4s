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

package com.eff3ct.criteria4s.dialect.postgresql

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions.*

class PostgreSQLExprSpec extends munit.FunSuite {

  test("PostgreSQL uses double-quoted column identifiers") {
    val show = implicitly[Show[Column, PostgreSQL]]
    assertEquals(show.show(Column("name")), "\"name\"")
  }

  test("PostgreSQL EQ renders with double-quoted columns") {
    val result = ===[PostgreSQL, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "\"age\" = 30")
  }

  test("PostgreSQL GT renders correctly") {
    val result = gt[PostgreSQL, Column, Int](col("age"), lit(18))
    assertEquals(result.value, "\"age\" > 18")
  }

  test("PostgreSQL AND renders correctly") {
    val left   = ===[PostgreSQL, Column, Int](col("a"), lit(1))
    val right  = ===[PostgreSQL, Column, Int](col("b"), lit(2))
    val result = and[PostgreSQL](left, right)
    assertEquals(result.value, "(\"a\" = 1) AND (\"b\" = 2)")
  }

  test("PostgreSQL IN renders correctly") {
    val result = in[PostgreSQL, Column, Seq[Int]](col("id"), array[PostgreSQL, Int](1, 2, 3))
    assertEquals(result.value, "\"id\" IN (1, 2, 3)")
  }

  test("PostgreSQL BETWEEN renders with AND separator") {
    val result =
      between[PostgreSQL, Column, (Int, Int)](col("age"), range[PostgreSQL, Int](18, 65))
    assertEquals(result.value, "\"age\" BETWEEN 18 AND 65")
  }

  test("PostgreSQL ISNULL renders correctly") {
    val result = isNull[PostgreSQL, Column](col("email"))
    assertEquals(result.value, "\"email\" IS NULL")
  }

  test("PostgreSQL ISTRUE renders correctly") {
    val result = isTrue[PostgreSQL, Column](col("active"))
    assertEquals(result.value, "\"active\" IS TRUE")
  }

  test("PostgreSQL NOT renders correctly") {
    val expr   = ===[PostgreSQL, Column, Int](col("x"), lit(1))
    val result = not[PostgreSQL](expr)
    assertEquals(result.value, "NOT (\"x\" = 1)")
  }

  test("extension syntax works with PostgreSQL") {
    val result = (col[PostgreSQL]("age") geq lit(18))
      .and(col[PostgreSQL]("active") === lit(true))
    assertEquals(result.value, "(\"age\" >= 18) AND (\"active\" = true)")
  }
}
