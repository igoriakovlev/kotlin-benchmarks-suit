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

// I am an abstract superclass for constraints having a single
// possible output variable.
abstract class UnaryConstraint(// Answer my current output variable.
    override val output: Variable, // possible output variable
    strength: Strength.Sym,
    planner: Planner
) : AbstractConstraint(strength) {

    // Answer true if this constraint is satisfied in the current solution.
    override var isSatisfied = false// true if I am currently satisfied
        protected set

    init {
        addConstraint(planner)
    }

    // Add myself to the constraint graph.
    override fun addToGraph() {
        output.addConstraint(this)
        isSatisfied = false
    }

    // Remove myself from the constraint graph.
    override fun removeFromGraph() {
        output.removeConstraint(this)
        isSatisfied = false
    }

    // Decide if I can be satisfied and record that decision.
    override fun chooseMethod(mark: Int): Direction? {
        isSatisfied = (output.mark != mark
                && strength.stronger(output.walkStrength))
        return null
    }

    abstract override fun execute()

    override fun inputsDo(fn: ForEachInterface<Variable>) {
        // I have no input variables
    }

    override fun inputsHasOne(fn: TestInterface<Variable>): Boolean = false

    // Record the fact that I am unsatisfied.
    override fun markUnsatisfied() {
        isSatisfied = false
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied."
    override fun recalculate() {
        output.walkStrength = strength
        output.stay = !isInput
        if (output.stay) {
            execute() // stay optimization
        }
    }
}