package jetson.vision.math

import jetson.vision.interfaces.Interpolable

class Rotation2D : Interpolable<Rotation2D> {

    var cos: Double = 0.toDouble()
    var sin: Double = 0.toDouble()

    val radians: Double
        get() = Math.atan2(sin, cos)

    val degrees: Double
        get() = Math.toDegrees(this.radians)

    @JvmOverloads constructor(x: Double = 1.0, y: Double = 0.0, normalize: Boolean = false) {
        cos = x
        sin = y

        if (normalize) {
            normalize()
        }
    }

    constructor(other: Rotation2D) {
        cos = other.cos
        sin = other.sin
    }

    constructor(direction: Translation2D, normalize: Boolean) : this(direction.x, direction.y, normalize) {}

    fun normalize() {
        val magnitude = Math.hypot(cos, sin)

        if (magnitude > kEpsilon) {
            sin /= magnitude
            cos /= magnitude
        } else {
            sin = 0.0
            cos = 1.0
        }
    }

    fun tan(): Double {
        return if (Math.abs(cos) < kEpsilon) {
            if (sin >= 0.0) {
                java.lang.Double.POSITIVE_INFINITY
            } else {
                java.lang.Double.NEGATIVE_INFINITY
            }
        } else sin / cos

    }

    fun rotateBy(other: Rotation2D): Rotation2D {
        return Rotation2D(cos * other.cos - sin * other.sin, cos * other.sin + sin * other.cos, true)
    }

    fun normal(): Rotation2D = Rotation2D(-sin, cos, false)

    fun inverse(): Rotation2D = Rotation2D(cos, -sin, false);

    fun toTranslation(): Translation2D = Translation2D(cos, sin)

    fun isParallel(other: Rotation2D): Boolean = Utils.epsilonEquals(Translation2D.cross(toTranslation(), other.toTranslation()), 0.0, kEpsilon)

    override fun interpolate(other: Rotation2D, x: Double): Rotation2D {
        if (x <= 0) {
            return Rotation2D(this)
        } else if(x >= 1) {
            return Rotation2D(other)
        }

        val deltaTheta = inverse().rotateBy(other).radians
        return this.rotateBy(jetson.vision.math.Rotation2D.fromRadians(deltaTheta * x))
    }

    companion object {
        protected val kIdentity = Rotation2D()

        fun identity(): Rotation2D = kIdentity

        protected val kEpsilon = 1E-9

        fun fromRadians(rads: Double): Rotation2D = Rotation2D(Math.cos(rads), Math.sin(rads), false)

        fun fromDegrees(degs: Double): Rotation2D = fromRadians(Math.toRadians(degs))
    }
}
