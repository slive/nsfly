package slive.nsfly.transport.kcp.conf;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述：<pre>
 *     kcp底层协议本身相关的配置，参考：https://github.com/skywind3000/kcp
 *
 * @author Slive
 * @date 2021/7/15 8:29 上午
 */
public class KcpConf {

    private static final int KCP_MTU = 1400;

    private static final int KCP_DEAD_LINK = 1400;

    private static final int KCP_INTERVAL = 25;

    private static final boolean KCP_NODELAY = true;

    private static final boolean KCP_NOCWND = false;

    private static final int KCP_FAST_RESEND = 2;

    private static final int KCP_SNDWND = 1024;

    private static final int KCP_RCVWND = 1024;

    private static final int KCP_MIN_RTO = 60;

    private static final boolean KCP_AUTO_SET_CONV = true;

    // 最大传输单元，为避免最底层协议分片，默认为1400
    private int mtu = KCP_MTU;

    // 最大重传次数，超过该次数后，连接将被中断
    private int deadLink = KCP_DEAD_LINK;

    // 触发快速重传的重复的ACK个数
    private int fastResend = KCP_FAST_RESEND;

    // 是否启用无延迟模式，无延迟模式下，rtomin将设置为0，拥塞控制不启动
    private boolean nodelay = KCP_NODELAY;

    // 是否取消拥塞控制
    private boolean nocwnd = KCP_NOCWND;

    // 内部flush刷新间隔，对系统循环效率有非常重要的影响
    private int interval = KCP_INTERVAL;

    // 发送窗口
    private int sndwnd = KCP_SNDWND;

    // 接收窗口
    private int rcvwnd = KCP_RCVWND;

    // 最小rto
    private int minrto = KCP_MIN_RTO;

    private boolean autoSetConv = KCP_AUTO_SET_CONV;

    // 扩展配置
    private Map<String, Object> extConf = new HashMap<>();

    public KcpConf() {

    }

    /**
     * nodelay ：是否启用 nodelay模式，0不启用；1启用。
     * interval ：协议内部工作的 interval，单位毫秒，比如 10ms或者 20ms
     * resend ：快速重传模式，默认0关闭，可以设置2（2次ACK跨越将会直接重传）
     * nc ：是否关闭流控，默认是0代表不关闭，1代表关闭。
     * 普通模式： ikcp_nodelay(kcp, 0, 40, 0, 0);
     * 极速模式： ikcp_nodelay(kcp, 1, 10, 2, 1);
     *
     * @return
     */
    public static KcpConf createServerNomal() {
        KcpConf kcpConf = createNomal();
        return kcpConf;
    }

    private static KcpConf createNomal() {
        KcpConf kcpConf = new KcpConf();
        kcpConf.setNodelay(false);
        kcpConf.setInterval(40);
        kcpConf.setFastResend(2);
        kcpConf.setNocwnd(true);
        return kcpConf;
    }

    /**
     * nodelay ：是否启用 nodelay模式，0不启用；1启用。
     * interval ：协议内部工作的 interval，单位毫秒，比如 10ms或者 20ms
     * resend ：快速重传模式，默认0关闭，可以设置2（2次ACK跨越将会直接重传）
     * nc ：是否关闭流控，默认是0代表不关闭，1代表关闭。
     * 普通模式： ikcp_nodelay(kcp, 0, 40, 0, 0);
     * 极速模式： ikcp_nodelay(kcp, 1, 10, 2, 1);
     *
     * @return
     */
    public static KcpConf createServerFast() {
        KcpConf kcpConf = createFast();
        kcpConf.setAutoSetConv(false);
        return kcpConf;
    }

    private static KcpConf createFast() {
        KcpConf kcpConf = new KcpConf();
        kcpConf.setNodelay(true);
        kcpConf.setInterval(10);
        kcpConf.setFastResend(2);
        kcpConf.setNocwnd(false);
        kcpConf.setDeadLink(20);
        kcpConf.setMinrto(30);
        return kcpConf;
    }

    /**
     * nodelay ：是否启用 nodelay模式，0不启用；1启用。
     * interval ：协议内部工作的 interval，单位毫秒，比如 10ms或者 20ms
     * resend ：快速重传模式，默认0关闭，可以设置2（2次ACK跨越将会直接重传）
     * nc ：是否关闭流控，默认是0代表不关闭，1代表关闭。
     * 普通模式： ikcp_nodelay(kcp, 0, 40, 0, 0);
     * 极速模式： ikcp_nodelay(kcp, 1, 10, 2, 1);
     *
     * @return
     */
    public static KcpConf createClientNomal() {
        KcpConf kcpConf = createNomal();
        kcpConf.setAutoSetConv(false);
        return kcpConf;
    }

    /**
     * nodelay ：是否启用 nodelay模式，0不启用；1启用。
     * interval ：协议内部工作的 interval，单位毫秒，比如 10ms或者 20ms
     * resend ：快速重传模式，默认0关闭，可以设置2（2次ACK跨越将会直接重传）
     * nc ：是否关闭流控，默认是0代表不关闭，1代表关闭。
     * 普通模式： ikcp_nodelay(kcp, 0, 40, 0, 0);
     * 极速模式： ikcp_nodelay(kcp, 1, 10, 2, 1);
     *
     * @return
     */
    public static KcpConf createClientFast() {
        KcpConf kcpConf = createFast();
        kcpConf.setAutoSetConv(false);
        return kcpConf;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public int getDeadLink() {
        return deadLink;
    }

    public void setDeadLink(int deadLink) {
        this.deadLink = deadLink;
    }

    public int getFastResend() {
        return fastResend;
    }

    public void setFastResend(int fastResend) {
        this.fastResend = fastResend;
    }

    public boolean isNodelay() {
        return nodelay;
    }

    public void setNodelay(boolean nodelay) {
        this.nodelay = nodelay;
    }

    public boolean isNocwnd() {
        return nocwnd;
    }

    public void setNocwnd(boolean nocwnd) {
        this.nocwnd = nocwnd;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getSndwnd() {
        return sndwnd;
    }

    public void setSndwnd(int sndwnd) {
        this.sndwnd = sndwnd;
    }

    public int getRcvwnd() {
        return rcvwnd;
    }

    public void setRcvwnd(int rcvwnd) {
        this.rcvwnd = rcvwnd;
    }

    public int getMinrto() {
        return minrto;
    }

    public void setMinrto(int minrto) {
        this.minrto = minrto;
    }

    public boolean isAutoSetConv() {
        return autoSetConv;
    }

    public void setAutoSetConv(boolean autoSetConv) {
        this.autoSetConv = autoSetConv;
    }

    public Map<String, Object> getExtConf() {
        return extConf;
    }

    public void setExtConf(Map<String, Object> extConf) {
        this.extConf = extConf;
    }
}
