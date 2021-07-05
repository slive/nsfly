package slive.nsfly.transport.httpx.common;

import slive.nsfly.transport.inter.conn.ConnType;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 2:01 下午
 */
public interface HttpXConstants {

    /**
     * httpx类型，支持http和http扩展的如（websocket）
     */
    ConnType CONN_HTTPX = ConnType.createConnType("httpx");

    /**
     * http类型
     */
    ConnType CONN_HTTP = ConnType.createConnType("http");

    /**
     * websocket类型
     */
    ConnType CONN_WS = ConnType.createConnType("ws");
}
