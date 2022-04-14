/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

import som.Vector.Companion.with
import som.ForEachInterface
import som.Vector

class Planner {
    private var currentMark = 1

    // Attempt to satisfy the given constraint and, if successful,
    // incrementally update the dataflow graph. Details: If satifying
    // the constraint is successful, it may override a weaker constraint
    // on its output. The algorithm attempts to resatisfy that
    // constraint using some other method. This process is repeated
    // until either a) it reaches a variable that was not previously
    // determined by any constraint or b) it reaches a constraint that
    // is too weak to be satisfied using any of its methods. The
    // variables of constraints that have been processed are marked with
    // a unique mark value so that we know where we've been. This allows
    // the algorithm to avoid getting into an infinite loop even if the
    // constraint graph has an inadvertent cycle.
    //
    fun incrementalAdd(c: AbstractConstraint) {
        val mark = newMark()
        var overridden = c.satisfy(mark, this)
        while (overridden != null) {
            overridden = overridden.satisfy(mark, this)
        }
    }

    // Entry point for retracting a constraint. Remove the given
    // constraint and incrementally update the dataflow graph.
    // Details: Retracting the given constraint may allow some currently
    // unsatisfiable downstream constraint to be satisfied. We therefore collect
    // a list of unsatisfied downstream constraints and attempt to
    // satisfy each one in turn. This list is traversed by constraint
    // strength, strongest first, as a heuristic for avoiding
    // unnecessarily adding and then overriding weak constraints.
    // Assume: c is satisfied.
    //
    fun incrementalRemove(c: AbstractConstraint) {
        val out = c.output
        c.markUnsatisfied()
        c.removeFromGraph()
        val unsatisfied = removePropagateFrom(out)
        unsatisfied.forEach { u: AbstractConstraint -> incrementalAdd(u) }
    }

    // Extract a plan for resatisfaction starting from the outputs of
    // the given constraints, usually a set of input constraints.
    //
    private fun extractPlanFromConstraints(constraints: Vector<AbstractConstraint>): Plan {
        val sources = Vector<AbstractConstraint>()
        constraints.forEach { c: AbstractConstraint ->
            if (c.isInput && c.isSatisfied) {
                sources.append(c)
            }
        }
        return makePlan(sources)
    }

    // Extract a plan for resatisfaction starting from the given source
    // constraints, usually a set of input constraints. This method
    // assumes that stay optimization is desired; the plan will contain
    // only constraints whose output variables are not stay. Constraints
    // that do no computation, such as stay and edit constraints, are
    // not included in the plan.
    // Details: The outputs of a constraint are marked when it is added
    // to the plan under construction. A constraint may be appended to
    // the plan when all its input variables are known. A variable is
    // known if either a) the variable is marked (indicating that has
    // been computed by a constraint appearing earlier in the plan), b)
    // the variable is 'stay' (i.e. it is a constant at plan execution
    // time), or c) the variable is not determined by any
    // constraint. The last provision is for past states of history
    // variables, which are not stay but which are also not computed by
    // any constraint.
    // Assume: sources are all satisfied.
    //
    private fun makePlan(sources: Vector<AbstractConstraint>): Plan {
        val mark = newMark()
        val plan = Plan()
        while (!sources.isEmpty) {
            val c = sources.removeFirst()!!
            if (c.output.mark != mark && c.inputsKnown(mark)) {
                // not in plan already and eligible for inclusion
                plan.append(c)
                c.output.mark = mark
                addConstraintsConsumingTo(c.output, sources)
            }
        }
        return plan
    }

    // The given variable has changed. Propagate new values downstream.
    fun propagateFrom(v: Variable) {
        val todo = Vector<AbstractConstraint>()
        addConstraintsConsumingTo(v, todo)
        while (!todo.isEmpty) {
            val c = todo.removeFirst()
            c!!.execute()
            addConstraintsConsumingTo(c.output, todo)
        }
    }

    private fun addConstraintsConsumingTo(v: Variable, coll: Vector<AbstractConstraint>) {
        val determiningC = v.determinedBy
        v.constraints.forEach { c: AbstractConstraint ->
            if (c !== determiningC && c.isSatisfied) {
                coll.append(c)
            }
        }
    }

    // Recompute the walkabout strengths and stay flags of all variables
    // downstream of the given constraint and recompute the actual
    // values of all variables whose stay flag is true. If a cycle is
    // detected, remove the given constraint and answer
    // false. Otherwise, answer true.
    // Details: Cycles are detected when a marked variable is
    // encountered downstream of the given constraint. The sender is
    // assumed to have marked the inputs of the given constraint with
    // the given mark. Thus, encountering a marked node downstream of
    // the output constraint means that there is a path from the
    // constraint's output to one of its inputs.
    //
    fun addPropagate(c: AbstractConstraint, mark: Int): Boolean {
        val todo = with(c)
        while (!todo.isEmpty) {
            val d = todo.removeFirst()!!
            if (d.output.mark == mark) {
                incrementalRemove(c)
                return false
            }
            d.recalculate()
            addConstraintsConsumingTo(d.output, todo)
        }
        return true
    }

