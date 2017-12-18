package jetson;

import com.google.gson.JsonObject;
import jetson.net.Server;
import jetson.vision.Vision;
import jetson.vision.pathfinding.MotionData;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Server.getInstance().run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            Vision.getInstance().run();
        }).start();

        while (true) {
            if (Server.getInstance().isConnected()) {
            }
        }
    }
}
