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

package com.eff3ct.criteria4s.dialect.elasticsearch

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.functions.*

class ElasticsearchExprSpec extends munit.FunSuite {

  test("Elasticsearch EQ renders as term query") {
    val result = ===[Elasticsearch, Column, Int](col("age"), lit(30))
    assertEquals(result.value, """{"term": {"age": 30}}""")
  }

  test("Elasticsearch NEQ renders as must_not term") {
    val result = =!=[Elasticsearch, Column, Int](col("age"), lit(30))
    assertEquals(result.value, """{"bool": {"must_not": [{"term": {"age": 30}}]}}""")
  }

  test("Elasticsearch GT renders as range query") {
    val result = gt[Elasticsearch, Column, Int](col("age"), lit(18))
    assertEquals(result.value, """{"range": {"age": {"gt": 18}}}""")
  }

  test("Elasticsearch LT renders as range query") {
    val result = lt[Elasticsearch, Column, Int](col("age"), lit(65))
    assertEquals(result.value, """{"range": {"age": {"lt": 65}}}""")
  }

  test("Elasticsearch GEQ renders as range query") {
    val result = geq[Elasticsearch, Column, Int](col("age"), lit(21))
    assertEquals(result.value, """{"range": {"age": {"gte": 21}}}""")
  }

  test("Elasticsearch LEQ renders as range query") {
    val result = leq[Elasticsearch, Column, Int](col("age"), lit(99))
    assertEquals(result.value, """{"range": {"age": {"lte": 99}}}""")
  }

  test("Elasticsearch LIKE renders as wildcard query") {
    val result = like[Elasticsearch, Column, String](col("name"), lit("Joh*"))
    assertEquals(result.value, """{"wildcard": {"name": {"value": Joh*}}}""")
  }

  test("Elasticsearch IN renders as terms query") {
    val result =
      in[Elasticsearch, Column, Seq[Int]](col("id"), array[Elasticsearch, Int](1, 2, 3))
    assertEquals(result.value, """{"terms": {"id": [1, 2, 3]}}""")
  }

  test("Elasticsearch NOTIN renders as must_not terms") {
    val result =
      notIn[Elasticsearch, Column, Seq[Int]](col("id"), array[Elasticsearch, Int](4, 5))
    assertEquals(
      result.value,
      """{"bool": {"must_not": [{"terms": {"id": [4, 5]}}]}}"""
    )
  }

  test("Elasticsearch ISNULL renders as must_not exists") {
    val result = isNull[Elasticsearch, Column](col("email"))
    assertEquals(
      result.value,
      """{"bool": {"must_not": [{"exists": {"field": "email"}}]}}"""
    )
  }

  test("Elasticsearch ISNOTNULL renders as exists") {
    val result = isNotNull[Elasticsearch, Column](col("email"))
    assertEquals(result.value, """{"exists": {"field": "email"}}""")
  }

  test("Elasticsearch BETWEEN renders as range query") {
    val result =
      between[Elasticsearch, Column, (Int, Int)](
        col("age"),
        range[Elasticsearch, Int](18, 65)
      )
    assertEquals(result.value, """{"range": {"age": {"gte": 18, "lt": 65}}}""")
  }

  test("Elasticsearch NOTBETWEEN renders as must_not range") {
    val result =
      notBetween[Elasticsearch, Column, (Int, Int)](
        col("age"),
        range[Elasticsearch, Int](0, 17)
      )
    assertEquals(
      result.value,
      """{"bool": {"must_not": [{"range": {"age": {"gte": 0, "lt": 17}}}]}}"""
    )
  }

  test("Elasticsearch ISTRUE renders as term true") {
    val result = isTrue[Elasticsearch, Column](col("active"))
    assertEquals(result.value, """{"term": {"active": true}}""")
  }

  test("Elasticsearch ISFALSE renders as term false") {
    val result = isFalse[Elasticsearch, Column](col("active"))
    assertEquals(result.value, """{"term": {"active": false}}""")
  }

  // -- Conjunctions --

  test("Elasticsearch AND renders as bool must") {
    val left   = ===[Elasticsearch, Column, Int](col("a"), lit(1))
    val right  = ===[Elasticsearch, Column, Int](col("b"), lit(2))
    val result = and[Elasticsearch](left, right)
    assertEquals(
      result.value,
      """{"bool": {"must": [{"term": {"a": 1}}, {"term": {"b": 2}}]}}"""
    )
  }

  test("Elasticsearch OR renders as bool should") {
    val left   = ===[Elasticsearch, Column, Int](col("a"), lit(1))
    val right  = ===[Elasticsearch, Column, Int](col("b"), lit(2))
    val result = or[Elasticsearch](left, right)
    assertEquals(
      result.value,
      """{"bool": {"should": [{"term": {"a": 1}}, {"term": {"b": 2}}]}}"""
    )
  }

  test("Elasticsearch NOT renders as bool must_not") {
    val expr   = ===[Elasticsearch, Column, Int](col("a"), lit(1))
    val result = not[Elasticsearch](expr)
    assertEquals(
      result.value,
      """{"bool": {"must_not": [{"term": {"a": 1}}]}}"""
    )
  }

  // -- Show instances --

  test("Show[Column, Elasticsearch] renders with double quotes") {
    val show = implicitly[Show[Column, Elasticsearch]]
    assertEquals(show.show(Column("name")), "\"name\"")
  }

  test("Show[Seq[Int], Elasticsearch] renders as bracketed list") {
    val show = implicitly[Show[Seq[Int], Elasticsearch]]
    assertEquals(show.show(Seq(1, 2, 3)), "[1, 2, 3]")
  }

  test("Show[(Int,Int), Elasticsearch] renders as gte/lt range") {
    val show = implicitly[Show[(Int, Int), Elasticsearch]]
    assertEquals(show.show((10, 20)), """{"gte": 10, "lt": 20}""")
  }
}
