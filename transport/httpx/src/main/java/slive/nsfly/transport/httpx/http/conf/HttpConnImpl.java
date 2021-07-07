package slive.nsfly.transport.httpx.http.conf;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.common.HttpXUtils;
import slive.nsfly.transport.httpx.http.conn.HttpConn;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.tcp.conn.TcpConnImpl;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/6 9:07 上午
 */
public class HttpConnImpl extends TcpConnImpl implements HttpConn {

    private static final Logger log = LoggerFactory.getLogger(HttpConnImpl.class);

    public HttpConnImpl(Object parent, ConnHandler connHandler, Channel channel) {
        super(parent, connHandler, HttpXConstants.CONN_HTTP, channel);
    }

    public HttpConnImpl(Object parent, ConnHandler connHandler, boolean server, Channel channel) {
        super(parent, connHandler, HttpXConstants.CONN_HTTP, server, channel);
    }

    @Override
    public void writeResponse(FullHttpResponse response) {
        if (response != null && response.refCnt() > 0) {
            HttpXUtils.wapperDefaultHeaders(response);
            ChannelFuture channelFuture = null;
            if (HttpUtil.isTransferEncodingChunked(response)) {
                channel.write(response);
                // 针对chunked等，需要一个写结束标识
                channelFuture = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                channelFuture = channel.writeAndFlush(response);
            }

            // 判断是否要保留链接，http的有些链接是可以复用的
            if (!HttpUtil.isKeepAlive(response)) {
                // 不再复用http链接
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        log.info("finish http response, then close, connId:{}, result:{}", getId(), future.isSuccess());
                        future.channel().close();
                    }
                });
            } else {
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        log.info("finish http response finish, connId:{}, result:{}", getId(), future.isSuccess());
                    }
                });
            }
        }
    }

    @Override
    public void writeRequest(FullHttpRequest request) {
        if (request != null && request.refCnt() > 0) {
            super.writeAsyn(request);
        }
    }
}
