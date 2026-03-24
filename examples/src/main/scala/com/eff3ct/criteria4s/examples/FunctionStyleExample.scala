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

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.examples.datastores.Postgres
import com.eff3ct.criteria4s.functions.*

/** Demonstrates the function-style API for building criteria expressions. */
object FunctionStyleExample extends App {

  // Simple comparisons
  val ageGreaterThan18: Criteria[Postgres] = gt(col("age"), lit(18))
  val nameLikeJohn: Criteria[Postgres]     = like(col("name"), lit("%John%"))
  val statusNotEqual: Criteria[Postgres]   = neq(col("status"), lit("inactive"))

  // Null checks
  val emailIsNull: Criteria[Postgres]    = isNull(col("email"))
  val phoneIsNotNull: Criteria[Postgres] = isNotNull(col("phone"))

  // Collection and range predicates
  val roleInList: Criteria[Postgres] = in(col("role"), array[Postgres, String]("admin", "editor"))
  val scoreInRange: Criteria[Postgres] =
    between(col("score"), range[Postgres, Int](70, 100))

  // Composing with conjunctions
  val activeAdults: Criteria[Postgres] =
    and(
      geq(col("age"), lit(18)),
      ===(col("active"), lit(true))
    )

  val searchFilter: Criteria[Postgres] =
    or(
      and(nameLikeJohn, ageGreaterThan18),
      and(emailIsNull, statusNotEqual)
    )

  // Negation
  val notInactive: Criteria[Postgres] = not(===(col("status"), lit("inactive")))

  println("=== Function-Style API ===")
  println(s"ageGreaterThan18:  $ageGreaterThan18")
  println(s"nameLikeJohn:      $nameLikeJohn")
  println(s"statusNotEqual:    $statusNotEqual")
  println(s"emailIsNull:       $emailIsNull")
  println(s"phoneIsNotNull:    $phoneIsNotNull")
  println(s"roleInList:        $roleInList")
  println(s"scoreInRange:      $scoreInRange")
  println(s"activeAdults:      $activeAdults")
  println(s"searchFilter:      $searchFilter")
  println(s"notInactive:       $notInactive")
}
