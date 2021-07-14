package slive.nsfly.transport.httpx.client.socket.websocket;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.transport.httpx.websocket.client.WsClientSocket;
import slive.nsfly.transport.httpx.websocket.client.WsClientSocketConf;
import slive.nsfly.transport.httpx.websocket.client.WsClientSocketConfImpl;
import slive.nsfly.transport.httpx.websocket.client.WsClientSocketImpl;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameBaseHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/14 8:27 上午
 */
public class WsClientSocketTest {
    private static final Logger log = LoggerFactory.getLogger(WsClientSocketTest.class);

    public static void main(String[] args) {
        WsClientSocketConf wsClientSocketConf = new WsClientSocketConfImpl(IpAddressUtils.getLocalIp(), 8880);
        wsClientSocketConf.getExtConf().setPath("/my/ws");
        WsClientSocket ws = new WsClientSocketImpl(wsClientSocketConf, new WsFrameBaseHandler() {
            @Override
            protected void onReadText(ConnHandlerContext ctx, TextWebSocketFrame txtframe) {
                log.info("wsframe...");
            }

            @Override
            protected void onReadBinary(ConnHandlerContext ctx, BinaryWebSocketFrame binFrame) {

            }
        });
        boolean dial = ws.dial();

        while (dial) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ws.getConn().writePing();
        }
    }
}
