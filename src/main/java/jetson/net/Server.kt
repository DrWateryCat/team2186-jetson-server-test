package jetson.net

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

import java.util.logging.Logger

object Server {

    private val PORT = 5800

    private val handler = ServerHandler()

    val isConnected: Boolean
        get() = handler.isConnected

    @Throws(Exception::class)
    fun run() {
        Logger.getLogger("Server").info("Starting server thread")

        val bossGroup = NioEventLoopGroup(2)
        val workerGroup = NioEventLoopGroup(2)

        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast(handler)
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            val f = b.bind(PORT).sync()
            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

    fun get(): JsonElement {
        return handler.recieve() ?: return JsonObject()
    }

    fun send(o: JsonObject) {
        handler.send(o)
    }

    fun currentMode(): RobotMode {
        return handler.mode
    }
}
