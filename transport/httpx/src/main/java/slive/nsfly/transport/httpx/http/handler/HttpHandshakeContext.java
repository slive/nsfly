package slive.nsfly.transport.httpx.http.handler;

import slive.nsfly.transport.inter.conn.handler.frame.HandshakeContext;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 2:40 下午
 */
public class HttpHandshakeContext extends HandshakeContext {

    private URI uri = null;

    private Map<String, Object> queryParams = null;

    private Map<String, Object> bodyParams = null;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
        mergeToAllParams(queryParams);
    }

    public Map<String, Object> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(Map<String, Object> bodyParams) {
        this.bodyParams = bodyParams;
        mergeToAllParams(bodyParams);
    }

    private void mergeToAllParams(Map<String, Object> inputParams) {
        Map<String, Object> allParams = getAllParams();
        if (allParams == null || inputParams == null) {
            return;
        }
        Set<String> keys = inputParams.keySet();
        for (String key : keys) {
            Object p1 = allParams.get(key);
            Object p2 = inputParams.get(key);
            if (p1 == null) {
                allParams.put(key, p1);
            } else {
                if (p1 instanceof List) {
                    if (p2 instanceof List) {
                        // 都是列表的情况
                        ((List) p1).addAll((List) p2);
                    } else {
                        // 单单p1是列表的情况
                        ((List) p1).add(p2);
                    }
                } else {
                    // 调整为列表顺序存储
                    allParams.remove(key);
                    List newList = new LinkedList();
                    newList.add(p1);
                    if (p2 instanceof List) {
                        // 列表的情况
                        newList.addAll((List) p2);
                    } else {
                        // 非是列表的情况
                        newList.add(p2);
                    }
                    allParams.put(key, newList);
                }
            }
        }
    }
}
