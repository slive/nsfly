package slive.nsfly.transport.httpx.server.socket;

import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.http.conf.HttpConf;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.tcp.socket.server.TcpServerSocketConfImpl;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 1:51 下午
 */
public class HttpXServerSocketConfImpl extends TcpServerSocketConfImpl implements HttpXServerSocketConf {

    // 默认child配置
    private static final HttpConf CHILD_CONF = new HttpConf();

    public HttpXServerSocketConfImpl(String ip, int port) {
        // 默认httpx
        super(ip, port, HttpXConstants.CONN_HTTPX, CHILD_CONF);
    }

    public HttpXServerSocketConfImpl(String ip, int port, ConnType connType) {
        super(ip, port, connType, CHILD_CONF);
    }

    public HttpXServerSocketConfImpl(String ip, int port, HttpConf childConf) {
        // 默认httpx
        super(ip, port, HttpXConstants.CONN_HTTPX, childConf);
    }

    public HttpXServerSocketConfImpl(String ip, int port, String connType, HttpConf childConf) {
        super(ip, port, connType, childConf);
    }

    public HttpXServerSocketConfImpl(String ip, int port, ConnType connType, HttpConf childConf) {
        super(ip, port, connType, childConf);
    }
}
