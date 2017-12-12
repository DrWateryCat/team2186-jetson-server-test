package jetson.vision.math

class Twist2D {
    companion object {
        protected val kIdentity: Twist2D = Twist2D(0.0, 0.0, 0.0)
        fun identity(): Twist2D = kIdentity
    }

    val dX: Double
    val dY: Double
    val dTheta: Double

    constructor(x: Double, y: Double, theta: Double) {
        dX = x
        dY = y
        dTheta = theta
    }

    fun scaled(s: Double): Twist2D = Twist2D(dX * s, dY * s, dTheta * s)
}