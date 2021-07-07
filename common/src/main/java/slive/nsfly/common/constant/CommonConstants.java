package slive.nsfly.common.constant;

import java.nio.charset.Charset;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/23 9:31 上午
 */
public interface CommonConstants {

    String CHARSET_NAME_UTF8 = "UTF-8";

    String CHARSET_NAME_DEFAULT = CHARSET_NAME_UTF8;

    Charset CHARSET_DEFAULT = Charset.forName(CHARSET_NAME_DEFAULT);

    String CHARSET_NAME_ISO_8859_1 = "ISO-8859-1";
}
