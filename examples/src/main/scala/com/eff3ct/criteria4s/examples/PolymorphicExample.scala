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

package com.eff3ct.criteria4s.examples

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.*
import com.eff3ct.criteria4s.examples.datastores.{MySQL, Postgres, WeirdDatastore}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions.*

/**
 * Demonstrates the tagless-final / polymorphic approach: a single criteria definition
 * is evaluated against multiple backends producing different output formats.
 */
object PolymorphicExample extends App {

  /** Simple filter: age >= 18 AND active = true */
  def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](implicit
      sc: Show[Column, T]
  ): Criteria[T] =
    (col[T]("age") geq lit(18)) and (col[T]("active") === lit(true))

  /** Complex filter using multiple predicates */
  def searchUsers[T <: CriteriaTag: GEQ: LIKE: ISNULL: NEQ: AND: OR](implicit
      sc: Show[Column, T]
  ): Criteria[T] =
    (col[T]("age") geq lit(18))
      .and(col[T]("name") like lit("%Smith%"))
      .or(col[T]("email").isNull)
      .and(col[T]("status") =!= lit("banned"))

  /** Filter with IN predicate */
  def usersByRole[T <: CriteriaTag: IN](implicit
      sc: Show[Column, T],
      ss: Show[Seq[String], T]
  ): Criteria[T] =
    in(col("role"), array[T, String]("admin", "editor", "moderator"))

  /** Filter with BETWEEN */
  def scoreInRange[T <: CriteriaTag: BETWEEN](implicit
      sc: Show[Column, T],
      st: Show[(Int, Int), T]
  ): Criteria[T] =
    col[T]("score") between range[T, Int](70, 100)

  /** Filter with NOT */
  def notInactive[T <: CriteriaTag: EQ: NOT](implicit sc: Show[Column, T]): Criteria[T] =
    not(col[T]("status") === lit("inactive"))

  // Evaluate each expression against all backends
  val backends = Seq(
    ("Postgres", () => activeAdults[Postgres]),
    ("MySQL", () => activeAdults[MySQL]),
    ("WeirdDatastore", () => activeAdults[WeirdDatastore]),
    ("MongoDB", () => activeAdults[MongoDB])
  )

  println("=== Polymorphic Criteria ===")
  println()

  println("activeAdults:")
  println(s"  Postgres:       ${activeAdults[Postgres]}")
  println(s"  MySQL:          ${activeAdults[MySQL]}")
  println(s"  WeirdDatastore: ${activeAdults[WeirdDatastore]}")
  println(s"  MongoDB:        ${activeAdults[MongoDB]}")
  println()

  println("searchUsers:")
  println(s"  Postgres:       ${searchUsers[Postgres]}")
  println(s"  MySQL:          ${searchUsers[MySQL]}")
  println(s"  WeirdDatastore: ${searchUsers[WeirdDatastore]}")
  println(s"  MongoDB:        ${searchUsers[MongoDB]}")
  println()

  println("usersByRole:")
  println(s"  Postgres:       ${usersByRole[Postgres]}")
  println(s"  MongoDB:        ${usersByRole[MongoDB]}")
  println()

  println("scoreInRange:")
  println(s"  Postgres:       ${scoreInRange[Postgres]}")
  println(s"  MongoDB:        ${scoreInRange[MongoDB]}")
  println()

  println("notInactive:")
  println(s"  Postgres:       ${notInactive[Postgres]}")
  println(s"  MongoDB:        ${notInactive[MongoDB]}")
}
