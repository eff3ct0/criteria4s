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
import com.eff3ct.criteria4s.dialect.mongodb.{given, *}
import com.eff3ct.criteria4s.examples.datastores.{Postgres, WeirdDatastore}
import com.eff3ct.criteria4s.functions.*

/** Demonstrates IS NULL, IN, NOT IN, and array operations. */
object ArraysExample extends App {

  // Concrete Postgres expressions
  val emailIsNull: Criteria[Postgres] = isNull(col("email"))
  val idInList: Criteria[Postgres]    = in(col("id"), array[Postgres, Int](1, 2, 3))
  val idNotInList: Criteria[Postgres] = notIn(col("id"), array[Postgres, Int](4, 5, 6))

  // Combining criteria
  val nullOrInList: Criteria[Postgres]    = or(emailIsNull, idInList)
  val withActiveCheck: Criteria[Postgres] = or(nullOrInList, ===(col("active"), lit(true)))

  // Polymorphic versions
  def nullCheck[T <: CriteriaTag: ISNULL](using sc: Show[Column, T]): Criteria[T] =
    isNull(col("email"))

  def membershipCheck[T <: CriteriaTag: IN](using
      sc: Show[Column, T],
      ss: Show[Seq[Int], T]
  ): Criteria[T] =
    in(col("id"), array[T, Int](1, 2, 3))

  println("=== Arrays & Null Examples ===")
  println(s"emailIsNull:     $emailIsNull")
  println(s"idInList:        $idInList")
  println(s"idNotInList:     $idNotInList")
  println(s"nullOrInList:    $nullOrInList")
  println(s"withActiveCheck: $withActiveCheck")
  println()
  println("nullCheck (polymorphic):")
  println(s"  Postgres:       ${nullCheck[Postgres]}")
  println(s"  WeirdDatastore: ${nullCheck[WeirdDatastore]}")
  println(s"  MongoDB:        ${nullCheck[MongoDB]}")
  println()
  println("membershipCheck (polymorphic):")
  println(s"  Postgres:       ${membershipCheck[Postgres]}")
  println(s"  WeirdDatastore: ${membershipCheck[WeirdDatastore]}")
  println(s"  MongoDB:        ${membershipCheck[MongoDB]}")
}
