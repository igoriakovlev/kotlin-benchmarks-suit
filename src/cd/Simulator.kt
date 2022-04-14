package cd

import som.Vector
import kotlin.math.cos
import kotlin.math.sin

class Simulator(numAircraft: Int) {
    private val aircraft: Vector<CallSign> = Vector()

    init {
        require(numAircraft.rem(2) == 0) { "numAircraft should be divided on two" }
        for (i in 0 until numAircraft) {
            aircraft.append(CallSign(i))
        }
    }

    fun simulate(time: Double): Vector<Aircraft> {
        val frame = Vector<Aircraft>()
        for (i in 0 until aircraft.size step 2) {
            frame.append(
                Aircraft(
                    aircraft.at(i)!!,
                    Vector3D(time, cos(time) * 2 + i * 3, 10.0)
                )
            )
            frame.append(
                Aircraft(
                    aircraft.at(i + 1)!!,
                    Vector3D(time, sin(time) * 2 + i * 3, 10.0)
                )
            )

        }
        return frame
    }
}