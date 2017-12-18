package jetson.vision.pathfinding

import jetson.vision.math.Translation2D
import kotlin.math.abs
import kotlin.math.sqrt

class Path (var waypoints: ArrayList<Waypoint>){
    class Waypoint(val position: Translation2D, val speed: Double, val marker: String?)
    val COMPLETE_PERCENTAGE = 0.99
    var markersCrossed = HashSet<String>()
    var segments = ArrayList<PathSegment>()
    init {
        (0 until waypoints.size - 1).forEach { i -> segments.add(PathSegment(
                waypoints[i].position,
                waypoints[i + 1].position,
                waypoints[i].speed
        )) }

        if (waypoints.size > 0) {
            val first = waypoints[0]
            if (first.marker.isNullOrEmpty().not()) {
                markersCrossed.add(first.marker.orEmpty())
            }

            waypoints.removeAt(0)
        }
    }

    fun update(pos: Translation2D): Double {
        var ret = 0.0

        for (seg in segments) {
            //Loop through all the segments
            val closestPointReport = seg.getClosestPoint(pos)

            //Check if it's complete
            if(closestPointReport.index >= COMPLETE_PERCENTAGE) {
                //Remove the waypoint
                segments.iterator().remove()

                if (waypoints.size > 0) {
                    //If there are more waypoints remove the first one, since we're there.
                    val waypoint = waypoints[0]

                    if (waypoint.marker.isNullOrEmpty().not()) {
                        markersCrossed.add(waypoint.marker.orEmpty())
                    }

                    waypoints.removeAt(0)
                }
            } else {
                if (closestPointReport.index > 0.0) {
                    //We can shorten this
                    seg.updateStart(closestPointReport.closestPoint)
                }

                //We're done!
                ret = closestPointReport.distance
                //Unless there's more

                if (segments.iterator().hasNext()) {
                    val next = segments.iterator().next()
                    val nextClosestPoint = next.getClosestPoint(pos)

                    if ((nextClosestPoint.index > 0)
                            .and(nextClosestPoint.index < COMPLETE_PERCENTAGE)
                            .and(nextClosestPoint.index < ret)) {
                        next.updateStart(nextClosestPoint.closestPoint)
                        ret = nextClosestPoint.distance

                        segments.removeAt(0)

                        if (waypoints.size > 0) {
                            val waypoint = waypoints[0]

                            if (waypoint.marker.isNullOrEmpty().not()) {
                                markersCrossed.add(waypoint.marker.orEmpty())
                            }

                            waypoints.removeAt(0)
                        }
                    }
                }

                break
            }
        }

        return ret
    }

    fun remainingDistance(): Double {
        return segments.sumByDouble { it.length }
    }

    fun getLookaheadPoint(pos: Translation2D, lookaheadDist: Double): PathSegment.Sample {
        if (segments.size == 0) {
            return PathSegment.Sample(Translation2D(), 0.0)
        }

        //Check the distance to the start and end of each segment
        //When we find a point further away than the lookahead distance
        //We know the right point lives somewhere on that segment
        val posInverse = pos.inverse()

        if (posInverse.translateBy(segments[0].start).norm() >= lookaheadDist) {
            //We're before the first point, so just return the first point
            return PathSegment.Sample(segments[0].start, segments[0].speed)
        }

        for (seg in segments) {
            val dist = posInverse.translateBy(seg.end).norm()

            if (dist >= lookaheadDist) {
                val intersectionPoint = getFirstCircleSegmentIntersection(seg, pos, lookaheadDist)

                if (intersectionPoint != null) {
                    return PathSegment.Sample(intersectionPoint, seg.speed)
                } else {
                    println("ERROR: No Intersection point?")
                }
            }
        }

        val lastSegment = segments[segments.size - 1]
        val newLastSegment = PathSegment(lastSegment.start, lastSegment.interpolate(10000.0), lastSegment.speed)

        val intersectionPoint = getFirstCircleSegmentIntersection(newLastSegment, pos, lastSegment.speed)

        when {
            intersectionPoint != null -> return PathSegment.Sample(intersectionPoint, lastSegment.speed)
            else -> {
                println("ERROR: No intersection point anywhere?")
                return PathSegment.Sample(lastSegment.end, lastSegment.speed)
            }
        }
    }

    companion object {
        fun getFirstCircleSegmentIntersection(segment: PathSegment, center: Translation2D, radius: Double): Translation2D? {
            val x1 = segment.start.x - center.x
            val y1 = segment.start.y - center.y
            val x2 = segment.end.x - center.x
            val y2 = segment.end.y - center.y

            val deltaX = x2 - x1
            val deltaY = y2 - y1

            val deltaRSquared = deltaX * deltaX + deltaY * deltaY
            val determinant = x1 * y2 - x2 * y1

            val discriminant = deltaRSquared * radius * radius - determinant * determinant

            if (discriminant < 0) {
                return null
            }

            val sqrtDisc = sqrt(discriminant)

            val posSolution = Translation2D(
                    ((determinant * deltaY + (if (deltaY < 0) -1 else 1) * deltaX * sqrtDisc) / deltaRSquared + center.x),
                    ((-determinant * deltaX + abs(deltaY) * sqrtDisc) / deltaRSquared * center.y)
            )

            val negSolution = Translation2D(
                    ((determinant * deltaY - (if (deltaY < 0) -1 else 1) * deltaX * sqrtDisc) / deltaRSquared + center.x),
                    ((-determinant * deltaX - abs(deltaY) * sqrtDisc) / deltaRSquared * center.y)
            )

            val posDotProduct = segment.dotProduct(posSolution)
            val negDotProduct = segment.dotProduct(negSolution)

            if ((posDotProduct < 0).and(negDotProduct >= 0)) {
                return negSolution
            } else if ((posDotProduct >= 0).and(negDotProduct < 0)) {
                return posSolution
            } else {
                if (abs(posDotProduct) <= abs(negDotProduct)) {
                    return posSolution
                } else {
                    return negSolution
                }
            }
        }
    }
}
