package jetson.vision

import com.google.gson.JsonObject
import jetson.constants.Constants
import jetson.net.Server
import jetson.vision.math.RigidTransform2D
import jetson.vision.odometry.FramesOfReference
import jetson.vision.odometry.RobotPoseEstimator
import jetson.vision.pathfinding.Path
import jetson.vision.pathfinding.PurePursuitController

import java.util.logging.Logger

object Vision{

    private val m_path: Path? = null
    private var pathFollower: PurePursuitController? = null

    fun followPath(path: Path, reversed: Boolean) {
        pathFollower = PurePursuitController(Constants.LOOKAHEAD,
                Constants.MAX_ACCEL,
                Constants.LOOPER_DT,
                path,
                reversed,
                0.25)
    }

    fun finishedPath(): Boolean {
        return pathFollower!!.isDone
    }

    fun run() {
        Logger.getLogger("Vision").info("Starting vision thread")
        Thread(Runnable { RobotPoseEstimator.run() }).start()
        while (!Thread.currentThread().isInterrupted) {
            if (Server.isConnected) {
                val currentPose = FramesOfReference.latestFieldToVehicle().value
                val command = pathFollower!!.update(currentPose, Server.get()
                        .asJsonObject
                        .get("timestamp")
                        .asDouble)


                val toRio = JsonObject()
                toRio.addProperty("deltaX", command.deltaX)
                toRio.addProperty("deltaY", command.deltaY)
                toRio.addProperty("deltaTheta", command.deltaTheta)

                Server.send(toRio)
            }
        }
    }
}
