package slive.nsfly.common.util;

import com.alibaba.fastjson.JSON;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 8:37 下午
 */
public class JSONUtils {

    public static <T extends Object> T parseObject(String jsonStr, Class<T> clazz) {
        return JSON.parseObject(jsonStr, clazz);
    }

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }
}
