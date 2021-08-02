package slive.nsfly.transport.kcp.socket.client;

import slive.nsfly.transport.inter.socket.client.ClientSocket;

/**
 * 描述：<pre>
 *     kcp客户端socket通信接口
 *
 * @author Slive
 * @date 2021/7/15 8:15 上午
 */
public interface KcpClientSocket<C extends KcpClientSocketConf> extends ClientSocket<C> {

    /**
     * kcp协议中的conv
     * @return
     */
    int getConv();
}
