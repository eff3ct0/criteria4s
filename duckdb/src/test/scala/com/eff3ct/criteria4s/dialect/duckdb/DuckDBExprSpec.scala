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

package com.eff3ct.criteria4s.dialect.duckdb

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F

class DuckDBExprSpec extends munit.FunSuite {

  test("DuckDB uses double-quoted column identifiers") {
    val show = summon[Show[Column, DuckDB]]
    assertEquals(show.show(Column("name")), "\"name\"")
  }

  test("DuckDB EQ renders with double-quoted columns") {
    val result = F.===[DuckDB, Column, Int](F.col("age"), F.lit(30))
    assertEquals(result.value, "\"age\" = 30")
  }

  test("DuckDB GT renders correctly") {
    val result = F.gt[DuckDB, Column, Int](F.col("age"), F.lit(18))
    assertEquals(result.value, "\"age\" > 18")
  }

  test("DuckDB AND renders correctly") {
    val left   = F.===[DuckDB, Column, Int](F.col("a"), F.lit(1))
    val right  = F.===[DuckDB, Column, Int](F.col("b"), F.lit(2))
    val result = F.and[DuckDB](left, right)
    assertEquals(result.value, "(\"a\" = 1) AND (\"b\" = 2)")
  }

  test("DuckDB IN renders correctly") {
    val result = F.in[DuckDB, Column, Seq[Int]](F.col("id"), F.array[DuckDB, Int](1, 2, 3))
    assertEquals(result.value, "\"id\" IN (1, 2, 3)")
  }

  test("DuckDB BETWEEN renders with AND separator") {
    val result =
      F.between[DuckDB, Column, (Int, Int)](F.col("age"), F.range[DuckDB, Int](18, 65))
    assertEquals(result.value, "\"age\" BETWEEN 18 AND 65")
  }

  test("DuckDB ISNULL renders correctly") {
    val result = F.isNull[DuckDB, Column](F.col("email"))
    assertEquals(result.value, "\"email\" IS NULL")
  }

  test("DuckDB ISTRUE renders correctly") {
    val result = F.isTrue[DuckDB, Column](F.col("active"))
    assertEquals(result.value, "\"active\" IS TRUE")
  }

  test("DuckDB NOT renders correctly") {
    val expr   = F.===[DuckDB, Column, Int](F.col("x"), F.lit(1))
    val result = F.not[DuckDB](expr)
    assertEquals(result.value, "NOT (\"x\" = 1)")
  }

  test("extension syntax works with DuckDB") {
    val result = (F.col[DuckDB]("age") geq F.lit(18))
      .and(F.col[DuckDB]("active") === F.lit(true))
    assertEquals(result.value, "(\"age\" >= 18) AND (\"active\" = true)")
  }
}
