package slive.nsfly.transport.inter.socket.client;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.conf.BaseSocketConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 9:56 上午
 */
public class BaseClientSocketConf extends BaseSocketConf implements ClientSocketConf {

    private boolean pingOnIdle = false;

    private Object extConf = null;

    public BaseClientSocketConf(String ip, int port, String connType) {
        super(ip, port, connType);
    }

    public BaseClientSocketConf(String ip, int port, ConnType connType) {
        super(ip, port, connType);
    }

    @Override
    public boolean isPingOnIdle() {
        return pingOnIdle;
    }

    @Override
    public void setPingOnIdle(boolean pingOnIdle) {
        this.pingOnIdle = pingOnIdle;
    }

    @Override
    public <E extends Object> E getExtConf() {
        return (E) extConf;
    }

    protected void setExtConf(Object extConf) {
        this.extConf = extConf;
    }
}
