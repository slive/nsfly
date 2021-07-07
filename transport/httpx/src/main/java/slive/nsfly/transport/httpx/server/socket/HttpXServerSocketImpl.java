package slive.nsfly.transport.httpx.server.socket;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.httpx.common.HttpXConstants;
import slive.nsfly.transport.httpx.common.HttpXUtils;
import slive.nsfly.transport.httpx.http.conf.HttpConf;
import slive.nsfly.transport.httpx.http.conf.HttpConnImpl;
import slive.nsfly.transport.httpx.http.conn.HttpConn;
import slive.nsfly.transport.httpx.http.handler.HttpHandshakeContext;
import slive.nsfly.transport.httpx.http.handler.HttpServerHandler;
import slive.nsfly.transport.httpx.websocket.conf.WsServerConf;
import slive.nsfly.transport.httpx.websocket.conn.WsConnImpl;
import slive.nsfly.transport.httpx.websocket.handler.frame.WsFrameHandler;
import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.BaseConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.conn.handler.frame.HandshakeContext;
import slive.nsfly.transport.tcp.socket.server.TcpServerSocketImpl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 1:58 下午
 */
public class HttpXServerSocketImpl<C extends HttpXServerSocketConf> extends TcpServerSocketImpl<C> implements HttpXServerSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(HttpXServerSocketImpl.class);

    private static final WsServerConf WS_SERVER_CONF = new WsServerConf();

    private boolean supportWs = false;

    public HttpXServerSocketImpl(Object parent, C serverConf) {
        super(parent, serverConf, new HttpXServerBaseHandler());
    }

    public HttpXServerSocketImpl(C serverConf) {
        super(serverConf, new HttpXServerBaseHandler());
    }

    @Override
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        // 添加http的解析
        pipeline.addLast(new HttpServerCodec());

        // 消息处理
        HttpConf childConf = getConf().getChildConf();
        int maxContextLen = childConf.getMaxContextLen();
        pipeline.addLast(new HttpObjectAggregator(maxContextLen));

        // response-chunk
        pipeline.addLast(new ChunkedWriteHandler());

        ConnType connType = getConf().getConnType();
        if (supportWs || HttpXConstants.CONN_HTTPX.equals(connType) || HttpXConstants.CONN_WS.equals(connType)) {
            // 支持websocket配置
            pipeline.addLast(new WebSocketServerCompressionHandler());
        }

        // 内部handler
        pipeline.addLast(new HttpXServerInnerHandler(connType));
    }

    @ChannelHandler.Sharable
    class HttpXServerInnerHandler extends SimpleChannelInboundHandler {

        private ConnType connType;

        public HttpXServerInnerHandler(ConnType connType) {
            this.connType = connType;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            String connId = HttpXUtils.getConnId(ctx, connType);
            if (log.isDebugEnabled()) {
                log.debug("httpx active, connId:{}", connId);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String connId = HttpXUtils.getConnId(ctx, connType);
            if (log.isDebugEnabled()) {
                log.debug("httpx inactive, connId:{}", connId);
            }
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                getChildren().remove(connId);
                getChildHandler().onRelease(new ConnHandlerContext(conn));
            }
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            String connId = HttpXUtils.getConnId(ctx, connType);
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                try {
                    ConnHandlerContext ctx1 = new ConnHandlerContext(conn);
                    ctx1.setAttach(cause);
                    getChildHandler().onException(ctx1);
                } finally {
                    conn.release();
                }
            }
            super.exceptionCaught(ctx, cause);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            String connId = HttpXUtils.getConnId(ctx, connType);
            if (msg instanceof WebSocketFrame) {
                // 接收和处理常规的websocket信息
                Conn conn = getChildren().get(connId);
                if (conn != null) {
                    // 处理读消息
                    ConnHandlerContext ctx1 = new ConnHandlerContext(conn);
                    ctx1.setAttach(msg);
                    getChildHandler().onRead(ctx1);
                }
            } else {
                if (msg instanceof FullHttpRequest) {
                    // http请求信息
                    FullHttpRequest request = ((FullHttpRequest) msg);
                    try {
                        String uri = request.uri();
                        URI requestUri = new URI(uri);
                        String path = requestUri.getPath();
                        boolean isWebSocket = HttpXUtils.isWebSocket(request);
                        log.info("handle request uri:{}, isWebSocket:{}", uri, isWebSocket);
                        if (isWebSocket) {
                            handleUpgrade(ctx, connId, request, requestUri);
                        } else {
                            handleHttp(ctx, connId, request, requestUri);
                        }
                    } catch (Exception ex) {
                        log.error("handle request error.", ex);
                        responseError(ctx, request, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
    }

    private void handleUpgrade(ChannelHandlerContext ctx, String connId, FullHttpRequest request, URI requestUri) throws InterruptedException {
        String path = requestUri.getPath();
        WsFrameHandler wsFrameHandler = patternWsHandler(path);
        if (wsFrameHandler != null) {
            if (!request.decoderResult().isSuccess()) {
                log.warn("websocket upgrade decoder failed, channelId:{}", connId);
                responseError(ctx, request, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            // 处理websocket的握手
            // TODO 如何配置？
            WsServerConf wsServerConf = WS_SERVER_CONF;
            HttpConf childConf = getConf().getChildConf();
            if (childConf instanceof WsServerConf) {
                wsServerConf = (WsServerConf) childConf;
            }

            Channel channel = ctx.channel();
            String wsFullUrl = getWsFullUrl(channel, request, path);
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(wsFullUrl, wsServerConf.getSubprotocol(), wsServerConf.isAllowExtentions());
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
            boolean result = handshaker.handshake(channel, request).await(getConf().getConnectTimeout(), TimeUnit.MILLISECONDS);
            log.info("websocket handshake requestUrl:{}, result:{}", wsFullUrl, result);
            if (result) {
                // 添加握手处理
                Conn conn = new WsConnImpl(this, getChildHandler(), true, channel);
                conn.setConf(childConf);
                // TODO 如果加上connType, id对应不上...，统一用外部的connId
                // getChildren().add(conn.getId(), conn);
                getChildren().add(connId, conn);
                // 记录wspatternhandler
                BaseConnHandler.addPatternHandler(conn, wsFrameHandler);

                // 触发的连接处理
                ConnHandlerContext ctx1 = new ConnHandlerContext(conn);
                HandshakeContext handshakeContext = new HandshakeContext();
                handshakeContext.setAllParams(HttpXUtils.converUrlParams(request.uri()));
                ctx1.setAttach(handshakeContext);
                getChildHandler().onConnect(ctx1);
                return;
            } else {
                log.error("websocket handshake fail, requestUrl:{}", wsFullUrl);
                responseError(ctx, request, HttpResponseStatus.BAD_GATEWAY);
                return;
            }
        } else {
            // 未找到uri
            log.error("invalidate websocket uri:{}", request.uri());
            responseError(ctx, request, HttpResponseStatus.NOT_FOUND);
        }
    }

    private void handleHttp(ChannelHandlerContext ctx, String connId, FullHttpRequest request, URI requestUri) {
        String path = requestUri.getPath();
        HttpServerHandler httpServerHandler = patternHttpHandler(path);
        if (httpServerHandler != null) {
            ConnHandlerContext ctx1 = null;
            SimpleMap<Conn> children = getChildren();
            HttpConn httpConn = (HttpConn) children.get(connId);
            if (httpConn == null) {
                synchronized (children) {
                    // 二次校验
                    httpConn = (HttpConn) children.get(connId);
                    if (httpConn == null) {
                        httpConn = new HttpConnImpl(this, getChildHandler(), true, ctx.channel());
                        httpConn.setConf(getConf().getChildConf());
                        ctx1 = new ConnHandlerContext(httpConn, request);
                        children.add(connId, httpConn);
                        // 记录wspatternhandler
                        BaseConnHandler.addPatternHandler(httpConn, httpServerHandler);
                        // 建立链接
                        getChildHandler().onConnect(ctx1);
                    }
                }
            }
            if (ctx1 == null) {
                ctx1 = new ConnHandlerContext(httpConn, request);
            }
            // 封装参数
            HttpHandshakeContext handshakeCtx = new HttpHandshakeContext();
            handshakeCtx.setRequest(request);
            ctx1.setAttach(handshakeCtx);

            // 读信息处理
            getChildHandler().onRead(ctx1);
            // http-response处理
            FullHttpResponse response = ctx1.getRet();
            if (response != null) {
                httpConn.writeResponse(response);
            }
        } else {
            // 未找到uri
            log.error("invalidate http uri:{}", request.uri());
            responseError(ctx, request, HttpResponseStatus.NOT_FOUND);
        }
    }

    private static void responseError(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        FullHttpMessage response = new DefaultFullHttpResponse(request.protocolVersion(),
                status);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 获取websocketUrl
     *
     * @param channel
     * @param request
     * @param path
     * @return
     */
    private static String getWsFullUrl(Channel channel, HttpRequest request, String path) {
        ChannelPipeline pipeline = channel.pipeline();
        String scheme = "ws";
        if (pipeline.get(SslHandler.class) != null) {
            // 判断是否是wss
            scheme = "wss";
        }
        return scheme + "://" + request.headers().get(HttpHeaderNames.HOST) + path;
    }

    @Override
    public HttpXServerSocket addHttpHanlder(String pathPattern, HttpServerHandler httpServerHandler) {
        addExtHandler(pathPattern, HttpXConstants.CONN_HTTP, httpServerHandler);
        return this;
    }

    @Override
    public HttpServerHandler patternHttpHandler(String path) {
        return (HttpServerHandler) patternExtHandler(path, HttpXConstants.CONN_HTTP);
    }

    @Override
    public void removeHttpHandler(String pathPattern) {
        removeExtHandler(pathPattern, HttpXConstants.CONN_HTTP);
    }

    @Override
    public HttpXServerSocket addWsFrameHanlder(String pathPattern, WsFrameHandler wsFrameHandler) {
        addExtHandler(pathPattern, HttpXConstants.CONN_WS, wsFrameHandler);
        supportWs = true;
        return this;
    }

    @Override
    public WsFrameHandler patternWsHandler(String path) {
        return (WsFrameHandler) patternExtHandler(path, HttpXConstants.CONN_WS);
    }

    @Override
    public void removeWsHandler(String pathPattern) {
        removeExtHandler(pathPattern, HttpXConstants.CONN_WS);
    }
}
