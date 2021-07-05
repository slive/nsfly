package slive.nsfly.transport.inter.conn.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.transport.inter.conn.Conn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:02 上午
 */
public abstract class BaseConnHandler implements ConnHandler {

    private static final Logger log = LoggerFactory.getLogger(BaseConnHandler.class);

    private static final String PATTERN_HANDLER = "pattern_handler";

    @Override
    public void onConnect(ConnHandlerContext ctx) {
        log.info("connected, connId:{}", ctx.getConnId());
    }

    @Override
    public void onRelease(ConnHandlerContext ctx) {
        log.info("released, connId:{}", ctx.getConnId());
    }

    @Override
    public void onRead(ConnHandlerContext ctx) {
        log.info("reading, connId:{}", ctx.getConnId());
    }

    @Override
    public void onException(ConnHandlerContext ctx) {
        log.error("excpetion, connId:{}", ctx.getConnId(), ctx.getError());
    }

    public static <T extends ConnExtHandler> T getPatternHandler(Conn conn) {
        return (T) conn.get(PATTERN_HANDLER);
    }

    public static <T extends ConnExtHandler> T removePatternHandler(Conn conn) {
        return (T) conn.remove(PATTERN_HANDLER);
    }

    public static <T extends ConnExtHandler> void addPatternHandler(Conn conn, T handler) {
        conn.add(PATTERN_HANDLER, handler);
    }
}
