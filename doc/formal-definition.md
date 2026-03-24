## Formal Definition of the Expressions

The formal definition of the Criteria4s' type-classes (expressions) is as follows:

```text
Criteria    :=  Conjunction Criteria Criteria | Predicate | Value<Boolean>
Predicate   :=  UnaryPred Ref | BinaryPred Ref Ref
Ref         :=  Value<T> | Col
Conjunction :=  AND | OR | NOT
UnaryPred   :=  IS_NULL | IS_NOT_NULL ...
BinaryPred  :=  EQ | NEQ | GT | LT | GEQ | LEQ | IN | LIKE ...
```

Where:

- `Criteria` is the main expression of the DSL
- `Conjunction` is the conjunction operator expression
- `UnaryPredOp` is the unary predicate operator expression
- `BinaryPredOp` is the binary predicate operator expression
- `Ref` is a reference to a value or a column
- `Value<T>` is a value expression of a certain type `T`
- `Col` is a column expression