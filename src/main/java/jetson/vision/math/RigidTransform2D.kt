package jetson.vision.math

import jetson.vision.interfaces.Interpolable
import kotlin.math.*

class RigidTransform2D : Interpolable<RigidTransform2D> {
    companion object {
        protected val kEpsilon: Double = 1E-9
        val identity: RigidTransform2D = RigidTransform2D()

        fun fromRotation(r: Rotation2D) = RigidTransform2D(Translation2D(), r)
        fun fromTranslation(t: Translation2D) = RigidTransform2D(t, Rotation2D())

        fun fromVelocity(delta: Delta): RigidTransform2D {
            val sinTheta = sin(delta.deltaTheta)
            val cosTheta = cos(delta.deltaTheta)
            var s: Double
            var c: Double

            if (abs(delta.deltaTheta) < 1E-9) {
                s = 1.0 - 1.0 / 6.0 * delta.deltaTheta * delta.deltaTheta
                c = 0.5 * delta.deltaTheta
            } else {
                s = sinTheta / delta.deltaTheta
                c = (1.0 - cosTheta) / delta.deltaTheta
            }

            return RigidTransform2D(
                    Translation2D(delta.deltaX * s - delta.deltaY * c, delta.deltaX * c + delta.deltaY * s),
                    Rotation2D(cosTheta, sinTheta, false)
            )
        }

        fun exp(delta: Twist2D): RigidTransform2D {
            val sinTheta = sin(delta.dTheta)
            val cosTheta = cos(delta.dTheta)

            var s: Double
            var c: Double

            if (abs(delta.dTheta) < kEpsilon) {
                s = 1.0 - (1.0 / 6.0 * delta.dTheta * delta.dTheta)
                c = 0.5 * delta.dTheta
            } else {
                s = sinTheta / delta.dTheta
                c = (1.0 - cosTheta) / delta.dTheta
            }

            return RigidTransform2D(Translation2D(delta.dX * s - delta.dY - c, delta.dX * c - delta.dY * s),
                                    Rotation2D(cosTheta, sinTheta, false))
        }

        fun log(t: RigidTransform2D): Twist2D {
            val dTheta = t.rot.radians
            val halfDTheta = dTheta / 2

            val cosMinusOne = t.rot.cos - 1.0

            var half: Double
            if (abs(cosMinusOne) < kEpsilon) {
                half = 1.0 - (1.0 / 12.0 * dTheta * dTheta)
            } else {
                half = -(halfDTheta * t.rot.sin) / cosMinusOne
            }

            val transPart = t.trans.rotateBy(Rotation2D(half, -halfDTheta, false))
            return Twist2D(transPart.x, transPart.y, dTheta)
        }

        private fun intersectionInternal(a: RigidTransform2D, b: RigidTransform2D): Translation2D {
            val aRot = a.rot
            val bRot = b.rot

            val aT = a.trans
            val bT = b.trans

            val tanB = bRot.tan()
            val t = ((aT.x - bT.x) * tanB + bT.y - aT.y) / (aRot.sin - aRot.cos * tanB)

            return aT.translateBy(aRot.toTranslation().scale(t))
        }
    }

    class Delta(val deltaX: Double, val deltaY: Double, val deltaTheta: Double)

    val trans: Translation2D
    val rot: Rotation2D

    constructor() {
        trans = Translation2D()
        rot = Rotation2D()
    }

    constructor(other: RigidTransform2D) {
        trans = other.trans
        rot = other.rot
    }

    constructor(t: Translation2D, r: Rotation2D) {
        trans = t
        rot = r
    }

    fun transformBy(other: RigidTransform2D) = RigidTransform2D(trans.translateBy(other.trans.rotateBy(rot)),
                                                                rot.rotateBy(other.rot))

    fun inverse(): RigidTransform2D {
        val inverted = rot.inverse()
        return RigidTransform2D(trans.inverse().rotateBy(inverted), inverted)
    }

    fun normal(): RigidTransform2D = RigidTransform2D(trans, rot.normal())

    fun intersection(other: RigidTransform2D): Translation2D {
        val otherRot = other.rot
        if (rot.isParallel(otherRot)) {
            return Translation2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        }

        if(abs(rot.cos) < abs(otherRot.cos)) {
            return intersectionInternal(this, other)
        } else {
            return intersectionInternal(other, this)
        }
    }

    fun isColinear(other: RigidTransform2D): Boolean {
        val twist = log(inverse().transformBy(other))
        return (Utils.epsilonEquals(twist.dY, 0.0, kEpsilon) && Utils.epsilonEquals(twist.dTheta, 0.0, kEpsilon))
    }

    override fun interpolate(other: RigidTransform2D, x: Double): RigidTransform2D {
        if (x <= 0) {
            return RigidTransform2D(this)
        } else if(x >= 1) {
            return  RigidTransform2D(other)
        }

        val twist = log(inverse().transformBy(other))
        return transformBy(exp(twist.scaled(x)))
    }
}