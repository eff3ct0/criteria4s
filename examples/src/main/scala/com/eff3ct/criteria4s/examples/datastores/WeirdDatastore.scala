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

package com.eff3ct.criteria4s.examples.datastores

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.instances.*

trait WeirdDatastore extends CriteriaTag

object WeirdDatastore {

  private def wope(symbol: String): (String, String) => String = (left: String, right: String) =>
    s"""{left: $left, opt: $symbol, right: $right }""".stripMargin

  private def wope1(symbol: String): String => String = (left: String) =>
    s"""{left: $left, opt: $symbol }""".stripMargin

  implicit def showSeq[V](implicit show: Show[V, WeirdDatastore]): Show[Seq[V], WeirdDatastore] =
    Show.create(_.map(show.show).mkString("[", ", ", "]"))

  implicit def showRange[V](implicit show: Show[V, WeirdDatastore]): Show[(V, V), WeirdDatastore] =
    Show.create { case (l, r) => s"[${show.show(l)}, ${show.show(r)}]" }

  implicit val showColumn: Show[Column, WeirdDatastore] = Show.create(_.colName)

  implicit val andConj: AND[WeirdDatastore]       = build[WeirdDatastore, AND](wope("AND"))
  implicit val orConj: OR[WeirdDatastore]         = build[WeirdDatastore, OR](wope("OR"))
  implicit val notConj: NOT[WeirdDatastore]       = build[WeirdDatastore, NOT](wope1("NOT"))
  implicit val eqPred: EQ[WeirdDatastore]         = build[WeirdDatastore, EQ](wope("="))
  implicit val neqPred: NEQ[WeirdDatastore]       = build[WeirdDatastore, NEQ](wope("!="))
  implicit val gtPred: GT[WeirdDatastore]         = build[WeirdDatastore, GT](wope(">"))
  implicit val geqPred: GEQ[WeirdDatastore]       = build[WeirdDatastore, GEQ](wope(">="))
  implicit val ltPred: LT[WeirdDatastore]         = build[WeirdDatastore, LT](wope("<"))
  implicit val leqPred: LEQ[WeirdDatastore]       = build[WeirdDatastore, LEQ](wope("<="))
  implicit val inPred: IN[WeirdDatastore]         = build[WeirdDatastore, IN](wope("IN"))
  implicit val notinPred: NOTIN[WeirdDatastore]   = build[WeirdDatastore, NOTIN](wope("NOT IN"))
  implicit val likePred: LIKE[WeirdDatastore]     = build[WeirdDatastore, LIKE](wope("LIKE"))
  implicit val isnullPred: ISNULL[WeirdDatastore] = build[WeirdDatastore, ISNULL](wope1("IS NULL"))
  implicit val isnotnullPred: ISNOTNULL[WeirdDatastore] =
    build[WeirdDatastore, ISNOTNULL](wope1("IS NOT NULL"))
  implicit val betweenPred: BETWEEN[WeirdDatastore] =
    build[WeirdDatastore, BETWEEN](wope("BETWEEN"))
  implicit val notbetweenPred: NOTBETWEEN[WeirdDatastore] =
    build[WeirdDatastore, NOTBETWEEN](wope("NOT BETWEEN"))

}
