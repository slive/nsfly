package slive.nsfly.transport.inter.common.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:23 上午
 */
public abstract class BaseSimpleMap<T extends Object> implements SimpleMap<T> {

    public static final Logger log = LoggerFactory.getLogger(BaseSimpleMap.class);

    public static final int DEFAULT_SIZE = 16;

    private Map<String, T> map = null;

    private boolean loggerOnChanged = false;

    public BaseSimpleMap() {
        init(false, false, DEFAULT_SIZE);
    }

    public BaseSimpleMap(boolean loggerOnChanged) {
        init(false, loggerOnChanged, DEFAULT_SIZE);
    }

    public BaseSimpleMap(boolean concurrent, boolean loggerOnChanged) {
        init(concurrent, loggerOnChanged, DEFAULT_SIZE);
    }

    public BaseSimpleMap(boolean concurrent, boolean loggerOnChanged, int defaultSize) {
        init(concurrent, loggerOnChanged, defaultSize);
    }

    private void init(boolean concurrent, boolean logger, int defaultSize) {
        if (defaultSize <= 0) {
            defaultSize = DEFAULT_SIZE;
        }
        if (concurrent) {
            map = new ConcurrentHashMap<>(defaultSize);
        } else {
            map = new HashMap<>(defaultSize);
        }
        this.loggerOnChanged = logger;
    }

    @Override
    public T remove(String key) {
        T obj = map.remove(key);
        boolean ret = (obj != null);
        // 记录删除日志
        if (loggerOnChanged) {
            log.info("remove attach, key:{}, size:{}, ret:{}", key, size(), ret);
        }
        return obj;
    }

    @Override
    public boolean add(String key, T v) {
        map.put(key, v);
        // 记录添加日志
        if (loggerOnChanged) {
            log.info("add attach, key:{}, size:{}", key, size());
        }
        return true;
    }

    @Override
    public void clear() {
        int size = size();
        map.clear();
        if (loggerOnChanged) {
            log.info("clear attach, size:{}", size);
        }
    }

    @Override
    public T get(String key) {
        return map.get(key);
    }

    @Override
    public Set<Map.Entry<String, T>> getAll() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }
}
