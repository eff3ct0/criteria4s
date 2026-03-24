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

package com.eff3ct.criteria4s.extensions

import com.eff3ct.criteria4s.{functions => F}
import com.eff3ct.criteria4s.core._

trait conjunctions {

  implicit class CriteriaOpsImplicit[T <: CriteriaTag](c: Criteria[T]) {
    def and(other: Criteria[T])(implicit H: AND[T]): Criteria[T] = F.and(c, other)
    def &&(other: Criteria[T])(implicit H: AND[T]): Criteria[T]  = F.and(c, other)
    def :&(other: Criteria[T])(implicit H: AND[T]): Criteria[T]  = F.and(c, other)
    def or(other: Criteria[T])(implicit H: OR[T]): Criteria[T]   = F.or(c, other)
    def ||(other: Criteria[T])(implicit H: OR[T]): Criteria[T]   = F.or(c, other)
    def :|(other: Criteria[T])(implicit H: OR[T]): Criteria[T]   = F.or(c, other)
    def not(implicit H: NOT[T]): Criteria[T]                     = F.not(c)
  }

}

object conjunctions extends conjunctions
