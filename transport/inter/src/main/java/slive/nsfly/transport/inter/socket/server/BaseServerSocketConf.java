package slive.nsfly.transport.inter.socket.server;

import slive.nsfly.transport.inter.conn.ConnType;
import slive.nsfly.transport.inter.socket.conf.BaseSocketConf;

/**
 * 描述：<pre>
 *     服务端配置
 *
 * @author Slive
 * @date 2021/6/11 8:38 上午
 */
public class BaseServerSocketConf extends BaseSocketConf implements ServerSocketConf {

    // 为0则不使用，部分不支持，如kcp
    private int bossThreads = 0;

    private int maxChildrenSize = -1;

    private Object childConf;

    public BaseServerSocketConf(String ip, int port, String connType, Object childConf) {
        super(ip, port, connType);
        this.childConf = childConf;
        // 默认work线程，可自行设置
        setWorkThreads(Runtime.getRuntime().availableProcessors() * 1);
    }

    public BaseServerSocketConf(String ip, int port, ConnType connType, Object childConf) {
        super(ip, port, connType);
        this.childConf = childConf;
        // 默认work线程，可自行设置
        setWorkThreads(Runtime.getRuntime().availableProcessors() * 1);
    }

    protected BaseServerSocketConf(String ip, int port, String connType) {
        super(ip, port, connType);
        // 默认work线程，可自行设置
        setWorkThreads(Runtime.getRuntime().availableProcessors() * 1);
    }

    protected BaseServerSocketConf(String ip, int port, ConnType connType) {
        super(ip, port, connType);
        // 默认work线程，可自行设置
        setWorkThreads(Runtime.getRuntime().availableProcessors() * 1);
    }

    @Override
    public <T> T getChildConf() {
        return (T) childConf;
    }

    protected void setChildConf(Object childConf) {
        this.childConf = childConf;
    }

    @Override
    public int getMaxChildrenSize() {
        return maxChildrenSize;
    }

    @Override
    public void setMaxChildrenSize(int maxChildrenSize) {
        this.maxChildrenSize = maxChildrenSize;
    }

    @Override
    public int getBossThreads() {
        return bossThreads;
    }

    @Override
    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }
}
