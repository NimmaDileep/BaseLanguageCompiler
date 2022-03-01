# Team

- Jinay Jain (Email: <jjain@udel.edu>)

- Dileep Nimma (Email: <dileep@udel.edu>)

# Build

    ./gradlew build
    ./gradlew installDist

# Run

    ./build/install/blc/bin/blc -h

# Testing

We use a custom script `blt.py` to run unit tests. This script runs the unit tests and compares their output with the `.expected` output. If the output is the same, the test passes. If the expected output is an error, the test passes if the program produces an error. Finally, some programs have been `.ignore`'d because they produce known errors in the compiler.

```
python3 scripts/blt.py tests/
```

Use `python3 scripts/blt.py --help` for more information.

# Known issues

Many of the tests in this project have been marked as `.ignore` in the test files. This is because the compiler is currently unable to handle the test files correctly. Below is a list of some issues that were found during testing:

- When the last character of the program is a syntax error, the compiler itself gets a runtime error (index out of bounds), instead of printing a syntax error to the user

- Addition and multiplication operation integers are not behaving as expected (addition produces an error and multiplication outputs the wrong result)

- Indexing an array does not work (i.e. calling `x[0]` will produce an error)

- Modulo operation on negative integers is invalid (`5 % -2` should be `-1`).

- Logical 'OR' operation '||' doesn’t function correctly (i.e., false || true gives a result false), which affects the short-circuiting behavior of the logical operators.

- Operator precedence could not be fully tested due to issues in the functioning of operators.

- Void functions/methods with return type `Unit` don't work.

- Accessing arguments that have been passed into a function produces an error.
