package slive.nsfly.transport.tcp.conn;

import io.netty.channel.Channel;
import slive.nsfly.transport.inter.conn.Conn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 11:25 上午
 */
public interface TcpConn extends Conn {

    void setChannel(Channel channel);
}
