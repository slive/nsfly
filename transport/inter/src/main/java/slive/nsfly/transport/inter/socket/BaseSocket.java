package slive.nsfly.transport.inter.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.util.JSONUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.exception.TransportRuntimeException;
import slive.nsfly.transport.inter.socket.conf.SocketConf;

import java.util.concurrent.TimeUnit;

/**
 * 描述：<pre>
 *     抽象的socket类
 *
 * @author Slive
 * @date 2021/6/11 8:51 上午
 */
public abstract class BaseSocket<C extends SocketConf> implements Socket<C> {

    private static final Logger log = LoggerFactory.getLogger(BaseSocket.class);

    private static final long DEFAULT_WRITE_TIME = 15 * 1000;

    private Object parent = null;

    protected String id = null;

    protected C conf = null;

    private boolean server = true;

    public BaseSocket(Object parent, C conf, boolean server) {
        init(parent, conf, server);
    }

    private void init(Object parent, C conf, boolean server) {
        if (conf == null) {
            throw new TransportRuntimeException("socket conf is null.");
        }

        this.parent = parent;
        this.conf = conf;
        this.server = server;
    }

    @Override
    public <T> T getParent() {
        return (T) parent;
    }

    @Override
    public String getId() {
        // TODO
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public C getConf() {
        return conf;
    }

    @Override
    public boolean isServer() {
        return server;
    }

    protected abstract Conn createConn(Channel channel);

    protected void addNettyIdelHandler(ChannelPipeline pipeline) {
        // 处理空闲等操作
        long timeoutReadIdle = fetchReadIdleTime();
        long timeoutWriteIde = fetchWriteIdleTime();
        long timeoutAllIdle = fetchAllIdleTime();
        log.info("add idlehandler, socketId:{}, connType:{}, server:{}, timeoutReadIdle:{}, timeoutWriteIde:{}, timeoutAllIdle:{}", getId(), getConf().getConnType(),
                isServer(), timeoutReadIdle, timeoutWriteIde, timeoutAllIdle);
        pipeline.addLast(new IdleStateHandler(timeoutReadIdle, timeoutWriteIde, timeoutAllIdle, TimeUnit.MILLISECONDS));
        pipeline.addLast(new SocketIdelStateTrigger());
    }

    protected void addNettyExtHandler(ChannelPipeline pipeline) {
        // TODO 根据需要扩展实现netty相关的handler
    }

    @ChannelHandler.Sharable
    class SocketIdelStateTrigger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (IdleState.READER_IDLE == state) {
                    handleReadIdle(ctx);
                } else if (IdleState.WRITER_IDLE == state) {
                    handleWriteIdle(ctx);
                } else if (IdleState.ALL_IDLE == state) {
                    handleAllIdle(ctx);
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    /**
     * 度空闲超时后，需自行处理
     *
     * @param ctx
     */
    protected abstract void handleReadIdle(ChannelHandlerContext ctx);

    protected void handleWriteIdle(ChannelHandlerContext ctx) {
        // TODO 默认不处理
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        // TODO 默认不处理
    }

    protected long fetchReadIdleTime() {
        C conf = getConf();
        return conf.getCloseTimeout();
    }

    protected long fetchWriteIdleTime() {
        C conf = getConf();
        long closeTimeout = conf.getCloseTimeout();
        // 设置为timeoutClose 1/2或者1/4，否则为默认值
        return Math.min(Math.max((closeTimeout / 4), DEFAULT_WRITE_TIME), (closeTimeout / 2));
    }

    protected long fetchAllIdleTime() {
        C conf = getConf();
        long closeTimeout = conf.getCloseTimeout();
        if (closeTimeout <= 0) {
            // 关闭超时后再加上一个冗余值
            return (closeTimeout + 500);
        }
        return closeTimeout;
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }
}
