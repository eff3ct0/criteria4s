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
import com.eff3ct.criteria4s.examples.datastores._
import com.eff3ct.criteria4s.extensions._
import com.eff3ct.criteria4s.functions._

/** Demonstrates NOT negation: function-style, symbol-style, and extension-style. */
object NotExample extends App {

  // Function-style not()
  val notInRange: Criteria[Postgres] = not(col[Postgres]("age").between(range(1, 10)))

  // Symbol-style !!()
  val notInRangeSymbol: Criteria[Postgres] = !!(col[Postgres]("age").between(range(1, 10)))

  // Extension-style .not
  val notActive: Criteria[Postgres] = (col[Postgres]("active") === lit(true)).not

  // Polymorphic NOT with GT
  def notAboveThreshold[T <: CriteriaTag: GT: NOT: Show[Column, *]]: Criteria[T] =
    not(col[T]("score") gt lit(100))

  // Polymorphic NOT combined with AND
  def notAboveButBelow[
      T <: CriteriaTag: NOT: GT: LEQ: AND: Show[Column, *]
  ]: Criteria[T] =
    not(col[T]("score") gt lit(2)) && (col[T]("score") leq lit(10))

  println("=== NOT Examples ===")
  println(s"notInRange:        $notInRange")
  println(s"notInRangeSymbol:  $notInRangeSymbol")
  println(s"notActive:         $notActive")
  println()
  println("notAboveThreshold (polymorphic):")
  println(s"  Postgres:       ${notAboveThreshold[Postgres]}")
  println(s"  WeirdDatastore: ${notAboveThreshold[WeirdDatastore]}")
  println(s"  MongoDB:        ${notAboveThreshold[MongoDB]}")
  println()
  println("notAboveButBelow (polymorphic):")
  println(s"  Postgres:       ${notAboveButBelow[Postgres]}")
  println(s"  WeirdDatastore: ${notAboveButBelow[WeirdDatastore]}")
  println(s"  MongoDB:        ${notAboveButBelow[MongoDB]}")
}
