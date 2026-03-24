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

package com.eff3ct.criteria4s.functions

import com.eff3ct.criteria4s.core.{Column, Criteria, CriteriaTag, Ref, Show}
import com.eff3ct.criteria4s.core.PredicateBinary.*
import com.eff3ct.criteria4s.core.PredicateUnary.*
import com.eff3ct.criteria4s.core.Ref.*

private[functions] trait predicates {

  def lit[T <: CriteriaTag, V](v: V): Value[T, V] =
    Ref.value(v)

  def bool[T <: CriteriaTag](v: Boolean): Bool[T] =
    Ref.bool(v)

  private[criteria4s] def __[T <: CriteriaTag]: Value[T, Nothing] = Ref.nothing[T]

  def col[T <: CriteriaTag](ref: String): Col[T] = Ref.col(Column(ref))

  def array[T <: CriteriaTag, V](vs: V*): Collection[T, V] = Ref.array(vs*)

  def range[T <: CriteriaTag, V](left: V, right: V): Ref.Range[T, V] = Ref.range(left, right)

  def lt[T <: CriteriaTag: LT, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, LT[T], L, R](cr1, cr2)

  def gt[T <: CriteriaTag: GT, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, GT[T], L, R](cr1, cr2)

  def ===[T <: CriteriaTag: EQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, EQ[T], L, R](cr1, cr2)

  def eq[T <: CriteriaTag: EQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, EQ[T], L, R](cr1, cr2)

  def eqv[T <: CriteriaTag: EQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, EQ[T], L, R](cr1, cr2)

  def =!=[T <: CriteriaTag: NEQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, NEQ[T], L, R](cr1, cr2)

  def neq[T <: CriteriaTag: NEQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, NEQ[T], L, R](cr1, cr2)

  def geq[T <: CriteriaTag: GEQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, GEQ[T], L, R](cr1, cr2)

  def leq[T <: CriteriaTag: LEQ, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, LEQ[T], L, R](cr1, cr2)

  def like[T <: CriteriaTag: LIKE, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, LIKE[T], L, R](cr1, cr2)

  def in[T <: CriteriaTag: IN, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, IN[T], L, R](cr1, cr2)

  def notIn[T <: CriteriaTag: NOTIN, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, NOTIN[T], L, R](cr1, cr2)

  def isNull[T <: CriteriaTag: ISNULL, V](cr1: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] =
    pred[T, ISNULL[T], V](cr1)

  def isNotNull[T <: CriteriaTag: ISNOTNULL, V](cr1: Ref[T, V])(implicit
      show: Show[V, T]
  ): Criteria[T] =
    pred[T, ISNOTNULL[T], V](cr1)

  def between[T <: CriteriaTag: BETWEEN, L, R](
      cr1: Ref[T, L],
      cr2: Ref[T, R]
  )(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, BETWEEN[T], L, R](cr1, cr2)

  def notBetween[T <: CriteriaTag: NOTBETWEEN, L, R](
      cr1: Ref[T, L],
      cr2: Ref[T, R]
  )(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, NOTBETWEEN[T], L, R](cr1, cr2)

  def startsWith[T <: CriteriaTag: STARTSWITH, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, STARTSWITH[T], L, R](cr1, cr2)

  def endsWith[T <: CriteriaTag: ENDSWITH, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, ENDSWITH[T], L, R](cr1, cr2)

  def contains[T <: CriteriaTag: CONTAINS, L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T] =
    pred[T, CONTAINS[T], L, R](cr1, cr2)

  def isTrue[T <: CriteriaTag: ISTRUE, V](cr1: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] =
    pred[T, ISTRUE[T], V](cr1)

  def isFalse[T <: CriteriaTag: ISFALSE, V](cr1: Ref[T, V])(implicit
      show: Show[V, T]
  ): Criteria[T] =
    pred[T, ISFALSE[T], V](cr1)
}

object predicates extends predicates
