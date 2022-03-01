# Base Language

# Introduction 

* The Base Language is a programming language built on the knowledge of basic C programming language and involves all the concepts
  similar to C programming. Although here the file handling concepts have not been explored other functionality seems to work relatively
  same but there are certain things which operate differently. Most of the programming languages are derived from basic C programming but
  all of them differ syntactically and sometimes semantically. Some languages are based on functionalities which could not be fulfilled
  by the based or current versions i.e., some extensions like object oriented programming for C++, Java etc and collects for Java. 
  This Base Language is based on Kotlin syntax. Most of the syntaxes and semantics are based on the Kotlin language. 

## Variables

Variables are declared using the "var" keyword. Variables must specify their type and must be initialized with an expression (e.g., `var x: Int = 5;`)


## Types

* `Boolean`

  A Boolean expression returns either true or false value and majorly used in checking the condition with if...else expressions. A boolean
  expression makes use of relational operators, for example >, <, >= etc.

  Syntax- `var name: Boolean = value`

  Here,
   var: keyword for variable
   name: variable name
   value: `true` or `false`

  Example- var x: Boolean = true;

* `Int`

  Int is used for definining numeric variables holding whole numbers.

  Syntax- `var name: Int = value`

  Here,
   var: keyword for variable
   name: variable name
   value: Integers

  Example- var num: Int = 56;

* `String`

  Strings are a sequence of characters.

  Syntax- `var name: String = "value"`

  Here,
   var: keyword for variable
   name: variable name
   value: any string

  Example- var str: String = "Hello, World";

* `Unit`

  It indicates the absence of a value. unit is the only value of type Unit.
  Unit is used to indicate that a function is called only for its side effects.
  It's functions can be used generically wherever a generic first class function is required.

  Syntax- `var name: Unit = value;`

  Here,
   var: keyword for variable
   name: variable name
   value: unit

  Example- var u: Unit = unit;

* `Structure`: `struct <identifier> { <identifier> : <type>, ... }`

  Structure is a user-defined datatype in C language which allows us to combine data of different types together.
  Structure helps to construct a complex data type which is more meaningful. It is somewhat similar to an Array,
  but an array holds data of similar type only.

  Syntax- `struct name {
           var1: type1,
           var2: type2,
           var3: type3
           }`

  Here,
   name: Structure name
   var: variable name
   type: variable type

  Example- struct Student {
            name: String,
            age: Int,
           }

* `Array`: `[<type>]`

  An array is a collection of a fixed number of values. The array items are called elements of the array.
  Each element can be referred to by an index. Arrays need at least one element.

* `Any` - super type of all types

  Syntax- `var name: Any = value;`

  Here,
   var: keyword for variable
   name: variable name
   value: Any type of value

  Example- var amount: Any = 35.6;

## Expressions

Expressions produce a value

### Arithmetic (+, -, %, *)

Arithmetic operators produce an `Int` value

Binary arithmetic operators (+, -, %, *) consume two `Int` values
Unary arithmetic operators (-) consume one `Int` value

Arithmetic operations - `Addition, Subtraction, Multiplication, Modulo`

### Addition

Syntax- `value3 = value1 + value2`

Example-  var x: Int = 5;
          var y: Int = 1;

          print(x + y);

### Subtraction

Syntax- `value3 = value1 1 value2`

Example-  var x: Int = 25;
          var y: Int = 15;

          print(x - y);

### Multiplication

Syntax- `value3 = value1 + value2`

Example-  var x: Int = 5;
          var y: Int = 2;

          print(x * y);

### Modulo

Syntax- `value3 = value1 % value2`

Example-  var x: Int = 10;
          var y: Int = 2;

          print(x % y);

### Comparison (>, >=, <, <=)

Comparison operators produce a `Boolean` value

Comparison operators consume two values that are either both `Boolean`, `Int`, or `String`

### Boolean 

Syntax- value3 = value1 `comparison operator` value2

value1: Boolean
value2: Boolean

Example- print(true <= false);

### Int

Syntax- value3 = value1 `comparison operator` value2

value1: Int
value2: Int

Example- print(55 > 5);

### String

Syntax- value3 = value1 `comparison operator` value2

value1: String
value2: String

Example- print("Compiler" <= "Construction");

### Equality (==, !=)

Equality operators produce a `Boolean` value

Equality operators consume two values that are any type

Syntax- value3 = value1 `Equality operator` value2

value1: Any type
value2: Any type

Example- print(true == "false");

### Logical (!, &&, ||)

Logical operators produce a `Boolean`

Binary logical operators (&&, ||) consume two `Boolean` values
Unary logical operators (!) consume one `Boolean` value

Binary logical operators "short-circuit". If the result of the expression can be determined from only evaluating the left-hand side value, the right-hand side value is not evaluated.

Syntax- value3 = value1 `logical operator` value2

value1: Boolean
value2: Boolean

Example- print(true && false);

### Literal values

Literal values of each type are expressions

* Boolean: `true`, `false`
* Int: `-5`, `10`, `100`, etc.
* String: `"hello world"`
* Unit: `unit`
* Structure: `Point { 10, 10 }`
* Array: `[1, 2, 3]`

### Index

Individual elements of an array can be accessed using square brackets (e.g., `a[0]`)

### Select

Fields of a structure can be access using a dot (e.g., `p.x`)

### Precedence and grouping

Arithmetic, comparison, equality, and logical operators have precedence and associativity that you'd expect coming from C.

Parenthesis, `(` and `)`, can be used to group expressions.

## Statements

Statements do not produce a value. They produce a side-effect.

* We can comment out lines of code by prefixing it with '//'.

  Syntax- `// code`

### Assignment

Assignment statements store a value in a location (variable, structure field, or array element). The type of the value must be compatible with the type of the location.

### Block

A block is a sequence of statements surrounded by braces `{` and `}`

### Expression statement

An expression followed by a semi-colon `;` is am expression statement

### If

An if statement executes one of two statements based on some condition value (e.g., `if (c) s1 else s2`)

Syntax:

```
 if (condition){
          statement1
        }
        else{
          statement2
        }
```

Example: 

```
if(x == 4) {
    print("x is 4");
} else {
    print("x is not 4");
}
```
An if statement cannot be executed without providing any condition. 
else block should always consists of its preceding if statement.

### While

A while loop executes the body repeatedly as long as the condition expression evaluates to true (e.g., `while (c) s`)

Syntax: while(condition){
          statement or block of statements
        }

Example: 

```
while(x < 10) {
    print(x);
    x = x + 1;
}
```

## Functions

Functions calls look like C: `callee(argument1, argument2, ...)`

Functions are declared using the "fun" keyword. Functions must have a name, and declared their parameter types and return type. See below for examples.

Values can be returned using the "return" statement. Functions whose return type is Unit do not need to contain a return statement. All other functions must return a value of the appropriate type.

### Builtin functions:

* `fun print(s: String) -> Unit { ... }`
* `fun concat(s1: String, s2: String) -> String { ... }`
* `fun str(a: Any) -> String { ... }`

