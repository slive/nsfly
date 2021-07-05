package slive.nsfly.transport.inter.conn.handler;

/**
 * 描述：<pre>
 *     conn通信的处理类，主要处理连接，读，写，异常和释放等
 *
 * @author Slive
 * @date 2021/6/10 9:13 上午
 */
public interface ConnHandler {

    /**
     * 在连接成功时触发的操作
     *
     * @param ctx handler上下文
     */
    void onConnect(ConnHandlerContext ctx);

    /**
     * 在释放时触发的操作
     *
     * @param ctx handler上下文
     */
    void onRelease(ConnHandlerContext ctx);

    /**
     * 在读到消息时触发的操作
     *
     * @param ctx handler上下文
     */
    void onRead(ConnHandlerContext ctx);

    /**
     * 在异常时触发的操作
     *
     * @param ctx handler上下文
     */
    void onException(ConnHandlerContext ctx);

    /**
     * 在写操作前的处理
     *
     * @param ctx handler上下文
     */
    void preWrite(ConnHandlerContext ctx);
}