    private fun change(variable: Variable, newValue: Int) {
        val editC = EditConstraint(variable, Strength.PREFERRED, this)
        val editV = with<AbstractConstraint>(editC)
        val plan = extractPlanFromConstraints(editV)
        for (i in 0..9) {
            variable.value = newValue
            plan.execute()
        }
        editC.destroyConstraint(this)
    }

    private fun constraintsConsuming(v: Variable, fn: ForEachInterface<AbstractConstraint>) {
        val determiningC = v.determinedBy
        v.constraints.forEach { c: AbstractConstraint ->
            if (c !== determiningC && c.isSatisfied) {
                fn.apply(c)
            }
        }
    }

    // Select a previously unused mark value.
    private fun newMark(): Int {
        currentMark++
        return currentMark
    }

    // Update the walkabout strengths and stay flags of all variables
    // downstream of the given constraint. Answer a collection of
    // unsatisfied constraints sorted in order of decreasing strength.
    private fun removePropagateFrom(out: Variable): Vector<AbstractConstraint> {
        val unsatisfied = Vector<AbstractConstraint>()
        out.determinedBy = null
        out.walkStrength = Strength.absoluteWeakest()
        out.stay = true
        val todo = with(out)
        while (!todo.isEmpty) {
            val v = todo.removeFirst()!!
            v.constraints.forEach { c: AbstractConstraint ->
                if (!c.isSatisfied) {
                    unsatisfied.append(c)
                }
            }
            constraintsConsuming(v) { c: AbstractConstraint ->
                c.recalculate()
                todo.append(c.output)
            }
        }
        unsatisfied.sort { c1: AbstractConstraint, c2: AbstractConstraint ->
            if (c1.strength.stronger(c2.strength)) -1 else 1
        }
        return unsatisfied
    }

    companion object {
        // This is the standard DeltaBlue benchmark. A long chain of
        // equality constraints is constructed with a stay constraint on
        // one end. An edit constraint is then added to the opposite end
        // and the time is measured for adding and removing this
        // constraint, and extracting and executing a constraint
        // satisfaction plan. There are two cases. In case 1, the added
        // constraint is stronger than the stay constraint and values must
        // propagate down the entire length of the chain. In case 2, the
        // added constraint is weaker than the stay constraint so it cannot
        // be accomodated. The cost in this case is, of course, very
        // low. Typical situations lie somewhere between these two
        // extremes.
        //
        fun chainTest(n: Int) {
            val planner = Planner()
            val vars = Array(n + 1) { Variable() }

            // Build chain of n equality constraints
            for (i in 0 until n) {
                val v1 = vars[i]
                val v2 = vars[i + 1]
                EqualityConstraint(v1, v2, Strength.REQUIRED, planner)
            }
            StayConstraint(vars[n], Strength.STRONG_DEFAULT, planner)
            val editC: AbstractConstraint = EditConstraint(vars[0], Strength.PREFERRED, planner)
            val editV = with(editC)
            val plan = planner.extractPlanFromConstraints(editV)
            for (i in 0..99) {
                vars[0].value = i
                plan.execute()
                check (vars[n].value == i) {
                    "Chain test failed!"
                }
            }
            editC.destroyConstraint(planner)
        }

        // This test constructs a two sets of variables related to each
        // other by a simple linear transformation (scale and offset). The
        // time is measured to change a variable on either side of the
        // mapping and to change the scale and offset factors.
        //
        fun projectionTest(n: Int) {
            val planner = Planner()
            val dests = Vector<Variable>()
            val scale = Variable.value(10)
            val offset = Variable.value(1000)
            var src: Variable? = null
            var dst: Variable? = null
            for (i in 1..n) {
                src = Variable.value(i)
                dst = Variable.value(i)
                dests.append(dst)
                StayConstraint(src, Strength.DEFAULT, planner)
                ScaleConstraint(src, scale, offset, dst, Strength.REQUIRED, planner)
            }
            check(src != null)
            check(dst != null)
            planner.change(src, 17)
            check (dst.value == 1170)
            planner.change(dst, 1050)
            check (src.value == 5) {
                "Projection test 2 failed!"
            }
            planner.change(scale, 5)
            for (i in 0 until n - 1) {
                check (dests.at(i)!!.value == (i + 1) * 5 + 1000) {
                    "Projection test 3 failed!"
                }
            }
            planner.change(offset, 2000)
            for (i in 0 until n - 1) {
                check (dests.at(i)!!.value == (i + 1) * 5 + 2000) {
                    "Projection test 4 failed!"
                }
            }
        }
    }
}