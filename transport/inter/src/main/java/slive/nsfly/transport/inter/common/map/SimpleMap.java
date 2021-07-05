package slive.nsfly.transport.inter.common.map;

import java.util.Map;
import java.util.Set;

/**
 * 描述：<pre>
 *
 * @author Slive
 * @date 2021/6/14 10:19 上午
 */
public interface SimpleMap<T extends Object> {

    boolean add(String key, T v);

    T remove(String key);

    void clear();

    T get(String key);

    Set<Map.Entry<String, T>> getAll();

    int size();
}
