package cd

import kotlin.math.sqrt

class Vector3D(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: Vector3D): Vector3D =
        Vector3D(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3D): Vector3D =
        Vector3D(x - other.x, y - other.y, z - other.z)

    fun dot(other: Vector3D): Double =
        x * other.x + y * other.y + z * other.z

    fun squaredMagnitude(): Double =
        dot(this)

    fun magnitude(): Double =
        sqrt(squaredMagnitude())

    operator fun times(amount: Double): Vector3D =
        Vector3D(x * amount, y * amount, z * amount)
}