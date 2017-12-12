package jetson.vision.pathfinding

class Lookahead(val minDistance: Double, val maxDistance: Double, val minSpeed: Double, val maxSpeed: Double) {
    val deltaX = maxDistance - minDistance
    val deltaV = maxSpeed - minSpeed

    fun getLookaheadForSpeed(speed: Double): Double{
        val lookahead = deltaX * (speed - minSpeed) / deltaV + minDistance
        if (lookahead == Double.NaN) {
            return minDistance
        } else {
            return Math.max(minDistance, Math.min(maxDistance, lookahead))
        }
    }
}