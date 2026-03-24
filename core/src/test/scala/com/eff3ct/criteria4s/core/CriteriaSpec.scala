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

package com.eff3ct.criteria4s.core

class CriteriaSpec extends munit.FunSuite {

  test("Criteria.pure wraps a string value") {
    val c = Criteria.pure[CriteriaTag]("test")
    assertEquals(c.value, "test")
  }

  test("Criteria.pure toString returns the value") {
    val c = Criteria.pure[CriteriaTag]("hello")
    assertEquals(c.toString, "hello")
  }

  test("Show.defaultStringShow is identity") {
    val show = summon[Show[String, CriteriaTag]]
    assertEquals(show.show("hello"), "hello")
  }

  test("Show.defaultIntShow renders AnyVal as toString") {
    val show = summon[Show[Int, CriteriaTag]]
    assertEquals(show.show(42), "42")
  }

  test("Ref.value asString uses Show") {
    given showInt: Show[Int, CriteriaTag] = Show.create(_.toString)
    val ref                                      = Ref.value[Int, CriteriaTag](42)
    assertEquals(ref.asString, "42")
  }

  test("Ref.col asString uses Show[Column, D]") {
    given showCol: Show[Column, CriteriaTag] = Show.create(c => s"[${c.colName}]")
    val ref                                         = Ref.col[CriteriaTag](Column("age"))
    assertEquals(ref.asString, "[age]")
  }

  test("Ref.bool creates a Criteria with boolean value") {
    val b = Ref.bool[CriteriaTag](true)
    assertEquals(b.value, "true")
  }

  test("Ref.array asString uses Show[Seq[V], D]") {
    given showSeq: Show[Seq[Int], CriteriaTag] =
      Show.create(_.mkString("[", ",", "]"))
    val ref = Ref.array[Int, CriteriaTag](1, 2, 3)
    assertEquals(ref.asString, "[1,2,3]")
  }

  test("Ref.range asString uses Show[(V,V), D]") {
    given showTuple: Show[(Int, Int), CriteriaTag] =
      Show.create { case (l, r) => s"$l..$r" }
    val ref = Ref.range[Int, CriteriaTag](1, 10)
    assertEquals(ref.asString, "1..10")
  }

  test("Column is a value class with colName") {
    val col = Column("name")
    assertEquals(col.colName, "name")
  }
}
