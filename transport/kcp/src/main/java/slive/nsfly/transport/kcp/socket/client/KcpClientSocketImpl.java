package slive.nsfly.transport.kcp.socket.client;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.concurrent.ThreadPoolUtils;
import slive.nsfly.transport.inter.common.map.SimpleMap;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.socket.client.BaseClientSocket;
import slive.nsfly.transport.kcp.common.KcpUtils;
import slive.nsfly.transport.kcp.conf.KcpConf;
import slive.nsfly.transport.kcp.conn.KcpConnImpl;

import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *     kcp客户端socket通信实现
 *
 * @author Slive
 * @date 2021/7/15 8:23 上午
 */
public class KcpClientSocketImpl<C extends KcpClientSocketConf> extends BaseClientSocket<C> implements KcpClientSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(KcpClientSocketImpl.class);

    private static final String THREAD_PREFIX_WORK = "kwc";

    private Bootstrap clientBootstrap = null;

    private EventLoopGroup workLoopGroup = null;

    public KcpClientSocketImpl(Object parent, C clientSocketConf, ConnHandler handler) {
        super(parent, clientSocketConf, handler);
    }

    public KcpClientSocketImpl(Object parent, C clientSocketConf) {
        super(parent, clientSocketConf);
    }

    @Override
    protected boolean _dial() {
        long startTime = System.currentTimeMillis();
        C sConf = getConf();
        ConnType connType = sConf.getConnType();
        log.info("start to dial {} client, sConf:{}", connType, sConf);
        try {
            initLoopGroup(THREAD_PREFIX_WORK);
            // 内存分配
            workLoopGroup = getWorkLoopGroup();
            if (workLoopGroup == null) {
                workLoopGroup = new NioEventLoopGroup(1, ThreadPoolUtils.createThreadFactory(THREAD_PREFIX_WORK));
            }

            clientBootstrap = new Bootstrap();
            // 设置参数
            KcpConf extConf = sConf.getExtConf();
            ChannelOptionHelper.nodelay(clientBootstrap, extConf.isNodelay(),
                    extConf.getInterval(), extConf.getFastResend(),
                    extConf.isNocwnd())
                    .option(UkcpChannelOption.UKCP_MTU, extConf.getMtu())
                    .option(UkcpChannelOption.UKCP_AUTO_SET_CONV, extConf.isAutoSetConv())
                    .option(UkcpChannelOption.UKCP_RCV_WND, extConf.getRcvwnd())
                    .option(UkcpChannelOption.UKCP_SND_WND, extConf.getSndwnd())
                    .option(UkcpChannelOption.UKCP_MIN_RTO, extConf.getMinrto())
                    .option(UkcpChannelOption.UKCP_DEAD_LINK, extConf.getDeadLink());

            // 链接
            clientBootstrap.group(workLoopGroup)
                    .channel(UkcpServerChannel.class)
                    .handler(new ChannelInitializer<UkcpChannel>() {
                        @Override
                        protected void initChannel(UkcpChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            addNettyIdelHandler(p);
                            addNettyExtHandler(p);
                        }
                    });
            ChannelFuture connect = clientBootstrap.connect(sConf.getIp(), sConf.getPort());
            boolean result = (boolean) connect.await(sConf.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (!result) {
                log.error("dial {} client timeout, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
                close();
            } else {
                Conn conn = createConn(connect.channel());
                conn.setConf(sConf);
                setConn(conn);
                getHandler().onConnect(new ConnHandlerContext(conn));
                log.info("dial {} client success, spendTime:{}", connType, (System.currentTimeMillis() - startTime));
            }
            return result;
        } catch (Exception ex) {
            log.error("dial {} client error:{}.", connType, ex);
            close();
        }
        return false;
    }

    @Override
    protected void _close() {
        log.info("close {} client, connId:{}", getConf().getConnType(), getId());
        Conn conn = getConn();
        if (conn != null) {
            conn.release();
        }

        if (workLoopGroup != null && getWorkLoopGroup() == null) {
            workLoopGroup.shutdownGracefully();
        }
    }

    @Override
    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new KcpClientInnerHandler());
    }

    @Override
    protected Conn createConn(Channel channel) {
        return new KcpConnImpl(this, getHandler(), channel, false);
    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        // 空闲时候的处理
        ConnType connType = getConf().getConnType();
        String connId = KcpUtils.getConnId(ctx, connType);
        Conn conn = getConn();
        log.info("{} client readIdle timeout then close, connId:{}", connType,
                connId);
        if (conn != null) {
            conn.release();
        } else {
            ctx.close();
        }
    }

    @ChannelHandler.Sharable
    class KcpClientInnerHandler extends SimpleChannelInboundHandler {

        private ConnType connType;

        public KcpClientInnerHandler() {
            connType = getConf().getConnType();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("active {} client, connId:{}", connType, connId);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("inactive {} client, connId:{}", connType, connId);
            Conn conn = getConn();
            if (conn != null) {
                getHandler().onRelease(new ConnHandlerContext(conn));
            }
            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.info("rev {} client message, connId:{}", connType, connId);
            if (msg instanceof ByteBuf) {
                final byte[] revData = InterUtils.revData(msg);
                if (revData != null) {
                    getHandler().onRead(new ConnHandlerContext(getConn(), revData));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            String connId = KcpUtils.getConnId(ctx, connType);
            log.warn("{} client error, connId:{}", connId, cause);
            Conn conn = getConn();
            if (conn != null) {
                try {
                    ConnHandlerContext context = new ConnHandlerContext(conn);
                    context.setError(cause);
                    getHandler().onException(context);
                } catch (Exception e) {
                    conn.release();
                }
            }
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    public int getConv() {
        return 0;
    }
}
