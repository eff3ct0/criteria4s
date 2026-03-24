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

package com.eff3ct.criteria4s.dialect.mongodb.driver

import com.eff3ct.criteria4s.core.*
import com.eff3ct.criteria4s.dialect.mongodb.{given, *}
import com.eff3ct.criteria4s.functions as F
import org.bson.BsonDocument
import org.bson.conversions.Bson

class MongoDriverIntegrationSpec extends munit.FunSuite {

  test("toBson converts EQ criteria to BsonDocument") {
    val criteria = F.===[MongoDB, Column, Int](F.col("age"), F.lit(30))
    val bson     = criteria.toBson
    assert(bson.isInstanceOf[BsonDocument])
    val doc = bson.asInstanceOf[BsonDocument]
    assert(doc.containsKey("age"))
  }

  test("toBsonDocument converts AND criteria") {
    val criteria = F.and[MongoDB](
      F.===[MongoDB, Column, Int](F.col("a"), F.lit(1)),
      F.===[MongoDB, Column, Int](F.col("b"), F.lit(2))
    )
    val doc = criteria.toBsonDocument
    assert(doc.containsKey("$and"))
  }

  test("given Conversion[Criteria[MongoDB], Bson] works") {
    val criteria: Criteria[MongoDB] = F.===[MongoDB, Column, Int](F.col("x"), F.lit(5))
    val bson: Bson                  = criteria
    assert(bson.isInstanceOf[BsonDocument])
  }

  test("toBson converts GT criteria correctly") {
    val criteria = F.gt[MongoDB, Column, Int](F.col("score"), F.lit(90))
    val doc      = criteria.toBsonDocument
    assert(doc.containsKey("score"))
    val scoreDoc = doc.getDocument("score")
    assert(scoreDoc.containsKey("$gt"))
    assertEquals(scoreDoc.getInt32("$gt").getValue, 90)
  }

  test("toBson converts IN criteria with array") {
    val criteria = F.in[MongoDB, Column, Seq[Int]](F.col("id"), F.array[MongoDB, Int](1, 2, 3))
    val doc      = criteria.toBsonDocument
    assert(doc.containsKey("id"))
    val idDoc = doc.getDocument("id")
    assert(idDoc.containsKey("$in"))
  }

  test("toBson converts BETWEEN criteria") {
    val criteria = F.between[MongoDB, Column, (Int, Int)](F.col("age"), F.range[MongoDB, Int](18, 65))
    val doc      = criteria.toBsonDocument
    assert(doc.containsKey("age"))
    val ageDoc = doc.getDocument("age")
    assert(ageDoc.containsKey("$gte"))
    assert(ageDoc.containsKey("$lt"))
  }
}
