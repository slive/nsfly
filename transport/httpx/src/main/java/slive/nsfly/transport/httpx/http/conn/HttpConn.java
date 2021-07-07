package slive.nsfly.transport.httpx.http.conn;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import slive.nsfly.transport.tcp.conn.TcpConn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/6 9:01 上午
 */
public interface HttpConn extends TcpConn {

    void writeResponse(FullHttpResponse response);

    void writeRequest(FullHttpRequest request);
}
