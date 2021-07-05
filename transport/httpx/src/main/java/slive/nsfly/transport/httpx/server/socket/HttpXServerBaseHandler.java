package slive.nsfly.transport.httpx.server.socket;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameHandler;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.BaseConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnExtHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.conn.handler.frame.HandshakeContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/28 8:58 上午
 */
public class HttpXServerBaseHandler extends BaseConnHandler {

    @Override
    public void preWrite(ConnHandlerContext ctx) {

    }

    @Override
    public void onConnect(ConnHandlerContext ctx) {
        Conn conn = ctx.getConn();
        ConnType connType = conn.getType();
        if (HttpXConstants.CONN_WS.equals(connType)) {
            HandshakeContext handshakeContext = ctx.getAttach();
            WsFrameHandler wsFrameHandler = getPatternHandler(conn);
            if (wsFrameHandler != null) {
                wsFrameHandler.onHandshake(ctx, null, handshakeContext);
            }
        }
    }

    @Override
    public void onRead(ConnHandlerContext ctx) {
        Conn conn = ctx.getConn();
        ConnType connType = conn.getType();
        if (HttpXConstants.CONN_WS.equals(connType)) {
            WebSocketFrame wsFrame = ctx.getAttach();
            WsFrameHandler wsFrameHandler = getPatternHandler(conn);
            if (wsFrameHandler != null) {
                wsFrameHandler.onReadFrame(ctx, wsFrame);
            }
        } else {
            // TODO
        }
    }

    @Override
    public void onRelease(ConnHandlerContext ctx) {
        Conn conn = ctx.getConn();
        ConnType connType = conn.getType();
        if (HttpXConstants.CONN_WS.equals(connType)) {
            WebSocketFrame wsFrame = ctx.getAttach();
            WsFrameHandler wsFrameHandler = removePatternHandler(conn);
            if (wsFrameHandler != null) {
                wsFrameHandler.onBye(ctx, wsFrame);
            }
        } else {
            // TODO
        }
        super.onRelease(ctx);
    }
}
