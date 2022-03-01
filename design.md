# Base Language Compiler Testing

Author:
  * James Clause - <clause@udel.edu>

Primary assignees:
  * Jinay Jain - <jjain@udel.edu>
  * Dileep Nimma - <dileep@udel.edu>

## Problem Description
The objective of this assignment is to get familiar with reading and
writing programs in Base Language Programming language. It is expected
to get familiar and write several input programs in base language referring
the Base Language documentation. The goal is to gain knowlege of working 
of base language compiler and to test the overall working when the the input
programs and coresponding test cases are executed together to understand its 
behaviour.

## Background and References
Knowledge of basic C programming language is required and the concept 
of what happens when we write and run the program on an IDE. Though 
there is a supporting documentation provided for Base Language, It is
preferable to gain some basic concepts of writing programs i.e., 
program syntaxes and semantics. Since the assignment majorly involves
testing the behaviour and performance of a compiler, hence it is preferable
to gain some knowledge of unit testing.

## Requirements
The Base Language Compiler has been developed such that it could be easily 
understood by an entry level programmer. It is written such that it can 
perform all major types of operations, handle methods and user defined 
data types. As a whole, it has been designed to function as a normal compiler.
The challenge here is to test, how well do they cover the language check if 
all the functionalities work properly and all together by testing all the 
functionalities based on how much confidence we want. 

## Proposed Design
We have documented a list of test cases. The test cases have been designed 
based on concepts on basic programming languages and corresponding testcases 
have been developed to cover majority of the concepts. We used following 
testcase plan:
> Types	
> Expressions
> Statements
> Variables
> Functions

## Testing
A python testing script was developed to run all the tests and compare original 
behaviour and expected outputs to check the working and coverage of the language.

