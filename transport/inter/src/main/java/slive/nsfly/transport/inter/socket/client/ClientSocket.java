package slive.nsfly.transport.inter.socket.client;

import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.socket.Socket;

import java.util.Map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 9:41 上午
 */
public interface ClientSocket<C extends ClientSocketConf> extends Socket<C> {

    /**
     * 获取conn连接
     *
     * @return
     */
    Conn getConn();

    /**
     * 拨号连接，需初始化conn
     *
     * @param dialParams
     * @return
     */
    boolean dial(Map<String, Object> dialParams);

    /**
     * 拨号连接，需初始化conn
     *
     * @return
     */
    boolean dial();

    /**
     * 获取handler配置
     *
     * @return
     */
    ConnHandler getHandler();

    /**
     * 支持动态修改handler
     *
     * @param handler
     */
    void setHandler(ConnHandler handler);

    /**
     * 是否已经打开
     *
     * @return
     */
    boolean isOpen();
}
