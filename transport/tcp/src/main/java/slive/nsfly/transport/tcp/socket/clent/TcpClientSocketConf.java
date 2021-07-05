package slive.nsfly.transport.tcp.socket.clent;

import slive.nsfly.transport.inter.socket.client.ClientSocketConf;
import slive.nsfly.transport.tcp.conf.TcpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/15 11:17 上午
 */
public interface TcpClientSocketConf extends ClientSocketConf {

    @Override
    TcpConf getExtConf();
}
