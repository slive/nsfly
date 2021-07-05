package slive.nsfly.transport.tcp.socket.server;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.server.BaseServerSocketConf;
import slive.nsfly.transport.tcp.common.TcpConstants;
import slive.nsfly.transport.tcp.conf.TcpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 2:38 下午
 */
public class TcpServerSocketConfImpl extends BaseServerSocketConf implements TcpServerSocketConf {

    // 默认child配置
    private static final TcpConf CHILD_CONF = new TcpConf();

    public TcpServerSocketConfImpl(String ip, int port) {
        // 默认TCP协议
        super(ip, port, TcpConstants.CONN_TCP, CHILD_CONF);
    }

    public TcpServerSocketConfImpl(String ip, int port, ConnType connType) {
        // 基于TCP之上，可自定义协议
        super(ip, port, connType, CHILD_CONF);
    }

    public TcpServerSocketConfImpl(String ip, int port, TcpConf childConf) {
        // 默认TCP协议
        super(ip, port, TcpConstants.CONN_TCP, childConf);
    }

    public TcpServerSocketConfImpl(String ip, int port, String connType, TcpConf childConf) {
        // 基于TCP之上，可自定义协议
        super(ip, port, connType, childConf);
    }

    public TcpServerSocketConfImpl(String ip, int port, ConnType connType, TcpConf childConf) {
        // 基于TCP之上，可自定义协议
        super(ip, port, connType, childConf);
    }
}
