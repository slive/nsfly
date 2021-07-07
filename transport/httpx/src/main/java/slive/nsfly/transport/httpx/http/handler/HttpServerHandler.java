package slive.nsfly.transport.httpx.http.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import slive.nsfly.transport.inter.conn.handler.ConnExtHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 2:34 下午
 */
public interface HttpServerHandler extends ConnExtHandler {

    FullHttpResponse onHandle(ConnHandlerContext ctx, HttpHandshakeContext handshakeCtx);
}
