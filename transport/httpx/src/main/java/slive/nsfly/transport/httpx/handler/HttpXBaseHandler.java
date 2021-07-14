package slive.nsfly.transport.httpx.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.http.handler.HttpServerHandler;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameHandler;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.BaseConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.conn.handler.frame.HandshakeContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/28 8:58 上午
 */
public class HttpXBaseHandler extends BaseConnHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpXBaseHandler.class);

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
        long currentTimeMillis = System.currentTimeMillis();
        Conn conn = ctx.getConn();
        ConnType connType = conn.getType();
        if (log.isDebugEnabled()) {
            log.debug("start to handle {} message, connId:{}", connType, conn.getId());
        }
        try {
            if (HttpXConstants.CONN_WS.equals(connType)) {
                WsFrameHandler wsFrameHandler = getPatternHandler(conn);
                if (wsFrameHandler != null) {
                    WebSocketFrame wsFrame = ctx.getAttach();
                    wsFrameHandler.onReadFrame(ctx, wsFrame);
                }
            } else if (HttpXConstants.CONN_HTTP.equals(connType)) {
                // TODO
                HttpServerHandler httpServerHandler = getPatternHandler(conn);
                if (httpServerHandler != null) {
                    FullHttpResponse response = httpServerHandler.onHandle(ctx, ctx.getAttach());
                    ctx.setRet(response);
                }
            }
        } finally {
            log.info("finish handle {} message, connId:{}, spendTime:{}", connType, conn.getId(), (System.currentTimeMillis() - currentTimeMillis));
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
