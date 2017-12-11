package jetson;

import com.google.gson.JsonObject;
import jetson.net.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            try {
                Server.getInstance().run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        while (true) {
            if (Server.getInstance().isConnected()) {
                JsonObject o = new JsonObject();
                o.addProperty("message", "Hello world from Java");
                o.addProperty("current_time", System.currentTimeMillis());

                System.out.println(Server.getInstance().get().toString());
                Server.getInstance().send(o);
            }
        }
    }
}
