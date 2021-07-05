package slive.nsfly.transport.inter.conn;

import slive.nsfly.transport.inter.exception.TransportRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述：<pre>
 *     通信类型：如tcp/upd/http/ws/kcp/kws等
 *
 * @author Slive
 * @date 2021/6/10 8:37 上午
 */
public class ConnType {

    private static final Map<String, ConnType> TYPE_MAP = new HashMap<>();

    // 协议名称
    private String value;

    private String desc;

    public ConnType(String value) {
        this.value = value;
    }

    public static ConnType getConnType(String typeVal) {
        if (typeVal != null) {
            // 统一小写
            typeVal = typeVal.toLowerCase();
            return TYPE_MAP.get(typeVal);
        }
        return null;
    }

    /**
     * 创建通信类型，如果存在则抛出异常
     *
     * @param typeVal 类型值
     * @return 创建成功的类型
     * @throws TransportRuntimeException
     */
    public static ConnType createConnType(String typeVal) {
        if (typeVal != null) {
            // 统一小写
            typeVal = typeVal.toLowerCase();
            ConnType connType = TYPE_MAP.get(typeVal);
            if (connType == null) {
                connType = new ConnType(typeVal);
                TYPE_MAP.put(typeVal, connType);
                return connType;
            } else {
                throw new TransportRuntimeException(typeVal + " has existed.");
            }
        }
        throw new TransportRuntimeException(typeVal + " is null.");
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
