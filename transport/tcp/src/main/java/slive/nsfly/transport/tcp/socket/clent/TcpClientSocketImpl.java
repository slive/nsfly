package slive.nsfly.transport.tcp.socket.clent;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.socket.client.BaseClientSocket;
import slive.nsfly.transport.tcp.conf.TcpConf;
import slive.nsfly.transport.tcp.conn.TcpConnImpl;

import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/15 10:30 下午
 */
public class TcpClientSocketImpl<C extends TcpClientSocketConf> extends BaseClientSocket<C> implements TcpClientSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(TcpClientSocketImpl.class);

    private static final String THREAD_PREFIX_WORK = "twc";

    private Bootstrap bootstrap = null;

    public TcpClientSocketImpl(C clientSocketConf, ConnHandler handler) {
        super(null, clientSocketConf, handler);
    }

    public TcpClientSocketImpl(Object parent, C clientSocketConf, ConnHandler handler) {
        super(parent, clientSocketConf, handler);
    }

    public TcpClientSocketImpl(Object parent, C clientSocketConf) {
        super(parent, clientSocketConf);
    }

    @Override
    protected boolean _dial() {
        long startTime = System.currentTimeMillis();
        TcpClientSocketConf clientConf = getConf();
        ConnType connType = clientConf.getConnType();
        log.info("start to dial {} client, clientConf:{}", connType, clientConf);
        boolean ret = false;
        try {
            initLoopGroup(THREAD_PREFIX_WORK);
            bootstrap = new Bootstrap();
            TcpConf extConf = clientConf.getExtConf();
            // 内存分配
            ByteBufAllocator allocator = getByteBufAllocator(extConf);
            bootstrap.group(getWorkLoopGroup())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.ALLOCATOR, allocator)
                    .option(ChannelOption.TCP_NODELAY, extConf.isTcpNoDelay())
                    .option(ChannelOption.SO_REUSEADDR, extConf.isSoResueAddr())
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(extConf.getRevBufMinimum(),
                            extConf.getRevBufInitial(),
                            extConf.getRevBufMaximum()))
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            addNettyIdelHandler(pipeline);
                            addNettyExtHandler(pipeline);
                        }
                    });
            ret = bootstrap.connect(clientConf.getIp(), clientConf.getPort()).await(clientConf.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (!ret) {
                log.error("start to dial {} client failed, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
            } else {
                log.info("start to dial {} client success, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
            }
        } catch (Exception ex) {
            log.error("start to dial {} client failed, error:", connType, ex);
        }
        return ret;
    }

    private static ByteBufAllocator getByteBufAllocator(TcpConf childConf) {
        ByteBufAllocator allocator = null;
        boolean directBuffer = childConf.isDirectBuffer();
        if (childConf.isPoolAllocator()) {
            allocator = new PooledByteBufAllocator(directBuffer);
        } else {
            allocator = new UnpooledByteBufAllocator(directBuffer);
        }
        return allocator;
    }

    @Override
    protected Conn createConn(Channel channel) {
        TcpConnImpl tcpConn = new TcpConnImpl(this, getHandler(), false, channel);
        setConn(tcpConn);
        return tcpConn;
    }

    @Override
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new TCPClientInnerHandler(getConf().getConnType()));
    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        Conn conn = getConn();
        if (conn != null) {
            // TODO 超时关闭
            conn.release();
        } else {
            ctx.close();
        }
    }

    @ChannelHandler.Sharable
    class TCPClientInnerHandler extends SimpleChannelInboundHandler {

        private ConnType connType = null;

        public TCPClientInnerHandler(ConnType connType) {
            this.connType = connType;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Conn conn = getConn();
            if (conn == null) {
                conn = createConn(ctx.channel());
                log.info("connect {} client conn, connId:{}", connType, conn.getId());
            }
            getHandler().onConnect(new ConnHandlerContext(conn));
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Conn conn = getConn();
            if (conn != null) {
                log.info("release {} clent conn, connId:{}", connType, conn.getId());
                getHandler().onRelease(new ConnHandlerContext(conn));
                close();
            }
            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            Conn conn = getConn();
            if (conn != null) {
                if (log.isDebugEnabled()) {
                    log.debug("rev {} client conn message, connId:{}", connType, conn.getId());
                }
                byte[] revData = InterUtils.revData(msg);
                getHandler().onRead(new ConnHandlerContext(conn, revData));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Conn conn = getConn();
            if (conn != null) {
                log.error("exception {} client conn, connId:{}", connType, conn.getId(), cause);
                ConnHandlerContext ctx1 = new ConnHandlerContext(conn);
                ctx1.setError(cause);
                getHandler().onException(ctx1);
            }
            super.exceptionCaught(ctx, cause);
        }
    }
}
