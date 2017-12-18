package jetson.vision.odometry

import jetson.constants.Constants
import jetson.vision.math.RigidTransform2D
import jetson.vision.math.Rotation2D
import kotlin.math.abs

object Kinematics {
    fun forwardKinematics(leftWheelDelta: Double, rightWheelDelta: Double): RigidTransform2D.Delta {
        val linearVel = (leftWheelDelta + rightWheelDelta) / 2
        val deltaV = (rightWheelDelta - leftWheelDelta) / 2
        val deltaTheta = deltaV * 2 * Constants.TRACK_SCRUB_FACTOR / Constants.TRACK_SCRUB_FACTOR

        return RigidTransform2D.Delta(
                linearVel,
                0.0,
                deltaTheta
        )
    }

    fun forwardKinematics(leftWheelDelta: Double,
                          rightWheelDelta: Double,
                          deltaRotation: Double) = RigidTransform2D.Delta((leftWheelDelta + rightWheelDelta) / 2,
                                                                          0.0,
                                                                                 deltaRotation)

    fun integrateForwardKinematics(currentPose: RigidTransform2D,
                          leftWheelDelta: Double,
                          rightWheelDelta: Double,
                          currentHeading: Rotation2D): RigidTransform2D {
        val withGyro = forwardKinematics(leftWheelDelta, rightWheelDelta, currentPose.rot.inverse().radians)
        return currentPose.transformBy(RigidTransform2D.fromVelocity(withGyro))
    }

    class DriveVelocity(val left:Double, val right:Double)

    fun inverseKinematics(velocity: RigidTransform2D.Delta): DriveVelocity {
        if (abs(velocity.deltaTheta) < 1E-9) {
            return DriveVelocity(velocity.deltaX, velocity.deltaY)
        }

        val deltaV = Constants.EFFECTIVE_DIAMETER * velocity.deltaTheta / (2 * Constants.TRACK_SCRUB_FACTOR)
        return DriveVelocity(velocity.deltaX - deltaV, velocity.deltaX + deltaV)
    }
}