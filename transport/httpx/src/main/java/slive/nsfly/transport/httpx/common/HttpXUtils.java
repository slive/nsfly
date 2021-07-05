package slive.nsfly.transport.httpx.common;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.constant.CommonConstants;
import slive.nsfly.transport.tcp.common.TcpUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/17 8:42 上午
 */
public class HttpXUtils extends TcpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpXUtils.class);

    public static Map<String, Object> converParams(String uncodeUrl) {
        Map<String, Object> retParams = new LinkedHashMap<>();
        if (uncodeUrl != null) {
            converAllUrlParams(retParams, uncodeUrl);
            if (retParams.isEmpty()) {
                converParams1(retParams, uncodeUrl, true);
            }
        }
        return retParams;
    }

    private static void converAllUrlParams(Map<String, Object> retParams, String uncodeUrl) {
        String decodeUrl = decodeStr(uncodeUrl);
        converParams1(retParams, decodeUrl, false);
    }

    private static void converParams1(Map<String, Object> retParams, String url, boolean decode) {
        if (url.contains("?")) {
            String[] split = url.split("\\?");
            if (split.length > 1) {
                String srcParam = split[1];
                if (srcParam.contains("&")) {
                    String[] params = srcParam.split("&");
                    for (String param : params) {
                        updateParam(retParams, param, decode);
                    }
                } else {
                    updateParam(retParams, srcParam, decode);
                }
            }
        }
    }

    private static void updateParam(Map<String, Object> retParams, String param, boolean decode) {
        if (param != null && param.contains("=")) {
            String[] p = param.split("=");
            if (p.length > 1) {
                String key = p[0];
                String val = p[1];
                if (decode) {
                    val = decodeStr(val);
                }
                if (!retParams.containsKey(key)) {
                    // 单个存储为值
                    retParams.put(key, val);
                } else {
                    Object o = retParams.get(key);
                    if (o instanceof List) {
                        // 多个List存储
                        ((List) o).add(val);
                    } else {
                        retParams.remove(key);
                        // 单个转换为list存储
                        List newL = new LinkedList();
                        newL.add(o);
                        newL.add(val);
                        retParams.put(key, newL);
                    }
                }
            }
        }
    }

    public static String decodeStr(String encodeStr) {
        try {
            return URLDecoder.decode(encodeStr, CommonConstants.CHARSET_UTF8);
        } catch (Exception ex1) {
            log.warn("decode error1, encodeStr:{}", encodeStr, ex1);
            try {
                return URLDecoder.decode(encodeStr, CommonConstants.CHARSET_ISO_8859_1);
            } catch (Exception ex2) {
                log.warn("decode error2, encodeStr:{}", encodeStr, ex2);
            }
        }
        // 解析失败，默认返回
        return encodeStr;
    }

    public static String encodeStr(String uncodeStr) {
        try {
            return URLEncoder.encode(uncodeStr, CommonConstants.CHARSET_UTF8);
        } catch (Exception ex1) {
            log.warn("encode error1, uncodeStr:{}", uncodeStr, ex1);
            try {
                return URLEncoder.encode(uncodeStr, CommonConstants.CHARSET_ISO_8859_1);
            } catch (Exception ex2) {
                log.warn("encode error2, uncodeStr:{}", uncodeStr, ex2);
            }
        }
        // 解析失败，默认返回
        return uncodeStr;
    }

    public static boolean isWebSocket(HttpRequest request) {
        return "websocket".equalsIgnoreCase(request.headers().get("Upgrade"));
    }
}
