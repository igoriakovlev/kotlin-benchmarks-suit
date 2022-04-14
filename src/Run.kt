import kotlin.reflect.KClass

/* This code is based on the SOM class library.
 *
 * Copyright (c) 2001-2016 see AUTHORS.md file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

class Run(private val name: String) {
    var numIterations: Int = 1
    var innerIterations: Int = 1
    private var total: Long = 0

    private fun runAllBenchmarks() {
        getAllSuites().forEach { benchmarkClass ->
            val benchmarkName = benchmarkClass.simpleName!!
            val benchmark = getSuiteFromName(benchmarkName)
            for (currentInnerIteration in benchmark.defaultInnerIterations) {
                innerIterations = currentInnerIteration
                with(Runner(benchmark, benchmarkName)) {
                    doRuns()
                    reportBenchmark()
                }
            }
        }
    }

    fun runBenchmark() {
        println("Starting $name benchmark ...")
        if (name == "runAll") {
            runAllBenchmarks()
        } else {
            with(Runner(getSuiteFromName(name), name)) {
                doRuns()
                reportBenchmark()
            }
        }
        println()
    }

    private inner class Runner(private val benchmark: Benchmark, private val name: String) {
        private fun measure(bench: Benchmark) {
            val startTime = System.nanoTime()
            check(bench.innerBenchmarkLoop(innerIterations)) { "Benchmark failed with incorrect result" }
            val endTime = System.nanoTime()
            val runTime = (endTime - startTime) / 1000
            printResult(runTime)
            total += runTime
        }

        fun doRuns() {
            for (i in 0 until numIterations) {
                measure(benchmark)
            }
        }

        fun reportBenchmark() {
            println("$name: iterations=$numIterations average: ${total / numIterations}us total: ${total}us")
        }

        private fun printResult(runTime: Long) {
            println("$name: iterations=1 runtime: ${runTime}us")
        }
    }

    fun printTotal() {
        println("Total Runtime: ${total}us")
    }

    companion object {
        val allBenchmarkNames: Sequence<String>
            get() = getAllSuites().map { it.simpleName!! }

        private fun getAllSuites(): Sequence<KClass<out Benchmark>> = sequence {
            yield(CD::class)
            yield(Havlak::class)
            yield(DeltaBlue::class)
            yield(NBody::class)
            yield(Json::class)
            yield(List::class)
            yield(Mandelbrot::class)
            yield(Permute::class)
            yield(Queens::class)
            yield(Richards::class)
            yield(Sieve::class)
            yield(Storage::class)
            yield(Towers::class)
            yield(Bounce::class)
        }

        private fun getSuiteFromName(name: String): Benchmark = when(name) {
            (CD::class).simpleName -> CD()
            (Havlak::class).simpleName -> Havlak()
            (DeltaBlue::class).simpleName -> DeltaBlue()
            (NBody::class).simpleName -> NBody()
            (Json::class).simpleName -> Json()
            (List::class).simpleName -> List()
            (Mandelbrot::class).simpleName -> Mandelbrot()
            (Permute::class).simpleName -> Permute()
            (Queens::class).simpleName -> Queens()
            (Richards::class).simpleName -> Richards()
            (Sieve::class).simpleName -> Sieve()
            (Storage::class).simpleName -> Storage()
            (Towers::class).simpleName -> Towers()
            (Bounce::class).simpleName -> Bounce()
            else -> error("Unsupported benchmark: $name")
        }
    }
}