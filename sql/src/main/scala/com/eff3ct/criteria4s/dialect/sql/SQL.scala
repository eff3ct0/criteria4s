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

package com.eff3ct.criteria4s.dialect.sql

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

trait SQL extends CriteriaTag

object SQL {

  private def conjExpr(symbol: String)(left: String, right: String): String =
    s"($left) $symbol ($right)"

  private def conjExpr1(symbol: String)(right: String): String =
    s"$symbol ($right)"

  private def predExpr(symbol: String)(left: String, right: String): String =
    s"$left $symbol $right"

  private def predExpr1(symbol: String)(value: String): String =
    s"$value $symbol"

  implicit val showColumn: Show[Column, SQL] =
    Show.create(col => col.colName)

  implicit def showSeq[V](implicit show: Show[V, SQL]): Show[Seq[V], SQL] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))

  implicit def showTuple[V](implicit show: Show[V, SQL]): Show[(V, V), SQL] =
    Show.create { case (l, r) => s"${show.show(l)} AND ${show.show(r)}" }

  trait SQLExpr[T <: SQL] {

    protected def conjExpr(symbol: String): (String, String) => String =
      (left: String, right: String) => SQL.conjExpr(symbol)(left, right)

    protected def conjExpr1(symbol: String): String => String =
      (right: String) => SQL.conjExpr1(symbol)(right)

    protected def predExpr(symbol: String): (String, String) => String =
      (left: String, right: String) => SQL.predExpr(symbol)(left, right)

    protected def predExpr1(symbol: String): String => String = (value: String) =>
      SQL.predExpr1(symbol)(value)

    implicit val andConj: AND[T]               = build[T, AND](conjExpr("AND"))
    implicit val orConj: OR[T]                 = build[T, OR](conjExpr("OR"))
    implicit val notConj: NOT[T]               = build[T, NOT](conjExpr1("NOT"))
    implicit val eqPred: EQ[T]                 = build[T, EQ](predExpr("="))
    implicit val neqPred: NEQ[T]               = build[T, NEQ](predExpr("!="))
    implicit val gtPred: GT[T]                 = build[T, GT](predExpr(">"))
    implicit val geqPred: GEQ[T]               = build[T, GEQ](predExpr(">="))
    implicit val ltPred: LT[T]                 = build[T, LT](predExpr("<"))
    implicit val leqPred: LEQ[T]               = build[T, LEQ](predExpr("<="))
    implicit val inPred: IN[T]                 = build[T, IN](predExpr("IN"))
    implicit val notinPred: NOTIN[T]           = build[T, NOTIN](predExpr("NOT IN"))
    implicit val likePred: LIKE[T]             = build[T, LIKE](predExpr("LIKE"))
    implicit val isnullPred: ISNULL[T]         = build[T, ISNULL](predExpr1("IS NULL"))
    implicit val isnotnullPred: ISNOTNULL[T]   = build[T, ISNOTNULL](predExpr1("IS NOT NULL"))
    implicit val betweenPred: BETWEEN[T]       = build[T, BETWEEN](predExpr("BETWEEN"))
    implicit val notbetweenPred: NOTBETWEEN[T] = build[T, NOTBETWEEN](predExpr("NOT BETWEEN"))
    implicit val startswithPred: STARTSWITH[T] = build[T, STARTSWITH](predExpr("LIKE"))
    implicit val endswithPred: ENDSWITH[T]     = build[T, ENDSWITH](predExpr("LIKE"))
    implicit val containsPred: CONTAINS[T]     = build[T, CONTAINS](predExpr("LIKE"))
    implicit val istruePred: ISTRUE[T]         = build[T, ISTRUE](predExpr1("IS TRUE"))
    implicit val isfalsePred: ISFALSE[T]       = build[T, ISFALSE](predExpr1("IS FALSE"))
  }

}
