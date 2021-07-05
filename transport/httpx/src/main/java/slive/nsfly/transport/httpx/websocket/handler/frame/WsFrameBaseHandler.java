package slive.nsfly.transport.httpx.websocket.handler.frame;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;
import slive.nsfly.transport.inter.conn.handler.frame.HandshakeContext;

import java.util.LinkedList;
import java.util.List;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 3:07 下午
 */
public abstract class WsFrameBaseHandler implements WsFrameHandler {

    private static final Logger log = LoggerFactory.getLogger(WsFrameBaseHandler.class);

    private static final String READ_FRAME_BUFFER = "read.frame.buffer";

    @Override
    public void onReadFrame(ConnHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            if (frame.isFinalFragment()) {
                onReadText(ctx, (TextWebSocketFrame) frame);
            } else {
                addWsFrameBuffer(ctx, frame);
            }
        } else if (frame instanceof BinaryWebSocketFrame) {
            if (frame.isFinalFragment()) {
                onReadBinary(ctx, (BinaryWebSocketFrame) frame);
            } else {
                addWsFrameBuffer(ctx, frame);
            }
        } else if (frame instanceof PingWebSocketFrame) {
            onPing(ctx, (PingWebSocketFrame) frame);
        } else if (frame instanceof PongWebSocketFrame) {
            onPong(ctx, (PongWebSocketFrame) frame);
        } else if (frame instanceof CloseWebSocketFrame) {
            onNotifyClose(ctx, (CloseWebSocketFrame) frame);
        } else if (frame instanceof ContinuationWebSocketFrame) {
            // 分片处理
            onContinunationFrame(ctx, (ContinuationWebSocketFrame) frame);
        }
    }

    protected abstract void onReadText(ConnHandlerContext ctx, TextWebSocketFrame txtframe);

    protected abstract void onReadBinary(ConnHandlerContext ctx, BinaryWebSocketFrame binFrame);

    private void onContinunationFrame(ConnHandlerContext ctx, ContinuationWebSocketFrame frame) {
        WebSocketFrameBuffer buffer = addWsFrameBuffer(ctx, frame);
        if (frame.isFinalFragment()) {
            try {
                // 最后一帧进行处理
                boolean isTextFrame = buffer.isTextFrame();
                if (isTextFrame) {
                    TextWebSocketFrame textFrame = buffer.getFullTextFrame();
                    onReadText(ctx, textFrame);
                    // TODO 后续不再使用，及时释放，避免内存泄漏
                    ReferenceCountUtil.release(textFrame);
                } else {
                    BinaryWebSocketFrame binaryFrame = buffer.getFullBinaryFrame();
                    onReadBinary(ctx, binaryFrame);
                    // TODO 后续不再使用，及时释放，避免内存泄漏
                    ReferenceCountUtil.release(binaryFrame);
                }
            } catch (Exception e) {
                log.error("onContinunationFrame error, connId:{}", ctx.getConnId(), e);
            } finally {
                removeWsFrameBuffer(ctx);
            }
        }
    }

    protected void onPing(ConnHandlerContext ctx, PingWebSocketFrame pingFrame) {
        // 收到ping默认回复pong
        ctx.getConn().writePong();
    }

    protected void onPong(ConnHandlerContext ctx, PongWebSocketFrame pongFrame) {
        //TODO 收到pong，暂时不处理
    }

    protected void onNotifyClose(ConnHandlerContext ctx, CloseWebSocketFrame closeFrame) {
        log.info("notify close, connId:{}", ctx.getConnId());
        ctx.getConn().release();
    }

    @Override
    public void preWrite(ConnHandlerContext ctx, WebSocketFrame frame) {
        // TODO
    }

    @Override
    public void onHandshake(ConnHandlerContext ctx, WebSocketFrame frame, HandshakeContext handshakeCtx) {
        // TODO
        log.info("websocket handshake connId:{}, handshake context:{}", ctx.getConnId(), handshakeCtx);
    }

    @Override
    public void onBye(ConnHandlerContext ctx, WebSocketFrame frame) {
        log.info("websocket bye connId:{}", ctx.getConnId());
    }

    static class WebSocketFrameBuffer {
        // 是否是text帧
        private boolean textFrame;

        private List<WebSocketFrame> frameBuffer = new LinkedList<>();

        public WebSocketFrameBuffer(WebSocketFrame frame) {
            cacheFrame(frame);
            textFrame = (frame instanceof TextWebSocketFrame);
        }

        public boolean isTextFrame() {
            return textFrame;
        }

        public void cacheFrame(WebSocketFrame frame) {
            if (!frame.isFinalFragment()) {
                // 如果非最后一帧，retain保留，避免被回收，以便后面处理
                frame.retain();
            }
            synchronized (frameBuffer) {
                frameBuffer.add(frame);
            }
            log.info("cache frame, isTextFrame:{}, bufferSize:{}", textFrame, frameBuffer.size());
        }

        public BinaryWebSocketFrame getFullBinaryFrame() {
            if (isTextFrame()) {
                return null;
            }

            ByteBuf byteBuf = fetchFullFrameByte();
            if (byteBuf != null) {
                return new BinaryWebSocketFrame(byteBuf);
            }
            return null;
        }

        public TextWebSocketFrame getFullTextFrame() {
            if (!isTextFrame()) {
                return null;
            }

            ByteBuf byteBuf = fetchFullFrameByte();
            if (byteBuf != null) {
                return new TextWebSocketFrame(byteBuf);
            }
            return null;
        }

        private ByteBuf fetchFullFrameByte() {
            ByteBuf byteBuf = null;
            if (!frameBuffer.isEmpty()) {
                synchronized (frameBuffer) {
                    for (WebSocketFrame frame : frameBuffer) {
                        ByteBuf content = frame.content();
                        if (byteBuf == null) {
                            byteBuf = content;
                        } else {
                            byteBuf.writeBytes(content);
                        }
                    }
                }
                log.info("fetch catchframe, isTextFrame:{}, bufferSize:{}, byteBufLen:{}",
                        textFrame, frameBuffer.size(), byteBuf.readableBytes());
                frameBuffer.clear();
            }
            return byteBuf;
        }

    }

    private static WebSocketFrameBuffer getWsFrameBuffer(ConnHandlerContext ctx) {
        return (WebSocketFrameBuffer) ctx.getConn().get(READ_FRAME_BUFFER);
    }

    private static WebSocketFrameBuffer addWsFrameBuffer(ConnHandlerContext ctx, WebSocketFrame frame) {
        WebSocketFrameBuffer buffer = getWsFrameBuffer(ctx);
        if (buffer == null) {
            buffer = new WebSocketFrameBuffer(frame);
            ctx.getConn().add(READ_FRAME_BUFFER, buffer);
        }
        buffer.cacheFrame(frame);
        return buffer;
    }

    private static void removeWsFrameBuffer(ConnHandlerContext ctx) {
        ctx.getConn().remove(READ_FRAME_BUFFER);
    }
}
