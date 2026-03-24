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
import com.eff3ct.criteria4s.examples.datastores.*
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions as F

/** Demonstrates NOT negation: function-style, symbol-style, and extension-style. */
object NotExample extends App {

  // Function-style not()
  val notInRange: Criteria[Postgres] = F.not(F.col[Postgres]("age").between(F.range(1, 10)))

  // Symbol-style !!()
  val notInRangeSymbol: Criteria[Postgres] = F.!!(F.col[Postgres]("age").between(F.range(1, 10)))

  // Extension-style .not
  val notActive: Criteria[Postgres] = (F.col[Postgres]("active") === F.lit(true)).not

  // Polymorphic NOT with GT
  def notAboveThreshold[T <: CriteriaTag: GT: NOT](using
      sc: Show[Column, T]
  ): Criteria[T] =
    F.not(F.col[T]("score") gt F.lit(100))

  // Polymorphic NOT combined with AND
  def notAboveButBelow[T <: CriteriaTag: NOT: GT: LEQ: AND](using
      sc: Show[Column, T]
  ): Criteria[T] =
    F.not(F.col[T]("score") gt F.lit(2)) && (F.col[T]("score") leq F.lit(10))

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
