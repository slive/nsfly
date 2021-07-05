package slive.nsfly.transport.inter.conn.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 8:58 上午
 */
public class ConnHandlerAdapter implements ConnHandler {
    private static final Logger log = getLogger(ConnHandlerAdapter.class);

    @Override
    public void onConnect(ConnHandlerContext ctx) {

    }

    @Override
    public void onRelease(ConnHandlerContext ctx) {

    }

    @Override
    public void onRead(ConnHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("rev connId:{}", ctx.getConnId());
        }
    }

    @Override
    public void onException(ConnHandlerContext ctx) {
        log.error("exception connId:{}", ctx.getConnId(), ctx.getError());
    }

    @Override
    public void preWrite(ConnHandlerContext ctx) {

    }
}
