package slive.nsfly.transport.inter.conn;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.inter.common.map.SimpleMapImpl;
import slive.nsfly.transport.inter.conn.handler.ConnHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.conn.monitor.ConnStatis;
import slive.nsfly.transport.inter.exception.TransportRuntimeException;
import slive.nsfly.transport.inter.socket.Socket;
import slive.nsfly.transport.inter.socket.conf.SocketConf;

import java.net.SocketAddress;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 9:25 上午
 */
public abstract class BaseConn extends SimpleMapImpl<Object> implements Conn {

    private static final Logger log = LoggerFactory.getLogger(BaseConn.class);

    private Object parent = null;

    private Object conf = null;

    private boolean server = true;

    private boolean connected = true;

    private boolean allowWrite;

    private ConnStatis statis = null;

    private SocketAddress localAddr;

    private SocketAddress remoteAddr;

    protected Conn self = null;

    protected ConnHandler handler = null;

    protected String id = null;

    protected ConnType type = null;

    protected Channel channel = null;

    /**
     * 初始化conn
     *
     * @param parent
     * @param connHandler
     * @param connType
     */
    protected BaseConn(Object parent, ConnHandler connHandler, ConnType connType, Channel channel, boolean server) {
        init(parent, connHandler, connType, channel, server);
    }

    /**
     * 初始化conn，默认标识的是服务端
     *
     * @param parent
     * @param connHandler
     * @param connType
     */
    protected BaseConn(Object parent, ConnHandler connHandler, ConnType connType, Channel channel) {
        init(parent, connHandler, connType, channel, true);
    }

    private void init(Object parent, ConnHandler connHandler, ConnType connType, Channel channel, boolean server) {
        if (channel == null) {
            throw new TransportRuntimeException("netty channel is null.");
        }
        this.parent = parent;
        this.handler = connHandler;
        this.type = connType;
        this.server = server;
        this.statis = new ConnStatis();
        this.self = this;
        this.setChannel(channel);
        this.setId(channel);
        this.setLocalAddr(channel.localAddress());
        this.setRemoteAddr(channel.remoteAddress());
    }

    @Override
    public <T> T getParent() {
        return (T) parent;
    }

    @Override
    public <C> C getConf() {
        return (C) conf;
    }

