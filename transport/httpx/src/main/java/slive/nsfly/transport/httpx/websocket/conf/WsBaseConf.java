package slive.nsfly.transport.httpx.websocket.conf;

import slive.nsfly.transport.httpx.http.conf.HttpConf;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/17 9:24 上午
 */
public class WsBaseConf extends HttpConf {

    private String subprotocol = null;

    private boolean allowExtentions = true;

    public String getSubprotocol() {
        return subprotocol;
    }

    public void setSubprotocol(String subprotocol) {
        this.subprotocol = subprotocol;
    }

    public boolean isAllowExtentions() {
        return allowExtentions;
    }

    public void setAllowExtentions(boolean allowExtentions) {
        this.allowExtentions = allowExtentions;
    }
}
