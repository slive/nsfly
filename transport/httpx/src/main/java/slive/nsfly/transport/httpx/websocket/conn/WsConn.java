package slive.nsfly.transport.httpx.websocket.conn;

import io.netty.channel.Channel;
import slive.nsfly.transport.tcp.conn.TcpConn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/23 8:50 上午
 */
public interface WsConn extends TcpConn {

    void setChannel(Channel channel);
}
