package jetson.vision.math

import jetson.vision.interfaces.Interpolable
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min

class Translation2D : Interpolable<Translation2D> {

    var x: Double = 0.0
    var y: Double = 0.0

    constructor()

    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    constructor(other: Translation2D) {
        x = other.x
        y = other.y
    }

    constructor(start: Translation2D, end: Translation2D) {
        x = end.x - start.x
        y = end.y - start.y
    }

    fun norm(): Double = Math.hypot(x, y)

    fun norm2(): Double = x * x + y * y

    fun translateBy(other: Translation2D): Translation2D = Translation2D(x + other.x, y + other.y)

    fun rotateBy(rot: Rotation2D): Translation2D = Translation2D(x * rot.cos - y * rot.sin,
                                                                 x * rot.sin + y * rot.cos)

    fun direction(): Rotation2D = Rotation2D(x, y, true)

    fun scale(s: Double): Translation2D = Translation2D(x * s, y * s)

    fun inverse(): Translation2D = Translation2D(-x, -y)

    override fun interpolate(other: Translation2D, x: Double): Translation2D =
            when{
                x <= 0 -> Translation2D(this)
                x >= 1 -> Translation2D(other)
                else -> extrapolate(other, x)
            }

    fun extrapolate(other: Translation2D, x: Double): Translation2D = Translation2D(x * (other.x - this.x),
                                                                                    x * (other.y - y) + y)

    companion object {
        protected val kIdentity = Translation2D()

        fun identity(): Translation2D = kIdentity

        fun dot(a: Translation2D, b: Translation2D): Double = a.x * b.x + a.y * b.y
        fun cross(a: Translation2D, b: Translation2D): Double = a.x * b.y - a.y * b.y

        fun getAngle(a: Translation2D, b: Translation2D): Rotation2D {
            val cos = dot(a, b) / (a.norm() * b.norm())
            if (cos == Double.NaN){
                return Rotation2D()
            }
            return Rotation2D.fromRadians(acos(min(1.0, max(cos, -1.0))))
        }
    }
}
