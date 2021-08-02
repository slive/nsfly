package slive.nsfly.transport.tcp.conf;

/**
 * 描述：<pre>
 *     TCP底层相关的配置
 *
 * @author Slive
 * @date 2021/6/14 11:08 上午
 */
public class TcpConf {

    private static final int REV_BUF_MINIMUM = 64;

    private static final int REV_BUF_INITIAL = 1024;

    private static final int REV_BUF_MAXIMUM = 1024 * 1024;

    // 启动TCP_NODELY
    private boolean tcpNoDelay = true;

    // SO_RESUEADDR让端口释放后，立即可以再次使用
    private boolean soResueAddr = true;

    // 是否是使用直接内存（非jvm）
    private boolean directBuffer = true;

    // 是否是池化申请
    private boolean poolAllocator = true;

    // 接收动态调整缓存区最小值
    private int revBufMinimum = REV_BUF_MINIMUM;

    // 接收动态调整缓存区初始化值
    private int revBufInitial = REV_BUF_INITIAL;

    // 接收动态调整缓存区最大值
    private int revBufMaximum = REV_BUF_MAXIMUM;

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isSoResueAddr() {
        return soResueAddr;
    }

    public void setSoResueAddr(boolean soResueAddr) {
        this.soResueAddr = soResueAddr;
    }

    public boolean isDirectBuffer() {
        return directBuffer;
    }

    public void setDirectBuffer(boolean directBuffer) {
        this.directBuffer = directBuffer;
    }

    public boolean isPoolAllocator() {
        return poolAllocator;
    }

    public void setPoolAllocator(boolean poolAllocator) {
        this.poolAllocator = poolAllocator;
    }

    public int getRevBufMinimum() {
        return revBufMinimum;
    }

    public void setRevBufMinimum(int revBufMinimum) {
        this.revBufMinimum = revBufMinimum;
    }

    public int getRevBufInitial() {
        return revBufInitial;
    }

    public void setRevBufInitial(int revBufInitial) {
        this.revBufInitial = revBufInitial;
    }

    public int getRevBufMaximum() {
        return revBufMaximum;
    }

    public void setRevBufMaximum(int revBufMaximum) {
        this.revBufMaximum = revBufMaximum;
    }
}
