package slive.nsfly.transport.httpx.websocket.client;

import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.websocket.conf.WsClientConf;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketConfImpl;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/13 9:03 上午
 */
public class WsClientSocketConfImpl extends TcpClientSocketConfImpl implements WsClientSocketConf {

    public WsClientSocketConfImpl(String ip, int port) {
        super(ip, port, HttpXConstants.CONN_WS, new WsClientConf());
    }

    public WsClientSocketConfImpl(String ip, int port, WsClientConf extConf) {
        super(ip, port, HttpXConstants.CONN_WS, extConf);
    }

    @Override
    public WsClientConf getExtConf() {
        return (WsClientConf) super.getExtConf();
    }

    @Override
    public String getFullAddr() {
        String scheme = null;
        String fullPath = "ws";
        WsClientConf extConf = getExtConf();
        if (extConf != null) {
            scheme = extConf.getEncryptFile() == null ? "ws" : "wss";
            fullPath = extConf.getPath();
        }

        if (fullPath != null) {
            return scheme + "://" + getIp() + ":" + getPort() + fullPath;
        }
        return scheme + "://" + getIp() + ":" + getPort();
    }
}
