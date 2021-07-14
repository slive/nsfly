package slive.nsfly.transport.httpx.websocket.conf;

import slive.nsfly.transport.httpx.common.HttpXUtils;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/17 9:22 上午
 */
public class WsClientConf extends WsBaseConf {
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        path = HttpXUtils.completePath(path);
        this.path = path;
    }
}
