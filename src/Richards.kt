import richards.Scheduler
/*
 * This version is a port of the SOM Richards benchmark to Java.
 * It is kept as close to the SOM version as possible, for cross-language
 * benchmarking.
 */

class Richards : Benchmark() {
    override fun benchmark(): Any =
        Scheduler().start()

    override fun verifyResult(result: Any): Boolean =
        result as Boolean
}