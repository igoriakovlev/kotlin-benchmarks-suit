import havlak.LoopTesterApp
import kotlin.collections.List

class Havlak : Benchmark() {
    override fun innerBenchmarkLoop(innerIterations: Int): Boolean {
        return verifyResult(
            result = LoopTesterApp().main(
                numDummyLoops = innerIterations,
                findLoopIterations = 50,
                parLoops = 10 /* was 100 */,
                pparLoops = 10,
                ppparLoops = 5
            ),
            innerIterations = innerIterations
        )
    }

    fun verifyResult(result: Any, innerIterations: Int): Boolean {
        val r = result as IntArray
        return when (innerIterations) {
            15000 -> r[0] == 46602 && r[1] == 5213
            1500 -> r[0] == 6102 && r[1] == 5213
            150 -> r[0] == 2052 && r[1] == 5213
            15 -> r[0] == 1647 && r[1] == 5213
            1 -> r[0] == 1605 && r[1] == 5213
            else -> false
        }
    }

    override val defaultInnerIterations: List<Int> = listOf(1, 15, 150, 1500, 15000)
}