package jetson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import jetson.net.RobotMode
import jetson.net.Server
import jetson.vision.Vision
import jetson.vision.pathfinding.Path

import org.opencv.core.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import java.util.logging.Logger

object Main {
    init {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }

    fun main(args: Array<String>) {
        Logger.getLogger("Main Thread").info("Starting main thread")
        Thread {
            Server.run()
        }.start()

        Thread { Vision.run() }.start()

        Logger.getLogger("Main Thread").info("Loading path from path.json")

        lateinit var jsonPath: JsonObject
        lateinit var path: Path

        var doOnce = false
        while (true) {
            if (Server.isConnected) {
                when {
                    Server.currentMode() == RobotMode.DISABLED -> {

                    }

                    Server.currentMode() == RobotMode.AUTO -> {
                        if (doOnce.not()) {
                            doOnce = true
                        }
                    }

                    Server.currentMode() == RobotMode.TELEOP -> {

                    }
                }
            }
        }
    }
}
