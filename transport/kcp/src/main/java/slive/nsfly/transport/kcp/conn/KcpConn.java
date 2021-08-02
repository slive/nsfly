package slive.nsfly.transport.kcp.conn;

import io.jpower.kcp.netty.UkcpChannel;
import slive.nsfly.transport.inter.conn.Conn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/15 8:59 上午
 */
public interface KcpConn extends Conn {
    void setChannel(UkcpChannel channel);
}
