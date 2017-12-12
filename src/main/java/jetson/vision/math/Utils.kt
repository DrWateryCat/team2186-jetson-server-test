package jetson.vision.math

object Utils {
    fun epsilonEquals(a: Double, b: Double, epsilon: Double): Boolean {

        return a - epsilon <= b && a + epsilon >= b

    }
}
