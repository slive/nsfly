package slive.nsfly.transport.inter.conn;

import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.monitor.ConnStatis;

import java.net.SocketAddress;

/**
 * 描述：<pre>
 *     通信的connect连接，主要是包括读的处理和写的处理
 *
 * @author Slive
 * @date 2021/6/10 8:35 上午
 */
public interface Conn extends SimpleMap<Object> {

    /**
     * 获取父对象
     *
     * @return 返回父对象
     */
    <T extends Object> T getParent();

    /**
     * 获取ConnHandler
     *
     * @return
     */
    ConnHandler getHandler();

    /**
     * 设置ConnHandler，不可为空
     *
     * @return
     */
    void setHandler(ConnHandler handler);

    /**
     * 获取通信相关配置
     *
     * @return
     */
    <C extends Object> C getConf();

    /**
     * 设置通信相关配置
     *
     * @param conf
     */
    void setConf(Object conf);

    /**
     * 获取统计相关信息
     *
     * @return
     */
    ConnStatis getStatis();

    /**
     * 获取通信类型
     *
     * @return
     */
    ConnType getType();

    /**
     * 是否是服务端生成的conn连接
     *
     * @return
     */
    boolean isServer();

    /**
     * 是否已经连接成功，连接成功后，可以收发信息
     *
     * @return
     */
    boolean isConnected();

    /**
     * conn连接的唯一id
     *
     * @return
     */
    String getId();

    /**
     * 立即释放连接
     */
    void release();

    /**
     * 同步写消息，等待回复
     *
     * @param msg 消息体
     * @return 成功与否
     */
    <M extends Object> boolean writeSyn(M msg);

    /**
     * 异步写，不回调
     *
     * @param msg 消息体
     */
    <M extends Object> void writeAsyn(M msg);

    /**
     * 异步写，支持回调
     *
     * @param msg 消息体
     */
    <M extends Object> void writeAsyn(M msg, ConnListener listener);

    /**
     * 写ping，一般要求对端回复pong
     */
    void writePing();

    /**
     * 写pong，一般是收到ping后的回复
     */
    void writePong();

    SocketAddress getLocalAddr();

    SocketAddress getRemoteAddr();
}
