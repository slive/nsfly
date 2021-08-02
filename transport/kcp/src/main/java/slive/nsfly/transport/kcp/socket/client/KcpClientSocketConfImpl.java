package slive.nsfly.transport.kcp.socket.client;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.client.BaseClientSocketConf;
import slive.nsfly.transport.kcp.common.KcpConstants;
import slive.nsfly.transport.kcp.conf.KcpConf;

/**
 * 描述：<pre>
 *     kcp客户端socket相关配置的实现
 *
 * @author Slive
 * @date 2021/7/15 8:25 上午
 */
public class KcpClientSocketConfImpl extends BaseClientSocketConf implements KcpClientSocketConf {

    private KcpConf extConf = null;

    public KcpClientSocketConfImpl(String ip, int port, ConnType connType) {
        super(ip, port, connType);
        this.extConf = new KcpConf();
    }

    public KcpClientSocketConfImpl(String ip, int port, ConnType connType, KcpConf extConf) {
        super(ip, port, connType);
        this.extConf = extConf;
    }

    public KcpClientSocketConfImpl(String ip, int port) {
        super(ip, port, KcpConstants.CONN_KCP);
        this.extConf = new KcpConf();
    }

    public KcpClientSocketConfImpl(String ip, int port, KcpConf extConf) {
        super(ip, port, KcpConstants.CONN_KCP);
        this.extConf = extConf;
    }

    @Override
    public KcpConf getExtConf() {
        return extConf;
    }
}
