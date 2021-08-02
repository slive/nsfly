package slive.nsfly.transport.kcp.socket.server;

import slive.nsfly.transport.inter.socket.server.ServerSocketConf;
import slive.nsfly.transport.kcp.conf.KcpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/15 9:26 上午
 */
public interface KcpServerScoketConf extends ServerSocketConf {

    @Override
    KcpConf getChildConf();
}
