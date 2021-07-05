package slive.nsfly.transport.tcp.socket.clent;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.client.BaseClientSocketConf;
import slive.nsfly.transport.tcp.common.TcpConstants;
import slive.nsfly.transport.tcp.conf.TcpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/15 10:11 下午
 */
public class TcpClientSocketConfImpl extends BaseClientSocketConf implements TcpClientSocketConf {

    // 默认child配置
    private static final TcpConf CHILD_CONF = new TcpConf();

    public TcpClientSocketConfImpl(String ip, int port) {
        super(ip, port, TcpConstants.CONN_TCP);
        setExtConf(CHILD_CONF);
    }

    public TcpClientSocketConfImpl(String ip, int port, ConnType connType) {
        super(ip, port, connType);
        setExtConf(CHILD_CONF);
    }

    public TcpClientSocketConfImpl(String ip, int port, TcpConf extConf) {
        super(ip, port, TcpConstants.CONN_TCP);
        setExtConf(extConf);
    }

    public TcpClientSocketConfImpl(String ip, int port, String connType, TcpConf extConf) {
        super(ip, port, connType);
        setExtConf(extConf);
    }

    public TcpClientSocketConfImpl(String ip, int port, ConnType connType, TcpConf extConf) {
        super(ip, port, connType);
        setExtConf(extConf);
    }

    @Override
    public TcpConf getExtConf() {
        return super.getExtConf();
    }
}
