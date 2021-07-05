package slive.nsfly.transport.httpx.websocket.conn;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.*;
import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.tcp.conn.TcpConnImpl;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/23 8:46 上午
 */
public class WsConnImpl extends TcpConnImpl implements WsConn {

    public WsConnImpl(Object parent, ConnHandler connHandler, Channel channel) {
        super(parent, connHandler, HttpXConstants.CONN_WS, channel);
    }

    public WsConnImpl(Object parent, ConnHandler connHandler, boolean server, Channel channel) {
        super(parent, connHandler, HttpXConstants.CONN_WS, server, channel);
    }

    @Override
    protected WriteObject convertWriteObject(Object msg) {
        Object finalWrite = msg;
        int byteNum = 0;
        // 支持byte[]和ByteBuf方式发送
        if (msg instanceof WebSocketFrame) {
            finalWrite = msg;
            byteNum = ((WebSocketFrame) msg).content().readableBytes();
        } else if (msg instanceof byte[]) {
            int length = ((byte[]) msg).length;
            ByteBuf buffer = channel.alloc().buffer(length);
            buffer.writeBytes((byte[]) msg);
            BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
            finalWrite = frame;
            byteNum = frame.content().readableBytes();
        } else if (msg instanceof String) {
            TextWebSocketFrame frame = new TextWebSocketFrame((String) msg);
            finalWrite = frame;
            byteNum = frame.content().readableBytes();
        } else if (msg instanceof ByteBuf) {
            byteNum = ((ByteBuf) msg).readableBytes();
            BinaryWebSocketFrame frame = new BinaryWebSocketFrame((ByteBuf) msg);
            finalWrite = frame;
        } else {
            // TODO  默认写字符串
            String s = msg.toString();
            byte[] bytes = s.getBytes();
            byteNum = bytes.length;
            ByteBuf buffer = channel.alloc().buffer(byteNum);
            buffer.writeBytes(bytes);
            finalWrite = buffer;
        }

        WriteObject writeObject = new WriteObject();
        writeObject.setByteNum(byteNum);
        writeObject.setFinalMsg(finalWrite);
        return writeObject;
    }

    @Override
    public void writePing() {
        super.writeAsyn(new PingWebSocketFrame());
    }

    @Override
    public void writePong() {
        super.writeAsyn(new PongWebSocketFrame());
    }
}
