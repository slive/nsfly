package slive.nsfly.transport.inter.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpUtil;
import slive.nsfly.common.constant.CommonConstants;
import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.common.util.StringUtils;
import slive.nsfly.transport.inter.conn.ConnType;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/15 10:45 上午
 */
public class InterUtils {

    private static String IP_SHORT = IpAddressUtils.getShortIp();

    public static byte[] revData(Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int revLen = byteBuf.readableBytes();
            byte[] data = new byte[revLen];
            byteBuf.readBytes(data);
            return data;
        }
        return null;
    }

    public static String revText(Object msg) {
        String charset = null;
        if (msg instanceof HttpMessage) {
            Charset chr = HttpUtil.getCharset((HttpMessage) msg);
            if (chr != null) {
                charset = chr.displayName();
            }
        }
        return revText(msg, charset);
    }

    public static String revText(Object msg, String charset) {
        if (msg instanceof HttpContent){
            msg = ((HttpContent) msg).content();
        }
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            int revLen = byteBuf.readableBytes();
            byte[] data = new byte[revLen];
            byteBuf.readBytes(data);
            if (StringUtils.isBlank(charset)) {
                charset = CommonConstants.CHARSET_NAME_DEFAULT;
            }
            try {
                return new String(data, charset);
            } catch (UnsupportedEncodingException e) {
                return new String(data);
            }
        }
        return null;
    }

    /**
     * 补齐path，如"user/login"->"/user/login"
     *
     * @param path
     * @return
     */
    public static String completePath(String path) {
        if (StringUtils.isBlank(path)) {
            return "/";
        }
        if (path.indexOf("/") != 0) {
            path = "/" + path.trim();
        }
        return path;
    }

    public static String getChannelId(ChannelHandlerContext ctx) {
        return IP_SHORT + "#" + ctx.channel().id().asShortText();
    }

    public static String getConnId(ChannelHandlerContext ctx, ConnType type) {
        return getConnId(ctx.channel(), type);
    }

    public static String getConnId(Channel channel, ConnType type) {
        return type.getValue() + "#" + IP_SHORT + "#" + channel.id().asShortText();
    }
}
