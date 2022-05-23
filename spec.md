# Base Language Documentation

The Base Language is a C-style programming language that implements many of the essential features in a modern programming language. This specific implementation includes additions onto the original Base Language including an object system with inheritance, type inference, division, for loops, and floating point numbers.

Table of Contents

- [Variables](#variables)
- [Functions](#functions)
- [Expressions](#expressions)
- [Classes And Objects](#classes-and-objects)

## Comments

Comments are used to document code and to document the language. Comments are ignored by the compiler.

Comments begin with the `//` prefix and continue to the end of the line.

```
// This is a comment
```

## Variables

Variables hold values that can be reused and manipulated throughout the program. Before a variable is used in expressions, it must be declared with the `var` keyword.

```
var x: Int = 5;
var y = true; // the type of y is inferred to be Boolean
```

### Types

#### Boolean

Booleans hold either `true` or `false` values. They can be used in places where conditional expressions are required such as `if`, `while`, and `for` statements. Their type keyword is `Boolean`.

```
// Boolean
var b: Boolean = true;
```

#### Int

Integers are used to represent signed whole numbers. They are used for numeric calculations and for indexing arrays. Their type keyword is `Int`. `Int` values are equivalent to the Java `Long` type.

```
// Int
var i: Int = 12;
```

#### String

Strings can represent text of arbitrary length. They can be used to print to the console and can be manipulated through some helper functions described later. Their type keyword is `String`.

```
// String
var s: String = "Hello World!";
```

#### Unit

Unit is a type that represents the absence of a value. `Unit` is not to be confused with `null`, but instead used as a "void" type. It is primarily used in functions that do not return a value. Its type keyword is `Unit`. Unit has only one literal value `unit`.

```
// Unit
var u: Unit = unit;
```

#### Structs

Structs form a composition of several "fields" that each have their own name and type. Structs are useful when you want to store many values in a single variable. Their type keyword is determined by the name of the struct. Structs must be declared inside a `struct` block if they are to be used in the program. The only way to create a struct is to use a default constructor that takes the values of the fields.

```
// Struct Declaration
struct Point {
  x: Int,
  y: Int
}

// Struct Initialization
var p: Point = Point(1, 2);
```

#### Arrays

Arrays hold a list of values of the same type. Array elements can be accessed by an integer index. For a type `Type`, the array type would be called `[Type]`. Arrays can be used in places where a list of values is required.

In some cases, arrays are required to have at least 1 element for type deduction, but most of the time, the type can be inferred.

```
// Array of Int
var a: [Int] = [1, 2, 3];
```

#### Any

The `Any` type can hold any value, with little to no restrictions. All types can be assigned to the `Any` type.

```
// Any
var a: Any = 1;
a = "Hello World!";
a = [1, 2, 3];
```

#### Classes

Classes are a way to create objects that a state and behavior associated with that state. Refer to the [Classes And Objects](#classes-and-objects) section for more information.

## Expressions

Expressions can be used as statements or as values in expressions. Expressions are used to manipulate values and perform actions. Expressions can be used in places where a value is required.

### Assignment

Assignment updates the value of a variable or field in a class or struct.

```
// Assignment
var x: Int = 5;
x = 10;
```

```
// Field Assignment
struct Point {
  x: Int,
  y: Int
}

var p: Point = Point(1, 2);
p.x = 10;
```

### Arithmetic

The following binary arithmetic operators are supported on both `Int` and `Float` types:

- `+`: addition
- `-`: subtraction
- `*`: multiplication
- `/`: division (integer division for `Int` types)
- `%`: modulus (float remainder for `Float` types)

```
var x: Int = 5;
var y: Int = 2;

print(x + y); // 7
print(x - y); // 3
print(x * y); // 10
print(x / y); // 2
print(x % y); // 1
```

```
var x: Float = 5.0;
var y: Float = 2.0;

print(x + y); // 7.0
print(x - y); // 3.0
print(x * y); // 10.0
print(x / y); // 2.5
print(x % y); // 1.0
```

In cases where the left and right operands are of the same type, the result is of that type. In cases where the left and right operands are of different types (`Int` and `Float`), the result is of type `Float`.

```

var x: Int = 5;
var y: Float = 5.0;

print(x + y); // prints 10.0
print(x - y); // prints -4.0
print(x \* y); // prints 25.0
print(x / y); // prints 1.0
print(x % y); // prints 0.0
```

Numerical types can also be negated using the `-` operator.

```
var x: Int = 5;
var y: Float = 4.2;

print(-x); // -5
print(-y); // -4.2
```

## Functions

Functions can be used to perform actions and return values. Functions are defined by the `fun` keyword. Values must be returned in all functions that don't return `Unit` by the `return` keyword.

All parameters to a typed, but the return type can often be inferred.

```

fun add(x: Int, y: Int) -> Int { // the "-> Int" is optional in this case
  return x + y;
}

```

Functions can be used in places where expressions are required using the function call syntax, similar to other languages.

```

var result = add(1, 2);

```

### Built-in Functions

`fun print(s: String) -> Unit { ... }`: Prints the string to the standard output.

Note: Values of non-String type will be automatically converted to String using the str() function.

`fun concat(s1: String, s2: String) -> String { ... }`: Concatenates two strings.

`fun str(a: Any) -> String { ... }`: Converts a value to a String

`fun len(arr: Array) -> Int { ... }`: Returns the length of an array

## Classes And Objects

This implementation of the Base Language supports a basic version of object-oriented programming. Classes are a combination of fields and methods that can operate on those fields. Classes are defined by the `class` keyword. Internally, classes use the `self` keyword to refer to themselves.

Here is a basic example of a class:

```kotlin
class Dog {
  var name: String;
  var age: Int;

  // Method
  fun bark() {
    print("Woof!");
  }

  // Method using "self"
  fun describe() {
    var description = concat("My name is ", concat(self.name, concat(" and I am ", self.age)));

    print(description);
  }
}

```

### Constructors

Currently, there is only one constructor for a class. The constructor requires the values of the fields of the class and all of its superclasses, in the order they are defined (super classes first).

```

class A {
  var a: String;
}

class B: A {
  var b: Int;
}

var b = B("Test", 2); // b.a = "Test", b.b = 2

```

### Members (Fields and Methods)

All members of a class are public. Fields can be accessed through the `self` keyword within a function and using the field selection syntax outside.

Methods are functions that are defined inside a class. Methods can be called on `self` or an instance of the class through the method call syntax

```

class Foo {
  var a: String;

  fun printA() {
    print(self.a); // using "self"
  }
}

var foo = Foo("Test");
a.printA(); // prints "Test"
print(foo.a); // prints "Test"

```

### Inheritance and Polymorphism

Classes can optionally have other classes as a "superclass" and inherit all of their fields and methods. Inheritance is achieved by placing a `:` after the class name.

Declaring a superclass allows the object to be used in places where the superclass is expected. Additionally, subclasses can override the superclass's methods to allow for specialization.

```

class Vehicle {
  var speed: Float;

  fun describe() {
    var description = concat("My speed is ", str(self.speed));
    print(description);
  }
}

class Car: Vehicle {
  var color: String;

  // Custom subclass method
  fun accelerate() {
    self.speed += 5;
  }

  // Override superclass method
  fun describe() {
    var description = concat("My color is ", self.color);
    print(description);
  }
}

var vehicles: [Vehicle] = [Vehicle(10.0), Car(20.0, "red")];

for (var i = 0; i < len(vehicles); i = i + 1) {
  vehicles[i].describe();
  // vehicles[i].accelerate(); (error: cannot access accelerate() since it is not defined in the Vehicle class)
}
```

The above example creates two classes, `Vehicle` and `Car`, and then creates an array of both. The `Vehicle` class has a field called `speed` and a method called `describe`. The `Car` class has a field called `color` and two methods. The `Car` class overrides the `describe` method of the `Vehicle` class.
