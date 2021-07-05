package slive.nsfly.transport.inter.conn.handler.frame;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：<pre>
 *     握手上下文
 *
 * @author Slive
 * @date 2021/6/14 10:58 上午
 */
public class HandshakeContext {

    // 握手时的路径
    private String path;

    // 握手时传递的参数
    protected Map<String, Object> allParams = new LinkedHashMap<>();

    /**
     * 获取握手时的路径
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置握手时的路径
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取握手时传递的参数
     *
     * @return
     */
    public Map<String, Object> getAllParams() {
        return allParams;
    }

    /**
     * 设置握手时传递的参数
     *
     * @param allParams
     */
    public void setAllParams(Map<String, Object> allParams) {
        this.allParams = allParams;
    }

    public String getFirstValue(String key) {
        Object param = getAllParams().get(key);
        if (param == null) {
            return null;
        }

        if (param instanceof List) {
            Object v = ((List) param).get(0);
            if (v == null) {
                return null;
            }
            return v.toString();
        }
        return param.toString();
    }
}
