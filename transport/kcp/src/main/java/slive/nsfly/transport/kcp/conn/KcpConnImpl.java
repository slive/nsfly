package slive.nsfly.transport.kcp.conn;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.Channel;
import slive.nsfly.transport.inter.conn.BaseConn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.kcp.common.KcpConstants;
import slive.nsfly.transport.kcp.common.KcpUtils;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/15 9:01 上午
 */
public class KcpConnImpl extends BaseConn implements KcpConn {

    public KcpConnImpl(Object parent, ConnHandler connHandler, Channel channel, boolean server) {
        super(parent, connHandler, KcpConstants.CONN_KCP, channel, server);
    }

    public KcpConnImpl(Object parent, ConnHandler connHandler, Channel channel) {
        super(parent, connHandler, KcpConstants.CONN_KCP, channel);
    }

    public KcpConnImpl(Object parent, ConnHandler connHandler, ConnType connType, Channel channel, boolean server) {
        super(parent, connHandler, connType, channel, server);
    }

    public KcpConnImpl(Object parent, ConnHandler connHandler, ConnType connType, Channel channel) {
        super(parent, connHandler, connType, channel);
    }

    @Override
    protected void setId(Channel channel) {
        this.id = KcpUtils.getConnId((UkcpChannel) channel, getType());
    }

    @Override
    protected void _release() {

    }

    @Override
    protected WriteObject convertWriteObject(Object msg) {
        return defaultConvertWriteObject(channel, msg);
    }

    @Override
    public void setChannel(UkcpChannel channel) {
        super.setChannel(channel);
    }
}
