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

import com.eff3ct.criteria4s.core.Criteria
import org.bson.BsonDocument
import org.bson.conversions.Bson

/**
 * MongoDB driver integration.
 *
 *  Import this package to use `Criteria[MongoDB]` directly where `Bson` is expected:
 *  {{{
 *  import com.eff3ct.criteria4s.dialect.mongodb.driver.given
 *
 *  val filter: Criteria[MongoDB] = col("age") geq lit(18)
 *  collection.find(filter)  // Criteria[MongoDB] converts to Bson automatically
 *  }}}
 */
package object driver {

  /**
   * Normalizes the MongoDB expression string to valid extended JSON.
   *  Quotes unquoted `$`-prefixed operators like `$eq`, `$gte`, etc.
   */
  private def toExtendedJson(mongoExpr: String): String =
    mongoExpr.replaceAll("\\$(\\w+)", "\"\\$$1\"")

  /** Converts a Criteria[MongoDB] to a Bson filter document. */
  given criteriaToBson[T <: MongoDB]: Conversion[Criteria[T], Bson] =
    criteria => BsonDocument.parse(toExtendedJson(criteria.value))

  extension [T <: MongoDB](criteria: Criteria[T]) {

    /** Converts this criteria to a Bson filter document. */
    def toBson: Bson = BsonDocument.parse(toExtendedJson(criteria.value))

    /** Converts this criteria to a BsonDocument. */
    def toBsonDocument: BsonDocument = BsonDocument.parse(toExtendedJson(criteria.value))
  }
}
