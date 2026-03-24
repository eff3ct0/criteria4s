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

package com.eff3ct.criteria4s.dialect.clickhouse.client

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.clickhouse.*
import com.eff3ct.criteria4s.functions as F

class CHClientIntegrationSpec extends munit.FunSuite {

  test("toSqlFragment returns the raw criteria value") {
    val criteria = F.===[ClickHouse, Column, Int](F.col("age"), F.lit(30))
    assertEquals(criteria.toSqlFragment, "`age` = 30")
  }

  test("toWhereClause prepends WHERE keyword") {
    val criteria = F.===[ClickHouse, Column, Int](F.col("age"), F.lit(30))
    assertEquals(criteria.toWhereClause, "WHERE `age` = 30")
  }

  test("appendTo builds a full SQL string") {
    val criteria = F.gt[ClickHouse, Column, Int](F.col("age"), F.lit(18))
    assertEquals(
      criteria.appendTo("SELECT * FROM users"),
      "SELECT * FROM users WHERE `age` > 18"
    )
  }

  test("appendTo with AND criteria") {
    val criteria = F.and[ClickHouse](
      F.geq[ClickHouse, Column, Int](F.col("age"), F.lit(18)),
      F.===[ClickHouse, Column, Boolean](F.col("active"), F.lit(true))
    )
    assertEquals(
      criteria.appendTo("SELECT * FROM events"),
      "SELECT * FROM events WHERE (`age` >= 18) AND (`active` = true)"
    )
  }

  test("given Conversion[Criteria[ClickHouse], String] works") {
    val criteria: Criteria[ClickHouse] = F.===[ClickHouse, Column, Int](F.col("x"), F.lit(5))
    val sqlString: String              = criteria
    assertEquals(sqlString, "`x` = 5")
  }
}
