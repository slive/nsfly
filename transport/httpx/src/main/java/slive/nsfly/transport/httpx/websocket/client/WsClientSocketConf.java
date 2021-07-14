package slive.nsfly.transport.httpx.websocket.client;

import slive.nsfly.transport.httpx.websocket.conf.WsClientConf;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/7 6:21 下午
 */
public interface WsClientSocketConf extends TcpClientSocketConf {

    @Override
    WsClientConf getExtConf();

    String getFullAddr();
}
