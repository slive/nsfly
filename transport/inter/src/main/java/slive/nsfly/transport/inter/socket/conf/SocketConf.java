package slive.nsfly.transport.inter.socket.conf;

import slive.nsfly.transport.inter.conn.ConnType;

import java.util.Map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 12:31 下午
 */
public interface SocketConf {

    /**
     * 默认socket连接超时时间，单位ms
     */
    long CONNET_TIMEOUT = 1000 * 10;

    /**
     * 默认socket写超时时间，单位ms
     */
    long WRITE_TIMEOUT = 1000 * 10;

    /**
     * 默认socket空闲等待多长时间后关闭的，单位ms
     */
    long CLOSE_TIMEOUT = 1000 * 60;

    /**
     * 获取ip地址
     *
     * @return
     */
    String getIp();

    /**
     * 获取端口
     *
     * @return
     */
    int getPort();

    /**
     * 获取conn类型
     *
     * @return
     */
    ConnType getConnType();

    /**
     * 获取连接超时时间，单位ms
     *
     * @return
     */
    long getConnectTimeout();

    /**
     * 设置连接超时时间，单位ms
     *
     * @param timeConnect
     */
    void setConnectTimeout(long timeConnect);

    /**
     * 获取写超时时间，单位ms
     *
     * @return
     */
    long getWriteTimeout();

    /**
     * 设置写超时时间，单位ms
     *
     * @param timeWrite
     */
    void setWriteTimeout(long timeWrite);

    /**
     * 获取关闭超时时间，单位ms
     *
     * @return
     */
    long getCloseTimeout();

    /**
     * 设置写超时时间，单位ms
     *
     * @param timeClose
     */
    void setCloseTimeout(long timeClose);

    /**
     * 获取工作线程数
     *
     * @return
     */
    int getWorkThreads();

    /**
     * 设置工作线程数
     *
     * @param workThreads
     */
    void setWorkThreads(int workThreads);

    /**
     * 获取扩展的配置
     *
     * @return
     */
    <E extends Object> E getExtConf();
}
