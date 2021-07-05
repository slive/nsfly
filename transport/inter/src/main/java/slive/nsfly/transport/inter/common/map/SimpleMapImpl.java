package slive.nsfly.transport.inter.common.map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:22 上午
 */
public class SimpleMapImpl<T> extends BaseSimpleMap<T> implements SimpleMap<T> {

    public SimpleMapImpl() {
        super(false, true);
    }

    public SimpleMapImpl(boolean loggerOnChanged) {
        super(false, loggerOnChanged);
    }

    public SimpleMapImpl(boolean loggerOnChanged, int defaultSize) {
        super(false, loggerOnChanged, defaultSize);
    }
}
