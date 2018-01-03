package jetson.vision.math

object Utils {
    fun epsilonEquals(a: Double, b: Double, epsilon: Double): Boolean = a - epsilon <= b && a + epsilon >= b
}
