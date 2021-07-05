package slive.nsfly.transport.inter.socket.server;

import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.socket.Socket;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 8:53 下午
 */
public interface ServerSocket<C extends ServerSocketConf> extends Socket<C> {

    boolean listen();

    boolean isListen();

    SimpleMap<Conn> getChildren();

    /**
     * 可动态设置子handler， 但不允许为空
     * @param childHandler
     */
    void setChildHandler(ConnHandler childHandler);

    ConnHandler getChildHandler();
}
