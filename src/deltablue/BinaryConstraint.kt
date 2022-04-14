/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

import som.ForEachInterface
import som.TestInterface

// I am an abstract superclass for constraints having two possible
// output variables.
abstract class BinaryConstraint(
    protected var v1: Variable, // possible output variables
    protected var v2: Variable,
    strength: Strength.Sym,
) : AbstractConstraint(strength) {
    protected var direction: Direction? = null // one of the following...

    // Answer true if this constraint is satisfied in the current solution.
    override val isSatisfied: Boolean
        get() = direction != null

    // Add myself to the constraint graph.
    override fun addToGraph() {
        v1.addConstraint(this)
        v2.addConstraint(this)
        direction = null
    }

    // Remove myself from the constraint graph.
    override fun removeFromGraph() {
        v1.removeConstraint(this)
        v2.removeConstraint(this)
        direction = null
    }

    // Decide if I can be satisfied and which way I should flow based on
    // the relative strength of the variables I relate, and record that
    // decision.
    //
    override fun chooseMethod(mark: Int): Direction? {
        direction = when {
            v1.mark == mark -> if (v2.mark != mark && strength.stronger(v2.walkStrength)) Direction.FORWARD else null
            v2.mark == mark -> if (strength.stronger(v1.walkStrength)) Direction.BACKWARD else null
            // If we get here, neither variable is marked, so we have a choice.
            v1.walkStrength.weaker(v2.walkStrength) -> if (strength.stronger(v1.walkStrength)) Direction.BACKWARD else null
            else -> if (strength.stronger(v2.walkStrength)) Direction.FORWARD else null
        }
        return direction
    }

    override fun inputsDo(fn: ForEachInterface<Variable>) {
        if (direction == Direction.FORWARD) {
            fn.apply(v1)
        } else {
            fn.apply(v2)
        }
    }

    override fun inputsHasOne(fn: TestInterface<Variable>): Boolean =
        if (direction == Direction.FORWARD) fn.test(v1) else fn.test(v2)

    // Record the fact that I am unsatisfied.
    override fun markUnsatisfied() {
        direction = null
    }

    // Answer my current output variable.
    override val output: Variable
        get() = if (direction == Direction.FORWARD) v2 else v1

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    //
    override fun recalculate() {
        val input: Variable
        val output: Variable
        if (direction == Direction.FORWARD) {
            input = v1
            output = v2
        } else {
            input = v2
            output = v1
        }
        output.walkStrength = strength.weakest(input.walkStrength)
        output.stay = input.stay
        if (output.stay) {
            execute()
        }
    }
}