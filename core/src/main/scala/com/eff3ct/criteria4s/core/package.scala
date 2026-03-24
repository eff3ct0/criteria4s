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

package com.eff3ct.criteria4s

package object core {

  type CriteriaTag = Criteria.CriteriaTag

  type AND[T <: CriteriaTag] = ConjunctionBinary.AND[T]

  type OR[T <: CriteriaTag] = ConjunctionBinary.OR[T]

  type NOT[T <: CriteriaTag] = ConjunctionUnary.NOT[T]

  type GT[T <: CriteriaTag] = PredicateBinary.GT[T]

  type LT[T <: CriteriaTag] = PredicateBinary.LT[T]

  type EQ[T <: CriteriaTag] = PredicateBinary.EQ[T]

  type NEQ[T <: CriteriaTag] = PredicateBinary.NEQ[T]

  type GEQ[T <: CriteriaTag] = PredicateBinary.GEQ[T]

  type LEQ[T <: CriteriaTag] = PredicateBinary.LEQ[T]

  type LIKE[T <: CriteriaTag] = PredicateBinary.LIKE[T]

  type IN[T <: CriteriaTag] = PredicateBinary.IN[T]

  type NOTIN[T <: CriteriaTag] = PredicateBinary.NOTIN[T]

  type ISNULL[T <: CriteriaTag] = PredicateUnary.ISNULL[T]

  type ISNOTNULL[T <: CriteriaTag] = PredicateUnary.ISNOTNULL[T]

  type BETWEEN[T <: CriteriaTag] = PredicateBinary.BETWEEN[T]

  type NOTBETWEEN[T <: CriteriaTag] = PredicateBinary.NOTBETWEEN[T]

  type STARTSWITH[T <: CriteriaTag] = PredicateBinary.STARTSWITH[T]

  type ENDSWITH[T <: CriteriaTag] = PredicateBinary.ENDSWITH[T]

  type CONTAINS[T <: CriteriaTag] = PredicateBinary.CONTAINS[T]

  type ISTRUE[T <: CriteriaTag] = PredicateUnary.ISTRUE[T]

  type ISFALSE[T <: CriteriaTag] = PredicateUnary.ISFALSE[T]

}
