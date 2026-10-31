name: Norman Sackett

email: nsackett@u.rochester.edu

My DPLL implementation is organized with an initial pass of the input string to parse it and create a symbol dictionary and clauses. My clauses are a class that contains an integer array the represents the value and appearance of any atom in the clause. The integer array has size equal to the number of atoms in the symbol library, with each index corresponding to one symbol. If an atom is not present in the clause, the value at that index is -1. If it is present, the value is 0 and if it is negated the value is 1. This allows me to convert 1 or 0 into a boolean value and XOR it with the value asserted by the model to mimic the not operator. The unit symbol test is housed within the clause class. I loop through every clause and each clause will check if it is a unit clause. The pure symbol check is performed outside of the clause class and checks if each symbol is pure. Once a pure symbol is found, it returns.

In terms of advantages and disadvantages, the unit clause and pure symbol checks can become costly when there are a lot of different clauses and symbols, they greatly cut down the search space when many simple clauses are present. In the case where there are no pure symbols, the pure symbol check is largely a waste of computing power. The unit clause test will get more useful as the DPLL algorithm naturally cuts down the size of clauses, but a statement in propositional logic can still be in a form that doesn't make the unit clause check useful until very far down the recursive chain.

For compiling the program, javac sat.java can be used. To run: java sat $'A,B\n~A,C' [--nounit] [--nopure]

Propositional logic is encoded with '~' = NOT, ',' = OR, '\n' = AND. Any other characters can be used as the names of atoms

--nounit and --nopure are both optional arguments which disable the unit clause check and pure symbol check respectively.
