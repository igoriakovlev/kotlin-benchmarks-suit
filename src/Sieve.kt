class Sieve : Benchmark() {
    override fun benchmark(): Any {
        val flags = BooleanArray(5000) { true }
        return sieve(flags, 5000)
    }

    override fun verifyResult(result: Any): Boolean =
        669 == result as Int

    private fun sieve(flags: BooleanArray, size: Int): Int {
        var primeCount = 0
        for (i in 2..size) {
            if (flags[i - 1]) {
                primeCount++
                var k = i + i
                while (k <= size) {
                    flags[k - 1] = false
                    k += i
                }
            }
        }
        return primeCount
    }
}