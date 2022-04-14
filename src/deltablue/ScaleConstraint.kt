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

// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
internal open class ScaleConstraint(
    src: Variable, // scale factor input variable
    private val scale: Variable,
    // offset input variable
    private val offset: Variable, destination: Variable, strength: Strength.Sym,
    planner: Planner
) : BinaryConstraint(src, destination, strength) {

    init {
        addConstraint(planner)
    }

    // Add myself to the constraint graph.
    override fun addToGraph() {
        v1.addConstraint(this)
        v2.addConstraint(this)
        scale.addConstraint(this)
        offset.addConstraint(this)
        direction = null
    }

    // Remove myself from the constraint graph.
    override fun removeFromGraph() {
        v1.removeConstraint(this)
        v2.removeConstraint(this)
        scale.removeConstraint(this)
        offset.removeConstraint(this)
        direction = null
    }

    // Enforce this constraint. Assume that it is satisfied.
    override fun execute() {
        if (direction == Direction.FORWARD) {
            v2.value = v1.value * scale.value + offset.value
        } else {
            v1.value = (v2.value - offset.value) / scale.value
        }
    }

    override fun inputsDo(fn: ForEachInterface<Variable>) {
        if (direction == Direction.FORWARD) {
            fn.apply(v1)
            fn.apply(scale)
            fn.apply(offset)
        } else {
            fn.apply(v2)
            fn.apply(scale)
            fn.apply(offset)
        }
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    override fun recalculate() {
        val input: Variable
        val output: Variable
        if (direction == Direction.FORWARD) {
            input = v1
            output = v2
        } else {
            output = v1
            input = v2
        }
        output.walkStrength = strength.weakest(input.walkStrength)
        output.stay = input.stay && scale.stay && offset.stay
        if (output.stay) {
            execute() // stay optimization
        }
    }
}