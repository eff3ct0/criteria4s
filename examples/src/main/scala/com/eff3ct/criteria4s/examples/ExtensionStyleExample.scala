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
import com.eff3ct.criteria4s.extensions.*
import com.eff3ct.criteria4s.functions.*

/** Demonstrates the extension/infix syntax for building criteria expressions. */
object ExtensionStyleExample extends App {

  // Comparisons using infix operators
  val ageAtLeast18: Criteria[Postgres]  = col[Postgres]("age") geq lit(18)
  val scoreUnder100: Criteria[Postgres] = col[Postgres]("score") lt lit(100)
  val nameEquals: Criteria[Postgres]    = col[Postgres]("name") === lit("Alice")
  val nameNotEquals: Criteria[Postgres] = col[Postgres]("name") =!= lit("Bob")
  val nameLike: Criteria[Postgres]      = col[Postgres]("name") like lit("%Smith%")
  val salaryAtMost: Criteria[Postgres]  = col[Postgres]("salary") leq lit(50000)
  val ratingAbove: Criteria[Postgres]   = col[Postgres]("rating") gt lit(4)
  val nameEqualsEqv: Criteria[Postgres] = col[Postgres]("name") eqv lit("Alice")

  // Null checks
  val deletedIsNull: Criteria[Postgres]   = col[Postgres]("deleted_at").isNull
  val verifiedNotNull: Criteria[Postgres] = col[Postgres]("verified_at").isNotNull

  // Collections and ranges
  val statusInSet: Criteria[Postgres] =
    col[Postgres]("status") in array[Postgres, String]("active", "pending")
  val statusNotInSet: Criteria[Postgres] =
    col[Postgres]("status") notIn array[Postgres, String]("banned", "deleted")
  val ageInRange: Criteria[Postgres] =
    col[Postgres]("age") between range[Postgres, Int](18, 65)
  val ageOutOfRange: Criteria[Postgres] =
    col[Postgres]("age") notBetween range[Postgres, Int](0, 17)

  // Composing with infix conjunctions
  val activeAdult: Criteria[Postgres] =
    (col[Postgres]("age") geq lit(18)) and (col[Postgres]("active") === lit(true))

  // Chained expression
  val complexFilter: Criteria[Postgres] =
    (col[Postgres]("age") geq lit(18))
      .and(col[Postgres]("role") === lit("admin"))
      .or(col[Postgres]("superuser") === lit(true))

  // Negation via extension
  val notBanned: Criteria[Postgres] =
    (col[Postgres]("status") === lit("banned")).not

  println("=== Extension/Infix Style ===")
  println(s"ageAtLeast18:    $ageAtLeast18")
  println(s"scoreUnder100:   $scoreUnder100")
  println(s"nameEquals:      $nameEquals")
  println(s"nameNotEquals:   $nameNotEquals")
  println(s"nameLike:        $nameLike")
  println(s"salaryAtMost:    $salaryAtMost")
  println(s"ratingAbove:     $ratingAbove")
  println(s"nameEqualsEqv:   $nameEqualsEqv")
  println(s"deletedIsNull:   $deletedIsNull")
  println(s"verifiedNotNull: $verifiedNotNull")
  println(s"statusInSet:     $statusInSet")
  println(s"statusNotInSet:  $statusNotInSet")
  println(s"ageInRange:      $ageInRange")
  println(s"ageOutOfRange:   $ageOutOfRange")
  println(s"activeAdult:     $activeAdult")
  println(s"complexFilter:   $complexFilter")
  println(s"notBanned:       $notBanned")
}
