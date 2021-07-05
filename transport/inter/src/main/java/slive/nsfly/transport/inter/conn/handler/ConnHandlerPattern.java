package slive.nsfly.transport.inter.conn.handler;

import slive.nsfly.common.util.JSONUtils;
import slive.nsfly.transport.inter.common.util.InterUtils;
import slive.nsfly.transport.inter.conn.ConnType;

import java.util.Objects;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/16 5:23 下午
 */
public class ConnHandlerPattern {

    private String pattern;

    private ConnType connType;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        pattern = InterUtils.completePath(pattern);
        this.pattern = pattern;
    }

    public ConnType getConnType() {
        return connType;
    }

    public void setConnType(ConnType connType) {
        this.connType = connType;
    }

    public String getHandlerKey(){
        return pattern + "#" + connType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConnHandlerPattern that = (ConnHandlerPattern) o;
        return Objects.equals(pattern, that.pattern) &&
                Objects.equals(connType, that.connType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, connType);
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }
}
