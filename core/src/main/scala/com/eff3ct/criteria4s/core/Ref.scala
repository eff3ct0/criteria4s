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

sealed trait Ref[D <: CriteriaTag, V] {
  def asString(implicit show: Show[V, D]): String
}

object Ref {

  trait Value[D <: CriteriaTag, V]      extends Ref[D, V]
  trait Collection[D <: CriteriaTag, V] extends Ref[D, Seq[V]]
  trait Col[D <: CriteriaTag]           extends Ref[D, Column]
  trait Bool[D <: CriteriaTag]          extends Value[D, Boolean] with Criteria[D]
  trait Range[D <: CriteriaTag, V]      extends Ref[D, (V, V)]

  private[criteria4s] def nothing[T <: CriteriaTag]: Value[T, Nothing] =
    (_: Show[Nothing, T]) => ""

  private[criteria4s] def value[V, D <: CriteriaTag](v: V): Value[D, V] =
    (show: Show[V, D]) => show.show(v)

  private[criteria4s] def col[D <: CriteriaTag](v: Column): Col[D] =
    (show: Show[Column, D]) => show.show(v)

  private[criteria4s] def bool[D <: CriteriaTag](v: Boolean): Bool[D] =
    new Bool[D] {
      override def value: String                                     = v.toString
      override def asString(implicit show: Show[Boolean, D]): String = v.toString
    }

  private[criteria4s] def array[V, D <: CriteriaTag](vs: V*): Collection[D, V] =
    (show: Show[Seq[V], D]) => show.show(vs)

  private[criteria4s] def range[V, D <: CriteriaTag](left: V, right: V): Range[D, V] =
    (show: Show[(V, V), D]) => show.show((left, right))

}

case class Column(colName: String) extends AnyVal
