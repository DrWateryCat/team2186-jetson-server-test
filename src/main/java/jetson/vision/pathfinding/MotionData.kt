package jetson.vision.pathfinding

import jetson.vision.math.Utils
import kotlin.math.sign
import kotlin.math.sqrt

class MotionData constructor(val t: Double, val pos: Double, val vel: Double, val acc: Double) {
    constructor(other: MotionData) : this(other.t, other.pos, other.vel, other.acc)

    fun extrapolate(t: Double): MotionData = extrapolate(t, acc)

    fun extrapolate(t:Double, acc: Double): MotionData {
        val dT = t - this.t
        return MotionData(t, pos + vel * dT + 0.5 * acc * dT * dT, vel + acc * dT, acc)
    }

    fun nextTimeAtPos(pos: Double): Double {
        if (Utils.epsilonEquals(pos, this.pos, 1E-9)) {
            return t
        }

        if (Utils.epsilonEquals(acc, 0.0, 1E-9)) {
            val deltaX = pos - this.pos
            if (!Utils.epsilonEquals(vel, 0.0, 1E-9) && sign(deltaX) == sign(vel)) {
                return deltaX / vel + t
            }

            return Double.NaN
        }

        val disc = vel * vel - 2.0 * acc * (this.pos - pos)
        if (disc < 0.0) {
            return Double.NaN
        }

        val sqrtDisc = sqrt(disc)
        val maxDT = (-vel + sqrtDisc) / acc
        val minDT = (-vel - sqrtDisc) / acc

        if(minDT >= 0.0 && (maxDT < 0.0 || minDT < maxDT)) {
            return t + minDT
        }

        if (maxDT >= 0.0) {
            return t + maxDT
        }

        return Double.NaN
    }

    override fun equals(other: Any?): Boolean = (other is MotionData) && equals(other, 1E-9)
    fun equals(other: MotionData, epsilon: Double): Boolean = coincident(other, epsilon)
                                                            && Utils.epsilonEquals(acc, other.acc, epsilon)

    fun coincident(other: MotionData): Boolean = coincident(other, 1E-9)
    fun coincident(other: MotionData, epsilon: Double) = Utils.epsilonEquals(t, other.t, epsilon)
                                                      && Utils.epsilonEquals(pos, other.pos, epsilon)
                                                      && Utils.epsilonEquals(vel, other.vel, epsilon)

    fun flipped(): MotionData = MotionData(t, -pos, -vel, -acc)
}