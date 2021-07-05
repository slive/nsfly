package slive.nsfly.transport.inter.conn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述：<pre>
 *     conn通信监听回调
 *
 * @author Slive
 * @date 2021/6/10 9:09 上午
 */
public abstract class ConnListenerAdapter implements ConnListener {

    private static final Logger log = LoggerFactory.getLogger(ConnListenerAdapter.class);

    @Override
    public void onFailed(Conn conn) {
        log.warn("onFailed, connId:{}, connType:{}", conn.getId(), conn.getType());
    }

    @Override
    public void onCancelled(Conn conn) {
        log.warn("onCancelled, connId:{}, connType:{}", conn.getId(), conn.getType());
    }
}
