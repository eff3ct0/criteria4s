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

package com.eff3ct.criteria4s.examples.datastores

import com.eff3ct.criteria4s.core.{Column, Show}
import com.eff3ct.criteria4s.dialect.sql.{SQL, *}

trait Postgres  extends SQL
object Postgres extends SQLExpr[Postgres] {

  /**
   * That's not the right symbol for Postgres but it's just an example of how to override the
   * default implementation
   */

  given showColumn: Show[Column, Postgres] = Show.create(col => s"'${col.colName}'")
  given showSeq[T](using show: Show[T, Postgres]): Show[Seq[T], Postgres] =
    Show.create(_.map(show.show).mkString("(", ", ", ")"))
  given betweenShow[T](using show: Show[T, Postgres]): Show[(T, T), Postgres] =
    Show.create { case (l, r) => s"${show.show(l)} TO ${show.show(r)}" }
}
