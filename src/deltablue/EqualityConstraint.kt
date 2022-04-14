/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

// I constrain two variables to have the same value: "v1 = v2".
class EqualityConstraint(
    var1: Variable,
    var2: Variable,
    strength: Strength.Sym,
    planner: Planner
) : BinaryConstraint(var1, var2, strength) {
    // Install a constraint with the given strength equating the given
    // variables.
    init {
        addConstraint(planner)
    }

    // Enforce this constraint. Assume that it is satisfied.
    override fun execute() {
        if (direction == Direction.FORWARD) {
            v2.value = v1.value
        } else {
            v1.value = v2.value
        }
    }
}