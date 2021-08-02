package slive.nsfly.transport.tcp.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.concurrent.ThreadPoolUtils;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.socket.server.BaseServerSocket;
import slive.nsfly.transport.tcp.common.TcpUtils;
import slive.nsfly.transport.tcp.conf.TcpConf;
import slive.nsfly.transport.tcp.conn.TcpConnImpl;

import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 2:37 下午
 */
public class TcpServerSocketImpl<C extends TcpServerSocketConf> extends BaseServerSocket<C> implements TcpServerSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(TcpServerSocketImpl.class);

    private static final String THREAD_PREFIX_BOSS = "tbs";

    private static final String THREAD_PREFIX_WORK = "tws";

    private ServerBootstrap serverBootstrap = null;

    public TcpServerSocketImpl(Object parent, C serverConf, ConnHandler childHandler) {
        super(parent, serverConf, childHandler);
    }

    public TcpServerSocketImpl(C serverConf, ConnHandler childHandler) {
        super(null, serverConf, childHandler);
    }

    @Override
    public boolean listen() {
        long startTime = System.currentTimeMillis();
        TcpServerSocketConf serverConf = getConf();
        ConnType connType = serverConf.getConnType();
        log.info("start to listen {} server, serverConf:{}", connType, serverConf);
        boolean ret = false;
        try {
            // 初始化NIO线程池
            initLoopGroup(THREAD_PREFIX_BOSS, THREAD_PREFIX_WORK);
            serverBootstrap = new ServerBootstrap();
            TcpConf childConf = serverConf.getChildConf();
            // 内存分配
            ByteBufAllocator allocator = getByteBufAllocator(childConf);
            EventLoopGroup bossLoopGroup = getBossLoopGroup();
            if (bossLoopGroup == null) {
                bossLoopGroup = new NioEventLoopGroup(1, ThreadPoolUtils.createThreadFactory(THREAD_PREFIX_BOSS));
            }
            serverBootstrap.group(bossLoopGroup, getWorkLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.ALLOCATOR, allocator)
                    .childOption(ChannelOption.TCP_NODELAY, childConf.isTcpNoDelay())
                    .childOption(ChannelOption.SO_REUSEADDR, childConf.isSoResueAddr())
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(childConf.getRevBufMinimum(),
                            childConf.getRevBufInitial(),
                            childConf.getRevBufMaximum()))
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            addNettyIdelHandler(pipeline);
                            addNettyExtHandler(pipeline);
                        }
                    });
            ret = serverBootstrap.bind(serverConf.getIp(), serverConf.getPort()).await(serverConf.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (!ret) {
                log.error("listen {} server timeout, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
                close();
            } else {
                log.info("listen {} server success, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
            }
        } catch (Exception ex) {
            log.error("listen {} server failed, error:{}", connType, ex);
            close();
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
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new TCPServerInnerHandler(getConf().getConnType()));
    }

    @Override
    protected Conn createConn(Channel channel) {
        return new TcpConnImpl(this, getChildHandler(), true, channel);
    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        String channelId = TcpUtils.getConnId(ctx, getConf().getConnType());
        Conn conn = getChildren().get(channelId);
        if (conn != null) {
            // TODO 超时关闭
            conn.release();
        } else {
            ctx.close();
        }
    }

    @ChannelHandler.Sharable
    class TCPServerInnerHandler extends SimpleChannelInboundHandler {

        private ConnType connType = null;

        public TCPServerInnerHandler(ConnType connType) {
            this.connType = connType;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            String connId = TcpUtils.getConnId(ctx, connType);
            log.info("add {} server conn, connId:{}", connType, connId);
            Conn conn = getChildren().get(connId);
            if (conn == null) {
                conn = createConn(ctx.channel());
                // 添加维护
                getChildren().add(connId, conn);
            }
            getChildHandler().onConnect(new ConnHandlerContext(conn));
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String connId = TcpUtils.getConnId(ctx, connType);
            log.info("remove {} server conn, connId:{}", connType, connId);
            Conn conn = getChildren().remove(connId);
            if (conn != null) {
                getChildHandler().onRelease(new ConnHandlerContext(conn));
            }
            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            String connId = TcpUtils.getConnId(ctx, connType);
            if (log.isDebugEnabled()) {
                log.debug("rev {} server conn message, connId:{}", connType, connId);
            }
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                byte[] revData = InterUtils.revData(msg);
                getChildHandler().onRead(new ConnHandlerContext(conn, revData));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            String connId = TcpUtils.getConnId(ctx, connType);
            log.error("exception {} server conn, connId:{}", connType, connId, cause);
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                ConnHandlerContext ctx1 = new ConnHandlerContext(conn);
                ctx1.setError(cause);
                getChildHandler().onException(ctx1);
            }
            super.exceptionCaught(ctx, cause);
        }
    }

}
