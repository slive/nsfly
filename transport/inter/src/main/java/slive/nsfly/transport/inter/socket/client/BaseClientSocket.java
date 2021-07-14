package slive.nsfly.transport.inter.socket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.concurrent.ThreadPoolUtils;
import slive.nsfly.common.util.StringUtils;
import slive.nsfly.transport.inter.conn.Conn;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.exception.TransportRuntimeException;
import slive.nsfly.transport.inter.socket.BaseSocket;

import java.util.Map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 9:58 上午
 */
public abstract class BaseClientSocket<C extends ClientSocketConf> extends BaseSocket<C> implements ClientSocket<C> {

    private static final Logger log = LoggerFactory.getLogger(BaseClientSocket.class);

    private static final String THREAD_PREFIX_WORK = "wc";

    private static EventLoopGroup GLOBAL_WORK_LOOPGROUP = null;

    private EventLoopGroup workLoopGroup = null;

    private ConnHandler handler = null;

    private Conn conn = null;

    private boolean open = false;

    private Map<String, Object> dialParams = null;

    public BaseClientSocket(Object parent, C clientSocketConf, ConnHandler handler) {
        super(parent, clientSocketConf, false);
        setHandler(handler);
    }

    public BaseClientSocket(Object parent, C clientSocketConf) {
        super(parent, clientSocketConf, false);
    }

    @Override
    protected void handleReadIdle(ChannelHandlerContext ctx) {
        // 读超时，关闭
        close();
    }

    @Override
    protected void handleWriteIdle(ChannelHandlerContext ctx) {
        if (getConf().isPingOnIdle()) {
            // 写超时时（一般来说写超时时间比读超时时间短），自动发送ping包
            Conn conn = getConn();
            if (conn != null) {
                this.conn.writePing();
            }
        }
    }

    @Override
    public String getId() {
        if (conn != null) {
            return conn.getId();
        }
        return super.getId();
    }

    @Override
    public Conn getConn() {
        return conn;
    }

    @Override
    public boolean dial(Map<String, Object> dialParams) {
        if (isOpen()) {
            log.warn("client socket {} is open.", getId());
            return false;
        } else {
            this.dialParams = dialParams;
            boolean ret = _dial();
            setOpen(ret);
            return ret;
        }
    }

    @Override
    public boolean dial() {
        return dial(null);
    }

    protected abstract boolean _dial();

    protected void setConn(Conn conn) {
        this.conn = conn;
    }

    @Override
    public ConnHandler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(ConnHandler handler) {
        if (handler == null) {
            throw new TransportRuntimeException("handler is null.");
        }
        this.handler = handler;
    }

    protected Map<String, Object> getDialParams() {
        return dialParams;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    protected void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public void close() {
        if (isOpen()) {
            conn.release();
            setOpen(false);
        }
    }

    protected void _close() {
        releaseLoopGroup();
    }

    protected EventLoopGroup getWorkLoopGroup() {
        return workLoopGroup;
    }

    protected void initLoopGroup(String prefixWork) {
        C conf = getConf();
        if (GLOBAL_WORK_LOOPGROUP != null) {
            // 全局优先
            workLoopGroup = GLOBAL_WORK_LOOPGROUP;
        } else {
            int workThreads = conf.getWorkThreads();
            if (workThreads <= 0) {
                // 默认为1
                workThreads = 1;
            }
            workLoopGroup = new NioEventLoopGroup(workThreads, ThreadPoolUtils.newThreadFactory(prefixWork));
        }
    }

    protected void releaseLoopGroup() {
        // 释放相关资源
        if (GLOBAL_WORK_LOOPGROUP == null && workLoopGroup != null) {
            workLoopGroup.shutdownGracefully();
        }
    }

    public static void setGlobalWorkLoopGroup(int workThreads, String prefix) {
        if (workThreads > 0) {
            if (StringUtils.isBlank(prefix)) {
                prefix = THREAD_PREFIX_WORK;
            }
            GLOBAL_WORK_LOOPGROUP = new NioEventLoopGroup(workThreads, ThreadPoolUtils.newThreadFactory(prefix));
        }
    }
}