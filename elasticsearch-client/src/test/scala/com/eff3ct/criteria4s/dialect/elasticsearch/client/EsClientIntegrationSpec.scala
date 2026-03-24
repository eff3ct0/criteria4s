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

package com.eff3ct.criteria4s.dialect.elasticsearch.client

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.elasticsearch.{*, given}
import com.eff3ct.criteria4s.functions as F
import co.elastic.clients.elasticsearch._types.query_dsl.Query

class EsClientIntegrationSpec extends munit.FunSuite {

  test("toQuery converts EQ criteria to Query") {
    val criteria = F.===[Elasticsearch, Column, Int](F.col("age"), F.lit(30))
    val query    = criteria.toQuery
    assert(query != null)
    assert(query.isWrapper)
  }

  test("given Conversion[Criteria[Elasticsearch], Query] works") {
    val criteria: Criteria[Elasticsearch] = F.===[Elasticsearch, Column, Int](F.col("x"), F.lit(5))
    val query: Query                      = criteria
    assert(query != null)
  }

  test("toQuery converts AND criteria") {
    val criteria = F.and[Elasticsearch](
      F.===[Elasticsearch, Column, Int](F.col("a"), F.lit(1)),
      F.===[Elasticsearch, Column, Int](F.col("b"), F.lit(2))
    )
    val query = criteria.toQuery
    assert(query.isWrapper)
  }

  test("toQuery converts range criteria") {
    val criteria = F.gt[Elasticsearch, Column, Int](F.col("score"), F.lit(90))
    val query    = criteria.toQuery
    assert(query.isWrapper)
  }
}
