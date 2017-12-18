package jetson.vision.pathfinding

import jetson.vision.math.Translation2D
import kotlin.math.max
import kotlin.math.min

class PathSegment(var start: Translation2D, var end: Translation2D, var speed: Double) {
    var length: Double = 0.toDouble()
    lateinit var startToEnd: Translation2D

    class Sample(val translation: Translation2D, val speed: Double)

    class ClosestPointReport {
        var index: Double = 0.toDouble()
        var clampedIndex: Double = 0.toDouble()
        var closestPoint: Translation2D = Translation2D()
        var distance: Double = 0.toDouble()
    }

    init {
        updateStart(start)
    }

    fun updateStart(newStart: Translation2D) {
        start = newStart
        startToEnd = start.inverse().translateBy(end)
        length = startToEnd.norm()
    }

    fun interpolate(index: Double): Translation2D = start.interpolate(end, index)

    fun dotProduct(other: Translation2D): Double {
        val startToOther = start.inverse().translateBy(other)
        return startToEnd.x * startToOther.x + startToEnd.y * startToOther.y
    }

    fun getClosestPoint(query: Translation2D): ClosestPointReport {
        var ret = ClosestPointReport()

        if(length > 1E-9) {
            val dot = dotProduct(query)
            ret.index = dot / (length * length)
            ret.clampedIndex = min(1.0, max(0.0, ret.index))
            ret.closestPoint = interpolate(ret.index)
        } else {
            ret.index = 0.0
            ret.clampedIndex = 0.0
            ret.closestPoint = Translation2D(start)
        }

        ret.distance = ret.closestPoint.inverse().translateBy(query).norm()
        return ret
    }
}
