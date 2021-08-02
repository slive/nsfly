package slive.nsfly.transport.tcp.conn;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import slive.nsfly.transport.inter.conn.BaseConn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.tcp.common.TcpConstants;
import slive.nsfly.transport.tcp.common.TcpUtils;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 11:26 上午
 */
public class TcpConnImpl extends BaseConn implements TcpConn {

    public TcpConnImpl(Object parent, ConnHandler connHandler, ConnType connType, boolean server, Channel channel) {
        super(parent, connHandler, connType, channel, server);
    }

    public TcpConnImpl(Object parent, ConnHandler connHandler, ConnType connType, Channel channel) {
        super(parent, connHandler, connType, channel);
    }

    public TcpConnImpl(Object parent, ConnHandler connHandler, Channel channel) {
        super(parent, connHandler, TcpConstants.CONN_TCP, channel);
    }

    public TcpConnImpl(Object parent, ConnHandler connHandler, boolean server, Channel channel) {
        super(parent, connHandler, TcpConstants.CONN_TCP, channel, server);
    }

    @Override
    public void setChannel(Channel channel) {
        super.setChannel(channel);
        setConnected(channel.isOpen());
        setAllowWrite(channel.isOpen());
    }

    @Override
    protected void setId(Channel channel) {
        // 根据type获取id
        this.id = TcpUtils.getConnId(channel, type);
    }

    @Override
    protected void _release() {
        // 空实现
    }

    @Override
    protected WriteObject convertWriteObject(Object msg) {
       return defaultConvertWriteObject(channel, msg);
    }
}
