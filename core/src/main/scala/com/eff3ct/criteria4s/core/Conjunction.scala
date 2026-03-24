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

package com.eff3ct.criteria4s.core

import com.eff3ct.criteria4s.core
import com.eff3ct.criteria4s.core.Criteria.*
import com.eff3ct.criteria4s.instances.builder.*

trait ConjunctionBinary[T <: CriteriaTag] {
  def eval(cr1: Criteria[T], cr2: Criteria[T]): Criteria[T]
}

trait ConjunctionUnary[T <: CriteriaTag] {
  def eval(cr: Criteria[T]): Criteria[T]
}

object ConjunctionBinary {

  def eval[T <: CriteriaTag](cr1: Criteria[T], cr2: Criteria[T])(implicit
      ev: ConjunctionBinary[T]
  ): Criteria[T] =
    ev.eval(cr1, cr2)

  trait OR[T <: CriteriaTag] extends ConjunctionBinary[T]

  trait AND[T <: CriteriaTag] extends ConjunctionBinary[T]

  implicit val orBuilder: BuilderBinary[OR] = new BuilderBinary[OR] {
    override def build[T <: CriteriaTag](F: (String, String) => String): OR[T] =
      new OR[T] {
        def eval(cr1: Criteria[T], cr2: Criteria[T]): Criteria[T] =
          pure(F(cr1.value, cr2.value))
      }
  }

  implicit val andBuilder: BuilderBinary[AND] = new BuilderBinary[AND] {
    override def build[T <: CriteriaTag](F: (String, String) => String): AND[T] =
      new AND[T] {
        def eval(cr1: Criteria[T], cr2: Criteria[T]): Criteria[T] =
          pure(F(cr1.value, cr2.value))
      }
  }

}

object ConjunctionUnary {

  trait NOT[T <: CriteriaTag] extends ConjunctionUnary[T]

  implicit val notBuilder: BuilderUnary[NOT] = new BuilderUnary[NOT] {
    override def build[T <: core.CriteriaTag](F: String => String): NOT[T] =
      new NOT[T] {
        def eval(cr: Criteria[T]): Criteria[T] = pure(F(cr.value))
      }
  }

}
