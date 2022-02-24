"""
Base Language Tester
====================

This script is used to run tests on the base language. Given a folder/file, it will run all `.bl` files 
and (a.) ensure they properly run and (b.) ensure that the output given is the same as the provided
`.bl.expected` file.

If the tests fail, the script will print the output of the program and the expected output.

"""

import argparse
from email.policy import default
import os
import subprocess


def main():
    args = parse_args()
    tests = get_test_files(args.path)

    # for each file, run the program and compare the output to the expected output
    # if the output is different, store this in a list of failed tests
    failed_tests = []

    print("Running {} tests".format(len(tests)))
    for test in tests:
        output, error = run_test(test, args.compiler)
        passed, expected = check_test_output(test, output)

        if not passed:
            failed_tests.append((test, output, expected, error))

    if len(failed_tests) > 0:
        print("Passed {}/{} tests".format(len(tests) - len(failed_tests), len(tests)))
        for test, output, expected, error in failed_tests:
            print("-----------------------------------------------------")
            print('Test "{}" failed'.format(test))
            print("=====================================================")
            print("Expected:")
            print(expected)
            print("Got:")
            print(output)

            if error:
                print("Error:")
                print(error)
            print("-----------------------------------------------------")

        print("Some tests failed, see above for details")
        exit(1)

    print("All tests passed")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Base Language Tester", formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("path", help="The file or folder to test")
    parser.add_argument("--compiler", "-c", default="build/install/blc/bin/blc",
                        help="The path to the compiler")
    return parser.parse_args()


def get_test_files(path):
    # Find which files need to be tested
    tests = []

    if os.path.isfile(path):
        tests = [path]
    elif os.path.isdir(path):
        for root, dirs, files in os.walk(path):
            for file in files:
                if file.endswith(".bl"):
                    tests.append(os.path.join(root, file))
    else:
        print("Invalid path, please provide a valid file or folder")
        exit(1)

    return tests


def run_test(path, compiler):
    # run the program and get the stdout and stderr
    process = subprocess.Popen(
        [compiler, path], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    stdout, stderr = stdout.decode("utf-8"), stderr.decode("utf-8")

    return stdout, stderr


def check_test_output(path, output):
    # check the output against the expected output
    expected_path = path.replace(".bl", ".bl.expected")

    if os.path.isfile(expected_path):
        with open(expected_path, "r") as f:
            expected = f.read()
    else:
        print("Expected output file not found: {}".format(expected_path))
        return False, ""

    return output == expected, expected


if __name__ == "__main__":
    main()