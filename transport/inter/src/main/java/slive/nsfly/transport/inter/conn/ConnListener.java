package slive.nsfly.transport.inter.conn;

/**
 * 描述：<pre>
 *     conn通信监听回调
 *
 * @author Slive
 * @date 2021/6/10 9:07 上午
 */
public interface ConnListener {

    /**
     * 成功后
     */
    void onSuccess(Conn conn);

    /**
     * 失败后
     */
    void onFailed(Conn conn);

    /**
     * 取消后
     */
    void onCancelled(Conn conn);
}
