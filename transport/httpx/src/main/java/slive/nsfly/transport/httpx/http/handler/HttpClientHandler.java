package slive.nsfly.transport.httpx.http.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 2:56 下午
 */
public interface HttpClientHandler {

    void onSuccess(ConnHandlerContext ctx, FullHttpResponse response);

    void onFailed(ConnHandlerContext ctx);
}
