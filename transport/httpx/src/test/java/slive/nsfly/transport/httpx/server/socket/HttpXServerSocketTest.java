package slive.nsfly.transport.httpx.server.socket;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.transport.httpx.common.HttpXUtils;
import slive.nsfly.transport.httpx.http.handler.HttpHandshakeContext;
import slive.nsfly.transport.httpx.http.handler.HttpServerHandler;
import slive.nsfly.transport.httpx.websocket.conf.WsServerConf;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameBaseHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/2 9:26 上午
 */
public class HttpXServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(HttpXServerSocketTest.class);

    public static void main(String[] args) throws InterruptedException {

        String localIp = IpAddressUtils.getLocalIp();
        int port = 8880;
        WsServerConf wsConf = new WsServerConf();
        HttpXServerSocketConfImpl httpXServerSocketConf = new HttpXServerSocketConfImpl(localIp, port, wsConf);
        HttpXServerSocket ss = new HttpXServerSocketImpl(httpXServerSocketConf);
        ss.addWsFrameHanlder("/my/ws", new WsFrameBaseHandler() {
            @Override
            protected void onReadText(ConnHandlerContext ctx, TextWebSocketFrame txtframe) {
                log.info("websocket rev text connId:{}, msg:{}", ctx.getConnId(), txtframe.text());
            }

            @Override
            protected void onReadBinary(ConnHandlerContext ctx, BinaryWebSocketFrame binFrame) {
                log.info("websocket rev binary, connId:{}, size:{}", ctx.getConnId(), binFrame.content().readableBytes());
            }
        });

        ss.addHttpHanlder("/my/http", new HttpServerHandler() {
            @Override
            public FullHttpResponse onHandle(ConnHandlerContext ctx, HttpHandshakeContext handshakeCtx) {
                FullHttpRequest request = handshakeCtx.getRequest();
                String rspMsg = "answer";
                log.info("http connId:{}, reqMsg:{}, respMsg:{}", ctx.getConnId(), HttpXUtils.revText(request.content()), rspMsg);
                return HttpXUtils.createDefaultResponse(rspMsg);
            }
        });
        boolean listen = ss.listen();
        log.info("start listen:{}", listen);
        while (listen) {
            synchronized (ss) {
                ss.wait();
            }
        }
    }
}
