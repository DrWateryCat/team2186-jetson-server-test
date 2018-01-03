package jetson.vision.math

class Twist2D(val dX: Double, val dY: Double, val dTheta: Double) {
    companion object {
        protected val kIdentity: Twist2D = Twist2D(0.0, 0.0, 0.0)
        fun identity(): Twist2D = kIdentity
    }
    fun scaled(s: Double): Twist2D = Twist2D(dX * s, dY * s, dTheta * s)
}