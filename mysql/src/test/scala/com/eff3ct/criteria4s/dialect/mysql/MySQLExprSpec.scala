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

package com.eff3ct.criteria4s.dialect.mysql

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F

class MySQLExprSpec extends munit.FunSuite {

  test("MySQL uses backtick-quoted column identifiers") {
    val show = summon[Show[Column, MySQL]]
    assertEquals(show.show(Column("name")), "`name`")
  }

  test("MySQL EQ renders with backtick-quoted columns") {
    val result = F.===[MySQL, Column, Int](F.col("age"), F.lit(30))
    assertEquals(result.value, "`age` = 30")
  }

  test("MySQL GT renders correctly") {
    val result = F.gt[MySQL, Column, Int](F.col("age"), F.lit(18))
    assertEquals(result.value, "`age` > 18")
  }

  test("MySQL AND renders correctly") {
    val left   = F.===[MySQL, Column, Int](F.col("a"), F.lit(1))
    val right  = F.===[MySQL, Column, Int](F.col("b"), F.lit(2))
    val result = F.and[MySQL](left, right)
    assertEquals(result.value, "(`a` = 1) AND (`b` = 2)")
  }

  test("MySQL IN renders correctly") {
    val result = F.in[MySQL, Column, Seq[Int]](F.col("id"), F.array[MySQL, Int](1, 2, 3))
    assertEquals(result.value, "`id` IN (1, 2, 3)")
  }

  test("MySQL BETWEEN renders correctly") {
    val result =
      F.between[MySQL, Column, (Int, Int)](F.col("age"), F.range[MySQL, Int](18, 65))
    assertEquals(result.value, "`age` BETWEEN 18 AND 65")
  }

  test("MySQL ISNULL renders correctly") {
    val result = F.isNull[MySQL, Column](F.col("email"))
    assertEquals(result.value, "`email` IS NULL")
  }

  test("MySQL ISTRUE renders correctly") {
    val result = F.isTrue[MySQL, Column](F.col("active"))
    assertEquals(result.value, "`active` IS TRUE")
  }

  test("MySQL NOT renders correctly") {
    val expr   = F.===[MySQL, Column, Int](F.col("x"), F.lit(1))
    val result = F.not[MySQL](expr)
    assertEquals(result.value, "NOT (`x` = 1)")
  }

  test("extension syntax works with MySQL") {
    val result = (F.col[MySQL]("age") geq F.lit(18))
      .and(F.col[MySQL]("active") === F.lit(true))
    assertEquals(result.value, "(`age` >= 18) AND (`active` = true)")
  }
}
