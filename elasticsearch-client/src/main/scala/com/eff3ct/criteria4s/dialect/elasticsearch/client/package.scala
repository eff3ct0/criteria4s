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

import com.eff3ct.criteria4s.core.Criteria
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.WrapperQuery
import java.util.Base64

/**
 * Elasticsearch Java client integration.
 *
 *  Import this package to use `Criteria[Elasticsearch]` directly as an ES Query:
 *  {{{
 *  import com.eff3ct.criteria4s.dialect.elasticsearch.client.given
 *
 *  val filter: Criteria[Elasticsearch] = col("age") geq lit(18)
 *  client.search(s => s.query(filter))  // Criteria converts to Query automatically
 *  }}}
 */
package object client {

  /**
   * Converts a Criteria[Elasticsearch] to an Elasticsearch Query using WrapperQuery.
   *  WrapperQuery accepts a base64-encoded JSON query string.
   */
  given criteriaToQuery[T <: Elasticsearch]: Conversion[Criteria[T], Query] =
    criteria => buildQuery(criteria)

  extension [T <: Elasticsearch](criteria: Criteria[T]) {

    /** Converts this criteria to an Elasticsearch Query. */
    def toQuery: Query = buildQuery(criteria)
  }

  private def buildQuery[T <: Elasticsearch](criteria: Criteria[T]): Query = {
    val jsonBytes = criteria.value.getBytes("UTF-8")
    val base64    = Base64.getEncoder.encodeToString(jsonBytes)
    val wrapper   = WrapperQuery.of(w => w.query(base64))
    wrapper._toQuery()
  }
}
