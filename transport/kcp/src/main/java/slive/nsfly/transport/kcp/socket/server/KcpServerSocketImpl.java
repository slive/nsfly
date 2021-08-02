package slive.nsfly.transport.kcp.socket.server;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.UkcpServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.concurrent.ThreadPoolCache;
import slive.nsfly.common.concurrent.ThreadPoolUtils;
import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.socket.server.BaseServerSocket;
import slive.nsfly.transport.kcp.common.KcpUtils;
import slive.nsfly.transport.kcp.conf.KcpConf;
import slive.nsfly.transport.kcp.conn.KcpConnImpl;

import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * <pre>
 *
 * @author Slive
 * @date 2021/7/16 8:04 上午
 */
public class KcpServerSocketImpl<C extends KcpServerScoketConf> extends BaseServerSocket<C> implements KcpServerSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(KcpServerSocketImpl.class);

    private static final String THREAD_PREFIX_BOSS = "kbs";

    private UkcpServerBootstrap serverBootstrap = null;

    private ThreadPoolCache workThreadPool = null;

    public KcpServerSocketImpl(Object parent, C serverConf, ConnHandler childHandler) {
        super(parent, serverConf, childHandler);
    }

    @Override
    public boolean listen() {
        long startTime = System.currentTimeMillis();
        C ssConf = getConf();
        ConnType connType = ssConf.getConnType();
        log.info("start to listen {} server, ssConf:{}", connType, ssConf);
        try {
            initLoopGroup(THREAD_PREFIX_BOSS);
            // 内存分配
            EventLoopGroup bossLoopGroup = getBossLoopGroup();
            if (bossLoopGroup == null) {
                bossLoopGroup = new NioEventLoopGroup(1, ThreadPoolUtils.createThreadFactory(THREAD_PREFIX_BOSS));
            }

            int workThreads = ssConf.getWorkThreads();
            int corePoolSize = Math.min(2, workThreads);
            workThreadPool = new ThreadPoolCache(THREAD_PREFIX_BOSS, corePoolSize, workThreads, workThreads * 10, 20);

            serverBootstrap = new UkcpServerBootstrap();
            // 设置参数
            KcpConf childConf = ssConf.getChildConf();
            ChannelOptionHelper.nodelay(serverBootstrap, childConf.isNodelay(),
                    childConf.getInterval(), childConf.getFastResend(),
                    childConf.isNocwnd())
                    .childOption(UkcpChannelOption.UKCP_MTU, childConf.getMtu())
                    .childOption(UkcpChannelOption.UKCP_AUTO_SET_CONV, childConf.isAutoSetConv())
                    .childOption(UkcpChannelOption.UKCP_RCV_WND, childConf.getRcvwnd())
                    .childOption(UkcpChannelOption.UKCP_SND_WND, childConf.getSndwnd())
                    .childOption(UkcpChannelOption.UKCP_MIN_RTO, childConf.getMinrto())
                    .childOption(UkcpChannelOption.UKCP_DEAD_LINK, childConf.getDeadLink());

            // 监听
            serverBootstrap.group(bossLoopGroup)
                    .channel(UkcpServerChannel.class)
                    .childHandler(new ChannelInitializer<UkcpChannel>() {
                        @Override
                        protected void initChannel(UkcpChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            addNettyIdelHandler(p);
                            addNettyExtHandler(p);
                        }
                    });
            boolean result = serverBootstrap.bind(ssConf.getIp(), ssConf.getPort()).await(ssConf.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (!result) {
                log.error("listen {} server timeout, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
                close();
            } else {
                log.info("listen {} server success, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
            }
            return result;
        } catch (Exception ex) {
            log.error("listen {} server error:{}.", connType, ex);
            close();
        }
        return false;
    }

    @Override
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new KcpServerInnerHandler());
    }

    @Override
    protected Conn createConn(Channel channel) {
        return new KcpConnImpl(this, getChildHandler(), channel, true);
    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        // 空闲时候的处理
        ConnType connType = getConf().getConnType();
        String connId = KcpUtils.getConnId(ctx, connType);
        Conn conn = getChildren().get(connId);
        log.info("{} server readIdle timeout then close, connId:{}", connType,
                connId);
        if (conn != null) {
            conn.release();
        } else {
            ctx.close();
        }
    }

    @ChannelHandler.Sharable
    class KcpServerInnerHandler extends SimpleChannelInboundHandler {

        private ConnType connType;

        public KcpServerInnerHandler() {
            connType = getConf().getConnType();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("active {}, connId:{}", connType, connId);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("inactive {}, connId:{}", connType, connId);
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                getChildren().remove(connId);
                getChildHandler().onRelease(new ConnHandlerContext(conn));
            }
            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("rev {} message, connId:{}", connType, connId);
            if (msg instanceof ByteBuf) {
                boolean hasConnected = false;
                SimpleMap<Conn> children = getChildren();
                Conn conn = children.get(connId);
                if (conn == null) {
                    synchronized (children) {
                        // 二次校验
                        conn = children.get(connId);
                        if (conn == null) {
                            conn = createConn(ctx.channel());
                            conn.setConf(getConf().getChildConf());
                            log.info("add {} conn, connId:{}", connType, connId);
                            children.add(connId, conn);
                            hasConnected = false;
                        }
                    }
                }

                boolean isConnect = hasConnected;
                final byte[] revData = InterUtils.revData(msg);
                workThreadPool.submitImmediately(new Runnable() {
                    @Override
                    public void run() {
                        Conn conn = children.get(connId);
                        try {
                            if (isConnect) {
                                getChildHandler().onConnect(new ConnHandlerContext(conn));
                            }
                            if (revData != null) {
                                getChildHandler().onRead(new ConnHandlerContext(conn, revData));
                            }
                        } catch (Exception e) {
                            log.error("handle rev {} message error, connId:{}", connType, conn.getId(), e);
                            // 释放资源
                            conn.release();
                        }
                    }
                });
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            Conn conn = getChildren().get(connId);
            if (conn != null) {
                try {
                    ConnHandlerContext context = new ConnHandlerContext(conn);
                    context.setError(cause);
                    getChildHandler().onException(context);
                } catch (Exception e) {
                    conn.release();
                }
            }
            super.exceptionCaught(ctx, cause);
        }
    }
}
