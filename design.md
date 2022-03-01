# Test Suite Design

## Methodology

The test cases were designed to be as comprehensive as possible. Each test case should ideally test a single aspect of the language while being minimally dependent on other language features. Some cases, however, are more complex than that and integrate several language features (found under `tests/programs`).

## Test Case Description

The overall language is covered by testing each documented feature of the language as described in the language spec. These tests cover desired and undesired usages of certain features to ensure general correctness of the compiler implementation. Tests are separated into logical subfolders that categorize the tests by the overall language feature they test. Positive and negative examples are used to expect certain errors or to expect certain outputs from the compiler execution. Together, these tests aim to cover the Base Language in its entirety.

### Types

`tests/types/struct/struct_empty.bl` - Test that a struct with no fields is valid.

`tests/types/struct/complex_type.bl.ignore` - Test the creation of a complex struct type with various nested structs and arrays.

`tests/types/struct/struct_basic.bl` - Test the creation of a basic struct type with primitive fields.

`tests/types/string/string.bl` - Test the creation and operation on basic string types.

`tests/types/unit/unit.bl` - Test the creation of the single unit type with the unit literal.

`tests/types/unit/unit_as_not_unit.bl` - Test whether creating a unit type as a non-unit type is an error.

`tests/types/any/any.bl` - Test the creation of the single Any type, using other types as the underlying type.

`tests/types/any/any_assignment.bl` - Test assigning Any to a non-Any type, even if the non-Any type is a subtype of Any.

`tests/types/any/any_operations.bl` - Test the possibility of using operations on the Any type.

`tests/types/boolean/boolean.bl` - Test basic assignment and negation of booleans.

`tests/types/int/int_as_string.bl` - Test the (invalid) creation of an integer type with a string literal.

`tests/types/int/integer.bl` - Test the creation of an integer type with various integer literals (positives, negatives, zero)

`tests/types/int/int_as_bool.bl` - Test the (invalid) creation of an integer type with a boolean literal.

`tests/types/array/arrays.bl.ignore` - Test the creation of an array with indexing and length checking.

### Functions

`tests/functions/fun_returns.bl` - Test the functionality of the `return` keyword in functions.

`tests/functions/fun_simple.bl.ignore` - Test the creation of a simple function that has no return or arguments.

`tests/functions/fun_invalid_arg.bl` - Test calling a function using an invalid argument type.

`tests/functions/fun_pass_by.bl.ignore` - Test the passing of arguments by value or by reference.

`tests/functions/builtins.bl` - Test the usage of built-in functions.

`tests/functions/fun_invalid_arg_num.bl` - Test calling a function using an invalid number of arguments.

### Expressions

`tests/expressions/logic/operators/short_circuit.bl.ignore` - Test the short-circuit evaluation of logical operators.

`tests/expressions/logic/operators/logical_ops.bl.ignore` - Test the usage of logical operators (&&, ||, !).

`tests/expressions/logic/operators/logical_order_of_operations.bl.ignore` - Test the order of operations of logical operators.

`tests/expressions/logic/equality/equality_array.bl` - Test the equality of arrays.

`tests/expressions/logic/equality/equality_two_type.bl` - Test the equality of two different types.

`tests/expressions/logic/equality/equality.bl` - Test the equality of two expressions of the same type.

`tests/expressions/logic/equality/equality_struct.bl` - Test the equality of two expressions of the same struct type.

`tests/expressions/logic/comparison/cmp_bool.bl` - Test the comparison of two boolean expressions using the comparison operations (<, >, <=, >=).

`tests/expressions/logic/comparison/cmp_int.bl` - Test the comparison of two integer expressions using the comparison operations.

`tests/expressions/logic/comparison/cmp_string.bl` - Test the comparison of two string expressions using the comparison operations.

`tests/expressions/arithmetic/mod_zero.bl` - Test the usage of the modulo operator with a zero divisor (should be an error).

`tests/expressions/arithmetic/order_of_operations.bl.ignore` - Test the order of operations of the arithmetic operators.

`tests/expressions/arithmetic/add.bl.ignore` - Test the addition of two integer expressions.

`tests/expressions/arithmetic/sub.bl` - Test the subtraction of two integer expressions.

`tests/expressions/arithmetic/mod.bl.ignore` - Test the modulo of two integer expressions.

`tests/expressions/arithmetic/mul.bl.ignore` - Test the multiplication of two integer expressions.

## Special Statements

`tests/statements/while/while_loop.bl` - Test the usage of the `while` statement with a simple loop.

`tests/statements/while/while_no_cond.bl` - Test the usage of the `while` statement with no condition (should be an error).

`tests/statements/if/if_stmt.bl` - Test the usage of the `if` statement with a simple condition.

`tests/statements/if/only_else.bl` - Test the usage of an isolated `else` statement (should be an error).

`tests/statements/if/if_no_cond.bl` - Test the usage of the `if` statement with no condition (should be an error).

## Comprehensive Test Cases

`tests/programs/pyramid.bl` - Creates a pyramid of asterisks using various language features like `while`, `if`, assignment, comparison, printing, etc.

`tests/programs/fibonacci.bl.ignore` - Ouputs the Fibonacci numbers using function recursion.
