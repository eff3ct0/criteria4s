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

package com.eff3ct.criteria4s

import com.eff3ct.criteria4s.core.*

package object functions extends predicates with conjunctions with transforms with clauses {

  def pred[T <: CriteriaTag, H <: PredicateBinary[T], L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      H: H,
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    H.eval(cr1, cr2)

  def pred[T <: CriteriaTag, H <: PredicateUnary[T], V](ref: Ref[T, V])(implicit
      H: H,
      show: Show[V, T]
  ): Criteria[T] =
    H.eval(ref)

  def cond[T <: CriteriaTag, H <: ConjunctionBinary[T]](cr1: Criteria[T], cr2: Criteria[T])(implicit
      H: H
  ): Criteria[T] =
    H.eval(cr1, cr2)

  def cond[T <: CriteriaTag, H <: ConjunctionUnary[T]](cr: Criteria[T])(implicit
      H: H
  ): Criteria[T] =
    H.eval(cr)

}
