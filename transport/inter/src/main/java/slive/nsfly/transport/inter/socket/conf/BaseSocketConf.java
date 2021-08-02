package slive.nsfly.transport.inter.socket.conf;

import slive.nsfly.common.util.IpAddressUtils;
import slive.nsfly.common.util.JSONUtils;
import slive.nsfly.common.util.StringUtils;
import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.exception.TransportRuntimeException;

/**
 * 描述：<pre>
 *
 *
 * @author Slive
 * @date 2021/6/10 6:43 下午
 */
public class BaseSocketConf implements SocketConf {

    private String ip;

    private int port;

    private ConnType connType;

    private int workThreads = 1;

    private long connetTimeout = CONNET_TIMEOUT;

    private long writeTimeout = WRITE_TIMEOUT;

    private long closeTimeout = CLOSE_TIMEOUT;

    public BaseSocketConf(String ip, int port, String connType) {
        ConnType ct = ConnType.getConnType(connType);
        init(ip, port, ct);
    }

    public BaseSocketConf(String ip, int port, ConnType connType) {
        init(ip, port, connType);
    }

    private void init(String ip, int port, ConnType ct) {
        if (ct == null) {
            throw new TransportRuntimeException("invalid connType:" + connType);
        }

        this.connType = ct;
        if (port <= 0) {
            throw new TransportRuntimeException("invalid port:" + port);
        }
        this.port = port;

        if (StringUtils.isBlank(ip)) {
            // 取默认ip
            this.ip = IpAddressUtils.getLocalIp();
        } else {
            this.ip = ip;
        }
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public ConnType getConnType() {
        return connType;
    }

    @Override
    public long getConnectTimeout() {
        return connetTimeout;
    }

    @Override
    public void setConnectTimeout(long connetTimeout) {
        this.connetTimeout = connetTimeout;
    }

    @Override
    public long getWriteTimeout() {
        return writeTimeout;
    }

    @Override
    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    @Override
    public long getCloseTimeout() {
        return closeTimeout;
    }

    @Override
    public void setCloseTimeout(long closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    @Override
    public int getWorkThreads() {
        return workThreads;
    }

    @Override
    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }
}
