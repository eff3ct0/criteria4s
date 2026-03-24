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

package com.eff3ct.criteria4s.dialect.sql

import com.eff3ct.criteria4s.core.Criteria
import java.sql.PreparedStatement

/** JDBC integration for SQL-based dialects.
 *
 *  Import this package to use `Criteria[T <: SQL]` directly as a WHERE clause:
 *  {{{
 *  import com.eff3ct.criteria4s.dialect.sql.jdbc.given
 *
 *  val filter: Criteria[SQL] = ...
 *  val sql: String = filter.toWhereClause
 *  // "WHERE age >= 18 AND active = true"
 *  }}}
 */
package object jdbc {

  /** Converts a Criteria to its SQL string value (the WHERE fragment). */
  given criteriaToSqlString[T <: SQL]: Conversion[Criteria[T], String] =
    criteria => criteria.value

  extension [T <: SQL](criteria: Criteria[T]) {

    /** Returns the criteria as a WHERE clause fragment (without the WHERE keyword). */
    def toSqlFragment: String = criteria.value

    /** Returns the criteria as a full WHERE clause. */
    def toWhereClause: String = s"WHERE ${criteria.value}"

    /** Appends this criteria as a WHERE clause to the given SQL string. */
    def appendTo(baseSql: String): String = s"$baseSql WHERE ${criteria.value}"

    /** Sets this criteria as the WHERE clause on a PreparedStatement query.
     *  Returns a PreparedStatement ready for execution.
     */
    def prepareOn(statement: PreparedStatement, baseSql: String): PreparedStatement = {
      val fullSql = s"$baseSql WHERE ${criteria.value}"
      statement.getConnection.prepareStatement(fullSql)
    }
  }
}
