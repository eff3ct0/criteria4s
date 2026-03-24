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

package com.eff3ct.criteria4s.dialect.mongodb

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions.*

class MongoDBExprSpec extends munit.FunSuite {

  // -- Predicates --

  test("MongoDB EQ renders correctly") {
    val result = ===[MongoDB, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "{\"age\": {$eq: 30}}")
  }

  test("MongoDB NEQ renders correctly") {
    val result = =!=[MongoDB, Column, Int](col("age"), lit(30))
    assertEquals(result.value, "{\"age\": {$ne: 30}}")
  }

  test("MongoDB GT renders correctly") {
    val result = gt[MongoDB, Column, Int](col("age"), lit(18))
    assertEquals(result.value, "{\"age\": {$gt: 18}}")
  }

  test("MongoDB LT renders correctly") {
    val result = lt[MongoDB, Column, Int](col("age"), lit(65))
    assertEquals(result.value, "{\"age\": {$lt: 65}}")
  }

  test("MongoDB GEQ renders correctly") {
    val result = geq[MongoDB, Column, Int](col("age"), lit(21))
    assertEquals(result.value, "{\"age\": {$gte: 21}}")
  }

  test("MongoDB LEQ renders correctly") {
    val result = leq[MongoDB, Column, Int](col("age"), lit(99))
    assertEquals(result.value, "{\"age\": {$lte: 99}}")
  }

  test("MongoDB IN renders correctly") {
    val result = in[MongoDB, Column, Seq[Int]](col("id"), array[MongoDB, Int](1, 2, 3))
    assertEquals(result.value, "{\"id\": {$in: [1, 2, 3]}}")
  }

  test("MongoDB NOTIN renders correctly") {
    val result = notIn[MongoDB, Column, Seq[Int]](col("id"), array[MongoDB, Int](4, 5))
    assertEquals(result.value, "{\"id\": {$nin: [4, 5]}}")
  }

  test("MongoDB ISNULL renders correctly") {
    val result = isNull[MongoDB, Column](col("email"))
    assertEquals(result.value, "{\"email\": null}")
  }

  test("MongoDB ISNOTNULL renders correctly") {
    val result = isNotNull[MongoDB, Column](col("email"))
    assertEquals(result.value, "{\"email\": {$ne: null}}")
  }

  test("MongoDB BETWEEN renders correctly") {
    val result = between[MongoDB, Column, (Int, Int)](col("age"), range[MongoDB, Int](18, 65))
    assertEquals(result.value, "{\"age\": { $gte: 18, $lt: 65 }}")
  }

  test("MongoDB NOTBETWEEN renders correctly") {
    val result =
      notBetween[MongoDB, Column, (Int, Int)](col("age"), range[MongoDB, Int](0, 17))
    assertEquals(result.value, "{\"age\": {$not: { $gte: 0, $lt: 17 }}}")
  }

  // -- Conjunctions --

  test("MongoDB AND renders correctly") {
    val left   = ===[MongoDB, Column, Int](col("a"), lit(1))
    val right  = ===[MongoDB, Column, Int](col("b"), lit(2))
    val result = and[MongoDB](left, right)
    assertEquals(result.value, "{$and: [{\"a\": {$eq: 1}}, {\"b\": {$eq: 2}}]}")
  }

  test("MongoDB OR renders correctly") {
    val left   = ===[MongoDB, Column, Int](col("a"), lit(1))
    val right  = ===[MongoDB, Column, Int](col("b"), lit(2))
    val result = or[MongoDB](left, right)
    assertEquals(result.value, "{$or: [{\"a\": {$eq: 1}}, {\"b\": {$eq: 2}}]}")
  }

  // -- Show instances --

  test("Show[Column, MongoDB] renders with double quotes") {
    val show = summon[Show[Column, MongoDB]]
    assertEquals(show.show(Column("name")), "\"name\"")
  }

  test("Show[Seq[Int], MongoDB] renders as bracketed list") {
    val show = summon[Show[Seq[Int], MongoDB]]
    assertEquals(show.show(Seq(1, 2, 3)), "[1, 2, 3]")
  }

  test("Show[(Int,Int), MongoDB] renders as $gte/$lt range") {
    val show = summon[Show[(Int, Int), MongoDB]]
    assertEquals(show.show((10, 20)), "{ $gte: 10, $lt: 20 }")
  }

  // -- New predicates (issue #7) --

  test("MongoDB STARTSWITH renders as $regex") {
    val result = startsWith[MongoDB, Column, String](col("name"), lit("^A"))
    assertEquals(result.value, "{\"name\": {$regex: ^A}}")
  }

  test("MongoDB ISTRUE renders correctly") {
    val result = isTrue[MongoDB, Column](col("active"))
    assertEquals(result.value, "{\"active\": true}")
  }

  test("MongoDB ISFALSE renders correctly") {
    val result = isFalse[MongoDB, Column](col("active"))
    assertEquals(result.value, "{\"active\": false}")
  }
}
