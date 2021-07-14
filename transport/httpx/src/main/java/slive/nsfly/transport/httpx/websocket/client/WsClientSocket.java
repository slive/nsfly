package slive.nsfly.transport.httpx.websocket.client;

import slive.nsfly.transport.tcp.socket.clent.TcpClientSocket;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/7 6:06 下午
 */
public interface WsClientSocket<C extends WsClientSocketConf> extends TcpClientSocket<C> {

    void addHeader(String name, Object value);
}

