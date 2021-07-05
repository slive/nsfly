package slive.nsfly.transport.inter.common.map;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:22 上午
 */
public class SimpleMapConcurrent<T> extends BaseSimpleMap<T> implements SimpleMap<T> {


    public SimpleMapConcurrent() {
        super(true);
    }

    public SimpleMapConcurrent(boolean loggerOnChanged) {
        super(true, loggerOnChanged);
    }

    public SimpleMapConcurrent(boolean loggerOnChanged, int defaultSize) {
        super(true, loggerOnChanged, defaultSize);
    }
}
