import cd.CollisionDetector
import cd.Simulator
import kotlin.collections.List

class CD : Benchmark() {
    private fun benchmark(numAircrafts: Int): Int {
        val numFrames = 200
        val simulator = Simulator(numAircrafts)
        val detector = CollisionDetector()
        var actualCollisions = 0
        for (i in 0 until numFrames) {
            val time = i / 10.0
            val collisions = detector.handleNewFrame(simulator.simulate(time))
            actualCollisions += collisions.size
        }
        return actualCollisions
    }

    override fun innerBenchmarkLoop(innerIterations: Int): Boolean {
        return verifyResult(benchmark(innerIterations), innerIterations)
    }

    private fun verifyResult(actualCollisions: Int, numAircrafts: Int): Boolean = when (numAircrafts) {
        1000 -> actualCollisions == 14484
        500 -> actualCollisions == 14484
        250 -> actualCollisions == 10830
        200 -> actualCollisions == 8655
        100 -> actualCollisions == 4305
        10 -> actualCollisions == 390
        2 -> actualCollisions == 42
        else -> false
    }

    override val defaultInnerIterations: List<Int> = listOf(2, 10, 100, 200, 250, 500, 1000)
}