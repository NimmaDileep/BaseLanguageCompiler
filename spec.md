# Base Language

# Introduction

The Base Language is a C-like programming language that includes many of the essential features in C. Built using Kotlin, the language compiles to a bytecode file that can be executed on the JVM.

## Comments

Comments are used to document code and to document the language. Comments are ignored by the compiler.

Each line of a comment is prefixed with a `//`.

Example:

```
// This is a comment
```

## Variables

Variables are declared using the "var" keyword. Variables must specify their type and must be initialized with an expression (e.g., `var x: Int = 5;`)

## Types

### Boolean

A Boolean expression returns either true or false value and majorly used in checking the condition with if...else expressions. A boolean
expression makes use of relational operators, for example >, <, >= etc.

Syntax: `var name: Boolean = value`

Here,

- _var_: keyword for variable
- _name_: variable name
- _value_: `true` or `false`

Example:

```
var x: Boolean = true;
```

### Int

Int is used for definining numeric variables holding whole numbers.

Syntax: `var name: Int = value`

Example:

```
var num: Int = 56;
```

### String

Strings are a sequence of zero or more characters.

Syntax: `var name: String = "value"`

Example:

```
var str: String = "Hello, World";
```

### Unit

It indicates the absence of a value. unit is the only value of type Unit.
Unit is used to indicate that a function is called only for its side effects.
It's functions can be used generically wherever a generic first class function is required.

Syntax: `var name: Unit = value;`

Example:

```
var u: Unit = unit;
```

### Structures

A `struct` is a user-defined datatype that hold a collection of fields. These fields can be of any type, including other `struct` types and arrays.

Syntax: `struct name { var1: type1, var2: type2, var3: type3 }`

Example:

```
struct Student {
  name: String,
  age: Int
}
```

### Arrays

An array is a collection of a fixed number of values. The array items are called elements of the array.
Each element can be referred to by an index.

**Arrays must be initialized with at least one element.**

Syntax: `var name: [type] = [value1, value2, value3]`

Example:

```
var arr: [Int] = [1, 2, 3];
```

### Any

`Any` is the supertype of all types, and can hold any other type as its value. However, most operations on `Any` are not allowed since they are not guaranteed to work.

Syntax: `var name: Any = value;`

Example:

```
var amount: Any = "I am a string";
amount = 5;
amount = true;
// and so on...
```

## Expressions

Expressions produce a value

### Arithmetic (+, -, %, \*)

Arithmetic operators produce an `Int` value

Binary arithmetic operators (+, -, %, \*) consume two `Int` values
Unary arithmetic operators (-) consume one `Int` value

Arithmetic operations - `Addition, Subtraction, Multiplication, Modulo`

### Addition

Syntax: `value3 = value1 + value2`

Example:

```
var x: Int = 5;
var y: Int = 1;

print(x + y);
```

### Subtraction

Syntax: `value3 = value1 1 value2`

Example:

```
var x: Int = 25;
var y: Int = 15;

print(x - y);
```

### Multiplication

Syntax: `value3 = value1 + value2`

Example:

```
var x: Int = 5;
var y: Int = 2;

print(x * y);
```

### Modulo

Syntax: `value3 = value1 % value2`

Example:

```
var x: Int = 10;
var y: Int = 2;

print(x % y);
```

### Comparison (>, >=, <, <=)

Comparison operators produce a `Boolean` value

Comparison operators consume two values that are either both `Boolean`, `Int`, or `String`

Syntax: `value3 = value1 [operator] value2`

#### Boolean Comparison

Compares two `Boolean` values, where `true` is greater than `false`.

Example:

```
print(true <= false);
```

#### Int Comparison

Compares the values of two `Int` values

Example:

```
print(55 > 5);
```

#### String Comparison

Compares the lexicographical order of the two strings.

Example:

```
print("Compiler" <= "Construction");
```

### Equality (==, !=)

Equality operators produce a `Boolean` value

Equality operators consume two values that are any type, returning whether the values are equal (and of the same type).

Syntax: `value3 = value1 [operator] value2`

Example:

```
print(true == "false");
```

### Logical (!, &&, ||)

Logical operators produce a `Boolean`

Binary logical operators (&&, ||) consume two `Boolean` values
Unary logical operators (!) consume one `Boolean` value

Binary logical operators "short-circuit". If the result of the expression can be determined from only evaluating the left-hand side value, the right-hand side value is not evaluated.

Syntax: `[unary]value1` or `value1 [binary] value2`

Example:

```
print(true && false);
```

### Literal values

Literal values of each type are expressions

- Boolean: `true`, `false`
- Int: `-5`, `10`, `100`, etc.
- String: `"hello world"`
- Unit: `unit`
- Structure: `Point { 10, 10 }`
- Array: `[1, 2, 3]`
- Any: can be any literal value

### Index

Individual elements of an array can be accessed using square brackets (e.g., `a[0]`)

### Select

Fields of a structure can be access using a dot (e.g., `p.x`)

### Precedence and grouping

Arithmetic, comparison, equality, and logical operators have precedence and associativity that you'd expect coming from C.

Parenthesis, `(` and `)`, can be used to group expressions.

## Statements

Statements do not produce a value. They produce a side-effect.

### Assignment

Assignment statements store a value in a location (variable, structure field, or array element). The type of the value must be compatible with the type of the location.

### Block

A block is a sequence of statements surrounded by braces `{` and `}`

### Expression statement

An expression followed by a semi-colon `;` is an expression statement

### If

An if statement executes one of two statements based on some condition value (e.g., `if (c) s1 else s2`)

Multiple `if` statements can be chained together using `else if`.

Syntax:

```
if (condition){
  statement1
} else {
  statement2
}
```

Example:

```
if(x == 4) {
  print("x is 4");
} else if(x == 5) {
  print("x is 5");
} else {
  print("x is neither 4 nor 5");
}
```

An if statement cannot be executed without providing any condition.
Else blocks should always follow an if block.

### While

A while loop executes the body repeatedly as long as the condition expression evaluates to true (e.g., `while (c) s`)

Syntax:

```
while(condition) { statement or block of statements }
```

Example:

```
while(x < 10) {
    print(x);
    x = x + 1;
}
```

## Functions

Functions calls look like C: `callee(argument1, argument2, ...)`

Functions are declared using the `fun` keyword. Functions must have a name, and declared their parameter types and return type. See below for examples.

Values can be returned using the `return` statement. Functions whose return type is `Unit` do not need to contain a return statement. All other functions must return a value of the appropriate type.

Example:

```
fun fib(n: Int): Int {
  if (n <= 1) {
    return 1;
  } else {
    return fib(n - 1) + fib(n - 2);
  }
}
```

### Builtin functions:

- `fun print(s: String) -> Unit { ... }`: Prints the string to the standard output.

  - Note: Values of non-`String` type will be automatically converted to `String` using the `str()` function.

- `fun concat(s1: String, s2: String) -> String { ... }`: Concatenates two strings.
- `fun str(a: Any) -> String { ... }`: Converts a value to a `String`
- `fun len(arr: Array) -> Int { ... }`: Returns the length of an array
