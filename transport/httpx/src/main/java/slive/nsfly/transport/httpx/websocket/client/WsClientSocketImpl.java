package slive.nsfly.transport.httpx.websocket.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.httpx.common.HttpXUtils;
import slive.nsfly.transport.httpx.handler.HttpXBaseHandler;
import slive.nsfly.transport.httpx.websocket.conf.WsClientConf;
import slive.nsfly.transport.httpx.websocket.conn.WsConn;
import slive.nsfly.transport.httpx.websocket.conn.WsConnImpl;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameHandler;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.tcp.socket.clent.TcpClientSocketImpl;

import java.net.URI;
import java.util.Map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/7/12 6:08 下午
 */
public class WsClientSocketImpl<C extends WsClientSocketConf> extends TcpClientSocketImpl<C> implements WsClientSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(WsClientSocketImpl.class);

    private DefaultHttpHeaders headers = new DefaultHttpHeaders();

    private WsFrameHandler frameHandler = null;

    public WsClientSocketImpl(C clientSocketConf, WsFrameHandler frameHandler) {
        super(null, clientSocketConf, new HttpXBaseHandler());
        this.frameHandler = frameHandler;
    }

    public WsClientSocketImpl(Object parent, C clientSocketConf, WsFrameHandler frameHandler) {
        super(parent, clientSocketConf, new HttpXBaseHandler());
        this.frameHandler = frameHandler;
    }

    @Override
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        // 初始化pipeline
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        int maxContextLen = getConf().getExtConf().getMaxContextLen();
        log.info("websocket maxContextLen:{}", maxContextLen);
        pipeline.addLast(new HttpObjectAggregator(maxContextLen));
        pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
        pipeline.addLast(new WsClientSocketInnerHandler());
    }

    @Override
    protected Conn createConn(Channel channel) {
        WsConn conn = new WsConnImpl(this, getHandler(), false, channel);
        conn.setConf(getConf().getExtConf());
        HttpXBaseHandler.addPatternHandler(conn, frameHandler);
        setConn(conn);
        return conn;
    }

    @ChannelHandler.Sharable
    class WsClientSocketInnerHandler extends SimpleChannelInboundHandler {

        private WebSocketClientHandshaker handshaker = null;

        public WsClientSocketInnerHandler() {
            C conf = getConf();
            String url = conf.getFullAddr();
            Map<String, Object> dialParams = getDialParams();
            if (dialParams != null) {
                url += "?" + HttpXUtils.params2Query(dialParams);
            }
            URI uri = URI.create(url);
            WsClientConf extConf = conf.getExtConf();
            log.info("init websocket innerhandler, url:{}, extConf:{}, headers:{}", url, extConf, headers);
            handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, extConf.getSubprotocol(), extConf.isAllowExtentions(),
                    headers, extConf.getMaxContextLen());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            String chId = HttpXUtils.getChannelId(ctx);
            log.info("websocket active, connId:{}", chId);
            ChannelFuture handshake = handshaker.handshake(ctx.channel());
            handshake.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    log.info("websocket handshake ok, connId:{}", chId);
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String chId = HttpXUtils.getChannelId(ctx);
            log.info("websocket inactive, connId:{}", chId);
            getHandler().onRelease(new ConnHandlerContext(getConn()));
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ConnHandlerContext wsContext = new ConnHandlerContext(getConn());
            wsContext.setError(cause);
            getHandler().onException(wsContext);
            log.error("websocket exception, connId:{}", wsContext.getConnId(), cause);
            super.exceptionCaught(ctx, cause);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            String chId = HttpXUtils.getChannelId(ctx);
            log.info("websocket revmessage, connId:{}", chId);
            if (!handshaker.isHandshakeComplete()) {
                ChannelPromise channelPromise = null;
                try {
                    Conn conn = getConn();
                    Channel channel = ctx.channel();
                    if (conn == null) {
                        conn = createConn(channel);
                    } else {
                        ((WsConn) conn).setChannel(channel);
                    }

                    channelPromise = ctx.newPromise();
                    handshaker.finishHandshake(channel, (FullHttpResponse) msg);
                    log.info("websocket remote handshake response:{}, connId:{}", msg, chId);
                    channelPromise.setSuccess();
                    ConnHandlerContext wsContext = new ConnHandlerContext(conn);
                    getHandler().onConnect(wsContext);
                    return;
                } catch (Exception e) {
                    log.error("websocket remote handshake error, connId:{}", chId, e);
                    if (channelPromise != null) {
                        channelPromise.setFailure(e);
                    }
                    throw e;
                }
            }

            // 读消息处理
            try {
                ConnHandlerContext wsContext = new ConnHandlerContext(getConn(), msg);
                getHandler().onRead(wsContext);
            } catch (Exception e) {
                log.error("handle websocket message error, connId:{}", chId, e);
                throw e;
            }

        }
    }

    @Override
    public void addHeader(String name, Object value) {
        headers.add(name, value);
    }
}
