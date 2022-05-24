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

# Documentation

Please refer to `spec.md` for an in-depth documentation of our implementation of the Base Language.