    @Override
    public void setConf(Object conf) {
        if (conf == null) {
            throw new TransportRuntimeException("conf is null.");
        }
        this.conf = conf;
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

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public ConnStatis getStatis() {
        return statis;
    }

    @Override
    public ConnType getType() {
        return type;
    }

    @Override
    public boolean isServer() {
        return server;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    protected void setConnected(boolean connected) {
        this.connected = connected;
        setAllowWrite(connected);
    }

    @Override
    public String getId() {
        return id;
    }

    protected abstract void setId(Channel channel);

    protected void setAllowWrite(boolean allowWrite) {
        this.allowWrite = allowWrite;
    }

    protected boolean isAllowWrite() {
        return allowWrite;
    }

    @Override
    public void release() {
        setAllowWrite(false);
        if (isConnected()) {
            try {
                _release();
            } catch (Exception e) {
                log.warn("release conn:{} error.", id, e);
            }
            try {
                // TODO 客户端和服务端是否要区别对待处理？
                channel.disconnect();
                channel.close();
            } catch (Exception e) {
                log.warn("relaase channel:{} error.", id, e);
            } finally {
                setConnected(false);
            }

        }
    }

    protected abstract void _release();

    @Override
    public <M> boolean writeSyn(M msg) {
        return _write(msg, true, null);
    }

    @Override
    public <M> void writeAsyn(M msg) {
        _write(msg, false, null);
    }

    @Override
    public <M> void writeAsyn(M msg, ConnListener listener) {
        _write(msg, false, listener);
    }

    @Override
    public void writePing() {
        // TODO
    }

    @Override
    public void writePong() {
        // TODO
    }

    @Override
    public SocketAddress getLocalAddr() {
        return localAddr;
    }

    protected void setLocalAddr(SocketAddress localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public SocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    protected void setRemoteAddr(SocketAddress remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    protected <M> boolean _write(M msg, boolean sync, ConnListener listener) {
        if (isAllowWrite()) {
            if (log.isDebugEnabled()) {
                log.debug("start to write {} message, connId:{}, msg:{}", type, getId(), msg);
            }
            long startTime = System.currentTimeMillis();
            // 写之前的处理
            ConnHandlerContext ctx = new ConnHandlerContext(this);
            ctx.setAttach(msg);
            getHandler().preWrite(ctx);
            // 返回值为转换的值
            Object forWrite = ctx.getRet();
            // 异常情况的处理
            if (forWrite == null) {
                forWrite = msg;
            } else if (ctx.getError() == null) {
                getHandler().onException(ctx);
                return false;
            }

            // 转换数据结构
            WriteObject writeObj = convertWriteObject(forWrite);

            // 写处理
            ChannelFuture future = channel.write(writeObj.getFinalMsg());
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    boolean isOk = (future.isDone() && future.isSuccess());
                    // 记录日志
                    logSend(startTime, writeObj.getByteNum(), isOk);
                    if (listener != null) {
                        // 调用listener
                        if (isOk) {
                            listener.onSuccess(self);
                        } else if (future.isCancelled()) {
                            listener.onCancelled(self);
                        } else {
                            listener.onFailed(self);
                        }
                    }
                }
            });
            // 缓冲区刷新到发送
            channel.flush();

            if (sync) {
                // 同步等待
                // 获取超时时间
                long writeTimeout = SocketConf.WRITE_TIMEOUT;
                Socket socket = getParent();
                if (socket != null) {
                    writeTimeout = socket.getConf().getWriteTimeout();
                }
                return future.awaitUninterruptibly(writeTimeout);
            }
        } else {
            log.warn("write {} message is not allowed, connId:{}, ", type, id);
        }
        // 默认返回false，异步等待时，不需要关心返回结果
        return false;
    }

    private void logSend(long startTime, int sendByteNum, boolean isOk) {
        long spendTime = (System.currentTimeMillis() - startTime);
        if (spendTime > 500) {
            log.warn("finish to write {} message, connId:{}, sendByteNum:{}, result:{}, spendTime:{} too long!", type, id, sendByteNum, isOk, spendTime);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("finish to write {} message, connId:{}, sendByteNum:{}, result:{}, spendTime:{}", type, id, sendByteNum, isOk, spendTime);
            }
        }
    }

    /**
     * 转换数据，在ConnHandler#preWrite之后调用
     *
     * @param msg
     * @return
     */
    protected abstract WriteObject convertWriteObject(Object msg);

    protected static WriteObject defaultConvertWriteObject(Channel channel, Object msg) {
        Object finalWrite = msg;
        int byteNum = 0;
        // 支持byte[]和ByteBuf方式发送
        if (msg instanceof byte[]) {
            byteNum = ((byte[]) msg).length;
            ByteBuf buffer = channel.alloc().buffer(byteNum);
            buffer.writeBytes((byte[]) msg);
            finalWrite = buffer;
        } else if (msg instanceof ByteBuf) {
            byteNum = ((ByteBuf) msg).readableBytes();
        } else {
            // TODO  默认写字符串
            String s = msg.toString();
            byte[] bytes = s.getBytes();
            byteNum = bytes.length;
            ByteBuf buffer = channel.alloc().buffer(byteNum);
            buffer.writeBytes(bytes);
            finalWrite = buffer;
        }

        WriteObject writeObject = new WriteObject();
        writeObject.setByteNum(byteNum);
        writeObject.setFinalMsg(finalWrite);
        return writeObject;
    }

    /**
     * 写对象，包括num和msg
     */
    public static class WriteObject {

        private int byteNum;

        private Object finalMsg;

        public int getByteNum() {
            return byteNum;
        }

        public void setByteNum(int byteNum) {
            this.byteNum = byteNum;
        }

        public Object getFinalMsg() {
            return finalMsg;
        }

        public void setFinalMsg(Object finalMsg) {
            this.finalMsg = finalMsg;
        }
    }
}
