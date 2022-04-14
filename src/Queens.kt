import java.util.Arrays

class Queens : Benchmark() {
    override fun benchmark(): Any {
        var result = true
        for (i in 0..9) {
            result = result && QueensImplementation().queens()
        }
        return result
    }

    override fun verifyResult(result: Any): Boolean =
        result as Boolean

    class QueensImplementation {
        private val freeRows = BooleanArray(8)
        private val freeMaxs = BooleanArray(16)
        private val freeMins = BooleanArray(16)
        private val queenRows = IntArray(8)

        fun queens(): Boolean {
            Arrays.fill(freeRows, true)
            Arrays.fill(freeMaxs, true)
            Arrays.fill(freeMins, true)
            Arrays.fill(queenRows, -1)
            return placeQueen(0)
        }

        private fun placeQueen(c: Int): Boolean {
            for (r in 0..7) {
                if (getRowColumn(r, c)) {
                    queenRows[r] = c
                    setRowColumn(r, c, false)
                    if (c == 7) {
                        return true
                    }
                    if (placeQueen(c + 1)) {
                        return true
                    }
                    setRowColumn(r, c, true)
                }
            }
            return false
        }

        private fun getRowColumn(r: Int, c: Int): Boolean =
            freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7]

        private fun setRowColumn(r: Int, c: Int, v: Boolean) {
            freeRows[r] = v
            freeMaxs[c + r] = v
            freeMins[c - r + 7] = v
        }
    }
}