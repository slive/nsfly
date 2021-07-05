package slive.nsfly.transport.inter.socket.server;

import slive.nsfly.transport.inter.socket.conf.SocketConf;

/**
 * 描述：<pre>
 *     服务端通用配置接口
 *
 * @author Slive
 * @date 2021/6/11 8:20 上午
 */
public interface ServerSocketConf extends SocketConf {

    /**
     * 获取子连接配置
     *
     * @return
     */
    <T extends Object> T getChildConf();

    /**
     * 获取最大子连接数，-1则不限制
     *
     * @return
     */
    int getMaxChildrenSize();

    /**
     * 设置最大子连接数，-1则不限制
     *
     * @return
     */
    void setMaxChildrenSize(int maxChildrenSize);

    /**
     * 获取主线程数
     *
     * @return
     */
    int getBossThreads();

    /**
     * 设置主线程数
     *
     * @param bossThreads
     */
    void setBossThreads(int bossThreads);
}
