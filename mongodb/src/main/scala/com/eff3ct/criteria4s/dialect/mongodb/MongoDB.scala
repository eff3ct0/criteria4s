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
    (value, regex) => predExpr("regex")(value, s"\\$regex\\")

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

  implicit val showColumn: Show[Column, MongoDB] =
    Show.create(col => s"\"${col.colName}\"")

  implicit def showSeq[T](implicit show: Show[T, MongoDB]): Show[Seq[T], MongoDB] =
    Show.create(_.map(show.show).mkString("[", ", ", "]"))

  implicit def betweenShow[T](implicit show: Show[T, MongoDB]): Show[(T, T), MongoDB] =
    Show.create { case (l, r) => s"{ $$gte: ${show.show(l)}, $$lt: ${show.show(r)} }" }

  trait MongoDBExpr[T <: MongoDB] {
    implicit val andConj: AND[T]               = build[T, AND](conjExpr("and"))
    implicit val orConj: OR[T]                 = build[T, OR](conjExpr("or"))
    implicit val notConj: NOT[T]               = build[T, NOT](notExpr)
    implicit val eqPred: EQ[T]                 = build[T, EQ](predExpr("eq"))
    implicit val neqPred: NEQ[T]               = build[T, NEQ](predExpr("ne"))
    implicit val gtPred: GT[T]                 = build[T, GT](predExpr("gt"))
    implicit val geqPred: GEQ[T]               = build[T, GEQ](predExpr("gte"))
    implicit val ltPred: LT[T]                 = build[T, LT](predExpr("lt"))
    implicit val leqPred: LEQ[T]               = build[T, LEQ](predExpr("lte"))
    implicit val inPred: IN[T]                 = build[T, IN](predExpr("in"))
    implicit val notInPred: NOTIN[T]           = build[T, NOTIN](predExpr("nin"))
    implicit val likePred: LIKE[T]             = build[T, LIKE](likeExpr)
    implicit val isNullPred: ISNULL[T]         = build[T, ISNULL](isNullExpr)
    implicit val isNotNullPred: ISNOTNULL[T]   = build[T, ISNOTNULL](isNotNullExpr)
    implicit val betweenPred: BETWEEN[T]       = build[T, BETWEEN](betweenExpr)
    implicit val notbetweenPred: NOTBETWEEN[T] = build[T, NOTBETWEEN](notBetweenExpr)
    implicit val startswithPred: STARTSWITH[T] = build[T, STARTSWITH](predExpr("regex"))
    implicit val endswithPred: ENDSWITH[T]     = build[T, ENDSWITH](predExpr("regex"))
    implicit val containsPred: CONTAINS[T]     = build[T, CONTAINS](predExpr("regex"))
    implicit val istruePred: ISTRUE[T]         = build[T, ISTRUE](isTrueExpr)
    implicit val isfalsePred: ISFALSE[T]       = build[T, ISFALSE](isFalseExpr)
  }
}
