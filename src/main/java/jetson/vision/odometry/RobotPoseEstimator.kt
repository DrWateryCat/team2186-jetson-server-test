package jetson.vision.odometry

import jetson.net.Server
import jetson.vision.math.Rotation2D
import java.util.logging.Logger

object RobotPoseEstimator {
    var leftEncoderPrev = 0.0
    var rightEncoderPrev = 0.0

    fun run() {
        Logger.getLogger("RobotPoseEstimator").info("Starting Pose Estimation thread")
        while(Thread.interrupted().not()) {
            if (Server.isConnected) {
                val input = Server.get().asJsonObject

                val left = input["left_distance"].asDouble
                val right = input["right_distance"].asDouble

                val time = input["timestamp"].asDouble

                val currentGyro = Rotation2D.fromDegrees(input["gyro_angle"].asDouble)

                val leftInchesPerSec = input["left_velocity"].asDouble
                val rightInchesPerSec = input["right_velocity"].asDouble

                val odometry = FramesOfReference.generateFromSensors(
                    left - leftEncoderPrev,
                    right - rightEncoderPrev,
                    currentGyro)

                val velocity = Kinematics.forwardKinematics(
                    leftInchesPerSec,
                    rightInchesPerSec
                )
                FramesOfReference.addObservations(time, odometry, velocity)
                leftEncoderPrev = left
                rightEncoderPrev = right
            }
        }
    }
}