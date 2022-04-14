import deltablue.Planner
import kotlin.collections.List

class DeltaBlue : Benchmark() {
    override fun innerBenchmarkLoop(innerIterations: Int): Boolean {
        Planner.chainTest(innerIterations)
        Planner.projectionTest(innerIterations)
        return true
    }

    override val defaultInnerIterations: List<Int> = listOf(1, 15, 150, 1500, 15000)
}