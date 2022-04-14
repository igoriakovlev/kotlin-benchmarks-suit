package cd

class Vector2D(val x: Double, val y: Double) : Comparable<Vector2D> {
    operator fun plus(other: Vector2D): Vector2D =
        Vector2D(x + other.x, y + other.y)

    operator fun minus(other: Vector2D): Vector2D =
        Vector2D(x - other.x, y - other.y)

    override fun compareTo(other: Vector2D): Int {
        val result = compareNumbers(x, other.x)
        return if (result != 0) result else compareNumbers(y, other.y)
    }

    companion object {
        private fun compareNumbers(a: Double, b: Double): Int = when {
            a == b -> 0
            a < b -> -1
            a > b -> 1
            // We say that NaN is smaller than non-NaN.
            else -> if (a == a) 1 else -1
        }
    }
}