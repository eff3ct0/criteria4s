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

package com.eff3ct.criteria4s.dialect.clickhouse

import com.eff3ct.criteria4s.core.Criteria
import com.clickhouse.client.api.Client
import com.clickhouse.client.api.query.{GenericRecord, QueryResponse}
import java.util.concurrent.CompletableFuture

/**
 * ClickHouse native client (client-v2) integration.
 *
 *  Import this package to use `Criteria[ClickHouse]` with the official ClickHouse Java client:
 *  {{{
 *  import com.eff3ct.criteria4s.dialect.clickhouse.client.given
 *
 *  val filter: Criteria[ClickHouse] = col("age") geq lit(18)
 *  val records = filter.queryAllWith(client, "SELECT * FROM users")
 *  }}}
 */
package object client {

  given criteriaToString[T <: ClickHouse]: Conversion[Criteria[T], String] =
    criteria => criteria.value

  extension [T <: ClickHouse](criteria: Criteria[T]) {

    /** Returns the criteria as a WHERE clause fragment (without the WHERE keyword). */
    def toSqlFragment: String = criteria.value

    /** Returns the criteria as a full WHERE clause. */
    def toWhereClause: String = s"WHERE ${criteria.value}"

    /** Appends this criteria as a WHERE clause to the given SQL string. */
    def appendTo(baseSql: String): String = s"$baseSql WHERE ${criteria.value}"

    /**
     * Executes an async query against the ClickHouse native client.
     *  Returns a `CompletableFuture[QueryResponse]`.
     */
    def queryWith(chClient: Client, baseSql: String): CompletableFuture[QueryResponse] =
      chClient.query(s"$baseSql WHERE ${criteria.value}")

    /**
     * Executes a synchronous query against the ClickHouse native client.
     *  Loads the full result set into memory as a `List[GenericRecord]`.
     */
    def queryAllWith(chClient: Client, baseSql: String): java.util.List[GenericRecord] =
      chClient.queryAll(s"$baseSql WHERE ${criteria.value}")
  }
}
