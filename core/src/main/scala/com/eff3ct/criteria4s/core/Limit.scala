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

/** A LIMIT expression. */
trait LimitExpr[T <: CriteriaTag] {
  def value: String
  override def toString: String = value
}

object LimitExpr {
  def apply[T <: CriteriaTag](v: String): LimitExpr[T] = new LimitExpr[T] {
    override def value: String = v
  }
}

/** A OFFSET expression. */
trait OffsetExpr[T <: CriteriaTag] {
  def value: String
  override def toString: String = value
}

object OffsetExpr {
  def apply[T <: CriteriaTag](v: String): OffsetExpr[T] = new OffsetExpr[T] {
    override def value: String = v
  }
}

/** Type class for building LIMIT expression. */
trait LimitBuilder[T <: CriteriaTag] {
  def eval(n: Int): String
}

/** Type class for building OFFSET expression. */
trait OffsetBuilder[T <: CriteriaTag] {
  def eval(n: Int): String
}
