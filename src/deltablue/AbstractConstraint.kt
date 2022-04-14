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

// ------------------------ constraints ------------------------------------
// I am an abstract class representing a system-maintainable
// relationship (or "constraint") between a set of variables. I supply
// a strength instance variable; concrete subclasses provide a means
// of storing the constrained variables and other information required
// to represent a constraint.
abstract class AbstractConstraint(strength: Strength.Sym) {
    val strength: Strength = Strength.of(strength) // the strength of this constraint

    // Normal constraints are not input constraints. An input constraint
    // is one that depends on external state, such as the mouse, the
    // keyboard, a clock, or some arbitrary piece of imperative code.
    open val isInput: Boolean
        get() = false

    // Answer true if this constraint is satisfied in the current solution.
    abstract val isSatisfied: Boolean

    // Activate this constraint and attempt to satisfy it.
    protected fun addConstraint(planner: Planner) {
        addToGraph()
        planner.incrementalAdd(this)
    }

    // Add myself to the constraint graph.
    abstract fun addToGraph()

    // Deactivate this constraint, remove it from the constraint graph,
    // possibly causing other constraints to be satisfied, and destroy
    // it.
    fun destroyConstraint(planner: Planner) {
        if (isSatisfied) {
            planner.incrementalRemove(this)
        }
        removeFromGraph()
    }

    // Remove myself from the constraint graph.
    abstract fun removeFromGraph()

    // Decide if I can be satisfied and record that decision. The output
    // of the chosen method must not have the given mark and must have
    // a walkabout strength less than that of this constraint.
    protected abstract fun chooseMethod(mark: Int): Direction?

    // Enforce this constraint. Assume that it is satisfied.
    abstract fun execute()
    abstract fun inputsDo(fn: ForEachInterface<Variable>)
    abstract fun inputsHasOne(fn: TestInterface<Variable>): Boolean

    // Assume that I am satisfied. Answer true if all my current inputs
    // are known. A variable is known if either a) it is 'stay' (i.e. it
    // is a constant at plan execution time), b) it has the given mark
    // (indicating that it has been computed by a constraint appearing
    // earlier in the plan), or c) it is not determined by any
    // constraint.
    fun inputsKnown(mark: Int): Boolean {
        return !inputsHasOne { v: Variable -> !(v.mark == mark || v.stay || v.determinedBy == null) }
    }

    // Record the fact that I am unsatisfied.
    abstract fun markUnsatisfied()

    // Answer my current output variable. Raise an error if I am not
    // currently satisfied.
    abstract val output: Variable

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    abstract fun recalculate()

    // Attempt to find a way to enforce this constraint. If successful,
    // record the solution, perhaps modifying the current dataflow
    // graph. Answer the constraint that this constraint overrides, if
    // there is one, or nil, if there isn't.
    // Assume: I am not already satisfied.
    //
    fun satisfy(mark: Int, planner: Planner): AbstractConstraint? {
        val overridden: AbstractConstraint?
        chooseMethod(mark)
        if (isSatisfied) {
            // constraint can be satisfied
            // mark inputs to allow cycle detection in addPropagate
            inputsDo { value: Variable -> value.mark = mark }
            val out = output
            overridden = out.determinedBy
            overridden?.markUnsatisfied()
            out.determinedBy = this
            check (planner.addPropagate(this, mark)) {
                "Cycle encountered"
            }
            out.mark = mark
        } else {
            overridden = null
            check (!strength.sameAs(Strength.required())) {
                "Could not satisfy a required constraint"
            }
        }
        return overridden
    }
}