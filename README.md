# Team 
    * Jinay Jain  
      Email: <jjain@udel.edu>

    * Dileep Nimma
      Email: <dileep@udel.edu>

# Build
    ./gradlew build
    ./gradlew installDist
    
# Run
	./build/install/blc/bin/blc -h

# Unit test


# Known issues
  * When the last character of the program is a syntax error, the compiler itself gets a runtime error (index out of bounds), instead of printing
  a syntax error.

  * Addition and multiplication operation integers doesn’t seem to work (i.e. x + 5 and x * 5)

  * Indexing of arrays doesn’t work.

  * Modulo operation on negative integers is invalid.

  * Logical 'OR' operation '||' doesn’t function correctly (i.e., false || true gives a result false), which will inturn affect the short-circuit 
  functionality of 'OR' in logical expressions.

  * Precedence of operators property is followed with || and && operators. 

  * Void functions/methods with return type 'unit' doesn’t work.
  
  
     
