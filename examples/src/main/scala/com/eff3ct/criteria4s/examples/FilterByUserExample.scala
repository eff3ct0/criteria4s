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

import java.util.UUID

/**
 * Realistic use case: parameterized criteria for filtering by a user ID.
 * Shows how to build reusable, polymorphic query fragments.
 */
object FilterByUserExample extends App {

  /** Build a criteria that matches a given field against a UUID. */
  def filterByUser[T <: CriteriaTag: EQ](fieldName: String, userId: UUID)(using
      sc: Show[Column, T]
  ): Criteria[T] =
    F.col[T](fieldName) === F.lit(userId.toString)

  val sampleUserId: UUID = UUID.randomUUID()

  println("=== Filter By User Example ===")
  println(s"userId: $sampleUserId")
  println()
  println(s"Postgres:       ${filterByUser[Postgres]("user_id", sampleUserId)}")
  println(s"MySQL:          ${filterByUser[MySQL]("user_id", sampleUserId)}")
  println(s"WeirdDatastore: ${filterByUser[WeirdDatastore]("user_id", sampleUserId)}")
  println(s"MongoDB:        ${filterByUser[MongoDB]("user_id", sampleUserId)}")
}
