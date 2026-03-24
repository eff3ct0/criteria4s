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

package com.eff3ct.criteria4s.extensions

import com.eff3ct.criteria4s.functions as F
import com.eff3ct.criteria4s.core.{Criteria, CriteriaTag, Ref, Show}
import com.eff3ct.criteria4s.core.PredicateBinary.*
import com.eff3ct.criteria4s.core.PredicateUnary.*
import com.eff3ct.criteria4s.core.TransformUnary.*
import com.eff3ct.criteria4s.core.Ref.Collection

trait predicates {

  implicit class CriteriaPredImplicit[T <: CriteriaTag, L](c: Ref[T, L]) {
    def lt[R](
        other: Ref[T, R]
    )(implicit H: LT[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.lt(c, other)
    def :<[R](
        other: Ref[T, R]
    )(implicit H: LT[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.lt(c, other)

    def gt[R](
        other: Ref[T, R]
    )(implicit H: GT[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.gt(c, other)
    def :>[R](
        other: Ref[T, R]
    )(implicit H: GT[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.gt(c, other)

    def leq[R](
        other: Ref[T, R]
    )(implicit H: LEQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.leq(c, other)
    def :<=[R](
        other: Ref[T, R]
    )(implicit H: LEQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.leq(c, other)

    def geq[R](
        other: Ref[T, R]
    )(implicit H: GEQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.geq(c, other)
    def :>=[R](
        other: Ref[T, R]
    )(implicit H: GEQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.geq(c, other)

    def ===[R](
        other: Ref[T, R]
    )(implicit H: EQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.===(c, other)

    def eqv[R](
        other: Ref[T, R]
    )(implicit H: EQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.===(c, other)

    def =!=[R](
        other: Ref[T, R]
    )(implicit H: NEQ[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.=!=(c, other)

    def like[R](
        other: Ref[T, R]
    )(implicit H: LIKE[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] = F.like(c, other)

    def in[R](
        other: Collection[T, R]
    )(implicit H: IN[T], showL: Show[L, T], showR: Show[Seq[R], T]): Criteria[T] = F.in(c, other)

    def notIn[R](
        other: Collection[T, R]
    )(implicit H: NOTIN[T], showL: Show[L, T], showR: Show[Seq[R], T]): Criteria[T] =
      F.notIn(c, other)

    def isNull(implicit H: ISNULL[T], show: Show[L, T]): Criteria[T] = F.isNull(c)

    def isNotNull(implicit H: ISNOTNULL[T], show: Show[L, T]): Criteria[T] = F.isNotNull(c)

    def between[R](
        other: Ref.Range[T, R]
    )(implicit H: BETWEEN[T], showL: Show[L, T], showR: Show[(R, R), T]): Criteria[T] =
      F.between(c, other)

    def notBetween[R](
        other: Ref.Range[T, R]
    )(implicit H: NOTBETWEEN[T], showL: Show[L, T], showR: Show[(R, R), T]): Criteria[T] =
      F.notBetween(c, other)

    def startsWith[R](
        other: Ref[T, R]
    )(implicit H: STARTSWITH[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] =
      F.startsWith(c, other)

    def endsWith[R](
        other: Ref[T, R]
    )(implicit H: ENDSWITH[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] =
      F.endsWith(c, other)

    def contains[R](
        other: Ref[T, R]
    )(implicit H: CONTAINS[T], showL: Show[L, T], showR: Show[R, T]): Criteria[T] =
      F.contains(c, other)

    def isTrue(implicit H: ISTRUE[T], show: Show[L, T]): Criteria[T] = F.isTrue(c)

    def isFalse(implicit H: ISFALSE[T], show: Show[L, T]): Criteria[T] = F.isFalse(c)

    def upper(implicit H: UPPER[T], show: Show[L, T]): Ref[T, L]      = F.upper(c)(using H, show)
    def lower(implicit H: LOWER[T], show: Show[L, T]): Ref[T, L]      = F.lower(c)(using H, show)
    def trim(implicit H: TRIM[T], show: Show[L, T]): Ref[T, L]        = F.trim(c)(using H, show)
  }

}

object predicates extends predicates
