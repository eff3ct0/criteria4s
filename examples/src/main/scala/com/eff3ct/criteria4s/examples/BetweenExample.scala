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

package com.eff3ct.criteria4s.examples

import com.eff3ct.criteria4s.core._
import com.eff3ct.criteria4s.dialect.mongodb.MongoDB
import com.eff3ct.criteria4s.examples.datastores.{Postgres, WeirdDatastore}
import com.eff3ct.criteria4s.extensions._
import com.eff3ct.criteria4s.functions._

/** Demonstrates BETWEEN and NOT BETWEEN with range operations. */
object BetweenExample extends App {

  // Concrete Postgres expressions
  val ageInRange: Criteria[Postgres]      = col[Postgres]("age").between(range(18, 65))
  val gradeOutOfRange: Criteria[Postgres] = col[Postgres]("grade").notBetween(range("A", "Z"))

  // Polymorphic version
  def scoreInRange[
      T <: CriteriaTag: BETWEEN: Show[Column, *]: Show[(Int, Int), *]
  ]: Criteria[T] =
    col[T]("score") between range(70, 100)

  println("=== Between Examples ===")
  println(s"ageInRange:      $ageInRange")
  println(s"gradeOutOfRange: $gradeOutOfRange")
  println()
  println("scoreInRange (polymorphic):")
  println(s"  Postgres:       ${scoreInRange[Postgres]}")
  println(s"  WeirdDatastore: ${scoreInRange[WeirdDatastore]}")
  println(s"  MongoDB:        ${scoreInRange[MongoDB]}")
}
