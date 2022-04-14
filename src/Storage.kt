import som.Random

class Storage : Benchmark() {
    private var count = 0
    override fun benchmark(): Any {
        val random = Random()
        count = 0
        buildTreeDepth(7, random)
        return count
    }

    override fun verifyResult(result: Any): Boolean =
        5461 == result as Int

    private fun buildTreeDepth(depth: Int, random: Random): Array<Any?> {
        count++
        return if (depth == 1) {
            arrayOfNulls(random.next() % 10 + 1)
        } else {
            Array(4) { buildTreeDepth(depth - 1, random) }
        }
    }
}