package slive.nsfly.common.util;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/10 8:36 下午
 */
public class StringUtils {

    public static boolean isBlank(String input) {
        return org.apache.commons.lang3.StringUtils.isBlank(input);
    }

    public static boolean isNotBlank(String input) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(input);
    }

    public static boolean isEmpty(String input) {
        return org.apache.commons.lang3.StringUtils.isEmpty(input);
    }

    public static boolean isNotEmpty(String input) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(input);
    }
}
