package jetson.vision.odometry

import jetson.net.Server
import jetson.vision.math.Rotation2D

object RobotPoseEstimator {
    var leftEncoderPrev = 0.0
    var rightEncoderPrev = 0.0

    fun run() {
        while(true) {
            if (Server.getInstance().isConnected) {
                val input = Server.getInstance().get().asJsonObject

                val left = input["leftDistance"].asDouble
                val right = input["rightDistance"].asDouble

                val time = input["timestamp"].asDouble

                val currentGyro = Rotation2D.fromDegrees(input["gyroAngle"].asDouble)

                val leftInchesPerSec = input["leftVelocity"].asDouble
                val rightInchesPerSec = input["rightVelocity"].asDouble

                val odometry = FramesOfReference.generateFromSensors(
                                                                    left - leftEncoderPrev,
                                                                    right - rightEncoderPrev,
                                                                                    currentGyro)

                val velocity = Kinematics.forwardKinematics(leftInchesPerSec, rightInchesPerSec)
                FramesOfReference.addObservations(time, odometry, velocity)
                leftEncoderPrev = left
                rightEncoderPrev = right
            }
        }
    }
}