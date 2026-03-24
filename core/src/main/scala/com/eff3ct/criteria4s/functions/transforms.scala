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

package com.eff3ct.criteria4s.functions

import com.eff3ct.criteria4s.core.*

private[functions] trait transforms {

  def upper[T <: CriteriaTag, V](ref: Ref[T, V])(using
      H: UPPER[T],
      show: Show[V, T]
  ): Ref[T, V] =
    Ref.transformed(ref, H)

  def lower[T <: CriteriaTag, V](ref: Ref[T, V])(using
      H: LOWER[T],
      show: Show[V, T]
  ): Ref[T, V] =
    Ref.transformed(ref, H)

  def trim[T <: CriteriaTag, V](ref: Ref[T, V])(using
      H: TRIM[T],
      show: Show[V, T]
  ): Ref[T, V] =
    Ref.transformed(ref, H)

  def coalesce[T <: CriteriaTag, V](ref1: Ref[T, V], ref2: Ref[T, V])(using
      H: COALESCE[T],
      show: Show[V, T]
  ): Ref[T, V] =
    Ref.transformed2(ref1, ref2, H)

  def concat[T <: CriteriaTag, V](ref1: Ref[T, V], ref2: Ref[T, V])(using
      H: CONCAT[T],
      show: Show[V, T]
  ): Ref[T, V] =
    Ref.transformed2(ref1, ref2, H)
}

object transforms extends transforms
