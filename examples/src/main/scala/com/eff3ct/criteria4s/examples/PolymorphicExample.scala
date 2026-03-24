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
import com.eff3ct.criteria4s.dialect.mongodb.{*, given}
import com.eff3ct.criteria4s.examples.datastores.{MySQL, Postgres, WeirdDatastore}
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F

/**
 * Demonstrates the tagless-final / polymorphic approach: a single criteria definition
 * is evaluated against multiple backends producing different output formats.
 */
object PolymorphicExample extends App {

  /** Simple filter: age >= 18 AND active = true */
  def activeAdults[T <: CriteriaTag: GEQ: EQ: AND](using
      sc: Show[Column, T]
  ): Criteria[T] =
    (F.col[T]("age") geq F.lit(18)) and (F.col[T]("active") === F.lit(true))

  /** Complex filter using multiple predicates */
  def searchUsers[T <: CriteriaTag: GEQ: LIKE: ISNULL: NEQ: AND: OR](using
      sc: Show[Column, T]
  ): Criteria[T] =
    (F.col[T]("age") geq F.lit(18))
      .and(F.col[T]("name") like F.lit("%Smith%"))
      .or(F.col[T]("email").isNull)
      .and(F.col[T]("status") =!= F.lit("banned"))

  /** Filter with IN predicate */
  def usersByRole[T <: CriteriaTag: IN](using
      sc: Show[Column, T],
      ss: Show[Seq[String], T]
  ): Criteria[T] =
    F.in(F.col("role"), F.array[T, String]("admin", "editor", "moderator"))

  /** Filter with BETWEEN */
  def scoreInRange[T <: CriteriaTag: BETWEEN](using
      sc: Show[Column, T],
      st: Show[(Int, Int), T]
  ): Criteria[T] =
    F.col[T]("score") between F.range[T, Int](70, 100)

  /** Filter with NOT */
  def notInactive[T <: CriteriaTag: EQ: NOT](using sc: Show[Column, T]): Criteria[T] =
    F.not(F.col[T]("status") === F.lit("inactive"))

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
