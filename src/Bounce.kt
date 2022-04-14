import som.Random
import kotlin.collections.List
import kotlin.math.abs

class Bounce : Benchmark() {
    private class Ball constructor(random: Random) {
        private var x: Int = random.next() % 500
        private var y: Int = random.next() % 500
        private var xVel: Int = random.next() % 300 - 150
        private var yVel: Int = random.next() % 300 - 150

        fun bounce(): Boolean {
            val xLimit = 500
            val yLimit = 500
            var bounced = false
            x += xVel
            y += yVel
            if (x > xLimit) {
                x = xLimit
                xVel = 0 - abs(xVel)
                bounced = true
            }
            if (x < 0) {
                x = 0
                xVel = abs(xVel)
                bounced = true
            }
            if (y > yLimit) {
                y = yLimit
                yVel = 0 - abs(yVel)
                bounced = true
            }
            if (y < 0) {
                y = 0
                yVel = abs(yVel)
                bounced = true
            }
            return bounced
        }
    }

    override fun benchmark(): Any {
        val random = Random()
        val ballCount = 100
        var bounces = 0
        val balls = Array(ballCount) { Ball(random) }
        for (i in 0..49) {
            for (ball in balls) {
                if (ball.bounce()) {
                    bounces += 1
                }
            }
        }
        return bounces
    }

    override fun verifyResult(result: Any): Boolean =
        1331 == result as Int
}