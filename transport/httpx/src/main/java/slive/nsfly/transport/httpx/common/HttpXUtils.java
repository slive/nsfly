package slive.nsfly.transport.httpx.common;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slive.nsfly.common.constant.CommonConstants;
import slive.nsfly.transport.tcp.common.TcpUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/17 8:42 上午
 */
public class HttpXUtils extends TcpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpXUtils.class);

    private static final String SERVER_NAME = "nsfly-agent";

    public static Map<String, Object> converUrlParams(String uncodeUrl) {
        Map<String, Object> retParams = new LinkedHashMap<>();
        if (uncodeUrl != null) {
            String decodeUrl = decodeStr(uncodeUrl);
            convertParams(retParams, decodeUrl, false);
            if (retParams.isEmpty()) {
                convertParams(retParams, uncodeUrl, true);
            }
            log.info("decode all url attrMap:{}", retParams);
        }
        return retParams;
    }

    public static Map<String, Object> convertAllParams(FullHttpRequest request) {
        Map<String, Object> retParams = new LinkedHashMap<>();
        if (request != null) {
            String uncodeUrl = request.uri();
            // 全部的参数由 url的参数+body参赛组成
            retParams = converUrlParams(uncodeUrl);
            convertBodyParams(request, retParams);
        }
        return retParams;
    }

    private static void convertParams(Map<String, Object> retParams, String url, boolean decode) {
        if (url.contains("?")) {
            String[] split = url.split("\\?");
            if (split.length > 1) {
                String srcParam = split[1];
                if (srcParam.contains("&")) {
                    String[] params = srcParam.split("&");
                    for (String param : params) {
                        mergeParam(retParams, param, decode);
                    }
                } else {
                    mergeParam(retParams, srcParam, decode);
                }
            }
        }
    }

    private static void mergeParam(Map<String, Object> retParams, String param, boolean decode) {
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

    public static Map<String, Object> convertBodyParams(FullHttpRequest request) {
        Map<String, Object> attrMap = new LinkedHashMap<>();
        convertBodyParams(request, attrMap);
        return attrMap;
    }

    private static void convertBodyParams(FullHttpRequest request, Map<String, Object> attrMap) {
        if (HttpMethod.POST.equals(request.method())) {
            FullHttpRequest cpReq = request.copy();
            HttpPostRequestDecoder reqDecoder = new HttpPostRequestDecoder(cpReq);
            List<InterfaceHttpData> bodyHttpDatas = reqDecoder.getBodyHttpDatas();
            if (bodyHttpDatas != null && !bodyHttpDatas.isEmpty()) {
                // 解析body里的参数
                for (InterfaceHttpData httpData : bodyHttpDatas) {
                    if (httpData instanceof MixedAttribute) {
                        MixedAttribute attr = (MixedAttribute) httpData;
                        String key = attr.getName();
                        String value = null;
                        try {
                            value = attr.getValue();
                        } catch (IOException e) {
                            log.error("getAttrValue error, key:{}", key, e);
                        }
                        if (value != null) {
                            List<String> values = null;
                            Object v = attrMap.get(key);
                            if (v instanceof List) {
                                values = (List<String>) v;
                            } else if (v == null) {
                                values = new LinkedList<>();
                                attrMap.put(key, values);
                            } else {
                                values = new LinkedList<>();
                                // 旧的放回列表中
                                values.add(v.toString());
                                attrMap.put(key, values);
                            }
                            value = decodeStr(value);
                            values.add(value);
                        }
                    }
                }
                log.info("decode all body attrMap:{}", attrMap);
            }
            ReferenceCountUtil.release(cpReq);
        }
    }

    public static String decodeStr(String encodeStr) {
        try {
            return URLDecoder.decode(encodeStr, CommonConstants.CHARSET_NAME_UTF8);
        } catch (Exception ex1) {
            log.warn("decode error1, encodeStr:{}", encodeStr, ex1);
            try {
                return URLDecoder.decode(encodeStr, CommonConstants.CHARSET_NAME_ISO_8859_1);
            } catch (Exception ex2) {
                log.warn("decode error2, encodeStr:{}", encodeStr, ex2);
            }
        }
        // 解析失败，默认返回
        return encodeStr;
    }

    public static String encodeStr(String uncodeStr) {
        try {
            return URLEncoder.encode(uncodeStr, CommonConstants.CHARSET_NAME_UTF8);
        } catch (Exception ex1) {
            log.warn("encode error1, uncodeStr:{}", uncodeStr, ex1);
            try {
                return URLEncoder.encode(uncodeStr, CommonConstants.CHARSET_NAME_ISO_8859_1);
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

    public static void wapperDefaultHeaders(FullHttpResponse response) {
        HttpHeaders headers = response.headers();
        if (!headers.contains(HttpHeaderNames.SERVER)) {
            headers.set(HttpHeaderNames.SERVER, SERVER_NAME);
        }

        if (!headers.contains(HttpHeaderNames.DATE)) {
            // 设置日期
            headers.set(HttpHeaderNames.DATE, new Date());
        }

        // chunk无须设置长度
        if (!HttpUtil.isTransferEncodingChunked(response)) {
            if (!HttpUtil.isContentLengthSet(response)) {
                // 设置长度
                HttpUtil.setContentLength(response, response.content().readableBytes());
            }
        }

        // 默认keepalive设置为false
        if (!HttpUtil.isKeepAlive(response)) {
            HttpUtil.setKeepAlive(response, false);
        }
    }

    public static FullHttpResponse createDefaultResponse() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public static FullHttpResponse createDefaultResponse(String msg) {
        FullHttpResponse response = createDefaultResponse();
        response.content().writeCharSequence(msg, HttpUtil.getCharset(response, CommonConstants.CHARSET_DEFAULT));
        return response;
    }
}
