package slive.nsfly.transport.inter.conn.handler.frame;

import slive.nsfly.transport.inter.conn.handler.ConnExtHandler;
import slive.nsfly.transport.inter.conn.handler.ConnHandlerContext;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:53 上午
 */
public interface FrameHandler<F> extends ConnExtHandler {

    void preWrite(ConnHandlerContext ctx, F frame);

    /**
     * 读取帧信息时进行的处理
     * @param ctx  handler上下文
     * @param frame 帧信息
     */
    void onReadFrame(ConnHandlerContext ctx, F frame);


    /**
     * 握手时进行的处理
     * @param ctx  handler上下文
     * @param frame 帧信息，非必须
     * @param handshakeCtx 握手的上下文
     */
    void onHandshake(ConnHandlerContext ctx, F frame, HandshakeContext handshakeCtx);

    /**
     * 挥手时进行的处理
     * @param ctx  handler上下文
     * @param frame 帧信息，非必须
     */
    void onBye(ConnHandlerContext ctx, F frame);
}
