package slive.nsfly.transport.kcp.socket.client;

import slive.nsfly.transport.inter.socket.client.ClientSocketConf;
import slive.nsfly.transport.kcp.conf.KcpConf;

/**
 * 描述：<pre>
 *     kcp客户端socket相关配置
 *
 * @author Slive
 * @date 2021/7/15 8:21 上午
 */
public interface KcpClientSocketConf extends ClientSocketConf {

    @Override
    KcpConf getExtConf();
}
