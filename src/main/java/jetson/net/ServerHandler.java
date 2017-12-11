package jetson.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.CharsetUtil;
import kotlin.jvm.internal.Ref;

import java.nio.charset.Charset;

@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private JsonElement currentInput;
    private JsonObject currentOutput = new JsonObject();

    private final Object lock = new Object();

    private boolean connected = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        JsonObject out;

        synchronized (lock) {
            currentInput = new JsonParser().parse(buf.toString(CharsetUtil.US_ASCII));
            out = currentOutput;

            connected = true;
        }

        final ByteBuf s = ctx.alloc().buffer();
        s.writeBytes((out.toString() + '\n').getBytes(Charset.forName("UTF-8")));

        final ChannelFuture f = ctx.writeAndFlush(s);
        f.addListener((ChannelFutureListener) future -> {
            assert f == future;
            synchronized (lock) {
                connected = false;
            }
            //buf.release();
            //s.release();
            //ctx.close();
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public JsonElement recieve() {
        synchronized (lock) {
            return currentInput;
        }
    }

    public boolean isConnected() {
        synchronized (lock) {
            return connected;
        }
    }

    public void send(JsonObject o) {
        synchronized (lock) {
            currentOutput = o;
        }
    }
}
