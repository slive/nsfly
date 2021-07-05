package slive.nsfly.transport.inter.conn.handler;

import slive.nsfly.transport.inter.common.map.SimpleMapImpl;
import slive.nsfly.transport.inter.conn.Conn;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 9:14 上午
 */
public class ConnHandlerContext extends SimpleMapImpl {

    private Conn conn = null;

    private String id = null;

    private Object attach = null;

    private Throwable error = null;

    private Object ret = null;

    public ConnHandlerContext(Conn conn) {
        this.conn = conn;
        this.id = conn.getId();
    }

    public ConnHandlerContext(Conn conn, Object attach) {
        this.conn = conn;
        this.id = conn.getId();
        this.attach = attach;
    }

    public String getConnId() {
        return id;
    }

    public Conn getConn() {
        return conn;
    }

    public <T extends Object> T getAttach() {
        return (T) attach;
    }

    public void setAttach(Object attach) {
        this.attach = attach;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public <T extends Object> T getRet() {
        return (T) ret;
    }

    public void setRet(Object ret) {
        this.ret = ret;
    }
}
