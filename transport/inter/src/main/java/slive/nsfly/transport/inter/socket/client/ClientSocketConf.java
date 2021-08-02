package slive.nsfly.transport.inter.socket.client;

import slive.nsfly.transport.inter.socket.conf.SocketConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 9:42 上午
 */
public interface ClientSocketConf extends SocketConf {

    /**
     * 获取是否空闲时发送ping包，默认否
     *
     * @return
     */
    boolean isPingOnIdle();

    /**
     * 设置是否空闲时发送ping包，默认否
     *
     * @return
     */
    void setPingOnIdle(boolean pingOnIdle);

    /**
     * 获取扩展的配置
     *
     * @return
     */
    <E extends Object> E getExtConf();
}
