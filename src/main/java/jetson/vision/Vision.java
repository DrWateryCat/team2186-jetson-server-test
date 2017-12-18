package jetson.vision;

import com.google.gson.JsonObject;
import jetson.net.Server;
import jetson.vision.odometry.RobotPoseEstimator;

public class Vision {
    private static Vision ourInstance = new Vision();

    public static Vision getInstance() {
        return ourInstance;
    }

    private Vision() {
    }

    public void run() {
        new Thread(RobotPoseEstimator.INSTANCE::run);
        while (true) {
            if (Server.getInstance().isConnected()) {
            }
        }
    }
}
