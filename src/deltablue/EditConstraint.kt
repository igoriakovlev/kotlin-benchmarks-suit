/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

// I am a unary input constraint used to mark a variable that the
// client wishes to change.
class EditConstraint(v: Variable, strength: Strength.Sym, planner: Planner) :
    UnaryConstraint(v, strength, planner) {
    // I indicate that a variable is to be changed by imperative code.
    override val isInput: Boolean = true

    override fun execute() {} // Edit constraints do nothing.
}