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

import com.eff3ct.criteria4s.core.Criteria.*
import com.eff3ct.criteria4s.instances.builder.{BuilderBinary, BuilderUnary}

sealed trait Predicate[T <: CriteriaTag]

trait PredicateUnary[T <: CriteriaTag] extends Predicate[T] {
  def eval[V](ref: Ref[T, V])(implicit show: Show[V, T]): Criteria[T]
}

trait PredicateBinary[T <: CriteriaTag] extends Predicate[T] {
  def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
      showL: Show[L, T],
      showR: Show[R, T]
  ): Criteria[T]
}

object PredicateUnary {
  trait ISNULL[T <: CriteriaTag] extends PredicateUnary[T]

  trait ISNOTNULL[T <: CriteriaTag] extends PredicateUnary[T]

  trait ISTRUE[T <: CriteriaTag] extends PredicateUnary[T]

  trait ISFALSE[T <: CriteriaTag] extends PredicateUnary[T]

  implicit val isNullBuilder: BuilderUnary[ISNULL] = new BuilderUnary[ISNULL] {
    override def build[T <: CriteriaTag](F: String => String): ISNULL[T] = new ISNULL[T] {
      override def eval[V](ref: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] = pure(
        F(ref.asString)
      )
    }
  }

  implicit val isNotNullBuilder: BuilderUnary[ISNOTNULL] = new BuilderUnary[ISNOTNULL] {
    override def build[T <: CriteriaTag](F: String => String): ISNOTNULL[T] = new ISNOTNULL[T] {
      override def eval[V](ref: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] = pure(
        F(ref.asString)
      )
    }
  }

  implicit val isTrueBuilder: BuilderUnary[ISTRUE] = new BuilderUnary[ISTRUE] {
    override def build[T <: CriteriaTag](F: String => String): ISTRUE[T] = new ISTRUE[T] {
      override def eval[V](ref: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] = pure(
        F(ref.asString)
      )
    }
  }

  implicit val isFalseBuilder: BuilderUnary[ISFALSE] = new BuilderUnary[ISFALSE] {
    override def build[T <: CriteriaTag](F: String => String): ISFALSE[T] = new ISFALSE[T] {
      override def eval[V](ref: Ref[T, V])(implicit show: Show[V, T]): Criteria[T] = pure(
        F(ref.asString)
      )
    }
  }
}

object PredicateBinary {

  trait GT[T <: CriteriaTag] extends PredicateBinary[T]

  trait LT[T <: CriteriaTag] extends PredicateBinary[T]

  trait EQ[T <: CriteriaTag] extends PredicateBinary[T]

  trait NEQ[T <: CriteriaTag] extends PredicateBinary[T]

  trait GEQ[T <: CriteriaTag] extends PredicateBinary[T]

  trait LEQ[T <: CriteriaTag] extends PredicateBinary[T]

  trait LIKE[T <: CriteriaTag] extends PredicateBinary[T]

  trait IN[T <: CriteriaTag] extends PredicateBinary[T]

  trait NOTIN[T <: CriteriaTag] extends PredicateBinary[T]

  /** Range predicate. Semantics vary by dialect:
   *  - SQL: `BETWEEN val1 AND val2` (inclusive both ends)
   *  - MongoDB: `{$gte: val1, $lt: val2}` (inclusive left, exclusive right)
   *  - Elasticsearch: `{"gte": val1, "lt": val2}` (inclusive left, exclusive right)
   */
  trait BETWEEN[T <: CriteriaTag] extends PredicateBinary[T]

  /** Negated range predicate. Same dialect-specific semantics as [[BETWEEN]]. */
  trait NOTBETWEEN[T <: CriteriaTag] extends PredicateBinary[T]

  trait STARTSWITH[T <: CriteriaTag] extends PredicateBinary[T]

  trait ENDSWITH[T <: CriteriaTag] extends PredicateBinary[T]

  trait CONTAINS[T <: CriteriaTag] extends PredicateBinary[T]

  implicit val gtBuilder: BuilderBinary[GT] = new BuilderBinary[GT] {
    override def build[T <: CriteriaTag](
        F: (String, String) => String
    ): GT[T] = new GT[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val ltBuilder: BuilderBinary[LT] = new BuilderBinary[LT] {
    override def build[T <: CriteriaTag](F: (String, String) => String): LT[T] = new LT[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val eqBuilder: BuilderBinary[EQ] = new BuilderBinary[EQ] {
    override def build[T <: CriteriaTag](F: (String, String) => String): EQ[T] = new EQ[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val neqBuilder: BuilderBinary[NEQ] = new BuilderBinary[NEQ] {
    override def build[T <: CriteriaTag](F: (String, String) => String): NEQ[T] = new NEQ[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val geqBuilder: BuilderBinary[GEQ] = new BuilderBinary[GEQ] {
    override def build[T <: CriteriaTag](F: (String, String) => String): GEQ[T] = new GEQ[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val leqBuilder: BuilderBinary[LEQ] = new BuilderBinary[LEQ] {
    override def build[T <: CriteriaTag](F: (String, String) => String): LEQ[T] = new LEQ[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val likeBuilder: BuilderBinary[LIKE] = new BuilderBinary[LIKE] {
    override def build[T <: CriteriaTag](F: (String, String) => String): LIKE[T] = new LIKE[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val inBuilder: BuilderBinary[IN] = new BuilderBinary[IN] {
    override def build[T <: CriteriaTag](F: (String, String) => String): IN[T] = new IN[T] {
      override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
          showL: Show[L, T],
          showR: Show[R, T]
      ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
    }
  }

  implicit val notinBuilder: BuilderBinary[NOTIN] = new BuilderBinary[NOTIN] {
    override def build[T <: CriteriaTag](F: (String, String) => String): NOTIN[T] =
      new NOTIN[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }

  implicit val betweenBuilder: BuilderBinary[BETWEEN] = new BuilderBinary[BETWEEN] {
    override def build[T <: CriteriaTag](F: (String, String) => String): BETWEEN[T] =
      new BETWEEN[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }

  implicit val notbetweenBuilder: BuilderBinary[NOTBETWEEN] = new BuilderBinary[NOTBETWEEN] {
    override def build[T <: CriteriaTag](F: (String, String) => String): NOTBETWEEN[T] =
      new NOTBETWEEN[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }

  implicit val startswithBuilder: BuilderBinary[STARTSWITH] = new BuilderBinary[STARTSWITH] {
    override def build[T <: CriteriaTag](F: (String, String) => String): STARTSWITH[T] =
      new STARTSWITH[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }

  implicit val endswithBuilder: BuilderBinary[ENDSWITH] = new BuilderBinary[ENDSWITH] {
    override def build[T <: CriteriaTag](F: (String, String) => String): ENDSWITH[T] =
      new ENDSWITH[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }

  implicit val containsBuilder: BuilderBinary[CONTAINS] = new BuilderBinary[CONTAINS] {
    override def build[T <: CriteriaTag](F: (String, String) => String): CONTAINS[T] =
      new CONTAINS[T] {
        override def eval[L, R](cr1: Ref[T, L], cr2: Ref[T, R])(implicit
            showL: Show[L, T],
            showR: Show[R, T]
        ): Criteria[T] = pure(F(cr1.asString, cr2.asString))
      }
  }
}
