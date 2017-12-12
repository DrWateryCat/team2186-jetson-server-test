package jetson.vision;

public class Vision {
    private static Vision ourInstance = new Vision();

    public static Vision getInstance() {
        return ourInstance;
    }

    private Vision() {
    }

    public void run() {
        System.out.println("Running vision");
    }
}
