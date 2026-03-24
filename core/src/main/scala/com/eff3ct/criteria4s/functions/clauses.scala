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

package com.eff3ct.criteria4s.functions

import com.eff3ct.criteria4s.core.*

private[functions] trait clauses {

  def asc[T <: CriteriaTag, V](ref: Ref[T, V])(implicit
      H: OrderAsc[T],
      show: Show[V, T]
  ): Order[T] =
    Order(H.eval(ref.asString))

  def desc[T <: CriteriaTag, V](ref: Ref[T, V])(implicit
      H: OrderDesc[T],
      show: Show[V, T]
  ): Order[T] =
    Order(H.eval(ref.asString))

  def limit[T <: CriteriaTag](n: Int)(implicit H: LimitBuilder[T]): LimitExpr[T] =
    LimitExpr(H.eval(n))

  def offset[T <: CriteriaTag](n: Int)(implicit H: OffsetBuilder[T]): OffsetExpr[T] =
    OffsetExpr(H.eval(n))

  def caseWhen[T <: CriteriaTag, V](condition: Criteria[T], result: Ref[T, V])(implicit
      show: Show[V, T]
  ): CaseExpr.WhenBuilder[T] =
    CaseExpr.when(condition, result)
}

object clauses extends clauses
