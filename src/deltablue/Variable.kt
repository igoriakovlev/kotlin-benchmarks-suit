/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

import som.Vector

// ------------------------------ variables ------------------------------
// I represent a constrained variable. In addition to my value, I
// maintain the structure of the constraint graph, the current
// dataflow graph, and various parameters of interest to the DeltaBlue
// incremental constraint solver.
class Variable {
    var value= 0 // my value; changed by constraints
    val constraints: Vector<AbstractConstraint> = Vector(2) // normal constraints that reference me
    var determinedBy: AbstractConstraint? = null // the constraint that currently determines
    // my value (or null if there isn't one)
    var mark: Int = 0 // used by the planner to mark constraints
    var walkStrength: Strength = Strength.absoluteWeakest() // my walkabout strength
    var stay: Boolean = true // true if I am a planning-time constant

    // Add the given constraint to the set of all constraints that refer to me.
    fun addConstraint(c: AbstractConstraint) {
        constraints.append(c)
    }

    // Remove all traces of c from this variable.
    fun removeConstraint(c: AbstractConstraint) {
        constraints.remove(c)
        if (determinedBy === c) {
            determinedBy = null
        }
    }

    companion object {
        fun value(aValue: Int): Variable =
            Variable().also { it.value = aValue }
    }
}