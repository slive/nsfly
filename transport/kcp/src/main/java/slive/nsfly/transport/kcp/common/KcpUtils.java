package slive.nsfly.transport.kcp.common;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.ChannelHandlerContext;
import slive.nsfly.transport.inter.conn.ConnType;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/15 8:26 上午
 */
public class KcpUtils {

    private static final long MASK_UNSIGN = 0xffffffffL;

    public static String getConnId(ChannelHandlerContext ctx, ConnType connType) {
        UkcpChannel channel = ((UkcpChannel) ctx.channel());
        return getConnId(channel, connType);
    }

    public static String getConnId(UkcpChannel channel, ConnType connType) {
        // 保证为正数
        return connType.getValue() + "#" + channel.id().asShortText() + "#" + (channel.conv() & MASK_UNSIGN);
    }
}
