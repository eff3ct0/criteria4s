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

package com.eff3ct.criteria4s.dialect.sparksql

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions.*

class SparkSQLExprSpec extends munit.FunSuite {

  test("SparkSQL uses backtick-quoted column identifiers") {
    val show = implicitly[Show[Column, SparkSQL]]
    assertEquals(show.show(Column("name")), "`name`")
  }

  test("SparkSQL EQ renders with backtick-quoted columns") {
    val result = ===[SparkSQL, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "`age` = 30")
  }

  test("SparkSQL GT renders correctly") {
    val result = gt[SparkSQL, Column, Int](col("age"), lit(18))
    assertEquals(result.value, "`age` > 18")
  }

  test("SparkSQL AND renders correctly") {
    val left   = ===[SparkSQL, Column, Int](col("a"), lit(1))
    val right  = ===[SparkSQL, Column, Int](col("b"), lit(2))
    val result = and[SparkSQL](left, right)
    assertEquals(result.value, "(`a` = 1) AND (`b` = 2)")
  }

  test("SparkSQL IN renders correctly") {
    val result = in[SparkSQL, Column, Seq[Int]](col("id"), array[SparkSQL, Int](1, 2, 3))
    assertEquals(result.value, "`id` IN (1, 2, 3)")
  }

  test("SparkSQL BETWEEN renders correctly") {
    val result =
      between[SparkSQL, Column, (Int, Int)](col("age"), range[SparkSQL, Int](18, 65))
    assertEquals(result.value, "`age` BETWEEN 18 AND 65")
  }

  test("SparkSQL ISNULL renders correctly") {
    val result = isNull[SparkSQL, Column](col("email"))
    assertEquals(result.value, "`email` IS NULL")
  }

  test("SparkSQL ISTRUE renders correctly") {
    val result = isTrue[SparkSQL, Column](col("active"))
    assertEquals(result.value, "`active` IS TRUE")
  }

  test("SparkSQL NOT renders correctly") {
    val expr   = ===[SparkSQL, Column, Int](col("x"), lit(1))
    val result = not[SparkSQL](expr)
    assertEquals(result.value, "NOT (`x` = 1)")
  }

  test("extension syntax works with SparkSQL") {
    val result = (col[SparkSQL]("age") geq lit(18))
      .and(col[SparkSQL]("active") === lit(true))
    assertEquals(result.value, "(`age` >= 18) AND (`active` = true)")
  }
}
