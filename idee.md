## implementation options

* preconditions
* shrinking
* More generally extract correlations between postulate
* agents to tie postconditions to specific points of code (e.g. if a postcondition is tied to a variable, enumerate the points in the code that actually change the variable for a given data point)
* Transform the exploratory suite at one point in time into a test suite
* Distinguish between fail/pass/ignored. 
* In output discovery mode, the goal is to have at least one data that passes the behaviour.
* In rule discovery mode, the goal is to have *every* data that either pass a behaviour or is ignored. If at least one data is failing, then the rule is not complete.
* you discovered all rules if all data passes all defined behaviour (or is ignored) and that each data passes at exactly one behaviour.
* correlation is computed based on data that passes a test only

## Dictionary

* input designates the data that is fed to the program being tested.
* A precondition is a check on the input
* a postcondition is a check on the output
* A postulate is a precondition or a postcondition.

## Modus operandi

 * identify entry points list inputs (create arbitraty instances and instances of interest)
 * List preconditions and postconditions
 * Lib checks for us which postConditions are satisfied and which correlations emerge between postConditions
 * iterate
 
 ## Next Meeting schedule (SHOULD[1] be respected)
 
 * === Proposition : On fait un autre kata de code legacy avec et sans ce truc et on compare?

## fun

* "It's generators all the way down" -- Romeu

[1] according to RFC XXXXX
