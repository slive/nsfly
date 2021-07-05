package slive.nsfly.transport.inter.socket;

import slive.nsfly.transport.inter.socket.conf.SocketConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/11 8:29 上午
 */
public interface Socket<C extends SocketConf> {

    <T> T getParent();

    String getId();

    void close();

    C getConf();

    /**
     * 是否是服务端
     *
     * @return
     */
    boolean isServer();
}
