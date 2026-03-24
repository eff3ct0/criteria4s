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

package com.eff3ct.criteria4s.dialect.mongodb

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

trait MongoDB extends CriteriaTag

object MongoDB {
  private def predExpr(symbol: String): (String, String) => String =
    (left, right) => s"{$left: {$$$symbol: $right}}"

  private def conjExpr(symbol: String): (String, String) => String =
    (left, right) => s"{$$$symbol: [$left, $right]}"

  private def conjExpr1(symbol: String): String => String =
    right => s"{$$$symbol: $right}"

  private def notExpr: String => String =
    ref => {
      val (left, right): (String, String) =
        (ref.takeWhile(c => c != ':'), ref.dropWhile(c => c != ':'))
      s"$left: {$$not$right}"
    }

  private val likeExpr: (String, String) => String =
    (value, pattern) => predExpr("regex")(value, pattern)

  private val isNullExpr: String => String =
    ref => s"{$ref: null}"

  private val isNotNullExpr: String => String =
    ref => s"{$ref: {$$ne: null}}"

  private val betweenExpr: (String, String) => String =
    (ref, range) => s"{$ref: $range}"

  private val notBetweenExpr: (String, String) => String =
    (ref, range) => s"{$ref: {$$not: $range}}"

  private val isTrueExpr: String => String =
    ref => s"{$ref: true}"

  private val isFalseExpr: String => String =
    ref => s"{$ref: false}"

  given showColumn: Show[Column, MongoDB] =
    Show.create(col => s"\"${col.colName}\"")

  given showSeq[T](using show: Show[T, MongoDB]): Show[Seq[T], MongoDB] =
    Show.create(_.map(show.show).mkString("[", ", ", "]"))

  given betweenShow[T](using show: Show[T, MongoDB]): Show[(T, T), MongoDB] =
    Show.create { case (l, r) => s"{ $$gte: ${show.show(l)}, $$lt: ${show.show(r)} }" }

  trait MongoDBExpr[T <: MongoDB] {
    given andConj: AND[T]               = build[T, AND](conjExpr("and"))
    given orConj: OR[T]                 = build[T, OR](conjExpr("or"))
    given notConj: NOT[T]               = build[T, NOT](notExpr)
    given eqPred: EQ[T]                 = build[T, EQ](predExpr("eq"))
    given neqPred: NEQ[T]               = build[T, NEQ](predExpr("ne"))
    given gtPred: GT[T]                 = build[T, GT](predExpr("gt"))
    given geqPred: GEQ[T]               = build[T, GEQ](predExpr("gte"))
    given ltPred: LT[T]                 = build[T, LT](predExpr("lt"))
    given leqPred: LEQ[T]               = build[T, LEQ](predExpr("lte"))
    given inPred: IN[T]                 = build[T, IN](predExpr("in"))
    given notInPred: NOTIN[T]           = build[T, NOTIN](predExpr("nin"))
    given likePred: LIKE[T]             = build[T, LIKE](likeExpr)
    given isNullPred: ISNULL[T]         = build[T, ISNULL](isNullExpr)
    given isNotNullPred: ISNOTNULL[T]   = build[T, ISNOTNULL](isNotNullExpr)
    given betweenPred: BETWEEN[T]       = build[T, BETWEEN](betweenExpr)
    given notbetweenPred: NOTBETWEEN[T] = build[T, NOTBETWEEN](notBetweenExpr)
    given startswithPred: STARTSWITH[T] = build[T, STARTSWITH](predExpr("regex"))
    given endswithPred: ENDSWITH[T]     = build[T, ENDSWITH](predExpr("regex"))
    given containsPred: CONTAINS[T]     = build[T, CONTAINS](predExpr("regex"))
    given istruePred: ISTRUE[T]         = build[T, ISTRUE](isTrueExpr)
    given isfalsePred: ISFALSE[T]       = build[T, ISFALSE](isFalseExpr)
  }
}
