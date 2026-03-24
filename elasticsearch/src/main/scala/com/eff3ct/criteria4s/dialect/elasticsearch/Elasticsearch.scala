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

package com.eff3ct.criteria4s.dialect.elasticsearch

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

/** Elasticsearch Query DSL dialect. */
trait Elasticsearch extends CriteriaTag

object Elasticsearch {

  // -- Expression builders --

  private def rangeExpr(op: String): (String, String) => String =
    (field, value) => s"""{"range": {$field: {"$op": $value}}}"""

  private def termExpr: (String, String) => String =
    (field, value) => s"""{"term": {$field: $value}}"""

  private val neqExpr: (String, String) => String =
    (field, value) => s"""{"bool": {"must_not": [{"term": {$field: $value}}]}}"""

  private val wildcardExpr: (String, String) => String =
    (field, pattern) => s"""{"wildcard": {$field: {"value": $pattern}}}"""

  private val inExpr: (String, String) => String =
    (field, values) => s"""{"terms": {$field: $values}}"""

  private val notInExpr: (String, String) => String =
    (field, values) => s"""{"bool": {"must_not": [{"terms": {$field: $values}}]}}"""

  private val isNullExpr: String => String =
    field => s"""{"bool": {"must_not": [{"exists": {"field": $field}}]}}"""

  private val isNotNullExpr: String => String =
    field => s"""{"exists": {"field": $field}}"""

  private val isTrueExpr: String => String =
    field => s"""{"term": {$field: true}}"""

  private val isFalseExpr: String => String =
    field => s"""{"term": {$field: false}}"""

  private val betweenExpr: (String, String) => String =
    (field, range) => s"""{"range": {$field: $range}}"""

  private val notBetweenExpr: (String, String) => String =
    (field, range) => s"""{"bool": {"must_not": [{"range": {$field: $range}}]}}"""

  private val andExpr: (String, String) => String =
    (left, right) => s"""{"bool": {"must": [$left, $right]}}"""

  private val orExpr: (String, String) => String =
    (left, right) => s"""{"bool": {"should": [$left, $right]}}"""

  private val notExpr: String => String =
    expr => s"""{"bool": {"must_not": [$expr]}}"""

  // -- Show instances --

  given showColumn: Show[Column, Elasticsearch] =
    Show.create(col => s""""${col.colName}"""")

  given showSeq[V](using show: Show[V, Elasticsearch]): Show[Seq[V], Elasticsearch] =
    Show.create(_.map(show.show).mkString("[", ", ", "]"))

  given showTuple[V](using
      show: Show[V, Elasticsearch]
  ): Show[(V, V), Elasticsearch] =
    Show.create { case (l, r) => s"""{"gte": ${show.show(l)}, "lt": ${show.show(r)}}""" }

  // -- Type-class instances --

  trait ElasticsearchExpr[T <: Elasticsearch] {
    given andConj: AND[T]               = build[T, AND](andExpr)
    given orConj: OR[T]                 = build[T, OR](orExpr)
    given notConj: NOT[T]               = build[T, NOT](notExpr)
    given eqPred: EQ[T]                 = build[T, EQ](termExpr)
    given neqPred: NEQ[T]               = build[T, NEQ](neqExpr)
    given gtPred: GT[T]                 = build[T, GT](rangeExpr("gt"))
    given geqPred: GEQ[T]               = build[T, GEQ](rangeExpr("gte"))
    given ltPred: LT[T]                 = build[T, LT](rangeExpr("lt"))
    given leqPred: LEQ[T]               = build[T, LEQ](rangeExpr("lte"))
    given inPred: IN[T]                 = build[T, IN](inExpr)
    given notInPred: NOTIN[T]           = build[T, NOTIN](notInExpr)
    given likePred: LIKE[T]             = build[T, LIKE](wildcardExpr)
    given isNullPred: ISNULL[T]         = build[T, ISNULL](isNullExpr)
    given isNotNullPred: ISNOTNULL[T]   = build[T, ISNOTNULL](isNotNullExpr)
    given betweenPred: BETWEEN[T]       = build[T, BETWEEN](betweenExpr)
    given notBetweenPred: NOTBETWEEN[T] = build[T, NOTBETWEEN](notBetweenExpr)
    given startswithPred: STARTSWITH[T] = build[T, STARTSWITH](wildcardExpr)
    given endswithPred: ENDSWITH[T]     = build[T, ENDSWITH](wildcardExpr)
    given containsPred: CONTAINS[T]     = build[T, CONTAINS](wildcardExpr)
    given istruePred: ISTRUE[T]         = build[T, ISTRUE](isTrueExpr)
    given isfalsePred: ISFALSE[T]       = build[T, ISFALSE](isFalseExpr)
  }
}
