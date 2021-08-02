package slive.nsfly.transport.kcp.socket.server;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.server.BaseServerSocketConf;
import slive.nsfly.transport.kcp.common.KcpConstants;
import slive.nsfly.transport.kcp.conf.KcpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/15 9:28 上午
 */
public class KcpServeSocketConfImpl extends BaseServerSocketConf implements KcpServerScoketConf {

    public KcpServeSocketConfImpl(String ip, int port, ConnType connType) {
        super(ip, port, connType);
        setChildConf(KcpConf.createServerNomal());
    }

    public KcpServeSocketConfImpl(String ip, int port, ConnType connType, KcpConf childConf) {
        super(ip, port, connType, childConf);
    }

    public KcpServeSocketConfImpl(String ip, int port) {
        super(ip, port, KcpConstants.CONN_KCP);
        setChildConf(KcpConf.createServerNomal());
    }

    public KcpServeSocketConfImpl(String ip, int port, KcpConf childConf) {
        super(ip, port, KcpConstants.CONN_KCP, childConf);
    }

    @Override
    public KcpConf getChildConf() {
        return super.getChildConf();
    }
}
